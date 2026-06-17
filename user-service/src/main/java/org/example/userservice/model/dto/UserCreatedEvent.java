package org.example.userservice.model.dto;

public record UserCreatedEvent(
        String email,
        String username,
        String role
) {
}
