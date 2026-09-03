package com.vidurarvs.blog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A single article/post authored by an admin.
 *
 * Content model kept deliberately simple (per project scope): a plain-text
 * body, one optional cover image, one optional embedded YouTube video, and
 * a free-text tag list used for search. Since only verified admins can ever
 * write a post, the body is rendered as trusted HTML (see post-detail.html)
 * so an author can still paste extra &lt;img&gt;/&lt;a&gt; tags if they want to -
 * this is a standard trusted-author CMS pattern, not something a visitor
 * can influence.
 */
@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(nullable = false, length = 400)
    private String summary;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    /** Relative path under /uploads, e.g. "uploads/1699999999-cover.jpg". Optional. */
    @Column(length = 300)
    private String coverImagePath;

    /** Just the 11-char YouTube video id, e.g. "dQw4w9WgXcQ". Optional. */
    @Column(length = 50)
    private String youtubeVideoId;

    /** Comma-separated free-text tags, used by keyword search. */
    @Column(length = 300)
    private String tags;

    /**
     * All images for this post (cover first, then gallery).
     * Replaces the single coverImagePath field for new posts.
     * Loaded eagerly on detail pages to avoid LazyInitializationException.
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<PostImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private long viewCount = 0L;

    @Column(nullable = false)
    private boolean published = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public boolean hasCoverImage() {
        return (images != null && !images.isEmpty()) ||
               (coverImagePath != null && !coverImagePath.isBlank());
    }

    /** Returns the primary/cover image path (first in sort order, or legacy coverImagePath). */
    public String primaryImagePath() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getImagePath();
        }
        return coverImagePath;
    }

    public boolean hasVideo() {
        return youtubeVideoId != null && !youtubeVideoId.isBlank();
    }

    public String youtubeWatchUrl() {
        return "https://www.youtube.com/watch?v=" + youtubeVideoId;
    }
}
