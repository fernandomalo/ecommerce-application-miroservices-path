package com.fernando.microservices.cart_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.cloud.openfeign.enabled=false"
})
class CartServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
