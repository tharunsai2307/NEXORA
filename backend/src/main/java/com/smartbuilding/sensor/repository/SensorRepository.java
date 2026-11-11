package com.smartbuilding.sensor.repository;

import com.smartbuilding.sensor.model.Sensor;
import com.smartbuilding.sensor.model.SensorStatus;
import com.smartbuilding.sensor.model.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, String> {

    List<Sensor> findByRoomId(String roomId);

    List<Sensor> findByRoomIdIn(Collection<String> roomIds);

    Optional<Sensor> findBySensorCode(String sensorCode);

    boolean existsByRoomIdAndSensorType(String roomId, SensorType sensorType);

    Optional<Sensor> findByRoomIdAndSensorType(String roomId, SensorType sensorType);

    List<Sensor> findByStatus(SensorStatus status);

    long countByStatus(SensorStatus status);

    long countBySensorType(SensorType sensorType);

    @Query("SELECT s FROM Sensor s JOIN s.room r JOIN r.floor f JOIN f.building b WHERE b.id = :buildingId")
    List<Sensor> findByBuildingId(@Param("buildingId") String buildingId);

    @Query("SELECT s FROM Sensor s JOIN s.room r JOIN r.floor f JOIN f.building b WHERE b.id = :buildingId AND s.status = :status")
    List<Sensor> findByBuildingIdAndStatus(@Param("buildingId") String buildingId, @Param("status") SensorStatus status);

    @Query("SELECT s FROM Sensor s JOIN s.room r JOIN r.floor f WHERE f.id = :floorId")
    List<Sensor> findByFloorId(@Param("floorId") String floorId);
}

