package com.smartbuilding.building.dto;

import com.smartbuilding.building.model.FloorStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FloorRequest(
        @NotBlank(message = "Floor name is required")
        @Size(max = 100, message = "Floor name cannot exceed 100 characters")
        String floorName,

        @NotNull(message = "Floor number is required")
        Integer floorNumber,

        String description,

        @DecimalMin(value = "1.0", message = "Layout width must be greater than 0")
        Double layoutWidth,

        @DecimalMin(value = "1.0", message = "Layout depth must be greater than 0")
        Double layoutDepth,

        FloorStatus status
) {}

