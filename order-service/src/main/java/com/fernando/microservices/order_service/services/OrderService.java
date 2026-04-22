package com.fernando.microservices.order_service.services;

import java.util.List;

import com.fernando.microservices.common_service.events.order_events.CreateOrderEvent;
import com.fernando.microservices.order_service.entitty.Order;

public interface OrderService {
    void createOrderFromCart(CreateOrderEvent createOrderEvent);
    List<Order> getOrdersByUserId(Long userId);
    Order getOrderById(Long orderId);
}
