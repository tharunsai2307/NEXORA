package com.smartbuilding.analytics.dto;

import com.smartbuilding.analytics.model.ReportFormat;
import com.smartbuilding.analytics.model.ReportType;

import java.time.Instant;

public class GeneratedReportResponse {

    private String id;
    private ReportType reportType;
    private String buildingId;
    private String buildingName;
    private String reportName;
    private ReportFormat reportFormat;
    private Instant dateRangeStart;
    private Instant dateRangeEnd;
    private String generatedBy;
    private String summaryMetadata;
    private Instant createdAt;

    public GeneratedReportResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public ReportFormat getReportFormat() { return reportFormat; }
    public void setReportFormat(ReportFormat reportFormat) { this.reportFormat = reportFormat; }

    public Instant getDateRangeStart() { return dateRangeStart; }
    public void setDateRangeStart(Instant dateRangeStart) { this.dateRangeStart = dateRangeStart; }

    public Instant getDateRangeEnd() { return dateRangeEnd; }
    public void setDateRangeEnd(Instant dateRangeEnd) { this.dateRangeEnd = dateRangeEnd; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public String getSummaryMetadata() { return summaryMetadata; }
    public void setSummaryMetadata(String summaryMetadata) { this.summaryMetadata = summaryMetadata; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

