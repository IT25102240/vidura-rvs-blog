package com.vidurarvs.blog.service;

import com.vidurarvs.blog.dto.AdminAccountFormDTO;
import com.vidurarvs.blog.model.User;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Everything about managing verified admin accounts. Only a SUPER_ADMIN
 * is allowed to call the mutating methods - callers (controllers) are
 * responsible for checking the caller's role before invoking them, and
 * this service double-checks with {@link com.vidurarvs.blog.exception.ForbiddenActionException}.
 */
public interface UserService {

    List<User> findAll();

    @NonNull
    User findByIdOrThrow(Long id);

    @NonNull
    User findByUsernameOrThrow(String username);

    /** Invite a new ADMIN. Only the acting user being a SUPER_ADMIN may call this. */
    User createAdmin(AdminAccountFormDTO form, User actingUser);

    /** Enable/disable login for an admin account. The SUPER_ADMIN account can't be deactivated. */
    void setActive(Long userId, boolean active, User actingUser);

    long countActiveAdmins();

    /**
     * Update an admin's bio and profile picture. An admin may only update
     * their own profile; the SUPER_ADMIN may update anyone's.
     */
    void updateProfile(Long userId, String bio, MultipartFile photo, User actingUser);
}
