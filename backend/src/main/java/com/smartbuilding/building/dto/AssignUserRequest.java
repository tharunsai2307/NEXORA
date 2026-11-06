package com.smartbuilding.building.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignUserRequest(
        @NotBlank(message = "User ID is required")
        String userId
) {}

