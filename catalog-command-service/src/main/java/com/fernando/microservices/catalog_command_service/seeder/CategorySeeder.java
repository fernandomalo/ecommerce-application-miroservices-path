package com.fernando.microservices.catalog_command_service.seeder;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fernando.microservices.catalog_command_service.entity.Category;
import com.fernando.microservices.catalog_command_service.repositories.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        List<String> categories = List.of(
                "Supplements",
                "Technology",
                "Games",
                "Books",
                "Distraction",
                "Home",
                "Health",
                "Accessories",
                "Tools",
                "Pets",
                "Sports",
                "Fitness",
                "Music",
                "Art & Craft",
                "Automotive",
                "Office Supplies",
                "Outdoor & Camping",
                "Photography",
                "Garden",
                "Travel",
                "Toys",
                "Beauty",
                "Watches",
                "Collectibles");

        for (String name : categories) {
            categoryRepository.findByName(name).orElseGet(() -> {
                Category category = new Category();
                category.setName(name);
                return categoryRepository.save(category);
            });
        }

        System.out.println("Categories seeded successfully");
    }

}
