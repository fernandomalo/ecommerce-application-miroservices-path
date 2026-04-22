package com.fernando.microservices.inventory_service.entity;

import java.math.BigInteger;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(com.fernando.microservices.inventory_service.listener.InventorySaveListener.class)
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = "product_id")
}, 
indexes = {
    @Index(columnList = "user_id")
})
public class Stock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productId;
    private Long userId;

    private BigInteger quantity;
    private BigInteger reservedQuantity;
    private BigInteger availableStock;

    private StockStatus status;

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public BigInteger calculateAvailableStock() {
        return quantity.subtract(reservedQuantity);
    }

    public boolean isAvailable(BigInteger amount) {
        return quantity.subtract(reservedQuantity).compareTo(amount) >= 0;
    }

    public void addStock(BigInteger amount) {
        this.quantity = this.quantity.add(amount);
        this.availableStock = calculateAvailableStock();
    }

    public void reduceStock(BigInteger amount) {
        if (isAvailable(amount)) {
            this.quantity = this.quantity.subtract(amount);
            this.availableStock = calculateAvailableStock();
        } else {
            throw new RuntimeException("Could not reduce the stock of this product");
        }
    }

    public void reserveStock(BigInteger amount) {
        this.reservedQuantity = this.reservedQuantity.add(amount);
        this.availableStock = calculateAvailableStock();
    }
}
