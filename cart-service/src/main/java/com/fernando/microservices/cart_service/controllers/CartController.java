package com.fernando.microservices.cart_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fernando.microservices.cart_service.entity.Cart;
import com.fernando.microservices.cart_service.services.CartService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {
    
    private final CartService cartService;

    @GetMapping("/anonymous")
    public Cart getCartByAnonymousToken(HttpServletRequest request) {
        return cartService.getCartByAnonymousToken(request);
    }

    @GetMapping("/auth-user")
    public Cart getCartByUserId(@RequestHeader("X-User-Id") String userIdString) {
        Long userId = Long.parseLong(userIdString);
        System.out.println("Received userId: " + userId + "String: " + userIdString);
        return cartService.getCartByUserId(userId);
    }

    @PostMapping("/add-item/{productId}")
    public ResponseEntity<?> addItemToCart(@RequestHeader(name= "X-User-Id", required = false) String userIdString, @PathVariable String productId, HttpServletRequest request, HttpServletResponse response, @RequestParam(required = false) Long ruleId) {
        Long userId = null;
        if (userIdString != null) {
            userId = Long.parseLong(userIdString);
        }
        System.out.println("Received userId: " + userId + "String: " + userIdString);
        cartService.addItemToCart(userId, request, response, productId, ruleId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    public ResponseEntity<?> mergeCart(HttpServletRequest request, HttpServletResponse response, @RequestHeader("X-User-Id") String userIdString) {
        Long userId = Long.parseLong(userIdString);
        cartService.mergeCart(request, response, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> createOrderFromCart(@RequestHeader("X-User-Id") String userIdString) {
        Long userId = Long.parseLong(userIdString);
        cartService.createOrderFromCart(userId);

        return ResponseEntity.ok("Order created successfully");
    }

    @DeleteMapping("/remove-item/{cartId}/{productId}")
    public ResponseEntity<?> removeItemFromCart(@PathVariable String cartId, @PathVariable String productId) {
        cartService.removeItemFromCart(cartId, productId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/increase-quantity/{cartId}/{productId}")
    public ResponseEntity<?> increaseItemQuantity(@PathVariable String cartId, @PathVariable String productId) {
        cartService.increaseItemQuantity(cartId, productId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/decrease-quantity/{cartId}/{productId}")
    public ResponseEntity<?> decreaseItemQuantity(@PathVariable String cartId, @PathVariable String productId) {
        cartService.decreaseItemQuantity(cartId, productId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/toggle-status/{cartId}/{productId}")
    public ResponseEntity<?> toggleItemCheckedStatus(@PathVariable String cartId, @PathVariable String productId) {
        cartService.toggleItemCheckedStatus(cartId, productId);
        return ResponseEntity.noContent().build();
    }
}
