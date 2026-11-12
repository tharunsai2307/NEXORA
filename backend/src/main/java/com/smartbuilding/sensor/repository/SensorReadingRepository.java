package com.smartbuilding.sensor.repository;

import com.smartbuilding.sensor.model.SensorReading;
import com.smartbuilding.sensor.model.SensorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, String> {

    Page<SensorReading> findBySensorIdOrderByRecordedAtDesc(String sensorId, Pageable pageable);

    List<SensorReading> findTop50BySensorIdOrderByRecordedAtDesc(String sensorId);

    List<SensorReading> findBySensorIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            String sensorId, Instant start, Instant end);

    @Query("SELECT sr FROM SensorReading sr JOIN sr.sensor s WHERE s.room.id = :roomId ORDER BY sr.recordedAt DESC")
    List<SensorReading> findRecentByRoomId(@Param("roomId") String roomId, Pageable pageable);

    @Query("SELECT sr FROM SensorReading sr JOIN sr.sensor s JOIN s.room r JOIN r.floor f JOIN f.building b " +
           "WHERE b.id = :buildingId ORDER BY sr.recordedAt DESC")
    List<SensorReading> findRecentByBuildingId(@Param("buildingId") String buildingId, Pageable pageable);

    @Query("SELECT sr FROM SensorReading sr JOIN sr.sensor s WHERE s.sensorType = :sensorType AND sr.recordedAt BETWEEN :start AND :end ORDER BY sr.recordedAt ASC")
    List<SensorReading> findByTypeAndRecordedAtBetween(@Param("sensorType") SensorType sensorType, @Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT sr FROM SensorReading sr JOIN sr.sensor s JOIN s.room r JOIN r.floor f JOIN f.building b " +
           "WHERE b.id = :buildingId AND s.sensorType = :sensorType AND sr.recordedAt BETWEEN :start AND :end ORDER BY sr.recordedAt ASC")
    List<SensorReading> findByBuildingAndTypeAndRecordedAtBetween(
            @Param("buildingId") String buildingId,
            @Param("sensorType") SensorType sensorType,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("SELECT sr FROM SensorReading sr JOIN sr.sensor s JOIN s.room r " +
           "WHERE r.id = :roomId AND s.sensorType = :sensorType AND sr.recordedAt BETWEEN :start AND :end ORDER BY sr.recordedAt ASC")
    List<SensorReading> findByRoomAndTypeAndRecordedAtBetween(
            @Param("roomId") String roomId,
            @Param("sensorType") SensorType sensorType,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("SELECT sr FROM SensorReading sr WHERE sr.recordedAt BETWEEN :start AND :end ORDER BY sr.recordedAt DESC")
    Page<SensorReading> findByRecordedAtBetween(@Param("start") Instant start, @Param("end") Instant end, Pageable pageable);
}

