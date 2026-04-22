package com.fernando.microservices.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fernando.microservices.payment_service.entitty.Payment;
import com.fernando.microservices.payment_service.entitty.PaymentStatus;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByWompiReference(String wompiReference);
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);
}
