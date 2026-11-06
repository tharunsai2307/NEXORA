package com.smartbuilding.user.dto;

import com.smartbuilding.user.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role is required")
        Role role
) {}

