package com.robotics.banking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank @Email String email) {
}
