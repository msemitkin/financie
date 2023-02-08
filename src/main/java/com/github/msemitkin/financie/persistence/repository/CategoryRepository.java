package com.github.msemitkin.financie.persistence.repository;

import com.github.msemitkin.financie.persistence.entity.CategoryEntity;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {
    @Nullable
    @Query("SELECT min(category.id) FROM CategoryEntity category WHERE category.name = :name")
    Long getCategoryEntityByName(String name);
}
