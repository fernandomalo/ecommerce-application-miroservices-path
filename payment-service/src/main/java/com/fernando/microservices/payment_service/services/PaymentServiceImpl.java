package com.fernando.microservices.payment_service.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fernando.microservices.common_service.events.order_events.PaymentFailedEvent;
import com.fernando.microservices.common_service.events.order_events.PaymentSuccessfulEvent;
import com.fernando.microservices.payment_service.config.OpenFeignUserInfo;
import com.fernando.microservices.payment_service.config.WompiClient;
import com.fernando.microservices.payment_service.config.WompiProperties;
import com.fernando.microservices.payment_service.dto.InitiatePaymentRequest;
import com.fernando.microservices.payment_service.dto.PaymentStatusResponse;
import com.fernando.microservices.payment_service.dto.UserInfoDto;
import com.fernando.microservices.payment_service.dto.WompiTransactionResponse;
import com.fernando.microservices.payment_service.entitty.Payment;
import com.fernando.microservices.payment_service.entitty.PaymentMethod;
import com.fernando.microservices.payment_service.entitty.PaymentStatus;
import com.fernando.microservices.payment_service.repository.PaymentRepository;
import com.fernando.microservices.payment_service.utils.WompiSignatureUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OpenFeignUserInfo feignUserInfo;
    private final WompiClient wompiClient;
    private final WompiProperties wompiProperties;
    private final JmsService jmsService;

    @Override
    @Transactional
    public void handleCreatePaymentEvent(Long userId, Long orderId, Double amount) {
        log.info("Payment event received — orderId={} userId={} amount={}", orderId, userId, amount);

        // Idempotent: skip if already exists
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            log.warn("Payment already exists for orderId={}, skipping duplicate event", orderId);
            return;
        }

        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setWompiReference(UUID.randomUUID().toString());

        paymentRepository.save(payment);
        log.info("PENDING payment created for orderId={} ref={}", orderId, payment.getWompiReference());
    }

    @Override
    @Transactional
    public PaymentStatusResponse initiatePayment(Long userId, Long orderId, InitiatePaymentRequest request) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("No pending payment for orderId: " + orderId));

        if (!payment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (payment.getStatus() == PaymentStatus.APPROVED) {
            throw new RuntimeException("This order is already paid");
        }

        UserInfoDto userInfo = feignUserInfo.getUserInfo(userId.toString());
        if (userInfo == null) {
            throw new RuntimeException("User info not found");
        }

        long amountInCents = Math.round(payment.getAmount() * 100);
        String reference = payment.getWompiReference();
        String currency = "COP";

        String signature = WompiSignatureUtil.buildIntegritySignature(
                reference, amountInCents, currency, wompiProperties.getIntegritySecret());

        Map<String, Object> body = new HashMap<>();
        body.put("amount_in_cents", amountInCents);
        body.put("currency", currency);
        body.put("customer_email", request.getEmail()); // ← from request, not userInfo
        body.put("reference", reference);
        body.put("acceptance_token", request.getAcceptanceToken());
        body.put("redirect_url", wompiProperties.getRedirectUrl() + "?ref=" + reference);
        body.put("signature", signature);

        Map<String, Object> customerData = new HashMap<>();
        customerData.put("phone_number", userInfo.getPhoneNumber()); // ← from userInfo
        customerData.put("full_name", userInfo.getCity()); // best you have without name
        body.put("customer_data", customerData);

        PaymentMethod method;
        Map<String, Object> paymentMethodBody = new HashMap<>();

        switch (request.getPaymentMethod().toUpperCase()) {
            case "CARD" -> {
                method = PaymentMethod.CARD;
                paymentMethodBody.put("type", "CARD");
                paymentMethodBody.put("token", request.getCardToken());
                paymentMethodBody.put("installments",
                        request.getInstallments() != null ? request.getInstallments() : 1);
            }
            case "PSE" -> {
                method = PaymentMethod.PSE;
                paymentMethodBody.put("type", "PSE");
                paymentMethodBody.put("user_type", Integer.parseInt(request.getUserType()));
                paymentMethodBody.put("user_legal_id", request.getUserLegalId());
                paymentMethodBody.put("user_legal_id_type", request.getUserLegalIdType());
                paymentMethodBody.put("financial_institution_code", request.getFinancialInstitutionCode());
            }
            case "NEQUI" -> {
                method = PaymentMethod.NEQUI;
                paymentMethodBody.put("type", "NEQUI");
                paymentMethodBody.put("phone_number", request.getPhoneNumber());
            }
            default -> throw new RuntimeException("Unsupported method: " + request.getPaymentMethod());
        }

        body.put("payment_method", paymentMethodBody);

        WompiTransactionResponse wompiResponse = wompiClient.createTransaction(body);
        WompiTransactionResponse.DataWrapper data = wompiResponse.getData();

        payment.setWompiTransactionId(data.getId());
        payment.setPaymentMethod(method);
        PaymentStatus newStatus = mapWompiStatus(data.getStatus());
        payment.setStatus(newStatus);
        paymentRepository.save(payment);

        // Fire failure event immediately if Wompi already declined it
        if (newStatus == PaymentStatus.DECLINED || newStatus == PaymentStatus.ERROR) {
            firePaymentFailed(payment);
        }

        String asyncUrl = null;
        if (method == PaymentMethod.PSE
                && data.getPaymentMethod() != null
                && data.getPaymentMethod().getExtra() != null) {
            asyncUrl = data.getPaymentMethod().getExtra().getAsyncPaymentUrl();
        }

        return new PaymentStatusResponse(
                data.getStatus(),
                data.getId(),
                reference,
                method.name(),
                payment.getAmount(),
                payment.getOrderId(),
                asyncUrl);
    }

    @Override
    @Transactional
    public PaymentStatusResponse getPaymentStatusByReference(String reference) {
        Payment payment = paymentRepository.findByWompiReference(reference)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + reference));

        // Re-fetch live status from Wompi if still PENDING
        if (payment.getStatus() == PaymentStatus.PENDING && payment.getWompiTransactionId() != null) {
            WompiTransactionResponse live = wompiClient.getTransaction(payment.getWompiTransactionId());
            PaymentStatus fresh = mapWompiStatus(live.getData().getStatus());
            payment.setStatus(fresh);
            paymentRepository.save(payment);

            if (fresh == PaymentStatus.APPROVED) {
                jmsService.sendToOrder(new PaymentSuccessfulEvent(payment.getUserId(), payment.getOrderId()));
            } else if (fresh == PaymentStatus.DECLINED || fresh == PaymentStatus.ERROR) {
                firePaymentFailed(payment);
            }
        }

        return toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentStatusResponse getPaymentStatusByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for orderId: " + orderId));

        return toResponse(payment);
    }

    @Override
    public String getWompiPublicKey() {
        return wompiProperties.getPublicKey();
    }

    private void firePaymentFailed(Payment payment) {
        log.warn("Payment failed/declined for orderId={}, firing PaymentFailedEvent", payment.getOrderId());
        PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(payment.getOrderId(), payment.getUserId());
        jmsService.sentToQueue(paymentFailedEvent);
    }

    private PaymentStatusResponse toResponse(Payment payment) {
        return new PaymentStatusResponse(
                payment.getStatus().name(),
                payment.getWompiTransactionId(),
                payment.getWompiReference(),
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null,
                payment.getAmount(),
                payment.getOrderId(),
                null);
    }

    private PaymentStatus mapWompiStatus(String s) {
        if (s == null)
            return PaymentStatus.PENDING;
        return switch (s.toUpperCase()) {
            case "APPROVED" -> PaymentStatus.APPROVED;
            case "DECLINED" -> PaymentStatus.DECLINED;
            case "VOIDED" -> PaymentStatus.VOIDED;
            case "ERROR" -> PaymentStatus.ERROR;
            default -> PaymentStatus.PENDING;
        };
    }

    @Override
    @Transactional
    public PaymentStatusResponse getPendingPaymentByUserId(Long userId) {
        Payment payment = paymentRepository.findByUserIdAndStatus(userId, PaymentStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("No pending payment found for user"));
        return toResponse(payment);
    }
}