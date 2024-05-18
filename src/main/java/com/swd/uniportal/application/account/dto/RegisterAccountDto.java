package com.swd.uniportal.application.account.dto;

import com.swd.uniportal.domain.account.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterAccountDto {

    @NotBlank(message = "Email must not be null or blank.")
    @Email(message = "Email must have valid structure")
    private String email;

    @NotBlank(message = "Name must not be null or blank.")
    private String firstName;

    @NotBlank(message = "Password must not be null or blank.")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters.")
    private String password;

    @NotNull(message = "Role must not be null.")
    private Role role;

    private String avatarLink;
}
