package org.example.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.userservice.client.BookClient;
import org.example.userservice.client.OrderClient;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.model.User;
import org.example.userservice.model.dto.AdminDashboardDto;
import org.example.userservice.model.dto.AuthResponse;
import org.example.userservice.model.dto.BookStatisticsResponse;
import org.example.userservice.model.dto.OrderStatisticsResponse;
import org.example.userservice.model.enums.Role;
import org.example.userservice.model.dto.LoginRequest;
import org.example.userservice.model.dto.RegisterRequest;
import org.example.userservice.model.dto.UserDto;
import org.example.userservice.exception.AccountBlockedException;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.service.UserService;
import org.example.userservice.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final BookClient bookClient;
    private final OrderClient orderClient;
    private final MessageSource messageSource;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    @Override
    @Transactional
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException(msg("error.username.taken"));
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException(msg("error.email.taken"));
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }


    @Override
    @Transactional
    public UserDto registerLibrarian(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException(msg("error.username.taken"));
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException(msg("error.email.taken"));
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.LIBRARIAN);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }


    @Override
    public List<UserDto> getAllLibrarians() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.LIBRARIAN)
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.USER)
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto toggleUserStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(msg("error.user.not.found")));

        user.setActive(active);
        User updatedUser = userRepository.save(user);

        return userMapper.toDto(updatedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException ex) {
            throw new RuntimeException(msg("error.invalid.credentials"));
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException(msg("error.user.not.found")));

        if (!user.isActive()) {
            throw new AccountBlockedException(msg("error.account.disabled"));
        }

        String token = jwtTokenUtil.generateToken(user.getEmail(), user.getRole().name());

        stringRedisTemplate.opsForValue().set(
                "active_token:" + user.getEmail(),
                token,
                Duration.ofMillis(jwtExpiration)
        );

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    @Override
    public AdminDashboardDto getAdminDashboardStatistics() {
        CompletableFuture<BookStatisticsResponse> bookFuture =
                CompletableFuture.supplyAsync(bookClient::getBookStatistics)
                        .exceptionally(e -> new BookStatisticsResponse(0L, 0L));
        CompletableFuture<OrderStatisticsResponse> orderFuture =
                CompletableFuture.supplyAsync(orderClient::getOrderStatistics)
                        .exceptionally(e -> new OrderStatisticsResponse(0L, 0.0));
        BookStatisticsResponse bookStats = bookFuture.join();
        OrderStatisticsResponse orderStats = orderFuture.join();
        return new AdminDashboardDto(
                bookStats.totalBookTitles(),
                bookStats.totalBookQuantity(),
                orderStats.totalOrders(),
                orderStats.totalRevenue()
        );
    }
}
