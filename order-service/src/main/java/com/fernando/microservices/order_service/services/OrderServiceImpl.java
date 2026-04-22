package com.fernando.microservices.order_service.services;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.fernando.microservices.common_service.events.order_events.CreateOrderEvent;
import com.fernando.microservices.common_service.events.order_events.CreatePaymentEvent;
import com.fernando.microservices.common_service.events.order_events.OrderConfirmedEvent;
import com.fernando.microservices.common_service.events.order_events.OrderNotCreatedEvent;
import com.fernando.microservices.common_service.events.order_events.PaymentFailedEvent;
import com.fernando.microservices.common_service.events.order_events.PaymentSuccessfulEvent;
import com.fernando.microservices.order_service.dto.ProductDto;
import com.fernando.microservices.order_service.entitty.Order;
import com.fernando.microservices.order_service.entitty.OrderItem;
import com.fernando.microservices.order_service.entitty.OrderStatus;
import com.fernando.microservices.order_service.repositories.OrderItemRepository;
import com.fernando.microservices.order_service.repositories.OrderRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private JmsService jmsService;

    private final RestClient restClient;

    public OrderServiceImpl(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://ecomod-inventory-service:8080").build();
    }

    @Override
    @Transactional
    @JmsListener(destination = "create.order.queue")
    public void createOrderFromCart(CreateOrderEvent createOrderEvent) {
        try {
            Order order = new Order();
            order.setUserId(createOrderEvent.getUserId());
            order.setTotalAmount(createOrderEvent.getTotalAmount());
            order.setStatus(OrderStatus.PENDING);
            orderRepository.save(order);

            List<OrderItem> orderItems = createOrderEvent.getItems().stream()
                    .map(i -> {
                        ProductDto product = new ProductDto(
                                i.getProductId(),
                                i.getImageUrls(),
                                i.getName(),
                                i.getPrice());

                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrder(order);
                        orderItem.setCompanyId(i.getCompanyId());
                        orderItem.setRuleId(i.getRuleId());
                        orderItem.setProduct(product);
                        orderItem.setQuantity(i.getQuantity());
                        orderItem.setSubtotal(i.getSubtotal());

                        reserveStock(createOrderEvent.getUserId(), i.getProductId(), i.getQuantity());

                        return orderItem;
                    })
                    .toList();
            order.getItems().addAll(orderItems);
            CreatePaymentEvent createPaymentEvent = new CreatePaymentEvent(createOrderEvent.getUserId(), order.getId(),
                    order.getTotalAmount());
            jmsService.sendToPayment(createPaymentEvent);
        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());

            OrderNotCreatedEvent event = new OrderNotCreatedEvent(createOrderEvent.getUserId());

            jmsService.sendToCart(event);

            throw e;
        }

        // Order order = new Order();
        // order.setUserId(createOrderEvents.get(0).getUserId());

        // List<OrderItem> orderItems = createOrderEvents.stream()
        // .map(event -> {
        // ProductDto product = new ProductDto();
        // product.setProductId(event.getProductId());
        // product.setName(event.getName());
        // product.setImages(event.getImageUrls());
        // product.setPrice(event.getPrice());

        // OrderItem orderItem = new OrderItem();
        // orderItem.setProduct(product);
        // orderItem.setQuantity(event.getQuantity());
        // orderItem.setSubtotal(event.getSubtotal());
        // orderItem.setOrder(order);

        // order.getItems().add(orderItem);
        // order.calculateTotalAmount();
        // orderRepository.save(order);
        // return orderItem;
        // })
        // .toList();

    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public void reserveStock(Long userId, String productId, BigInteger quantity) {
        restClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/inventory/products/reserve/{productId}/{quantity}")
                        .queryParam("userId", userId)
                        .build(productId, quantity))
                .retrieve()
                .toBodilessEntity();
    }

    @JmsListener(destination = "payment.failed.queue")
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("Payment failed for orderId={}, marking as FAILED", event.getOrderId());
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
        });

        OrderNotCreatedEvent orderNotCreatedEvent = new OrderNotCreatedEvent(event.getUserId());
        jmsService.sendToCart(orderNotCreatedEvent);
    }

    @JmsListener(destination = "payment.success.queue")
    @Transactional
    public void onPaymentSuccess(PaymentSuccessfulEvent event) {
        System.out.println("processing orders");
        orderRepository.findByUserIdAndId(event.getUserId(), event.getOrderId())
                .ifPresent(order -> {
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                    System.out.println("order confirmed");
                });
        
        jmsService.sendOrderConfirmed(new OrderConfirmedEvent(event.getUserId()));
    }

}
