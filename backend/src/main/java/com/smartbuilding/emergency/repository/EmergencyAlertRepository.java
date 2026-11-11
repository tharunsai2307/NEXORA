package com.smartbuilding.emergency.repository;

import com.smartbuilding.emergency.model.EmergencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, String> {
    List<EmergencyAlert> findByBuildingIdAndActiveTrueOrderByBroadcastAtDesc(String buildingId);
    List<EmergencyAlert> findByIncidentIdOrderByBroadcastAtDesc(String incidentId);
}
