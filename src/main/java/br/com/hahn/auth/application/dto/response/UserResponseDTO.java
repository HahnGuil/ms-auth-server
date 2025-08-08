package br.com.hahn.auth.application.dto.response;

import java.util.UUID;

public record UserResponseDTO(UUID userId,
                              String userName,
                              String email,
                              String firstName,
                              String lastName,
                              String pictureUrl,
                              String token) {

    public UserResponseDTO(String userName, String email, String token) {
        this(null, userName, email, null, null, null, token);
    }

}
