package com.financeapp.model;

import java.util.Objects;

public class Category {
    private Long id;
    private Long userId;
    private String name;
    private CategoryType type;

    public Category() {
    }

    public Category(Long id, Long userId, String name, CategoryType type) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
    }

    public Category(Long userId, String name, CategoryType type) {
        this.userId = userId;
        this.name = name;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category category)) return false;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

