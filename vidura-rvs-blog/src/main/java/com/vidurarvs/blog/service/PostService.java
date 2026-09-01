package com.vidurarvs.blog.service;

import com.vidurarvs.blog.dto.PostFormDTO;
import com.vidurarvs.blog.model.Category;
import com.vidurarvs.blog.model.Post;
import com.vidurarvs.blog.model.User;
import org.springframework.data.domain.Page;

/**
 * All post business logic: publishing rules, search, view counting and
 * per-author authorization. Controllers depend on this interface, never
 * on {@code PostServiceImpl} directly (dependency inversion) - a caching
 * or file-backed implementation could be swapped in without touching a
 * single controller.
 */
public interface PostService {

    /** Latest published posts first - the visitor home feed. */
    Page<Post> findLatestPublished(int page, int pageSize);

    Page<Post> findLatestPublishedByCategory(Category category, int page, int pageSize);

    Page<Post> search(String keyword, int page, int pageSize);

    /** Looks up a published post by slug and records a view. Throws if missing/unpublished. */
    Post findPublishedBySlugAndRecordView(String slug);

    Page<Post> relatedTo(Post post, int limit);

    /** Admin-facing lists (drafts included). */
    Page<Post> findAllForAdmin(int page, int pageSize);

    Page<Post> findAllByAuthor(User author, int page, int pageSize);

    Post findByIdOrThrow(Long id);

    /**
     * Throws ForbiddenActionException unless actingUser wrote the post or is
     * the super-admin. Controllers must call this before *displaying* an
     * edit form too, not just before saving - otherwise any signed-in admin
     * could still view (and infer the contents of) another author's draft.
     */
    void checkEditable(Post post, User actingUser);

    Post create(PostFormDTO form, User author);

    /** Only the author, or the super-admin, may edit/delete a post. */
    Post update(Long postId, PostFormDTO form, User actingUser);

    void delete(Long postId, User actingUser);

    long countPublished();

    long countByAuthor(User author);

    long totalViews();

    long totalViewsForAuthor(User author);
}
