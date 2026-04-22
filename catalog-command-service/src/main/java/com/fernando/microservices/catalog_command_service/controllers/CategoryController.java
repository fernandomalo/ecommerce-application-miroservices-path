package com.fernando.microservices.catalog_command_service.controllers;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fernando.microservices.catalog_command_service.dto.CategoryRequest;
import com.fernando.microservices.catalog_command_service.entity.Category;
import com.fernando.microservices.catalog_command_service.repositories.CategoryRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {
    
    private final CategoryRepository categoryRepository;

    @GetMapping("/list")
    @Cacheable(cacheNames = "categoriesCache")
    public List<Category> findCategories() {
        return categoryRepository.findAll();
    }

    @PostMapping(consumes = "application/json", path = "/add-new")
    public ResponseEntity<?> addCategory(@RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        categoryRepository.save(category);

        return ResponseEntity.ok(category);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CategoryRequest request) {
        Category category = categoryRepository.findById(id)
            .orElse(null);

        if (category == null) {
            return ResponseEntity.internalServerError().body("Category not found");
        }
        category.setName(request.getName());
        categoryRepository.save(category);

        return ResponseEntity.ok(category);
    }
}
