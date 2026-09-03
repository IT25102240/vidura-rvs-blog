package com.vidurarvs.blog.repository;

import com.vidurarvs.blog.model.Category;
import com.vidurarvs.blog.model.Post;
import com.vidurarvs.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Persistence for posts. Query methods only - no business rules here
 * (those live in PostService) so this interface stays swappable, e.g. for
 * a future file-backed or cached implementation without touching callers.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Every finder below explicitly "join fetch"es category and author.
     * Post.category/author are LAZY (see Post entity), and these results
     * are rendered by Thymeleaf templates *after* the transaction that
     * loaded them has closed - accessing post.category.name at render time
     * would otherwise throw LazyInitializationException ("no Session").
     * Fetching both eagerly here keeps the lazy mapping (cheap loads
     * elsewhere) while making list/detail views always render safely.
     */
    @Query("select p from Post p join fetch p.category join fetch p.author where p.slug = :slug")
    Optional<Post> findBySlug(@Param("slug") String slug);

    boolean existsBySlug(String slug);

    /** Latest published posts first - the visitor-facing home feed. */
    @Query(value = "select p from Post p join fetch p.category join fetch p.author where p.published = true order by p.createdAt desc",
            countQuery = "select count(p) from Post p where p.published = true")
    Page<Post> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = "select p from Post p join fetch p.category join fetch p.author where p.published = true and p.category = :category order by p.createdAt desc",
            countQuery = "select count(p) from Post p where p.published = true and p.category = :category")
    Page<Post> findByPublishedTrueAndCategoryOrderByCreatedAtDesc(@Param("category") Category category, Pageable pageable);

    @Query(value = "select p from Post p join fetch p.category join fetch p.author where p.published = true and p.category = :category and p.id <> :id order by p.createdAt desc",
            countQuery = "select count(p) from Post p where p.published = true and p.category = :category and p.id <> :id")
    Page<Post> findByPublishedTrueAndCategoryAndIdNotOrderByCreatedAtDesc(@Param("category") Category category, @Param("id") Long id, Pageable pageable);

    /** All posts regardless of published state, for the admin list. */
    @Query(value = "select p from Post p join fetch p.category join fetch p.author order by p.createdAt desc",
            countQuery = "select count(p) from Post p")
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = "select p from Post p join fetch p.category join fetch p.author where p.author = :author order by p.createdAt desc",
            countQuery = "select count(p) from Post p where p.author = :author")
    Page<Post> findByAuthorOrderByCreatedAtDesc(@Param("author") User author, Pageable pageable);

    long countByPublishedTrue();

    long countByAuthor(User author);

    /** Used by CategoryServiceImpl to prevent deletion of categories that still have posts. */
    long countByCategory(Category category);

    @Query("select coalesce(sum(p.viewCount), 0) from Post p")
    long sumAllViewCounts();

    @Query("select coalesce(sum(p.viewCount), 0) from Post p where p.author = :author")
    long sumViewCountsForAuthor(@Param("author") User author);

    /**
     * Keyword search across title, summary, body, tags and category name.
     * Only published posts are searchable by visitors; newest first.
     *
     * Implemented as a native query (plain SQL) rather than JPQL: Hibernate
     * 6's HQL function-argument validator has a known false-positive when
     * the same named parameter is reused inside several lower(...) like ...
     * comparisons in one query, which rejects this search at startup. Native
     * SQL sidesteps that validator entirely and is the standard escape
     * hatch for this kind of multi-column LIKE search.
     *
     * The caller passes an already-lower-cased "%keyword%" pattern (see
     * PostServiceImpl).
     */
    @Query(value = """
            select p.* from posts p
            join categories c on c.id = p.category_id
            where p.published = true
              and (
                lower(p.title) like :pattern
                or lower(p.summary) like :pattern
                or lower(p.content) like :pattern
                or lower(p.tags) like :pattern
                or lower(c.name) like :pattern
              )
            order by p.created_at desc
            """,
            countQuery = """
            select count(*) from posts p
            join categories c on c.id = p.category_id
            where p.published = true
              and (
                lower(p.title) like :pattern
                or lower(p.summary) like :pattern
                or lower(p.content) like :pattern
                or lower(p.tags) like :pattern
                or lower(c.name) like :pattern
              )
            """,
            nativeQuery = true)
    Page<Post> searchPublished(@Param("pattern") String pattern, Pageable pageable);
}
