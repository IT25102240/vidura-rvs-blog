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
import org.springframework.web.bind.annotation.GetMapping;

/** The signed-in author's/owner's overview: how the blog is doing at a glance. */
@Controller
public class AdminDashboardController {

    private final PostService postService;
    private final UserService userService;

    public AdminDashboardController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserPrincipal principal, Model model) {
        User current = principal.getUser();
        boolean superAdmin = current.isSuperAdmin();

        long myPosts = postService.countByAuthor(current);
        long myViews = postService.totalViewsForAuthor(current);
        Page<Post> recent = postService.findAllByAuthor(current, 0, 5);

        model.addAttribute("currentUser", current);
        model.addAttribute("myPostCount", myPosts);
        model.addAttribute("myViewCount", myViews);
        model.addAttribute("recentPosts", recent);

        if (superAdmin) {
            model.addAttribute("sitePublishedCount", postService.countPublished());
            model.addAttribute("siteViewCount", postService.totalViews());
            model.addAttribute("activeAdminCount", userService.countActiveAdmins());
            model.addAttribute("sitePosts", postService.findAllForAdmin(0, 5));
        }

        return "admin/dashboard";
    }
}
