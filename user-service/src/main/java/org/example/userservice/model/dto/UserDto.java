package org.example.userservice.model.dto;

public record UserDto(
        Long id,
        String username,
        String email,
        String role,
        boolean active
) {
}
