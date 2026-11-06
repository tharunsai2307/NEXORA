package com.smartbuilding.building.dto;

import com.smartbuilding.building.model.BuildingStatus;
import com.smartbuilding.building.model.BuildingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BuildingRequest(
        @NotBlank(message = "Building name is required")
        @Size(max = 150, message = "Building name cannot exceed 150 characters")
        String name,

        @NotBlank(message = "Building code is required")
        @Size(max = 64, message = "Building code cannot exceed 64 characters")
        String buildingCode,

        @NotNull(message = "Building type is required")
        BuildingType buildingType,

        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address,

        BuildingStatus status,

        String description
) {}
