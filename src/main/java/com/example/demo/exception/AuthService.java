package com.example.demo.service;

import com.example.demo.dto.AuthResponseDto;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.RegisterRequestDto;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.exception.AuthException;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    private final java.util.concurrent.ConcurrentHashMap<String, LoginAttempt> loginAttempts = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 60_000; // 1 минута

    private static class LoginAttempt {
        int count = 0;
        long lockedUntil = 0;
    }

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public void register(RegisterRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new AuthException("Email уже зарегистрирован");
        }
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        userRepository.save(user);
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto dto) {
        checkRateLimit(dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail()).orElse(null);

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            recordFailedAttempt(dto.getEmail());
            throw new AuthException("Неверный email или пароль");
        }

        resetAttempts(dto.getEmail());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(user.getId(), refreshToken);

        return new AuthResponseDto(accessToken, refreshToken);
    }

    private void checkRateLimit(String email) {
        LoginAttempt attempt = loginAttempts.get(email);
        if (attempt != null && attempt.lockedUntil > System.currentTimeMillis()) {
            throw new com.example.demo.exception.RateLimitException("Слишком много попыток входа");
        }
    }

    private void recordFailedAttempt(String email) {
        LoginAttempt attempt = loginAttempts.computeIfAbsent(email, k -> new LoginAttempt());
        attempt.count++;
        if (attempt.count >= MAX_ATTEMPTS) {
            attempt.lockedUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
            attempt.count = 0;
        }
    }

    private void resetAttempts(String email) {
        loginAttempts.remove(email);
    }

    @Transactional
    public AuthResponseDto refresh(String refreshToken) {
        String tokenHash = hash(refreshToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException("Невалидный refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new AuthException("Refresh token истёк или отозван");
        }

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new AuthException("Невалидный refresh token");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new AuthException("Пользователь не найден"));

        String newAccessToken = jwtService.generateAccessToken(user);

        return new AuthResponseDto(newAccessToken, refreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = hash(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException("Невалидный refresh token"));
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
    }

    private void saveRefreshToken(Long userId, String rawToken) {
        RefreshToken entity = new RefreshToken();
        entity.setUserId(userId);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(Instant.now().plusMillis(7L * 24 * 60 * 60 * 1000));
        entity.setRevoked(false);
        refreshTokenRepository.save(entity);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}