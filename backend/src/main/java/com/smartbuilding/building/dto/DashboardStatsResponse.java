package com.smartbuilding.building.dto;

import java.util.Map;

public record DashboardStatsResponse(
        long totalBuildings,
        long totalFloors,
        long totalRooms,
        int totalCapacity,
        int currentOccupancy,
        double occupancyPercentage,
        long activeUsers,
        Map<String, Long> safetySummary
) {}

