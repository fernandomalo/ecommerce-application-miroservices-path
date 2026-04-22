package com.fernando.microservices.user_info_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientCofigurer {

    @Bean
    @Primary
    RestClient.Builder restClient() {
        return RestClient.builder();
    }
}
