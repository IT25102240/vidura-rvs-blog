package com.vidurarvs.blog.controller;

import com.vidurarvs.blog.dto.AdminAccountFormDTO;
import com.vidurarvs.blog.model.User;
import com.vidurarvs.blog.security.CustomUserPrincipal;
import com.vidurarvs.blog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Owner-only screen for granting/revoking verified-admin access.
 * Restricted to ROLE_SUPER_ADMIN by SecurityConfig; UserService enforces
 * the same rule again so this never depends on the URL mapping alone.
 */
@Controller
@RequestMapping("/admin/admins")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("admins", userService.findAll());
        return "admin/admin-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("adminForm", new AdminAccountFormDTO());
        return "admin/admin-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("adminForm") AdminAccountFormDTO form,
                          BindingResult bindingResult,
                          @AuthenticationPrincipal CustomUserPrincipal principal,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/admin-form";
        }
        User created = userService.createAdmin(form, principal.getUser());
        redirectAttributes.addFlashAttribute("successMessage",
                "Invited " + created.getFullName() + " as a verified admin. Share their username and temporary password securely.");
        return "redirect:/admin/admins";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserPrincipal principal,
                              RedirectAttributes redirectAttributes) {
        userService.setActive(id, false, principal.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Admin access revoked.");
        return "redirect:/admin/admins";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserPrincipal principal,
                            RedirectAttributes redirectAttributes) {
        userService.setActive(id, true, principal.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Admin access restored.");
        return "redirect:/admin/admins";
    }
}
