package com.vidurarvs.blog.service.impl;

import com.vidurarvs.blog.dto.AdminAccountFormDTO;
import com.vidurarvs.blog.exception.DuplicateResourceException;
import com.vidurarvs.blog.exception.ForbiddenActionException;
import com.vidurarvs.blog.exception.ResourceNotFoundException;
import com.vidurarvs.blog.model.Role;
import com.vidurarvs.blog.model.User;
import com.vidurarvs.blog.repository.UserRepository;
import com.vidurarvs.blog.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

    private void requireSuperAdmin(User actingUser) {
        if (actingUser == null || !actingUser.isSuperAdmin()) {
            throw new ForbiddenActionException("Only the blog owner can manage admin accounts.");
        }
    }
}
