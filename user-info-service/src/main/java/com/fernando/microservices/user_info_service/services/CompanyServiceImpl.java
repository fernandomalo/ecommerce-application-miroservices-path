package com.fernando.microservices.user_info_service.services;

import java.math.BigInteger;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.fernando.microservices.common_service.events.ProductPublishedEvent;
import com.fernando.microservices.common_service.events.ProductSendCompanyInfoEvent;
import com.fernando.microservices.user_info_service.entity.Company;
import com.fernando.microservices.user_info_service.entity.UserInfo;
import com.fernando.microservices.user_info_service.repositories.CompanyRepository;
import com.fernando.microservices.user_info_service.repositories.UserInfoRepository;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final RestClient restClient;

    public CompanyServiceImpl(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://ecomod-api-gateway:8080").build();
    }

    @Override
    @Transactional(readOnly = true)
    // @Cacheable(cacheNames = "companyCacheByUserId", key = "#userId")
    public Company showCompanyToUser(Long userId) {
        return companyRepository.findByUserInfoUserId(userId)
            .orElse(null);
    }

    @Override
    @Transactional
    // @CacheEvict(cacheNames = {"companyCacheByUserId", "companyCacheBySlug"}, allEntries = false, key = "#userId")
    public void registerNewCompany(Long userId, Company company) {
        UserInfo userInfo = userInfoRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User has no information"));

        Company newCompany = new Company();
        newCompany.setName(company.getName());
        newCompany.setCountry(company.getCountry());
        newCompany.setRegion(company.getRegion());
        newCompany.setCity(company.getCity());
        newCompany.setLocation(company.getLocation());

        String slug = generateSlug(company.getName());
        newCompany.setSlug(slug);
        newCompany.setSells(BigInteger.ZERO);
        newCompany.setCreatedAt(LocalDateTime.now());
        newCompany.setUserInfo(userInfo);
        userInfo.setCompany(newCompany);

        companyRepository.save(newCompany);
        userInfoRepository.save(userInfo);

        changeStatusToBusiness(userId);
    }

    @Override
    @Transactional(readOnly = true)
    // @Cacheable(cacheNames = "companyCacheBySlug", key = "#slug")
    public Company getCompanyBySlug(String slug) {
        return companyRepository.findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    private String generateSlug(String name) {
        String base = name.toLowerCase()
                          .replaceAll("[^a-z0-9\\s-]", "")
                          .trim()
                          .replaceAll("\\s+", "-");

        String slug = base;
        int suffix = 1;

        while (companyRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }

        return slug;
    }

    public void changeStatusToBusiness(Long userId) {
        restClient.put()
            .uri("/auth/change-status/{id}", userId)
            .retrieve()
            .toBodilessEntity();
    }

    @KafkaListener(topics = "product-published-topic", groupId = "user-service")
    @Override
    @Transactional
    public void sendCompanyInfoToProduct(ProductPublishedEvent event) {
        Company company = companyRepository.findByUserInfoUserId(event.getUserId())
            .orElseThrow(() -> new RuntimeException("Company not found for that user"));

        ProductSendCompanyInfoEvent productSendCompanyInfoEvent =
            new ProductSendCompanyInfoEvent(
                event.getAggregateId(), 
                company.getId(), 
                company.getName(), 
                company.getSlug()
            );
        
        kafkaTemplate.send("product-company-info-topic", productSendCompanyInfoEvent);
    }

    @Override
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Company not found"));
    }
    
}
