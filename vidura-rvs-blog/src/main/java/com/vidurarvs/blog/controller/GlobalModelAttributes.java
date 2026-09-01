package com.vidurarvs.blog.controller;

import com.vidurarvs.blog.model.Category;
import com.vidurarvs.blog.service.CategoryService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * Attributes every template needs (the category list for nav, whether
 * someone is signed in) without repeating them in every controller method.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final CategoryService categoryService;

    public GlobalModelAttributes(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ModelAttribute("navCategories")
    public List<Category> navCategories() {
        return categoryService.findAll();
    }

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    @ModelAttribute("siteName")
    public String siteName() {
        return "ViduraRvs";
    }
}
