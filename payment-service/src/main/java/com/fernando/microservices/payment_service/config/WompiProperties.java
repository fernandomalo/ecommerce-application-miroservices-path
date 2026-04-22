package com.fernando.microservices.payment_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wompi")
public class WompiProperties {
    private String baseUrl;
    private String publicKey;
    private String privateKey;
    private String integritySecret;
    private String eventsSecret;
    private String redirectUrl;
}