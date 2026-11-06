package com.smartbuilding.building.dto;

import com.smartbuilding.building.model.RoomType;
import com.smartbuilding.building.model.SafetyStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RoomRequest(
        @NotBlank(message = "Room name is required")
        @Size(max = 100, message = "Room name cannot exceed 100 characters")
        String roomName,

        @NotBlank(message = "Room code is required")
        @Size(max = 64, message = "Room code cannot exceed 64 characters")
        String roomCode,

        @NotNull(message = "Room type is required")
        RoomType roomType,

        String description,

        @Min(value = 1, message = "Maximum capacity must be at least 1")
        int maximumCapacity,

        @Min(value = 0, message = "Current occupancy cannot be negative")
        Integer currentOccupancy,

        SafetyStatus safetyStatus,

        Double posX,
        Double posY,
        Double posZ,

        @DecimalMin(value = "0.5", message = "Room width must be at least 0.5m")
        Double width,

        @DecimalMin(value = "0.5", message = "Room depth must be at least 0.5m")
        Double depth,

        @DecimalMin(value = "0.5", message = "Room height must be at least 0.5m")
        Double height,

        Boolean active
) {}

