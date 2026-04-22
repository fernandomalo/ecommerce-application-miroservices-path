package com.fernando.microservices.inventory_service.listener;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fernando.microservices.common_service.events.ProductOutOfStock;
import com.fernando.microservices.inventory_service.InventoryServiceApplication;
import com.fernando.microservices.inventory_service.entity.Stock;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;

import java.math.BigInteger;

@Component
public class InventorySaveListener {
    
    @SuppressWarnings("unchecked")
    private KafkaTemplate<String, Object> getKafkaTemplate() {
        return (KafkaTemplate<String, Object>) InventoryServiceApplication.getBean(KafkaTemplate.class); 
    }

    @PostPersist
    @PostUpdate
    public void checkAndSend(Stock stock) {
        if (stock.getQuantity() != null && (stock.getQuantity().subtract(stock.getReservedQuantity()).compareTo(BigInteger.ZERO) <= 0)) {
            ProductOutOfStock event = new ProductOutOfStock(stock.getProductId());
            getKafkaTemplate().send("product-out-of-stock-topic", event);
        }
    }
}
