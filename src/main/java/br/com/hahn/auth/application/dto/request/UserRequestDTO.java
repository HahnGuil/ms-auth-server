package br.com.hahn.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequestDTO(
        @NotBlank(message = "User Name is required")
        String userName,
        @NotNull(message = "Email is required")
        String email,
        String password,
        String firstName,
        String lastName,
        String pictureUrl
) {

}
