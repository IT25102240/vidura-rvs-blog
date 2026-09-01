package com.vidurarvs.blog.config;

import com.vidurarvs.blog.model.Category;
import com.vidurarvs.blog.model.Role;
import com.vidurarvs.blog.model.User;
import com.vidurarvs.blog.repository.CategoryRepository;
import com.vidurarvs.blog.repository.UserRepository;
import com.vidurarvs.blog.util.SlugUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * First-run bootstrap: seeds the topic categories and creates your
 * SUPER_ADMIN (owner) account from application.properties, but only if
 * the database is empty. Safe to leave in place - it never overwrites
 * existing data.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.super-admin.full-name}")
    private String superAdminFullName;

    @Value("${app.bootstrap.super-admin.username}")
    private String superAdminUsername;

    @Value("${app.bootstrap.super-admin.email}")
    private String superAdminEmail;

    @Value("${app.bootstrap.super-admin.password}")
    private String superAdminPassword;

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Technology",
            "Science",
            "Programming",
            "Information & Communication Technology",
            "Gaming",
            "Travel",
            "Food",
            "Education",
            "Philosophy",
            "Society & Opinion"
    );

    public DataInitializer(CategoryRepository categoryRepository,
                            UserRepository userRepository,
                            PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedCategories();
        seedSuperAdmin();
    }

    private void seedCategories() {
        for (String name : DEFAULT_CATEGORIES) {
            if (!categoryRepository.existsByNameIgnoreCase(name)) {
                categoryRepository.save(new Category(name, SlugUtils.toSlug(name)));
            }
        }
    }

    private void seedSuperAdmin() {
        if (userRepository.count() > 0) {
            return;
        }
        User owner = new User();
        owner.setFullName(superAdminFullName);
        owner.setUsername(superAdminUsername);
        owner.setEmail(superAdminEmail);
        owner.setPassword(passwordEncoder.encode(superAdminPassword));
        owner.setRole(Role.SUPER_ADMIN);
        owner.setActive(true);
        userRepository.save(owner);
        System.out.println("=========================================================");
        System.out.println(" ViduraRvs: created your owner account.");
        System.out.println(" Username: " + superAdminUsername);
        System.out.println(" Log in at /login, then change the password immediately");
        System.out.println(" (edit application.properties before first run to set it).");
        System.out.println("=========================================================");
    }
}
