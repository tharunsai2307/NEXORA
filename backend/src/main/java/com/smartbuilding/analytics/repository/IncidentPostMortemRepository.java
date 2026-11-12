package com.smartbuilding.analytics.repository;

import com.smartbuilding.analytics.model.IncidentPostMortem;
import com.smartbuilding.analytics.model.PostMortemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentPostMortemRepository extends JpaRepository<IncidentPostMortem, String> {

    Optional<IncidentPostMortem> findByIncidentId(String incidentId);

    List<IncidentPostMortem> findByReportStatusOrderByCreatedAtDesc(PostMortemStatus reportStatus);

    List<IncidentPostMortem> findAllByOrderByCreatedAtDesc();

    boolean existsByIncidentId(String incidentId);
}

