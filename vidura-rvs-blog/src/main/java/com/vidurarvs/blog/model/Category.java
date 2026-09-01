package com.vidurarvs.blog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A topic bucket for posts (Technology, Science, Travel, Gaming, Food,
 * Education, Philosophy, ICT, Programming, Society, ...). Seeded on first
 * run by DataInitializer and manageable by growing that seed list.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
}
