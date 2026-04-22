package com.fernando.microservices.cart_service.services;

import com.fernando.microservices.cart_service.entity.Cart;
import com.fernando.microservices.common_service.events.order_events.OrderNotCreatedEvent;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface CartService {
    void addItemToCart(Long userId, HttpServletRequest request, HttpServletResponse response, String productId, Long ruleId);
    void removeItemFromCart(String cartId, String productId);
    void increaseItemQuantity(String cartId, String productId);
    void decreaseItemQuantity(String cartId, String productId);
    void toggleItemCheckedStatus(String cartId, String productId);
    Cart getCartByUserId(Long userId);
    Cart getCartByAnonymousToken(HttpServletRequest request);
    void mergeCart(HttpServletRequest request, HttpServletResponse response, Long userId);
    void createOrderFromCart(Long userId);
    void rollbackCheckedoutCart(OrderNotCreatedEvent orderNotCreatedEvent);
    // String createHandshakeCart(HttpServletRequest request, HttpServletResponse response);
}
