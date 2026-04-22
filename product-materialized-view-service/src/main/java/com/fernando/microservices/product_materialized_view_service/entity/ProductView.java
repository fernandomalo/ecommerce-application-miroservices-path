package com.fernando.microservices.product_materialized_view_service.entity;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(indexes = {
        @Index(columnList = "name"),
        @Index(columnList = "description")
})
public class ProductView {

    @Id
    private String id;
    private Long companyId;

    private String companyName;
    private String companySlug;

    private String name;
    private String description;
    private Double price;
    private List<String> imageUrls;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_view_categories", joinColumns = @JoinColumn(name = "product_view_id"), indexes = @Index(columnList = "category"))
    @Column(name = "category")
    private List<String> categories;

    private BigInteger availableStock;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
