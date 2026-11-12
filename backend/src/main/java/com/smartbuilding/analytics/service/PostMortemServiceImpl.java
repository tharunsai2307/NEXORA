package com.smartbuilding.analytics.service;

import com.smartbuilding.analytics.dto.PostMortemRequest;
import com.smartbuilding.analytics.dto.PostMortemResponse;
import com.smartbuilding.analytics.model.IncidentPostMortem;
import com.smartbuilding.analytics.model.PostMortemStatus;
import com.smartbuilding.analytics.repository.IncidentPostMortemRepository;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.emergency.model.Incident;
import com.smartbuilding.emergency.model.IncidentActivity;
import com.smartbuilding.emergency.repository.IncidentActivityRepository;
import com.smartbuilding.emergency.repository.IncidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostMortemServiceImpl implements PostMortemService {

    private final IncidentPostMortemRepository postMortemRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentActivityRepository activityRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public PostMortemServiceImpl(IncidentPostMortemRepository postMortemRepository,
                                 IncidentRepository incidentRepository,
                                 IncidentActivityRepository activityRepository) {
        this.postMortemRepository = postMortemRepository;
        this.incidentRepository = incidentRepository;
        this.activityRepository = activityRepository;
    }

    @Override
    @Transactional
    public PostMortemResponse getPostMortemByIncident(String incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ServiceException("Incident not found: " + incidentId));

        Optional<IncidentPostMortem> existing = postMortemRepository.findByIncidentId(incidentId);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        IncidentPostMortem draft = createInitialDraft(incident);
        draft = postMortemRepository.save(draft);
        return toResponse(draft);
    }

    @Override
    @Transactional
    public PostMortemResponse createOrUpdatePostMortem(String incidentId, PostMortemRequest request, String userId, String userName) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ServiceException("Incident not found: " + incidentId));

        IncidentPostMortem postMortem = postMortemRepository.findByIncidentId(incidentId)
                .orElseGet(() -> {
                    IncidentPostMortem pm = new IncidentPostMortem();
                    pm.setIncident(incident);
                    pm.setAuthorId(userId);
                    pm.setAuthorName(userName != null ? userName : "Safety Officer");
                    return pm;
                });

        if (request.getReportTitle() != null && !request.getReportTitle().isBlank()) {
            postMortem.setReportTitle(request.getReportTitle());
        } else if (postMortem.getReportTitle() == null) {
            postMortem.setReportTitle("Post-Mortem: " + incident.getReferenceNumber() + " - " + incident.getTitle());
        }

        if (request.getExecutiveSummary() != null) postMortem.setExecutiveSummary(request.getExecutiveSummary());
        if (request.getIncidentOverview() != null) postMortem.setIncidentOverview(request.getIncidentOverview());
        if (request.getTimelineNarrative() != null) postMortem.setTimelineNarrative(request.getTimelineNarrative());
        if (request.getSensorAnalysis() != null) postMortem.setSensorAnalysis(request.getSensorAnalysis());
        if (request.getAiAssessmentSummary() != null) postMortem.setAiAssessmentSummary(request.getAiAssessmentSummary());
        if (request.getResponseEvaluation() != null) postMortem.setResponseEvaluation(request.getResponseEvaluation());
        if (request.getEvacuationSummary() != null) postMortem.setEvacuationSummary(request.getEvacuationSummary());
        if (request.getRootCauseAssessment() != null) postMortem.setRootCauseAssessment(request.getRootCauseAssessment());
        if (request.getCorrectiveActions() != null) postMortem.setCorrectiveActions(request.getCorrectiveActions());
        if (request.getPreventiveRecommendations() != null) postMortem.setPreventiveRecommendations(request.getPreventiveRecommendations());

        if (request.getReportStatus() != null) {
            postMortem.setReportStatus(request.getReportStatus());
            if (request.getReportStatus() == PostMortemStatus.REVIEWED) {
                postMortem.setReviewedBy(request.getReviewedBy() != null ? request.getReviewedBy() : userName);
                postMortem.setReviewedAt(Instant.now());
            } else if (request.getReportStatus() == PostMortemStatus.FINALIZED) {
                if (postMortem.getReviewedBy() == null) {
                    postMortem.setReviewedBy(request.getReviewedBy() != null ? request.getReviewedBy() : userName);
                    postMortem.setReviewedAt(Instant.now());
                }
                postMortem.setFinalizedAt(Instant.now());
            }
        }

        postMortem = postMortemRepository.save(postMortem);
        return toResponse(postMortem);
    }

    @Override
    @Transactional
    public PostMortemResponse finalizePostMortem(String incidentId, String reviewerName) {
        IncidentPostMortem postMortem = postMortemRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new ServiceException("Post-mortem report not found for incident: " + incidentId));

        postMortem.setReportStatus(PostMortemStatus.FINALIZED);
        postMortem.setReviewedBy(reviewerName != null ? reviewerName : "Senior Safety Director");
        postMortem.setReviewedAt(Instant.now());
        postMortem.setFinalizedAt(Instant.now());

        postMortem = postMortemRepository.save(postMortem);
        return toResponse(postMortem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostMortemResponse> listPostMortems(String status) {
        List<IncidentPostMortem> list;
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            try {
                PostMortemStatus st = PostMortemStatus.valueOf(status.toUpperCase());
                list = postMortemRepository.findByReportStatusOrderByCreatedAtDesc(st);
            } catch (IllegalArgumentException e) {
                list = postMortemRepository.findAllByOrderByCreatedAtDesc();
            }
        } else {
            list = postMortemRepository.findAllByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private IncidentPostMortem createInitialDraft(Incident incident) {
        IncidentPostMortem pm = new IncidentPostMortem();
        pm.setIncident(incident);
        pm.setReportTitle("Incident Post-Mortem: " + incident.getReferenceNumber() + " - " + incident.getTitle());
        pm.setReportStatus(PostMortemStatus.DRAFT);
        pm.setAuthorName("System AI Assistant");

        String detectedStr = incident.getDetectedAt() != null ? TIME_FMT.format(incident.getDetectedAt()) : "N/A";
        String resolvedStr = incident.getResolvedAt() != null ? TIME_FMT.format(incident.getResolvedAt()) : "Pending";
        String roomName = incident.getRoom() != null ? incident.getRoom().getRoomName() + " (" + incident.getRoom().getRoomCode() + ")" : "Building Zone";
        String floorName = incident.getFloor() != null ? incident.getFloor().getFloorName() : "General";

        pm.setExecutiveSummary(String.format("On %s, a %s severity %s incident was detected in %s on %s. " +
                "The incident reference %s was tracked by NEXORA and reached status %s at %s.",
                detectedStr, incident.getSeverity(), incident.getCategory(), roomName, floorName,
                incident.getReferenceNumber(), incident.getStatus(), resolvedStr));

        pm.setIncidentOverview(String.format("Title: %s\nCategory: %s\nSeverity: %s\nDetection Source: %s\nLocation: %s, %s\nDescription: %s",
                incident.getTitle(), incident.getCategory(), incident.getSeverity(), incident.getDetectionSource(),
                floorName, roomName, incident.getDescription() != null ? incident.getDescription() : "N/A"));

        List<IncidentActivity> activities = activityRepository.findByIncidentIdOrderByCreatedAtDesc(incident.getId());
        StringBuilder timeline = new StringBuilder();
        if (activities.isEmpty()) {
            timeline.append("Incident created at ").append(detectedStr).append("\n");
        } else {
            for (IncidentActivity act : activities) {
                timeline.append(TIME_FMT.format(act.getCreatedAt()))
                        .append(" - [").append(act.getAction()).append("] ")
                        .append(act.getDetails() != null ? act.getDetails() : "")
                        .append(" (by ").append(act.getPerformedByName() != null ? act.getPerformedByName() : "System").append(")\n");
            }
        }
        pm.setTimelineNarrative(timeline.toString());

        pm.setSensorAnalysis(String.format("Telemetry analysis indicates abnormal environmental thresholds during detection window. " +
                "Sensor readings in %s triggered safety violations corresponding with %s indicators.",
                roomName, incident.getCategory()));

        pm.setAiAssessmentSummary("Edge AI Isolation Forest and Random Forest risk models flagged anomalous pattern metrics. " +
                "Elevated threat probability was calculated prior to threshold breach.");

        pm.setResponseEvaluation("Incident acknowledgment and containment protocols were evaluated against building SLA benchmarks.");

        pm.setEvacuationSummary(incident.getCategory().name().contains("FIRE") || incident.getSeverity().name().contains("CRITICAL")
                ? "Emergency evacuation routing was initiated. Dynamic pathfinding redirected occupants away from hazard zones."
                : "Evacuation protocol was placed on standby; localized containment achieved without full building clearance.");

        pm.setRootCauseAssessment("Pending forensic investigation and sensor correlation analysis.");
        pm.setCorrectiveActions("1. Calibrate IoT sensor thresholds in affected room.\n2. Verify air circulation and fire dampening controls.\n3. Conduct inspection of electrical distribution sub-panels.");
        pm.setPreventiveRecommendations("1. Enhance AI sensitivity thresholds during non-business hours.\n2. Schedule quarterly dynamic evacuation drill.");

        return pm;
    }

    private PostMortemResponse toResponse(IncidentPostMortem pm) {
        PostMortemResponse res = new PostMortemResponse();
        res.setId(pm.getId());
        if (pm.getIncident() != null) {
            Incident inc = pm.getIncident();
            res.setIncidentId(inc.getId());
            res.setIncidentReferenceNumber(inc.getReferenceNumber());
            res.setIncidentCategory(inc.getCategory() != null ? inc.getCategory().name() : null);
            res.setIncidentSeverity(inc.getSeverity() != null ? inc.getSeverity().name() : null);
            res.setIncidentTitle(inc.getTitle());
            if (inc.getBuilding() != null) {
                res.setBuildingId(inc.getBuilding().getId());
                res.setBuildingName(inc.getBuilding().getName());
            }
            if (inc.getFloor() != null) {
                res.setFloorName(inc.getFloor().getFloorName());
            }
            if (inc.getRoom() != null) {
                res.setRoomNumber(inc.getRoom().getRoomCode());
            }
        }

        res.setReportTitle(pm.getReportTitle());
        res.setReportStatus(pm.getReportStatus());
        res.setAuthorId(pm.getAuthorId());
        res.setAuthorName(pm.getAuthorName());
        res.setExecutiveSummary(pm.getExecutiveSummary());
        res.setIncidentOverview(pm.getIncidentOverview());
        res.setTimelineNarrative(pm.getTimelineNarrative());
        res.setSensorAnalysis(pm.getSensorAnalysis());
        res.setAiAssessmentSummary(pm.getAiAssessmentSummary());
        res.setResponseEvaluation(pm.getResponseEvaluation());
        res.setEvacuationSummary(pm.getEvacuationSummary());
        res.setRootCauseAssessment(pm.getRootCauseAssessment());
        res.setCorrectiveActions(pm.getCorrectiveActions());
        res.setPreventiveRecommendations(pm.getPreventiveRecommendations());
        res.setReviewedBy(pm.getReviewedBy());
        res.setReviewedAt(pm.getReviewedAt());
        res.setFinalizedAt(pm.getFinalizedAt());
        res.setCreatedAt(pm.getCreatedAt());
        res.setUpdatedAt(pm.getUpdatedAt());

        return res;
    }
}
