package com.fernando.microservices.product_materialized_view_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
// @EnableCaching
public class ProductMaterializedViewServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductMaterializedViewServiceApplication.class, args);
	}

}
