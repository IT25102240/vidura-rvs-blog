package com.vidurarvs.blog.repository;

import com.vidurarvs.blog.model.Post;
import com.vidurarvs.blog.model.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostOrderBySortOrderAsc(Post post);

    @Modifying
    @Query("delete from PostImage pi where pi.post = :post and pi.id in :ids")
    void deleteByPostAndIdIn(@Param("post") Post post, @Param("ids") List<Long> ids);

    long countByPost(Post post);
}
