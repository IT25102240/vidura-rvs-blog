package com.vidurarvs.blog.service.impl;

import com.vidurarvs.blog.dto.PostFormDTO;
import com.vidurarvs.blog.exception.ForbiddenActionException;
import com.vidurarvs.blog.exception.ResourceNotFoundException;
import com.vidurarvs.blog.model.Category;
import com.vidurarvs.blog.model.Post;
import com.vidurarvs.blog.model.PostImage;
import com.vidurarvs.blog.model.User;
import com.vidurarvs.blog.repository.PostImageRepository;
import com.vidurarvs.blog.repository.PostRepository;
import com.vidurarvs.blog.service.CategoryService;
import com.vidurarvs.blog.service.PostService;
import com.vidurarvs.blog.util.SlugUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PostServiceImpl implements PostService {

    private static final int MAX_IMAGES = 5;

    // Regex patterns for YouTube ID extraction
    private static final Pattern YT_WATCH  = Pattern.compile("[?&]v=([A-Za-z0-9_-]{11})");
    private static final Pattern YT_SHORT  = Pattern.compile("youtu\\.be/([A-Za-z0-9_-]{11})");
    private static final Pattern YT_EMBED  = Pattern.compile("youtube\\.com/embed/([A-Za-z0-9_-]{11})");
    private static final Pattern YT_BARE   = Pattern.compile("^([A-Za-z0-9_-]{11})$");

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final CategoryService categoryService;
    private final Path uploadRoot;

    public PostServiceImpl(PostRepository postRepository,
                           PostImageRepository postImageRepository,
                           CategoryService categoryService,
                           org.springframework.core.env.Environment env) {
        this.postRepository = postRepository;
        this.postImageRepository = postImageRepository;
        this.categoryService = categoryService;
        this.uploadRoot = Path.of(env.getProperty("app.upload.dir", "uploads"));
    }

    @Override
    public Page<Post> findLatestPublished(int page, int pageSize) {
        return postRepository.findByPublishedTrueOrderByCreatedAtDesc(pageable(page, pageSize));
    }

    @Override
    public Page<Post> findLatestPublishedByCategory(Category category, int page, int pageSize) {
        return postRepository.findByPublishedTrueAndCategoryOrderByCreatedAtDesc(category, pageable(page, pageSize));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Post> search(String keyword, int page, int pageSize) {
        String trimmed = keyword == null ? "" : keyword.trim().toLowerCase();
        String pattern = "%" + trimmed + "%";
        Page<Post> results = postRepository.searchPublished(pattern, unsortedPageable(page, pageSize));
        results.forEach(post -> {
            org.hibernate.Hibernate.initialize(post.getCategory());
            org.hibernate.Hibernate.initialize(post.getAuthor());
        });
        return results;
    }

    @Override
    @Transactional
    public Post findPublishedBySlugAndRecordView(String slug) {
        Post post = postRepository.findBySlug(slug)
                .filter(Post::isPublished)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + slug));
        post.incrementViewCount();
        return postRepository.save(post);
    }

    @Override
    public Page<Post> relatedTo(Post post, int limit) {
        return postRepository.findByPublishedTrueAndCategoryAndIdNotOrderByCreatedAtDesc(
                post.getCategory(), post.getId(), PageRequest.of(0, limit));
    }

    @Override
    public Page<Post> findAllForAdmin(int page, int pageSize) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable(page, pageSize));
    }

    @Override
    public Page<Post> findAllByAuthor(User author, int page, int pageSize) {
        return postRepository.findByAuthorOrderByCreatedAtDesc(author, pageable(page, pageSize));
    }

    @Override
    public Post findByIdOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id " + id));
    }

    @Override
    @Transactional
    public Post create(PostFormDTO form, User author) {
        Post post = new Post();
        post.setAuthor(author);
        applyForm(post, form, true);
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public Post update(Long postId, PostFormDTO form, User actingUser) {
        Post post = findByIdOrThrow(postId);
        requireOwnerOrSuperAdmin(post, actingUser);
        applyForm(post, form, false);
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public void delete(Long postId, User actingUser) {
        Post post = findByIdOrThrow(postId);
        requireOwnerOrSuperAdmin(post, actingUser);
        postRepository.delete(post);
    }

    @Override
    @Transactional
    public void toggleVisibility(Long postId, User actingUser) {
        Post post = findByIdOrThrow(postId);
        requireOwnerOrSuperAdmin(post, actingUser);
        post.setPublished(!post.isPublished());
        postRepository.save(post);
    }

    @Override
    public long countPublished() {
        return postRepository.countByPublishedTrue();
    }

    @Override
    public long countByAuthor(User author) {
        return postRepository.countByAuthor(author);
    }

    @Override
    public long totalViews() {
        return postRepository.sumAllViewCounts();
    }

    @Override
    public long totalViewsForAuthor(User author) {
        return postRepository.sumViewCountsForAuthor(author);
    }

    // ---- internal helpers ----------------------------------------------

    private void applyForm(Post post, PostFormDTO form, boolean isNew) {
        Category category = categoryService.findByIdOrThrow(form.getCategoryId());

        post.setTitle(form.getTitle());
        post.setSummary(form.getSummary());
        post.setContent(form.getContent());
        post.setCategory(category);
        post.setTags(form.getTags());
        post.setPublished(form.isPublished());

        // Parse YouTube input — accepts any format
        String ytId = extractYouTubeId(form.getYoutubeInput());
        post.setYoutubeVideoId(ytId);

        if (isNew || post.getSlug() == null) {
            post.setSlug(generateUniqueSlug(form.getTitle()));
        }

        // Remove requested images
        if (form.getRemoveImageIds() != null && !form.getRemoveImageIds().isEmpty() && post.getId() != null) {
            postImageRepository.deleteByPostAndIdIn(post, form.getRemoveImageIds());
        }

        // Handle legacy single cover image removal
        if (form.isRemoveCoverImage()) {
            post.setCoverImagePath(null);
        }

        // Store new images (up to MAX_IMAGES total)
        if (form.getNewImages() != null) {
            long existing = post.getId() != null ? postImageRepository.countByPost(post) : 0;
            int slotsLeft = (int) Math.max(0, MAX_IMAGES - existing);
            int order = (int) existing;
            for (MultipartFile file : form.getNewImages()) {
                if (slotsLeft <= 0) break;
                if (file == null || file.isEmpty()) continue;
                String stored = storeImage(file);
                PostImage img = new PostImage(post, stored, order++);
                post.getImages().add(img);
                slotsLeft--;
            }
        }

        // Legacy single cover image upload (from old form)
        if (form.getCoverImage() != null && !form.getCoverImage().isEmpty()) {
            post.setCoverImagePath(storeImage(form.getCoverImage()));
        }
    }

    /**
     * Extracts the 11-character YouTube video ID from any common format.
     * Returns null if input is blank or not a recognisable YouTube reference.
     */
    public static String extractYouTubeId(String input) {
        if (!StringUtils.hasText(input)) return null;
        String trimmed = input.trim();

        for (Pattern p : List.of(YT_WATCH, YT_SHORT, YT_EMBED)) {
            Matcher m = p.matcher(trimmed);
            if (m.find()) return m.group(1);
        }
        // Bare 11-char ID (may be inside an iframe; try to isolate it)
        Matcher bare = YT_BARE.matcher(trimmed);
        if (bare.matches()) return bare.group(1);

        return null; // unrecognised format — ignore
    }

    private String generateUniqueSlug(String title) {
        String base = SlugUtils.toSlug(title);
        if (!StringUtils.hasText(base)) {
            base = "post";
        }
        String candidate = base;
        int suffix = 2;
        while (postRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private String storeImage(MultipartFile file) {
        try {
            Files.createDirectories(uploadRoot);
            String original = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
            String extension = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                extension = original.substring(dot);
            }
            String storedName = UUID.randomUUID() + extension;
            Path target = uploadRoot.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return storedName;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store uploaded image", e);
        }
    }

    @Override
    public void checkEditable(Post post, User actingUser) {
        requireOwnerOrSuperAdmin(post, actingUser);
    }

    private void requireOwnerOrSuperAdmin(Post post, User actingUser) {
        boolean isOwner = post.getAuthor() != null && post.getAuthor().getId().equals(actingUser.getId());
        if (!isOwner && !actingUser.isSuperAdmin()) {
            throw new ForbiddenActionException("You can only edit or delete your own posts.");
        }
    }

    private Pageable pageable(int page, int pageSize) {
        int safePage = Math.max(page, 0);
        int safeSize = pageSize <= 0 ? 8 : pageSize;
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private Pageable unsortedPageable(int page, int pageSize) {
        int safePage = Math.max(page, 0);
        int safeSize = pageSize <= 0 ? 8 : pageSize;
        return PageRequest.of(safePage, safeSize);
    }
}
