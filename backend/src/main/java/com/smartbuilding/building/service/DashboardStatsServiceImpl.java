package com.smartbuilding.building.service;

import com.smartbuilding.building.dto.DashboardStatsResponse;
import com.smartbuilding.building.model.Room;
import com.smartbuilding.building.model.SafetyStatus;
import com.smartbuilding.building.repository.BuildingRepository;
import com.smartbuilding.building.repository.FloorRepository;
import com.smartbuilding.building.repository.RoomRepository;
import com.smartbuilding.user.model.Role;
import com.smartbuilding.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardStatsServiceImpl implements DashboardStatsService {

    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public DashboardStatsServiceImpl(
            BuildingRepository buildingRepository,
            FloorRepository floorRepository,
            RoomRepository roomRepository,
            UserRepository userRepository) {
        this.buildingRepository = buildingRepository;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalBuildings = buildingRepository.count();
        long totalFloors = floorRepository.count();
        long totalRooms = roomRepository.count();
        int totalCapacity = roomRepository.sumTotalCapacity();
        int currentOccupancy = roomRepository.sumTotalOccupancy();

        double occupancyPercentage = totalCapacity > 0
                ? Math.round(((double) currentOccupancy / totalCapacity * 100.0) * 10.0) / 10.0
                : 0.0;

        long activeUsers = userRepository.countByRoleAndActive(Role.SYSTEM_ADMIN, true)
                + userRepository.countByRoleAndActive(Role.BUILDING_MANAGER, true)
                + userRepository.countByRoleAndActive(Role.SAFETY_OFFICER, true);

        List<Room> allRooms = roomRepository.findAll();
        Map<String, Long> safetySummary = new HashMap<>();
        safetySummary.put("NORMAL", allRooms.stream().filter(r -> r.getSafetyStatus() == SafetyStatus.NORMAL).count());
        safetySummary.put("WARNING", allRooms.stream().filter(r -> r.getSafetyStatus() == SafetyStatus.WARNING).count());
        safetySummary.put("CRITICAL", allRooms.stream().filter(r -> r.getSafetyStatus() == SafetyStatus.CRITICAL).count());

        return new DashboardStatsResponse(
                totalBuildings,
                totalFloors,
                totalRooms,
                totalCapacity,
                currentOccupancy,
                occupancyPercentage,
                activeUsers,
                safetySummary
        );
    }
}

