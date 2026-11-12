package com.smartbuilding.analytics.service;

import com.smartbuilding.analytics.dto.*;
import com.smartbuilding.analytics.model.GeneratedReport;
import com.smartbuilding.analytics.model.IncidentPostMortem;
import com.smartbuilding.analytics.model.ReportFormat;
import com.smartbuilding.analytics.model.ReportType;
import com.smartbuilding.analytics.repository.GeneratedReportRepository;
import com.smartbuilding.analytics.repository.IncidentPostMortemRepository;
import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.repository.BuildingRepository;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.emergency.model.Incident;
import com.smartbuilding.emergency.repository.IncidentRepository;
import com.smartbuilding.sensor.model.SensorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportExportServiceImpl implements ReportExportService {

    private final AnalyticsAggregationService analyticsService;
    private final IncidentPostMortemRepository postMortemRepository;
    private final GeneratedReportRepository reportRepository;
    private final BuildingRepository buildingRepository;
    private final IncidentRepository incidentRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    public ReportExportServiceImpl(
            AnalyticsAggregationService analyticsService,
            IncidentPostMortemRepository postMortemRepository,
            GeneratedReportRepository reportRepository,
            BuildingRepository buildingRepository,
            IncidentRepository incidentRepository) {
        this.analyticsService = analyticsService;
        this.postMortemRepository = postMortemRepository;
        this.reportRepository = reportRepository;
        this.buildingRepository = buildingRepository;
        this.incidentRepository = incidentRepository;
    }

    @Override
    @Transactional
    public byte[] exportReport(String reportTypeStr, String buildingId, String formatStr, Instant start, Instant end, String generatedBy) {
        ReportType type;
        try {
            type = ReportType.valueOf(reportTypeStr.toUpperCase());
        } catch (Exception e) {
            type = ReportType.EXECUTIVE_SUMMARY;
        }

        ReportFormat format;
        try {
            format = ReportFormat.valueOf(formatStr.toUpperCase());
        } catch (Exception e) {
            format = ReportFormat.PDF;
        }

        Building building = null;
        if (buildingId != null && !buildingId.isBlank()) {
            building = buildingRepository.findById(buildingId).orElse(null);
        }

        byte[] content = format == ReportFormat.PDF
                ? generatePdfReport(type, building, start, end)
                : generateCsvReport(type, building, start, end);

        // Record in catalog
        GeneratedReport rec = new GeneratedReport();
        rec.setReportType(type);
        rec.setBuilding(building);
        rec.setReportName(type.name() + " - " + (building != null ? building.getName() : "Global Portfolio"));
        rec.setReportFormat(format);
        rec.setDateRangeStart(start != null ? start : Instant.now().minusSeconds(86400 * 7));
        rec.setDateRangeEnd(end != null ? end : Instant.now());
        rec.setGeneratedBy(generatedBy != null ? generatedBy : "SYSTEM");
        rec.setSummaryMetadata("Generated " + format.name() + " (" + content.length + " bytes)");
        reportRepository.save(rec);

        return content;
    }

    @Override
    @Transactional
    public byte[] exportPostMortem(String postMortemId, String formatStr) {
        IncidentPostMortem pm = postMortemRepository.findById(postMortemId)
                .orElseThrow(() -> new ServiceException("Post-mortem report not found: " + postMortemId));

        ReportFormat format;
        try {
            format = ReportFormat.valueOf(formatStr.toUpperCase());
        } catch (Exception e) {
            format = ReportFormat.PDF;
        }

        if (format == ReportFormat.PDF) {
            return generatePostMortemPdf(pm);
        } else {
            return generatePostMortemCsv(pm);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeneratedReportResponse> listReports(String buildingId) {
        List<GeneratedReport> list = (buildingId != null && !buildingId.isBlank())
                ? reportRepository.findByBuildingIdOrderByCreatedAtDesc(buildingId)
                : reportRepository.findAllByOrderByCreatedAtDesc();

        return list.stream().map(r -> {
            GeneratedReportResponse res = new GeneratedReportResponse();
            res.setId(r.getId());
            res.setReportType(r.getReportType());
            if (r.getBuilding() != null) {
                res.setBuildingId(r.getBuilding().getId());
                res.setBuildingName(r.getBuilding().getName());
            }
            res.setReportName(r.getReportName());
            res.setReportFormat(r.getReportFormat());
            res.setDateRangeStart(r.getDateRangeStart());
            res.setDateRangeEnd(r.getDateRangeEnd());
            res.setGeneratedBy(r.getGeneratedBy());
            res.setSummaryMetadata(r.getSummaryMetadata());
            res.setCreatedAt(r.getCreatedAt());
            return res;
        }).collect(Collectors.toList());
    }

    private byte[] generatePdfReport(ReportType type, Building building, Instant start, Instant end) {
        String buildingName = building != null ? building.getName() : "All Buildings Portfolio";
        String bId = building != null ? building.getId() : null;

        return switch (type) {
            case EXECUTIVE_SUMMARY -> {
                AnalyticsOverviewResponse overview = analyticsService.getOverview(bId, start, end);
                PdfReportGenerator pdf = new PdfReportGenerator("Executive Safety & Operations Summary", buildingName);

                pdf.addSection("Key Safety & Operations Performance Indicators");
                List<PdfReportGenerator.KpiItem> row1 = new ArrayList<>();
                row1.add(new PdfReportGenerator.KpiItem("Safety Index", overview.getSystemSafetyScore() + "/100", "Composite Rating"));
                row1.add(new PdfReportGenerator.KpiItem("Total Incidents", String.valueOf(overview.getTotalIncidents()), "Logged in Period"));
                row1.add(new PdfReportGenerator.KpiItem("Critical Alerts", String.valueOf(overview.getCriticalIncidents()), "Immediate Hazard"));
                row1.add(new PdfReportGenerator.KpiItem("Avg Occupancy", String.valueOf(overview.getAverageOccupancy()), "Peak: " + overview.getPeakOccupancy()));
                pdf.addKpiRow(row1);

                List<PdfReportGenerator.KpiItem> row2 = new ArrayList<>();
                row2.add(new PdfReportGenerator.KpiItem("Avg Temp", overview.getAverageTemperature() + " °C", "Standard: 22.0 °C"));
                row2.add(new PdfReportGenerator.KpiItem("Air Quality", overview.getAverageAirQuality() + " AQI", "Good (< 50)"));
                row2.add(new PdfReportGenerator.KpiItem("Smoke Index", overview.getAverageSmokeLevel() + " ppm", "Threshold: 2.0"));
                row2.add(new PdfReportGenerator.KpiItem("Edge Anomalies", String.valueOf(overview.getTotalAnomaliesDetected()), "Flagged by AI"));
                pdf.addKpiRow(row2);

                pdf.addSection("Executive Operational Assessment");
                pdf.addParagraph(String.format("During the reporting window, NEXORA monitored %d telemetry data points across building facilities. " +
                        "Overall facility safety index registered at %.1f, with %d high-risk room zones under proactive Edge-AI surveillance. " +
                        "A total of %d emergency evacuation simulation drills were recorded, maintaining an average evacuation clearance time of %.1f seconds.",
                        overview.getTotalReadings(), overview.getSystemSafetyScore(), overview.getHighRiskRoomsCount(),
                        overview.getTotalEvacuationSessions(), overview.getAverageEvacuationDurationSeconds()));

                IncidentAnalyticsResponse incidents = analyticsService.getIncidentAnalytics(bId, start, end);
                pdf.addSection("Recent Incident Posture");
                String[] incHeaders = {"Metric", "Value", "Operational Impact"};
                List<String[]> incRows = new ArrayList<>();
                incRows.add(new String[]{"Total Incidents Recorded", String.valueOf(incidents.getTotalIncidents()), "All categories logged"});
                incRows.add(new String[]{"Active / In-Progress", String.valueOf(incidents.getActiveIncidents()), "Currently monitored"});
                incRows.add(new String[]{"Resolved & Closed", String.valueOf(incidents.getResolvedIncidents()), "Containment confirmed"});
                incRows.add(new String[]{"Mean Time to Acknowledge (MTTA)", incidents.getMeanTimeToAcknowledgeMinutes() + " mins", "Target SLA: < 5 mins"});
                incRows.add(new String[]{"Mean Time to Resolve (MTTR)", incidents.getMeanTimeToResolveMinutes() + " mins", "Target SLA: < 30 mins"});
                pdf.addTable(incHeaders, incRows, new float[]{2.5f, 1.5f, 3f});

                yield pdf.build();
            }

            case ENVIRONMENTAL_AUDIT -> {
                SensorAnalyticsResponse temp = analyticsService.getSensorAnalytics(bId, null, SensorType.TEMPERATURE, start, end);
                SensorAnalyticsResponse smoke = analyticsService.getSensorAnalytics(bId, null, SensorType.SMOKE, start, end);
                SensorAnalyticsResponse aqi = analyticsService.getSensorAnalytics(bId, null, SensorType.AIR_QUALITY, start, end);
                SensorAnalyticsResponse hum = analyticsService.getSensorAnalytics(bId, null, SensorType.HUMIDITY, start, end);

                PdfReportGenerator pdf = new PdfReportGenerator("IoT Environmental Sensor Audit Report", buildingName);

                pdf.addSection("Environmental Metrics Overview");
                List<PdfReportGenerator.KpiItem> kpi = new ArrayList<>();
                kpi.add(new PdfReportGenerator.KpiItem("Temperature", temp.getAvgValue() + " °C", "Violations: " + temp.getThresholdViolations()));
                kpi.add(new PdfReportGenerator.KpiItem("Humidity", hum.getAvgValue() + " %", "Violations: " + hum.getThresholdViolations()));
                kpi.add(new PdfReportGenerator.KpiItem("Smoke Level", smoke.getAvgValue() + " ppm", "Violations: " + smoke.getThresholdViolations()));
                kpi.add(new PdfReportGenerator.KpiItem("Air Quality", aqi.getAvgValue() + " AQI", "Violations: " + aqi.getThresholdViolations()));
                pdf.addKpiRow(kpi);

                pdf.addSection("Room Environmental Breakdown");
                String[] headers = {"Room", "Room Name", "Avg Reading", "Peak Reading", "Violations"};
                List<String[]> rows = new ArrayList<>();
                for (RoomMetricComparisonDto r : temp.getRoomComparisons()) {
                    rows.add(new String[]{r.getRoomNumber(), r.getRoomName(), r.getAvgValue() + " °C", r.getPeakValue() + " °C", String.valueOf(r.getViolationCount())});
                }
                pdf.addTable(headers, rows, new float[]{1.5f, 2.5f, 1.5f, 1.5f, 1.5f});

                yield pdf.build();
            }

            case OCCUPANCY_INTELLIGENCE -> {
                OccupancyAnalyticsResponse occ = analyticsService.getOccupancyAnalytics(bId, start, end);
                PdfReportGenerator pdf = new PdfReportGenerator("Building Occupancy Intelligence Report", buildingName);

                pdf.addSection("Occupancy & Utilization Overview");
                List<PdfReportGenerator.KpiItem> kpi = new ArrayList<>();
                kpi.add(new PdfReportGenerator.KpiItem("Current Occupancy", String.valueOf(occ.getCurrentTotalOccupancy()), "People in Building"));
                kpi.add(new PdfReportGenerator.KpiItem("Utilization", occ.getOverallUtilizationPercent() + " %", "Of Design Capacity"));
                kpi.add(new PdfReportGenerator.KpiItem("Peak Daily", String.valueOf(occ.getPeakOccupancy()), "Peak Hour: " + occ.getPeakHour() + ":00"));
                kpi.add(new PdfReportGenerator.KpiItem("Daily Avg", String.valueOf(occ.getAverageDailyOccupancy()), "Average Flow"));
                pdf.addKpiRow(kpi);

                pdf.addSection("Floor Utilization Distribution");
                String[] flHeaders = {"Floor Number", "Floor Name", "Occupancy", "Capacity", "Utilization %"};
                List<String[]> flRows = new ArrayList<>();
                for (FloorOccupancyDto f : occ.getFloorDistribution()) {
                    flRows.add(new String[]{"Floor " + f.getFloorNumber(), f.getFloorName(), String.valueOf(f.getOccupancy()), String.valueOf(f.getCapacity()), f.getUtilizationPercent() + "%"});
                }
                pdf.addTable(flHeaders, flRows, new float[]{1.5f, 2.5f, 1.5f, 1.5f, 1.5f});

                pdf.addSection("Room Occupancy Top Highlights");
                String[] rmHeaders = {"Room", "Room Name", "Occupants", "Capacity", "Utilization %"};
                List<String[]> rmRows = new ArrayList<>();
                int limit = 0;
                for (RoomOccupancyDto r : occ.getRoomOccupancy()) {
                    if (limit++ >= 12) break;
                    rmRows.add(new String[]{r.getRoomNumber(), r.getRoomName(), String.valueOf(r.getOccupancy()), String.valueOf(r.getCapacity()), r.getUtilizationPercent() + "%"});
                }
                pdf.addTable(rmHeaders, rmRows, new float[]{1.5f, 2.5f, 1.5f, 1.5f, 1.5f});

                yield pdf.build();
            }

            case AI_DIAGNOSTICS -> {
                AiAnalyticsResponse ai = analyticsService.getAiAnalytics(bId, start, end);
                PdfReportGenerator pdf = new PdfReportGenerator("Edge AI Diagnostics & Threat Report", buildingName);

                pdf.addSection("AI Telemetry & Anomaly Statistics");
                List<PdfReportGenerator.KpiItem> kpi = new ArrayList<>();
                kpi.add(new PdfReportGenerator.KpiItem("Total Anomalies", String.valueOf(ai.getTotalAnomalies()), "Detected by Edge AI"));
                kpi.add(new PdfReportGenerator.KpiItem("Avg Score", String.valueOf(ai.getAvgAnomalyScore()), "Isolation Forest"));
                kpi.add(new PdfReportGenerator.KpiItem("Critical Rooms", String.valueOf(ai.getCriticalRiskRooms()), "Immediate Hazard"));
                kpi.add(new PdfReportGenerator.KpiItem("High Risk Rooms", String.valueOf(ai.getHighRiskRooms()), "Elevated Vigilance"));
                pdf.addKpiRow(kpi);

                pdf.addSection("Top Anomaly Hotspot Rooms");
                String[] rmHeaders = {"Room", "Room Name", "Anomalies", "Severity", "Last Detected"};
                List<String[]> rmRows = new ArrayList<>();
                for (AnomalyRoomSummaryDto r : ai.getTopAnomalyRooms()) {
                    rmRows.add(new String[]{
                            r.getRoomNumber(),
                            r.getRoomName(),
                            String.valueOf(r.getAnomalyCount()),
                            r.getHighestSeverity(),
                            r.getLastAnomalyAt() != null ? TIME_FMT.format(r.getLastAnomalyAt()) : "N/A"
                    });
                }
                pdf.addTable(rmHeaders, rmRows, new float[]{1.5f, 2.5f, 1.5f, 1.5f, 2f});

                pdf.addSection("Edge AI Model Fleet Metadata");
                String[] mHeaders = {"Model Type", "Version", "Operational Status", "Validation Score"};
                List<String[]> mRows = new ArrayList<>();
                for (AiModelSummaryDto m : ai.getModelMetadata()) {
                    mRows.add(new String[]{m.getModelType(), m.getVersion(), m.getStatus(), String.valueOf(m.getAccuracy())});
                }
                pdf.addTable(mHeaders, mRows, new float[]{2.5f, 1.5f, 2f, 1.5f});

                yield pdf.build();
            }

            case INCIDENT_LOG -> {
                IncidentAnalyticsResponse inc = analyticsService.getIncidentAnalytics(bId, start, end);
                PdfReportGenerator pdf = new PdfReportGenerator("Incident History & Response Audit Log", buildingName);

                pdf.addSection("Incident Overview");
                List<PdfReportGenerator.KpiItem> kpi = new ArrayList<>();
                kpi.add(new PdfReportGenerator.KpiItem("Total Incidents", String.valueOf(inc.getTotalIncidents()), "Logged in Period"));
                kpi.add(new PdfReportGenerator.KpiItem("Active Incidents", String.valueOf(inc.getActiveIncidents()), "Pending Resolution"));
                kpi.add(new PdfReportGenerator.KpiItem("MTTA Response", inc.getMeanTimeToAcknowledgeMinutes() + " mins", "Target: < 5 min"));
                kpi.add(new PdfReportGenerator.KpiItem("MTTR Resolution", inc.getMeanTimeToResolveMinutes() + " mins", "Target: < 30 min"));
                pdf.addKpiRow(kpi);

                pdf.addSection("Detailed Incident Log");
                List<Incident> list = building != null
                        ? incidentRepository.findByBuildingIdOrderByDetectedAtDesc(building.getId())
                        : incidentRepository.findAll();

                String[] headers = {"Ref Number", "Title", "Category", "Severity", "Status", "Detected At"};
                List<String[]> rows = new ArrayList<>();
                int limit = 0;
                for (Incident i : list) {
                    if (limit++ >= 20) break;
                    rows.add(new String[]{
                            i.getReferenceNumber(),
                            i.getTitle(),
                            i.getCategory().name(),
                            i.getSeverity().name(),
                            i.getStatus().name(),
                            i.getDetectedAt() != null ? TIME_FMT.format(i.getDetectedAt()) : "N/A"
                    });
                }
                pdf.addTable(headers, rows, new float[]{1.8f, 2.5f, 1.5f, 1.2f, 1.2f, 1.8f});

                yield pdf.build();
            }

            case EVACUATION_REVIEW -> {
                EvacuationAnalyticsResponse evac = analyticsService.getEvacuationAnalytics(bId, start, end);
                PdfReportGenerator pdf = new PdfReportGenerator("Emergency Evacuation Performance Review", buildingName);

                pdf.addSection("Evacuation Protocol Metrics");
                List<PdfReportGenerator.KpiItem> kpi = new ArrayList<>();
                kpi.add(new PdfReportGenerator.KpiItem("Total Drills", String.valueOf(evac.getTotalSessions()), "Drills Executed"));
                kpi.add(new PdfReportGenerator.KpiItem("Completed", String.valueOf(evac.getCompletedSessions()), "100% Cleared"));
                kpi.add(new PdfReportGenerator.KpiItem("Avg Clearance", evac.getAverageDurationSeconds() + " s", "Target: < 180s"));
                kpi.add(new PdfReportGenerator.KpiItem("Evacuees", String.valueOf(evac.getTotalPersonsEvacuated()), "Safely Routed"));
                pdf.addKpiRow(kpi);

                pdf.addSection("Clearance Progression Timeline");
                String[] headers = {"Elapsed (min)", "Cleared Rooms", "Remaining Rooms", "Evacuees Cleared"};
                List<String[]> rows = new ArrayList<>();
                for (EvacuationClearancePointDto p : evac.getClearanceTimeline()) {
                    rows.add(new String[]{p.getElapsedMinutes() + " min", String.valueOf(p.getClearedRooms()), String.valueOf(p.getRemainingRooms()), String.valueOf(p.getPersonsEvacuated())});
                }
                pdf.addTable(headers, rows, new float[]{1.5f, 1.5f, 1.5f, 1.5f});

                yield pdf.build();
            }

            case POST_MORTEM -> throw new ServiceException("Use exportPostMortem endpoint for incident post-mortem exports.");
        };
    }

    private byte[] generateCsvReport(ReportType type, Building building, Instant start, Instant end) {
        StringBuilder csv = new StringBuilder();
        String bId = building != null ? building.getId() : null;

        switch (type) {
            case EXECUTIVE_SUMMARY -> {
                AnalyticsOverviewResponse o = analyticsService.getOverview(bId, start, end);
                csv.append("Metric,Value,Unit\n");
                csv.append("Total Readings,").append(o.getTotalReadings()).append(",count\n");
                csv.append("Total Incidents,").append(o.getTotalIncidents()).append(",count\n");
                csv.append("Critical Incidents,").append(o.getCriticalIncidents()).append(",count\n");
                csv.append("System Safety Score,").append(o.getSystemSafetyScore()).append(",index(0-100)\n");
                csv.append("Average Occupancy,").append(o.getAverageOccupancy()).append(",persons\n");
                csv.append("Peak Occupancy,").append(o.getPeakOccupancy()).append(",persons\n");
                csv.append("Average Temperature,").append(o.getAverageTemperature()).append(",celsius\n");
                csv.append("Average Smoke,").append(o.getAverageSmokeLevel()).append(",ppm\n");
                csv.append("Average Air Quality,").append(o.getAverageAirQuality()).append(",aqi\n");
                csv.append("Average Humidity,").append(o.getAverageHumidity()).append(",percent\n");
                csv.append("Total Anomalies Detected,").append(o.getTotalAnomaliesDetected()).append(",count\n");
                csv.append("High Risk Rooms Count,").append(o.getHighRiskRoomsCount()).append(",count\n");
                csv.append("Average Evacuation Duration,").append(o.getAverageEvacuationDurationSeconds()).append(",seconds\n");
            }
            case ENVIRONMENTAL_AUDIT -> {
                SensorAnalyticsResponse temp = analyticsService.getSensorAnalytics(bId, null, SensorType.TEMPERATURE, start, end);
                csv.append("Room ID,Room Number,Room Name,Average Value,Peak Value,Threshold Violations\n");
                for (RoomMetricComparisonDto r : temp.getRoomComparisons()) {
                    csv.append(escapeCsv(r.getRoomId())).append(",")
                            .append(escapeCsv(r.getRoomNumber())).append(",")
                            .append(escapeCsv(r.getRoomName())).append(",")
                            .append(r.getAvgValue()).append(",")
                            .append(r.getPeakValue()).append(",")
                            .append(r.getViolationCount()).append("\n");
                }
            }
            case OCCUPANCY_INTELLIGENCE -> {
                OccupancyAnalyticsResponse occ = analyticsService.getOccupancyAnalytics(bId, start, end);
                csv.append("Room ID,Room Number,Room Name,Current Occupancy,Capacity,Utilization Percent\n");
                for (RoomOccupancyDto r : occ.getRoomOccupancy()) {
                    csv.append(escapeCsv(r.getRoomId())).append(",")
                            .append(escapeCsv(r.getRoomNumber())).append(",")
                            .append(escapeCsv(r.getRoomName())).append(",")
                            .append(r.getOccupancy()).append(",")
                            .append(r.getCapacity()).append(",")
                            .append(r.getUtilizationPercent()).append("\n");
                }
            }
            case AI_DIAGNOSTICS -> {
                AiAnalyticsResponse ai = analyticsService.getAiAnalytics(bId, start, end);
                csv.append("Room ID,Room Number,Room Name,Anomaly Count,Highest Severity,Last Detected\n");
                for (AnomalyRoomSummaryDto r : ai.getTopAnomalyRooms()) {
                    csv.append(escapeCsv(r.getRoomId())).append(",")
                            .append(escapeCsv(r.getRoomNumber())).append(",")
                            .append(escapeCsv(r.getRoomName())).append(",")
                            .append(r.getAnomalyCount()).append(",")
                            .append(escapeCsv(r.getHighestSeverity())).append(",")
                            .append(r.getLastAnomalyAt() != null ? r.getLastAnomalyAt().toString() : "").append("\n");
                }
            }
            case INCIDENT_LOG -> {
                List<Incident> list = building != null
                        ? incidentRepository.findByBuildingIdOrderByDetectedAtDesc(building.getId())
                        : incidentRepository.findAll();
                csv.append("Reference Number,Title,Category,Severity,Status,Building,Floor,Room,Detected At,Resolved At\n");
                for (Incident i : list) {
                    csv.append(escapeCsv(i.getReferenceNumber())).append(",")
                            .append(escapeCsv(i.getTitle())).append(",")
                            .append(i.getCategory().name()).append(",")
                            .append(i.getSeverity().name()).append(",")
                            .append(i.getStatus().name()).append(",")
                            .append(escapeCsv(i.getBuilding() != null ? i.getBuilding().getName() : "")).append(",")
                            .append(escapeCsv(i.getFloor() != null ? i.getFloor().getFloorName() : "")).append(",")
                            .append(escapeCsv(i.getRoom() != null ? i.getRoom().getRoomCode() : "")).append(",")
                            .append(i.getDetectedAt() != null ? i.getDetectedAt().toString() : "").append(",")
                            .append(i.getResolvedAt() != null ? i.getResolvedAt().toString() : "").append("\n");
                }
            }
            case EVACUATION_REVIEW -> {
                EvacuationAnalyticsResponse evac = analyticsService.getEvacuationAnalytics(bId, start, end);
                csv.append("Elapsed Minutes,Cleared Rooms,Remaining Rooms,Persons Evacuated\n");
                for (EvacuationClearancePointDto p : evac.getClearanceTimeline()) {
                    csv.append(p.getElapsedMinutes()).append(",")
                            .append(p.getClearedRooms()).append(",")
                            .append(p.getRemainingRooms()).append(",")
                            .append(p.getPersonsEvacuated()).append("\n");
                }
            }
            case POST_MORTEM -> throw new ServiceException("Use exportPostMortem endpoint for incident post-mortem exports.");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] generatePostMortemPdf(IncidentPostMortem pm) {
        Incident inc = pm.getIncident();
        String ref = inc != null ? inc.getReferenceNumber() : "INC-000";
        String bldg = inc != null && inc.getBuilding() != null ? inc.getBuilding().getName() : "Central Campus";

        PdfReportGenerator pdf = new PdfReportGenerator(pm.getReportTitle(), "Incident Post-Mortem | " + bldg);

        pdf.addSection("Report Metadata & Incident Classification");
        List<PdfReportGenerator.KpiItem> kpi = new ArrayList<>();
        kpi.add(new PdfReportGenerator.KpiItem("Incident Ref", ref, "Unique ID"));
        kpi.add(new PdfReportGenerator.KpiItem("Severity", inc != null ? inc.getSeverity().name() : "N/A", "Hazard Level"));
        kpi.add(new PdfReportGenerator.KpiItem("Report Status", pm.getReportStatus().name(), "Lifecycle State"));
        kpi.add(new PdfReportGenerator.KpiItem("Author", pm.getAuthorName() != null ? pm.getAuthorName() : "Safety Lead", "Signoff"));
        pdf.addKpiRow(kpi);

        pdf.addKeyValue("Incident Category", inc != null ? inc.getCategory().name() : "-");
        pdf.addKeyValue("Incident Title", inc != null ? inc.getTitle() : "-");
        pdf.addKeyValue("Location Zone", (inc != null && inc.getRoom() != null ? inc.getRoom().getRoomName() + " (" + inc.getRoom().getRoomCode() + ")" : "General Area"));
        pdf.addKeyValue("Detection Timestamp", inc != null && inc.getDetectedAt() != null ? TIME_FMT.format(inc.getDetectedAt()) : "-");
        pdf.addKeyValue("Resolution Timestamp", inc != null && inc.getResolvedAt() != null ? TIME_FMT.format(inc.getResolvedAt()) : "Pending");
        pdf.addKeyValue("Reviewed By", pm.getReviewedBy() != null ? pm.getReviewedBy() : "Pending Formal Review");

        pdf.addSection("1. Executive Summary");
        pdf.addParagraph(pm.getExecutiveSummary());

        pdf.addSection("2. Incident Overview & Sequence of Events");
        pdf.addParagraph(pm.getIncidentOverview());

        pdf.addSection("3. Chronological Response & Timeline");
        pdf.addParagraph(pm.getTimelineNarrative());

        pdf.addSection("4. IoT Sensor Telemetry & Environmental Impact");
        pdf.addParagraph(pm.getSensorAnalysis());

        pdf.addSection("5. Edge-AI Detection & Predictive Risk Evaluation");
        pdf.addParagraph(pm.getAiAssessmentSummary());

        pdf.addSection("6. Emergency Response & Containment Evaluation");
        pdf.addParagraph(pm.getResponseEvaluation());

        pdf.addSection("7. Dynamic Evacuation & Occupant Safety");
        pdf.addParagraph(pm.getEvacuationSummary());

        pdf.addSection("8. Root Cause Assessment");
        pdf.addParagraph(pm.getRootCauseAssessment());

        pdf.addSection("9. Corrective Actions Required");
        pdf.addParagraph(pm.getCorrectiveActions());

        pdf.addSection("10. Preventive Recommendations & SLA Upgrades");
        pdf.addParagraph(pm.getPreventiveRecommendations());

        return pdf.build();
    }

    private byte[] generatePostMortemCsv(IncidentPostMortem pm) {
        StringBuilder csv = new StringBuilder();
        csv.append("Field,Value\n");
        csv.append("Report Title,").append(escapeCsv(pm.getReportTitle())).append("\n");
        csv.append("Report Status,").append(pm.getReportStatus().name()).append("\n");
        csv.append("Author,").append(escapeCsv(pm.getAuthorName())).append("\n");
        csv.append("Incident Reference,").append(pm.getIncident() != null ? pm.getIncident().getReferenceNumber() : "").append("\n");
        csv.append("Executive Summary,").append(escapeCsv(pm.getExecutiveSummary())).append("\n");
        csv.append("Incident Overview,").append(escapeCsv(pm.getIncidentOverview())).append("\n");
        csv.append("Timeline Narrative,").append(escapeCsv(pm.getTimelineNarrative())).append("\n");
        csv.append("Sensor Analysis,").append(escapeCsv(pm.getSensorAnalysis())).append("\n");
        csv.append("AI Assessment Summary,").append(escapeCsv(pm.getAiAssessmentSummary())).append("\n");
        csv.append("Response Evaluation,").append(escapeCsv(pm.getResponseEvaluation())).append("\n");
        csv.append("Evacuation Summary,").append(escapeCsv(pm.getEvacuationSummary())).append("\n");
        csv.append("Root Cause Assessment,").append(escapeCsv(pm.getRootCauseAssessment())).append("\n");
        csv.append("Corrective Actions,").append(escapeCsv(pm.getCorrectiveActions())).append("\n");
        csv.append("Preventive Recommendations,").append(escapeCsv(pm.getPreventiveRecommendations())).append("\n");
        csv.append("Reviewed By,").append(escapeCsv(pm.getReviewedBy())).append("\n");
        csv.append("Finalized At,").append(pm.getFinalizedAt() != null ? pm.getFinalizedAt().toString() : "").append("\n");

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escapeCsv(String str) {
        if (str == null) return "\"\"";
        return "\"" + str.replace("\"", "\"\"") + "\"";
    }
}
