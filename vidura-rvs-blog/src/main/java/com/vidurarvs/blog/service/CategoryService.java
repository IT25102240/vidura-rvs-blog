package com.vidurarvs.blog.service;

import com.vidurarvs.blog.model.Category;

import java.util.List;

/** Category lookups. Small and focused on purpose (interface segregation). */
public interface CategoryService {

    List<Category> findAll();

    Category findBySlugOrThrow(String slug);

    Category findByIdOrThrow(Long id);
}
