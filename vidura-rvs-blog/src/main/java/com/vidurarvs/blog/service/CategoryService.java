package com.vidurarvs.blog.service;

import com.vidurarvs.blog.model.Category;
import org.springframework.lang.NonNull;

import java.util.List;

/** Category lookups and admin management (interface segregation). */
public interface CategoryService {

    List<Category> findAll();

    @NonNull
    Category findBySlugOrThrow(String slug);

    @NonNull
    Category findByIdOrThrow(Long id);

    /**
     * Create a new category with the given name.
     * The slug is auto-generated from the name.
     * Throws {@link com.vidurarvs.blog.exception.DuplicateResourceException}
     * if a category with the same name already exists (case-insensitive).
     */
    Category create(String name);

    /**
     * Permanently delete a category.
     * Throws {@link com.vidurarvs.blog.exception.ForbiddenActionException}
     * if any published or draft post is still assigned to that category.
     */
    void delete(Long id);
}
