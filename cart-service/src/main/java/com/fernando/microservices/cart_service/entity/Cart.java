package com.fernando.microservices.cart_service.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Cart {

    @Id
    private String id;
    private Long userId;
    private String anonymousToken;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    private Double totalPrice;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Double totalShippingPrice;

    private Double totalCharge;

    private CartStatus status;

    public void calculateTotalPrice() {
        this.totalPrice = items.stream()
                .map(item -> item.getSubtotal())
                .reduce(0.0, Double::sum);
    }
}
