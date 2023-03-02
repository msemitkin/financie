package com.github.msemitkin.financie.domain;

import com.github.msemitkin.financie.persistence.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category getCategory(long id) {
        return categoryRepository.findById(id)
            .map(cat -> new Category(cat.getId(), cat.getName()))
            .orElse(null);
    }
}
