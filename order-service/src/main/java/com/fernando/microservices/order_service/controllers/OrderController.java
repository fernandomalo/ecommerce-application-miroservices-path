package com.fernando.microservices.order_service.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fernando.microservices.order_service.entitty.Order;
import com.fernando.microservices.order_service.services.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    
    private final OrderService orderService;

    @GetMapping("/user")
    public List<Order> getByUserId(@RequestHeader("X-User-Id") String userIdString) {
        Long userId = Long.parseLong(userIdString);
        
        return orderService.getOrdersByUserId(userId);
    }

    @GetMapping("/{id}")
    public Order getById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }
}
