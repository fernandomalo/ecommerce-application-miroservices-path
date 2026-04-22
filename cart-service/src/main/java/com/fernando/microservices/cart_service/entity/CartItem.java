package com.fernando.microservices.cart_service.entity;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fernando.microservices.cart_service.dto.ProductDto;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ruleId;

    private Long companyId;

    @Embedded
    private ProductDto product;

    private BigInteger quantity;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonBackReference
    private Cart cart;

    private CartItemStatus status;

    private Double subtotal = 0.0;

    public void increaseQuantity() {
        if (quantity != null) {
            this.quantity = quantity.add(BigInteger.ONE);
            calculateSubtotal();
        } else {
            this.quantity = BigInteger.ONE;
            calculateSubtotal();
        }
    }

    public void decreaseQuantity() {
        if (quantity != null && quantity.compareTo(BigInteger.ONE) > 0) {
            this.quantity = quantity.subtract(BigInteger.ONE);
            calculateSubtotal();
        }
    }

    public void calculateSubtotal() {
        if (product != null && product.getPrice() != null && quantity != null && status == CartItemStatus.CHECKED) {
            this.subtotal = product.getPrice() * quantity.doubleValue();
        } else {
            this.subtotal = 0.0;
        }
    }
}
