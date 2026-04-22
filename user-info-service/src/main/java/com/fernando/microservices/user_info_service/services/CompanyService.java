package com.fernando.microservices.user_info_service.services;

import com.fernando.microservices.common_service.events.ProductPublishedEvent;
import com.fernando.microservices.user_info_service.entity.Company;

public interface CompanyService {
    Company showCompanyToUser(Long userId);
    Company getCompanyBySlug(String slug);
    Company getCompanyById(Long id);
    void registerNewCompany(Long userId, Company company);
    void sendCompanyInfoToProduct(ProductPublishedEvent event);
}
