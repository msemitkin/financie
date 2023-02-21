package com.github.msemitkin.financie.persistence.mapper;

@FunctionalInterface
public interface CategoryNameSource {
    String getName(long categoryId);
}
