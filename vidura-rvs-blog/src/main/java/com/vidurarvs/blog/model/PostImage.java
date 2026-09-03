package com.vidurarvs.blog.model;

import jakarta.persistence.*;

/**
 * An image attached to a blog post.
 * Posts can have up to 5 images; the first (sort_order=0) acts as the cover.
 * Images are served from the same /uploads/** path as all other uploads.
 *
 * Explicit getters/setters (no Lombok) to avoid annotation-processing
 * ordering issues that surface on a Maven clean compile.
 */
@Entity
@Table(name = "post_images")
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /** Filename stored on disk (UUID-based), served at /uploads/{imagePath}. */
    @Column(nullable = false, length = 300)
    private String imagePath;

    /** 0 = cover/primary image; higher numbers are gallery images. */
    @Column(nullable = false)
    private int sortOrder = 0;

    @Column(length = 200)
    private String caption;

    public PostImage() {}

    public PostImage(Post post, String imagePath, int sortOrder) {
        this.post = post;
        this.imagePath = imagePath;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
}
