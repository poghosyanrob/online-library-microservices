package org.example.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.userservice.model.dto.AdminDashboardDto;
import org.example.userservice.model.dto.RegisterRequest;
import org.example.userservice.model.dto.UpdateStatusRequest;
import org.example.userservice.model.dto.UserDto;
import org.example.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @PostMapping("/librarians")
    public ResponseEntity<UserDto> createLibrarian(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.registerLibrarian(request));
    }

    @GetMapping("/librarians")
    public ResponseEntity<List<UserDto>> getAllLibrarians() {
        return ResponseEntity.ok(userService.getAllLibrarians());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/librarians/{id}/status")
    public ResponseEntity<UserDto> updateLibrarianStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(userService.toggleUserStatus(id, request.active()));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(userService.toggleUserStatus(id, request.active()));
    }

    @GetMapping("/dashboard-statistics")
    public ResponseEntity<AdminDashboardDto> getDashboardStatistics() {
        return ResponseEntity.ok(userService.getAdminDashboardStatistics());
    }

}
