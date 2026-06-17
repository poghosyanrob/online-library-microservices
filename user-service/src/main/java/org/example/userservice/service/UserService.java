package org.example.userservice.service;

import org.example.userservice.model.dto.AdminDashboardDto;
import org.example.userservice.model.dto.AuthResponse;
import org.example.userservice.model.dto.LoginRequest;
import org.example.userservice.model.dto.RegisterRequest;
import org.example.userservice.model.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserDto registerLibrarian(RegisterRequest request);
    List<UserDto> getAllLibrarians();
    List<UserDto> getAllUsers();
    UserDto toggleUserStatus(Long userId, boolean active);
    AdminDashboardDto getAdminDashboardStatistics();
}
