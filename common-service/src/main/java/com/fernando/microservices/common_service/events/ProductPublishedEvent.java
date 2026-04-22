package com.fernando.microservices.common_service.events;

import java.util.List;

public class ProductPublishedEvent {
    private String aggregateId;
    private Long userId;
    private String name;
    private String description;
    private Double price;
    private List<String> imageUrls;
    private List<CategoryInfo> categories;

    public ProductPublishedEvent() {
    }

    public ProductPublishedEvent(String aggregateId, Long userId, String name, String description, Double price,
            List<String> imageUrls, List<CategoryInfo> categories) {
        this.aggregateId = aggregateId;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrls = imageUrls;
        this.categories = categories;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<CategoryInfo> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryInfo> categories) {
        this.categories = categories;
    }
}
