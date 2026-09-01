package com.vidurarvs.blog.controller;

import com.vidurarvs.blog.dto.PostFormDTO;
import com.vidurarvs.blog.model.Post;
import com.vidurarvs.blog.model.User;
import com.vidurarvs.blog.security.CustomUserPrincipal;
import com.vidurarvs.blog.service.CategoryService;
import com.vidurarvs.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Everything a signed-in admin uses to write and manage articles. Ownership
 * checks (an ADMIN may only touch their own posts) live in PostService, not
 * here - this controller only translates HTTP <-> service calls.
 */
@Controller
@RequestMapping("/admin/posts")
public class AdminPostController {

    private final PostService postService;
    private final CategoryService categoryService;

    public AdminPostController(PostService postService, CategoryService categoryService) {
        this.postService = postService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserPrincipal principal,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        User current = principal.getUser();
        Page<Post> posts = current.isSuperAdmin()
                ? postService.findAllForAdmin(page, 10)
                : postService.findAllByAuthor(current, page, 10);
        model.addAttribute("posts", posts);
        model.addAttribute("currentUser", current);
        return "admin/post-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        PostFormDTO form = new PostFormDTO();
        model.addAttribute("postForm", form);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isNew", true);
        return "admin/post-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("postForm") PostFormDTO form,
                          BindingResult bindingResult,
                          @AuthenticationPrincipal CustomUserPrincipal principal,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("isNew", true);
            return "admin/post-form";
        }
        Post created = postService.create(form, principal.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Published \"" + created.getTitle() + "\".");
        return "redirect:/admin/posts";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserPrincipal principal,
                            Model model) {
        Post post = postService.findByIdOrThrow(id);
        postService.checkEditable(post, principal.getUser());
        PostFormDTO form = new PostFormDTO();
        form.setId(post.getId());
        form.setTitle(post.getTitle());
        form.setSummary(post.getSummary());
        form.setContent(post.getContent());
        form.setCategoryId(post.getCategory().getId());
        form.setTags(post.getTags());
        form.setYoutubeVideoId(post.getYoutubeVideoId());
        form.setPublished(post.isPublished());

        model.addAttribute("postForm", form);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isNew", false);
        model.addAttribute("existingPost", post);
        return "admin/post-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("postForm") PostFormDTO form,
                          BindingResult bindingResult,
                          @AuthenticationPrincipal CustomUserPrincipal principal,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("isNew", false);
            model.addAttribute("existingPost", postService.findByIdOrThrow(id));
            return "admin/post-form";
        }
        Post updated = postService.update(id, form, principal.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Saved \"" + updated.getTitle() + "\".");
        return "redirect:/admin/posts";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                          @AuthenticationPrincipal CustomUserPrincipal principal,
                          RedirectAttributes redirectAttributes) {
        postService.delete(id, principal.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Post deleted.");
        return "redirect:/admin/posts";
    }
}
