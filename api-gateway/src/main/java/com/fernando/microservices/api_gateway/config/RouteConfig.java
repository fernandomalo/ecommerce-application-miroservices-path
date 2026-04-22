package com.fernando.microservices.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fernando.microservices.api_gateway.filter.AuthenticationFilter;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator gatewayRouter(RouteLocatorBuilder builder, AuthenticationFilter filter) {
        return builder.routes()
            .route(p -> p.path("/auth/**")
                .filters(f -> f.filter(filter.apply(new AuthenticationFilter.Config())))
                .uri("http://ecomod-auth-service:8080")
            )
            .route(p -> p.path("/api/v1/users/**")
                .filters(f -> f.filter(filter.apply(new AuthenticationFilter.Config())))
                .uri("http://ecomod-user-info-service:8080")
            )
            .route(p -> p.path("/api/v1/cart/**")
                .filters(f -> f.filter(filter.apply(new AuthenticationFilter.Config())))
                .uri("http://ecomod-cart-service:8080")
            )
            .route(p -> p.path("/api/v1/zones/**")
                .filters(f -> f.filter(filter.apply(new AuthenticationFilter.Config())))
                .uri("http://ecomod-shipping-rules-service:8080")
            )
            .route(p -> p.path("/api/v1/shipping-rules/**")
                .filters(f -> f.filter(filter.apply(new AuthenticationFilter.Config())))
                .uri("http://ecomod-shipping-rules-service:8080")
            )
            .route(p -> p.path("/api/v1/companies/**")
                .filters(f -> f.filter(filter.apply(new AuthenticationFilter.Config())))
                .uri("http://ecomod-user-info-service:8080")
            )
            .route(p -> p.path("/api/v1/inventory/**")
                .filters(f -> f.filter(filter.apply(new AuthenticationFilter.Config())))
                .uri("http://ecomod-inventory-service:8080")
            )
            .route(p -> p.path("/api/v1/products/see/**")
                .filters(f -> f.filter(filter.apply(new AuthenticationFilter.Config())))
                .uri("http://ecomod-product-materialized-view-service:8080")
            )
            .route(p -> p.path("/api/v1/orders/**")
                .filters(f -> f.filter(filter.apply(new AuthenticationFilter.Config())))
                .uri("http://ecomod-order-service:8080")
            )
            .route(p -> p.path("/api/v1/payments/**")
                .filters(f -> f.filter(filter.apply(new AuthenticationFilter.Config())))
                .uri("http://ecomod-payment-service:8080")
            )
            .route(p -> p.path("/api/v1/**")
                .filters(f -> f.filter(filter.apply(new AuthenticationFilter.Config())))
                .uri("http://ecomod-catalog-command-service:8080")
            )
            .build();
    }
}
