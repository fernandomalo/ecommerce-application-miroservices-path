package com.fernando.microservices.api_gateway.filter;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidator {
    
    public static final List<String> routes = List.of(
        "/auth/login",
        "/auth/register",
        "/auth/refresh",
        "/auth/logout",
        "/auth/change-status",
        "/auth/verify",
        "/auth/resend-code",
        "/api/v1/categories/list",
        "/api/v1/products/see",
        "/api/v1/payments/webhook"
    );

    public static final List<String> normalRoutes = List.of(
        "/api/v1/cart/anonymous",
        "/api/v1/cart/add-item",
        "/api/v1/cart/remove-item",
        "/api/v1/cart/increase-quantity",
        "/api/v1/cart/decrease-quantity",
        "/api/v1/cart/toggle-status"
    );

    public Predicate<ServerHttpRequest> isSecured = 
        request -> routes.stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

    public Predicate<ServerHttpRequest> isSecuredOrNormal = 
        request -> normalRoutes.stream()
                    .anyMatch(uri -> request.getURI().getPath().contains(uri));
                    
}
