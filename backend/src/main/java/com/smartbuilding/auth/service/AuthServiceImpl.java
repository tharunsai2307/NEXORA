package com.smartbuilding.auth.service;

import com.smartbuilding.audit.service.AuditService;
import com.smartbuilding.auth.dto.AuthResponse;
import com.smartbuilding.auth.dto.LoginRequest;
import com.smartbuilding.auth.dto.RegisterRequest;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.security.jwt.JwtTokenProvider;
import com.smartbuilding.user.dto.UserResponse;
import com.smartbuilding.user.model.Role;
import com.smartbuilding.user.model.User;
import com.smartbuilding.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw ServiceException.badRequest("Password and confirmation password do not match");
        }

        if (userRepository.existsByEmail(request.email().toLowerCase().trim())) {
            throw ServiceException.conflict("A user with this email address already exists");
        }

        String userId = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(request.password());

        // New public registrations default to BUILDING_MANAGER
        User user = new User(
                userId,
                request.fullName().trim(),
                request.email().toLowerCase().trim(),
                encodedPassword,
                request.phoneNumber() != null ? request.phoneNumber().trim() : null,
                Role.BUILDING_MANAGER,
                true
        );

        User savedUser = userRepository.save(user);

        auditService.log(
                savedUser.getId(),
                "USER_REGISTERED",
                "USER",
                savedUser.getId(),
                "User registered with role " + savedUser.getRole()
        );

        String token = tokenProvider.generateTokenForUser(savedUser);
        return AuthResponse.of(token, tokenProvider.getJwtExpirationMs(), UserResponse.fromEntity(savedUser));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ServiceException.unauthorized("Invalid email or password"));

        if (!user.isActive()) {
            throw ServiceException.unauthorized("Your account has been deactivated. Please contact your system administrator.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ServiceException.unauthorized("Invalid email or password");
        }

        String token = tokenProvider.generateTokenForUser(user);

        auditService.log(
                user.getId(),
                "USER_LOGIN",
                "USER",
                user.getId(),
                "User successfully logged in"
        );

        return AuthResponse.of(token, tokenProvider.getJwtExpirationMs(), UserResponse.fromEntity(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> ServiceException.notFound("User not found"));
        return UserResponse.fromEntity(user);
    }
}

