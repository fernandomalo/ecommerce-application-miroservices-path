package com.fernando.microservices.order_service.entitty;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fernando.microservices.order_service.dto.ProductDto;

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
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;
    private Long ruleId;

    @Embedded
    private ProductDto product;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private Order order;
    
    private BigInteger quantity;
    private Double subtotal;
}
