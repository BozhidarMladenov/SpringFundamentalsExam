package com.softuni.gearshare.model.dto;

import com.softuni.gearshare.model.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters.")
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Please provide a valid email address.")
    @Size(max = 120, message = "Email must be at most 120 characters.")
    private String email;

    @NotBlank(message = "Full name is required.")
    @Size(min = 2, max = 60, message = "Full name must be between 2 and 60 characters.")
    private String fullName;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters.")
    private String password;

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;

    @NotNull(message = "Please select a role.")
    private UserRole role;
}
