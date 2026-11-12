package com.smartbuilding.analytics.service;

import com.smartbuilding.analytics.dto.GeneratedReportResponse;

import java.time.Instant;
import java.util.List;

public interface ReportExportService {

    byte[] exportReport(String reportType, String buildingId, String format, Instant start, Instant end, String generatedBy);

    byte[] exportPostMortem(String postMortemId, String format);

    List<GeneratedReportResponse> listReports(String buildingId);
}

