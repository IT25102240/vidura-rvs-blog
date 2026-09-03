package com.vidurarvs.blog.service.impl;

import com.vidurarvs.blog.dto.AdminAccountFormDTO;
import com.vidurarvs.blog.exception.DuplicateResourceException;
import com.vidurarvs.blog.exception.ForbiddenActionException;
import com.vidurarvs.blog.exception.ResourceNotFoundException;
import com.vidurarvs.blog.model.Role;
import com.vidurarvs.blog.model.User;
import com.vidurarvs.blog.repository.UserRepository;
import com.vidurarvs.blog.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
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

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Path uploadRoot;

    public UserServiceImpl(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.upload.dir:uploads}") String uploadDir) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.uploadRoot = Path.of(uploadDir);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAllByOrderByCreatedAtAsc();
    }

    @Override
    public User findByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id " + id));
    }

    @Override
    public User findByUsernameOrThrow(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + username));
    }

    @Override
    @Transactional
    public User createAdmin(AdminAccountFormDTO form, User actingUser) {
        requireSuperAdmin(actingUser);

        if (userRepository.existsByUsernameIgnoreCase(form.getUsername())) {
            throw new DuplicateResourceException("That username is already taken: " + form.getUsername());
        }
        if (userRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new DuplicateResourceException("That email is already registered: " + form.getEmail());
        }

        User admin = new User();
        admin.setFullName(form.getFullName());
        admin.setUsername(form.getUsername());
        admin.setEmail(form.getEmail());
        admin.setPassword(passwordEncoder.encode(form.getPassword()));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        return userRepository.save(admin);
    }

    @Override
    @Transactional
    public void setActive(Long userId, boolean active, User actingUser) {
        requireSuperAdmin(actingUser);
        User target = findByIdOrThrow(userId);
        if (target.isSuperAdmin()) {
            throw new ForbiddenActionException("The owner account cannot be deactivated.");
        }
        target.setActive(active);
        userRepository.save(target);
    }

    @Override
    public long countActiveAdmins() {
        return userRepository.countByActiveTrue();
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, String bio, MultipartFile photo, User actingUser) {
        // An admin can only edit their own profile; super admin can edit anyone's
        boolean isSelf = actingUser.getId().equals(userId);
        if (!isSelf && !actingUser.isSuperAdmin()) {
            throw new ForbiddenActionException("You can only edit your own profile.");
        }
        User target = findByIdOrThrow(userId);
        if (StringUtils.hasText(bio)) {
            target.setBio(bio.trim());
        }
        if (photo != null && !photo.isEmpty()) {
            target.setProfilePicturePath(storeProfilePhoto(photo));
        }
        userRepository.save(target);
    }

    // ---- helpers -------------------------------------------------------

    private String storeProfilePhoto(MultipartFile file) {
        try {
            Files.createDirectories(uploadRoot);
            String original = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "photo" : file.getOriginalFilename());
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0)
                ext = original.substring(dot);
            String name = "profile-" + UUID.randomUUID() + ext;
            Files.copy(file.getInputStream(), uploadRoot.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            return name;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store profile photo", e);
        }
    }

    private void requireSuperAdmin(User actingUser) {
        if (actingUser == null || !actingUser.isSuperAdmin()) {
            throw new ForbiddenActionException("Only the blog owner can manage admin accounts.");
        }
    }
}
