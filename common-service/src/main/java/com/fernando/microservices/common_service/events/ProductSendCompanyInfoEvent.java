package com.fernando.microservices.common_service.events;

public class ProductSendCompanyInfoEvent {
    
    private String aggregateId;
    private Long companyId;
    private String companyName;
    private String companySlug;
    
    public ProductSendCompanyInfoEvent() {
    }

    public ProductSendCompanyInfoEvent(String aggregateId, Long companyId, String companyName, String companySlug) {
        this.aggregateId = aggregateId;
        this.companyId = companyId;
        this.companyName = companyName;
        this.companySlug = companySlug;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanySlug() {
        return companySlug;
    }

    public void setCompanySlug(String companySlug) {
        this.companySlug = companySlug;
    }
}
