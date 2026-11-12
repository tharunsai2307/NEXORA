package com.smartbuilding.analytics.repository;

import com.smartbuilding.analytics.model.GeneratedReport;
import com.smartbuilding.analytics.model.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, String> {

    List<GeneratedReport> findByBuildingIdOrderByCreatedAtDesc(String buildingId);

    List<GeneratedReport> findAllByOrderByCreatedAtDesc();

    List<GeneratedReport> findByReportTypeOrderByCreatedAtDesc(ReportType reportType);
}

