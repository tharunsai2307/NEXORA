package com.smartbuilding.sensor.service;

import com.smartbuilding.sensor.dto.SensorReadingResponse;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

public interface SensorReadingService {

    Page<SensorReadingResponse> getSensorReadings(String sensorId, int page, int size);

    List<SensorReadingResponse> getSensorHistory(String sensorId, Instant start, Instant end);

    List<SensorReadingResponse> getLatestRoomReadings(String roomId);

    List<SensorReadingResponse> getLatestBuildingReadings(String buildingId);
}
