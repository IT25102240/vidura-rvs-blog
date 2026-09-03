package com.vidurarvs.blog.controller;

import com.vidurarvs.blog.dto.PostFormDTO;
import com.vidurarvs.blog.model.Post;
import com.vidurarvs.blog.model.User;
import com.vidurarvs.blog.security.CustomUserPrincipal;
import com.vidurarvs.blog.service.CategoryService;
import com.vidurarvs.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
        // Display existing YT ID in the input field
        form.setYoutubeInput(post.getYoutubeVideoId());
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

    /**
     * Quick publish ↔ draft toggle — called from the post-list table.
     * Returns a redirect so the list refreshes with the updated status badge.
     */
    @PostMapping("/{id}/toggle-visibility")
    public String toggleVisibility(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserPrincipal principal,
                                    RedirectAttributes redirectAttributes) {
        postService.toggleVisibility(id, principal.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Post visibility updated.");
        return "redirect:/admin/posts";
    }

    /**
     * AJAX endpoint: browser auto-save posts the Quill HTML body here.
     * We save it with published=false (draft) so the admin can come back later.
     * Returns 200 OK or 400 if the post doesn't exist / isn't editable.
     */
    @PostMapping("/{id}/autosave")
    @ResponseBody
    public ResponseEntity<String> autosave(@PathVariable Long id,
                                            @RequestBody(required = false) String content,
                                            @AuthenticationPrincipal CustomUserPrincipal principal) {
        try {
            Post post = postService.findByIdOrThrow(id);
            postService.checkEditable(post, principal.getUser());
            // Build a minimal DTO just to save the content as a draft
            PostFormDTO draft = new PostFormDTO();
            draft.setId(id);
            draft.setTitle(post.getTitle());
            draft.setSummary(post.getSummary() == null ? "" : post.getSummary());
            draft.setContent(content == null ? "" : content);
            draft.setCategoryId(post.getCategory().getId());
            draft.setTags(post.getTags());
            draft.setYoutubeInput(post.getYoutubeVideoId());
            draft.setPublished(post.isPublished());
            postService.update(id, draft, principal.getUser());
            return ResponseEntity.ok("saved");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("error: " + e.getMessage());
        }
    }
}
