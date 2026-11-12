package com.smartbuilding.analytics.model;

import com.smartbuilding.building.model.Building;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "generated_reports")
public class GeneratedReport {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", length = 64, nullable = false)
    private ReportType reportType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private Building building;

    @Column(name = "report_name", length = 255, nullable = false)
    private String reportName;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_format", length = 16, nullable = false)
    private ReportFormat reportFormat = ReportFormat.PDF;

    @Column(name = "date_range_start")
    private Instant dateRangeStart;

    @Column(name = "date_range_end")
    private Instant dateRangeEnd;

    @Column(name = "generated_by", length = 128, nullable = false)
    private String generatedBy = "SYSTEM";

    @Column(name = "summary_metadata", columnDefinition = "TEXT")
    private String summaryMetadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public GeneratedReport() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

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

