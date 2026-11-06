package com.smartbuilding.user.dto;

import com.smartbuilding.user.model.Role;
import com.smartbuilding.user.model.User;

import java.time.Instant;

public record UserResponse(
        String id,
        String fullName,
        String email,
        String phoneNumber,
        Role role,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

