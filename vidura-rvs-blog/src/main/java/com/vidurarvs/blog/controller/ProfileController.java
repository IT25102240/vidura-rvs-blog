package com.vidurarvs.blog.controller;

import com.vidurarvs.blog.model.Post;
import com.vidurarvs.blog.model.User;
import com.vidurarvs.blog.security.CustomUserPrincipal;
import com.vidurarvs.blog.service.PostService;
import com.vidurarvs.blog.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Public author profile page and admin self-profile edit page.
 * Public page: /author/{username}  — no login required.
 * Admin edit:  /admin/profile      — requires any admin login.
 */
@Controller
public class ProfileController {

    private final UserService userService;
    private final PostService postService;

    public ProfileController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    // ---- Public author profile ----------------------------------------

    @GetMapping("/author/{username}")
    public String authorProfile(@PathVariable String username, Model model) {
        User author = userService.findByUsernameOrThrow(username);
        Page<Post> posts = postService.findAllByAuthor(author, 0, 12);
        long totalViews = postService.totalViewsForAuthor(author);

        model.addAttribute("profileUser", author);
        model.addAttribute("authorPosts", posts);
        model.addAttribute("totalViews", totalViews);
        return "author-profile";
    }

    // ---- Admin self-profile edit --------------------------------------

    @GetMapping("/admin/profile")
    public String profileForm(@AuthenticationPrincipal CustomUserPrincipal principal, Model model) {
        model.addAttribute("profileUser", principal.getUser());
        return "admin/profile";
    }

    @PostMapping("/admin/profile")
    public String saveProfile(@AuthenticationPrincipal CustomUserPrincipal principal,
                               @RequestParam(required = false) String bio,
                               @RequestParam(name = "photo", required = false) MultipartFile photo,
                               RedirectAttributes redirectAttributes) {
        User current = principal.getUser();
        userService.updateProfile(current.getId(), bio, photo, current);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        return "redirect:/admin/profile";
    }
}
