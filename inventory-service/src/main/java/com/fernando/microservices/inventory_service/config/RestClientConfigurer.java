package com.fernando.microservices.inventory_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfigurer {

    @Bean
    @Primary
    RestClient.Builder restClient() {
        return RestClient.builder();
    }
}
