package org.example.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.userservice.model.dto.AuthResponse;
import org.example.userservice.model.dto.LoginRequest;
import org.example.userservice.model.dto.RegisterRequest;
import org.example.userservice.model.dto.UserDto;
import org.example.userservice.service.impl.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        UserDto registeredUser = userService.register(request);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = userService.login(request);
        String fullToken = "Bearer " + authResponse.token();
        AuthResponse updatedResponse = new AuthResponse(
                fullToken,
                authResponse.username(),
                authResponse.role()
        );
        return ResponseEntity.ok(updatedResponse);
    }

}
