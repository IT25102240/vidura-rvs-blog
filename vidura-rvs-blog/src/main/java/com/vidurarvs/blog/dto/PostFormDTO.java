package com.vidurarvs.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data carried by the create/edit post form. Kept separate from the Post
 * entity so validation and file-upload handling never leak into the
 * persistence model (single-responsibility for each class).
 */
@Getter
@Setter
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

    private String youtubeVideoId;

    /** Optional new cover image upload. Leave empty to keep the existing one. */
    private MultipartFile coverImage;

    /** Set when the author wants to remove the existing cover image without replacing it. */
    private boolean removeCoverImage;

    private boolean published = true;
}
