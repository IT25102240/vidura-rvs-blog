package com.vidurarvs.blog.controller;

import com.vidurarvs.blog.model.Category;
import com.vidurarvs.blog.model.Post;
import com.vidurarvs.blog.service.CategoryService;
import com.vidurarvs.blog.service.PostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Everything a visitor (no login required) can see: the latest-first home
 * feed, a single post, category browsing, and keyword search.
 */
@Controller
public class HomeController {

    private final PostService postService;
    private final CategoryService categoryService;

    @Value("${app.pagination.page-size:8}")
    private int pageSize;

    public HomeController(PostService postService, CategoryService categoryService) {
        this.postService = postService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Post> posts = postService.findLatestPublished(page, pageSize);
        model.addAttribute("posts", posts);
        model.addAttribute("heading", "Latest posts");
        return "index";
    }

    @GetMapping("/category/{slug}")
    public String category(@PathVariable String slug,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        Category category = categoryService.findBySlugOrThrow(slug);
        Page<Post> posts = postService.findLatestPublishedByCategory(category, page, pageSize);
        model.addAttribute("posts", posts);
        model.addAttribute("heading", category.getName());
        model.addAttribute("activeCategory", category);
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false, defaultValue = "") String q,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        Page<Post> posts = q.isBlank()
                ? Page.empty()
                : postService.search(q, page, pageSize);
        model.addAttribute("posts", posts);
        model.addAttribute("heading", q.isBlank() ? "Search" : "Results for \"" + q + "\"");
        model.addAttribute("query", q);
        return "index";
    }

    @GetMapping("/post/{slug}")
    public String postDetail(@PathVariable String slug, Model model) {
        Post post = postService.findPublishedBySlugAndRecordView(slug);
        model.addAttribute("post", post);
        model.addAttribute("relatedPosts", postService.relatedTo(post, 3));
        return "post-detail";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
