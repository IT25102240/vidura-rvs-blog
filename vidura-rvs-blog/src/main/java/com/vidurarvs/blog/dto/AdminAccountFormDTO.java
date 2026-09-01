package com.vidurarvs.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data carried by the "invite a new admin" form on the super-admin panel.
 */
@Getter
@Setter
public class AdminAccountFormDTO {

    @NotBlank(message = "Full name is required")
    @Size(max = 120)
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must be 3-60 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Set a temporary password for this admin")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
