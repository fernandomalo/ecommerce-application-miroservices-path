package com.fernando.microservices.product_materialized_view_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=dummy:9092",
    "spring.kafka.listener.auto-startup=false"
})
class ProductMatirializedViewServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
