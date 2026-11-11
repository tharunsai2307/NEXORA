package com.smartbuilding.sensor.service;

import com.smartbuilding.common.ServiceException;
import com.smartbuilding.sensor.dto.SensorReadingResponse;
import com.smartbuilding.sensor.model.Sensor;
import com.smartbuilding.sensor.model.SensorReading;
import com.smartbuilding.sensor.repository.SensorReadingRepository;
import com.smartbuilding.sensor.repository.SensorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SensorReadingServiceImpl implements SensorReadingService {

    private final SensorReadingRepository readingRepository;
    private final SensorRepository sensorRepository;

    public SensorReadingServiceImpl(SensorReadingRepository readingRepository, SensorRepository sensorRepository) {
        this.readingRepository = readingRepository;
        this.sensorRepository = sensorRepository;
    }

    @Override
    public Page<SensorReadingResponse> getSensorReadings(String sensorId, int page, int size) {
        if (!sensorRepository.existsById(sensorId)) {
            throw ServiceException.notFound("Sensor not found with ID: " + sensorId);
        }
        int clampedPage = Math.max(0, page);
        int clampedSize = Math.min(100, Math.max(1, size));
        PageRequest pageRequest = PageRequest.of(clampedPage, clampedSize, Sort.by("recordedAt").descending());

        return readingRepository.findBySensorIdOrderByRecordedAtDesc(sensorId, pageRequest)
                .map(SensorReadingResponse::fromEntity);
    }

    @Override
    public List<SensorReadingResponse> getSensorHistory(String sensorId, Instant start, Instant end) {
        if (!sensorRepository.existsById(sensorId)) {
            throw ServiceException.notFound("Sensor not found with ID: " + sensorId);
        }
        if (start != null && end != null) {
            return readingRepository.findBySensorIdAndRecordedAtBetweenOrderByRecordedAtAsc(sensorId, start, end).stream()
                    .map(SensorReadingResponse::fromEntity)
                    .collect(Collectors.toList());
        }
        // Return latest 50 readings if no window specified
        return readingRepository.findTop50BySensorIdOrderByRecordedAtDesc(sensorId).stream()
                .map(SensorReadingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SensorReadingResponse> getLatestRoomReadings(String roomId) {
        List<Sensor> sensors = sensorRepository.findByRoomId(roomId);
        List<SensorReadingResponse> result = new ArrayList<>();
        for (Sensor s : sensors) {
            if (s.getLastReadingValue() != null) {
                SensorReading dummy = new SensorReading(s, s.getLastReadingValue(), null, null);
                dummy.setRecordedAt(s.getLastReadingAt() != null ? s.getLastReadingAt() : Instant.now());
                result.add(SensorReadingResponse.fromEntity(dummy));
            }
        }
        return result;
    }

    @Override
    public List<SensorReadingResponse> getLatestBuildingReadings(String buildingId) {
        List<Sensor> sensors = sensorRepository.findByBuildingId(buildingId);
        List<SensorReadingResponse> result = new ArrayList<>();
        for (Sensor s : sensors) {
            if (s.getLastReadingValue() != null) {
                SensorReading dummy = new SensorReading(s, s.getLastReadingValue(), null, null);
                dummy.setRecordedAt(s.getLastReadingAt() != null ? s.getLastReadingAt() : Instant.now());
                result.add(SensorReadingResponse.fromEntity(dummy));
            }
        }
        return result;
    }
}
