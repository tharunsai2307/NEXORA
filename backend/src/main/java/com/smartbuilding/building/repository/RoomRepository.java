package com.smartbuilding.building.repository;

import com.smartbuilding.building.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByFloorIdOrderByRoomCodeAsc(String floorId);
    Optional<Room> findByFloorIdAndRoomCode(String floorId, String roomCode);
    boolean existsByFloorIdAndRoomCode(String floorId, String roomCode);
    long countByFloorId(String floorId);
    long countByFloorBuildingId(String buildingId);
    List<Room> findByFloorBuildingId(String buildingId);

    @Query("SELECT COALESCE(SUM(r.maximumCapacity), 0) FROM Room r WHERE r.floor.building.id = :buildingId")
    int sumCapacityByBuildingId(@Param("buildingId") String buildingId);

    @Query("SELECT COALESCE(SUM(r.currentOccupancy), 0) FROM Room r WHERE r.floor.building.id = :buildingId")
    int sumOccupancyByBuildingId(@Param("buildingId") String buildingId);

    @Query("SELECT COALESCE(SUM(r.maximumCapacity), 0) FROM Room r WHERE r.floor.id = :floorId")
    int sumCapacityByFloorId(@Param("floorId") String floorId);

    @Query("SELECT COALESCE(SUM(r.currentOccupancy), 0) FROM Room r WHERE r.floor.id = :floorId")
    int sumOccupancyByFloorId(@Param("floorId") String floorId);

    @Query("SELECT COALESCE(SUM(r.maximumCapacity), 0) FROM Room r")
    int sumTotalCapacity();

    @Query("SELECT COALESCE(SUM(r.currentOccupancy), 0) FROM Room r")
    int sumTotalOccupancy();
}

