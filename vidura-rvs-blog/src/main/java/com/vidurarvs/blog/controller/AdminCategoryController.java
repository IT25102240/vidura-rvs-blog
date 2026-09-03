package com.vidurarvs.blog.controller;

import com.vidurarvs.blog.exception.DuplicateResourceException;
import com.vidurarvs.blog.exception.ForbiddenActionException;
import com.vidurarvs.blog.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin category management — SUPER_ADMIN only.
 * Access is enforced by SecurityConfig (/admin/categories/** → SUPER_ADMIN).
 * Regular admins see the category list but cannot add or delete.
 */
@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** Show all categories with post counts and an Add form. */
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/category-list";
    }

    /** Create a new category (SUPER_ADMIN only). */
    @PostMapping("/add")
    public String addCategory(@RequestParam("name") String name,
                              RedirectAttributes redirectAttributes) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Category name cannot be blank.");
            return "redirect:/admin/categories";
        }
        try {
            categoryService.create(trimmed);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Category \"" + trimmed + "\" added successfully.");
        } catch (DuplicateResourceException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    /**
     * Delete a category (SUPER_ADMIN only).
     * Blocked by the service if any posts still use the category.
     */
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMsg", "Category deleted.");
        } catch (ForbiddenActionException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
