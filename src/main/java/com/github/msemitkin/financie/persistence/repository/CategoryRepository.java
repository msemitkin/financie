package com.github.msemitkin.financie.persistence.repository;

import com.github.msemitkin.financie.persistence.entity.CategoryEntity;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    @Nullable
    @Query("SELECT min(category.id) FROM CategoryEntity category WHERE category.name = :name")
    Long getCategoryEntityByName(String name);

    List<CategoryEntity> findAllByIdIn(List<Long> ids);

    default Map<Long, CategoryEntity> findAllByIds(List<Long> ids) {
        return findAllByIdIn(ids).stream()
            .collect(Collectors.toMap(CategoryEntity::getId, Function.identity()));
    }
}
