package com.fernando.microservices.user_info_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fernando.microservices.user_info_service.entity.Company;
import com.fernando.microservices.user_info_service.services.CompanyService;


@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {
    
    @Autowired
    private CompanyService companyService;

    @GetMapping("/id/{id}")
    public Company getCompanyById(@PathVariable Long id) {
        return companyService.getCompanyById(id);
    }

    @GetMapping("/user")
    public Company getCompanyToUser(@RequestHeader("X-User-Id") String userIdString) {
        Long userId = Long.parseLong(userIdString);
        return companyService.showCompanyToUser(userId);
    }

    @GetMapping("/slug/{slug}")
    public Company getCompanyBySlug(@PathVariable String slug) {
        return companyService.getCompanyBySlug(slug);
    }

    @PostMapping("/create-company")
    public ResponseEntity<?> createCompany(@RequestHeader("X-User-Id") String userIdString, @RequestBody Company company) {
        Long userId = Long.parseLong(userIdString);
        System.out.println("user-id: " + userIdString);

        companyService.registerNewCompany(userId, company);

        return ResponseEntity.ok("Company registered successfully");
    }
}
