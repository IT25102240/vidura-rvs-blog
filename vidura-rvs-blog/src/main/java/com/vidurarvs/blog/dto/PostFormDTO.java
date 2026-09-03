package com.vidurarvs.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Data carried by the create/edit post form. Kept separate from the Post
 * entity so validation and file-upload handling never leak into the
 * persistence model (single-responsibility for each class).
 *
 * NOTE: No Lombok on DTOs — explicit getters/setters avoids annotation-
 * processing ordering issues with the Maven compiler plugin.
 */
public class PostFormDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be 200 characters or fewer")
    private String title;

    @NotBlank(message = "Give readers a one or two sentence summary")
    @Size(max = 400, message = "Summary must be 400 characters or fewer")
    private String summary;

    @NotBlank(message = "The post needs some content")
    private String content;

    @NotNull(message = "Choose a category")
    private Long categoryId;

    private String tags;

    /**
     * YouTube input — accepts any format the admin pastes:
     *   - Full URL:   https://www.youtube.com/watch?v=XXXXXXXXXXX
     *   - Short URL:  https://youtu.be/XXXXXXXXXXX
     *   - Embed code: <iframe src="https://www.youtube.com/embed/XXXXXXXXXXX"...>
     *   - Bare 11-char ID: XXXXXXXXXXX
     */
    private String youtubeInput;

    /** Up to 5 new image uploads. */
    private List<MultipartFile> newImages = new ArrayList<>();

    /** IDs of existing PostImage records the admin wants to remove. */
    private List<Long> removeImageIds = new ArrayList<>();

    private boolean published = true;

    // Legacy single-image fields kept so old form binding paths still compile.
    private MultipartFile coverImage;
    private boolean removeCoverImage;

    // ---- Explicit getters/setters (no Lombok on DTOs) ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getYoutubeInput() { return youtubeInput; }
    public void setYoutubeInput(String youtubeInput) { this.youtubeInput = youtubeInput; }

    public List<MultipartFile> getNewImages() { return newImages; }
    public void setNewImages(List<MultipartFile> newImages) { this.newImages = newImages; }

    public List<Long> getRemoveImageIds() { return removeImageIds; }
    public void setRemoveImageIds(List<Long> removeImageIds) { this.removeImageIds = removeImageIds; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public MultipartFile getCoverImage() { return coverImage; }
    public void setCoverImage(MultipartFile coverImage) { this.coverImage = coverImage; }

    public boolean isRemoveCoverImage() { return removeCoverImage; }
    public void setRemoveCoverImage(boolean removeCoverImage) { this.removeCoverImage = removeCoverImage; }
}
