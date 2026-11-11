package com.smartbuilding.emergency.repository;

import com.smartbuilding.emergency.model.IncidentActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentActivityRepository extends JpaRepository<IncidentActivity, String> {
    List<IncidentActivity> findByIncidentIdOrderByCreatedAtDesc(String incidentId);
}
