package com.fernando.microservices.api_gateway.filter;

import java.net.http.HttpHeaders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import com.fernando.microservices.api_gateway.services.JwtService;

import io.netty.handler.codec.http.HttpHeaderNames;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {

    }

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtService jwtService;

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())
                    && !validator.isSecuredOrNormal.test(exchange.getRequest())) {
                System.out.println("Secured route accessed, validating token...");
                if (!exchange.getRequest().getHeaders().containsHeader(HttpHeaderNames.AUTHORIZATION.toString())) {
                    throw new RuntimeException("Missing authorization header");
                }

                String token = exchange.getRequest().getHeaders().getFirst("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                } else {
                    throw new RuntimeException("Token not valid");
                }

                try {
                    jwtService.validateToken(token);
                } catch (Exception e) {
                    throw new RuntimeException("token not valid: " + e);
                }

                ServerHttpRequest request = exchange
                        .getRequest()
                        .mutate()
                        .header("X-User-Id", jwtService.getUserIdFromToken(token).toString())
                        .header("roles", jwtService.getRolesFromToken(token))
                        .build();
                return chain.filter(exchange.mutate().request(request).build());
            } else if (validator.isSecuredOrNormal.test(exchange.getRequest())) {
                if (exchange.getRequest().getHeaders().containsHeader("Authorization")) {
                    System.out.println("Authorization header found, validating token...");
                    String token = exchange.getRequest().getHeaders().getFirst("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        try {
                            jwtService.validateToken(token);
                        } catch (Exception e) {
                            throw new RuntimeException("token not valid: " + e);
                        }

                        ServerHttpRequest request = exchange
                                .getRequest()
                                .mutate()
                                .header("X-User-Id", jwtService.getUserIdFromToken(token).toString())
                                .header("roles", jwtService.getRolesFromToken(token))
                                .build();
                        return chain.filter(exchange.mutate().request(request).build());
                    }
                } else {
                    System.out.println("No Authorization header found, proceeding without authentication");
                }

            }

            return chain.filter(exchange);
        });
    }
}
