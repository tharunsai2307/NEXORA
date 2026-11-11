package com.smartbuilding.sensor.service;

import com.smartbuilding.sensor.dto.*;
import com.smartbuilding.sensor.model.SensorStatus;
import com.smartbuilding.sensor.model.SensorType;

import java.util.List;

public interface SensorService {

    SensorResponse createSensor(String roomId, CreateSensorRequest request);

    List<SensorResponse> getSensorsByRoom(String roomId);

    List<SensorResponse> getSensors(String buildingId, String floorId, String roomId, SensorType type, SensorStatus status);

    SensorResponse getSensorById(String sensorId);

    SensorResponse updateSensor(String sensorId, UpdateSensorRequest request);

    SensorResponse updateSensorStatus(String sensorId, SensorStatus status);

    void deleteSensor(String sensorId);

    List<SensorResponse> provisionDefaultSensors(String roomId);

    List<SensorResponse> provisionBuildingSensors(String buildingId);

    SensorReadingResponse recordManualReading(String sensorId, double readingValue);

    SensorStatsResponse getSensorStats();
}

