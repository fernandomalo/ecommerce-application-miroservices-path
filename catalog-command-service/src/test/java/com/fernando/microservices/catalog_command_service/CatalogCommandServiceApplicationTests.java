package com.fernando.microservices.catalog_command_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=dummy:9092",
    "spring.kafka.listener.auto-startup=false"
})
class CatalogCommandServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
