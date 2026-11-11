package com.smartbuilding.digitaltwin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record RoomLayoutUpdateRequest(
        @NotNull Double posX,
        @NotNull Double posY,
        @NotNull Double posZ,
        @NotNull @DecimalMin(value = "0.5") Double width,
        @NotNull @DecimalMin(value = "0.5") Double depth,
        @NotNull @DecimalMin(value = "0.5") Double height
) {}
