package com.vidurarvs.blog.service.impl;

import com.vidurarvs.blog.exception.DuplicateResourceException;
import com.vidurarvs.blog.exception.ForbiddenActionException;
import com.vidurarvs.blog.exception.ResourceNotFoundException;
import com.vidurarvs.blog.model.Category;
import com.vidurarvs.blog.repository.CategoryRepository;
import com.vidurarvs.blog.repository.PostRepository;
import com.vidurarvs.blog.service.CategoryService;
import com.vidurarvs.blog.util.SlugUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final PostRepository     postRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               PostRepository postRepository) {
        this.categoryRepository = categoryRepository;
        this.postRepository     = postRepository;
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Override
    @NonNull
    public Category findBySlugOrThrow(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + slug));
    }

    @Override
    @NonNull
    public Category findByIdOrThrow(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("Category id cannot be null");
        }
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
    }

    /**
     * Create a new category. Slug is auto-generated from the name.
     * Evicts the navCategories cache so the public navigation updates immediately.
     */
    @Override
    @Transactional
    @CacheEvict(value = "navCategories", allEntries = true)
    public Category create(String name) {
        String trimmed = (name != null ? name.trim() : "");
        if (categoryRepository.existsByNameIgnoreCase(trimmed)) {
            throw new DuplicateResourceException(
                    "A category named \"" + trimmed + "\" already exists.");
        }
        String slug = SlugUtils.toSlug(trimmed);
        // Guard against duplicate slug (e.g. "AI" and "A.I." → same slug)
        if (categoryRepository.findBySlug(slug).isPresent()) {
            slug = slug + "-" + System.currentTimeMillis();
        }
        return categoryRepository.save(new Category(trimmed, slug));
    }

    /**
     * Delete a category only when it has no posts.
     * Evicts the navCategories cache so the public navigation updates immediately.
     */
    @Override
    @Transactional
    @CacheEvict(value = "navCategories", allEntries = true)
    public void delete(Long id) {
        Category category = findByIdOrThrow(id);
        long postCount = postRepository.countByCategory(category);
        if (postCount > 0) {
            throw new ForbiddenActionException(
                    "Cannot delete \"" + category.getName() + "\" — it still has "
                    + postCount + " post(s) assigned. Move or delete those posts first.");
        }
        categoryRepository.delete(category);
    }
}
