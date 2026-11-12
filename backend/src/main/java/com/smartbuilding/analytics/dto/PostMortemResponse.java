package com.smartbuilding.analytics.dto;

import com.smartbuilding.analytics.model.PostMortemStatus;
import java.time.Instant;

public class PostMortemResponse {

    private String id;
    private String incidentId;
    private String incidentReferenceNumber;
    private String incidentCategory;
    private String incidentSeverity;
    private String incidentTitle;
    private String buildingId;
    private String buildingName;
    private String floorName;
    private String roomNumber;
    private String reportTitle;
    private PostMortemStatus reportStatus;
    private String authorId;
    private String authorName;
    private String executiveSummary;
    private String incidentOverview;
    private String timelineNarrative;
    private String sensorAnalysis;
    private String aiAssessmentSummary;
    private String responseEvaluation;
    private String evacuationSummary;
    private String rootCauseAssessment;
    private String correctiveActions;
    private String preventiveRecommendations;
    private String reviewedBy;
    private Instant reviewedAt;
    private Instant finalizedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public PostMortemResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIncidentId() { return incidentId; }
    public void setIncidentId(String incidentId) { this.incidentId = incidentId; }

    public String getIncidentReferenceNumber() { return incidentReferenceNumber; }
    public void setIncidentReferenceNumber(String incidentReferenceNumber) { this.incidentReferenceNumber = incidentReferenceNumber; }

    public String getIncidentCategory() { return incidentCategory; }
    public void setIncidentCategory(String incidentCategory) { this.incidentCategory = incidentCategory; }

    public String getIncidentSeverity() { return incidentSeverity; }
    public void setIncidentSeverity(String incidentSeverity) { this.incidentSeverity = incidentSeverity; }

    public String getIncidentTitle() { return incidentTitle; }
    public void setIncidentTitle(String incidentTitle) { this.incidentTitle = incidentTitle; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getFloorName() { return floorName; }
    public void setFloorName(String floorName) { this.floorName = floorName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public PostMortemStatus getReportStatus() { return reportStatus; }
    public void setReportStatus(PostMortemStatus reportStatus) { this.reportStatus = reportStatus; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; }

    public String getIncidentOverview() { return incidentOverview; }
    public void setIncidentOverview(String incidentOverview) { this.incidentOverview = incidentOverview; }

    public String getTimelineNarrative() { return timelineNarrative; }
    public void setTimelineNarrative(String timelineNarrative) { this.timelineNarrative = timelineNarrative; }

    public String getSensorAnalysis() { return sensorAnalysis; }
    public void setSensorAnalysis(String sensorAnalysis) { this.sensorAnalysis = sensorAnalysis; }

    public String getAiAssessmentSummary() { return aiAssessmentSummary; }
    public void setAiAssessmentSummary(String aiAssessmentSummary) { this.aiAssessmentSummary = aiAssessmentSummary; }

    public String getResponseEvaluation() { return responseEvaluation; }
    public void setResponseEvaluation(String responseEvaluation) { this.responseEvaluation = responseEvaluation; }

    public String getEvacuationSummary() { return evacuationSummary; }
    public void setEvacuationSummary(String evacuationSummary) { this.evacuationSummary = evacuationSummary; }

    public String getRootCauseAssessment() { return rootCauseAssessment; }
    public void setRootCauseAssessment(String rootCauseAssessment) { this.rootCauseAssessment = rootCauseAssessment; }

    public String getCorrectiveActions() { return correctiveActions; }
    public void setCorrectiveActions(String correctiveActions) { this.correctiveActions = correctiveActions; }

    public String getPreventiveRecommendations() { return preventiveRecommendations; }
    public void setPreventiveRecommendations(String preventiveRecommendations) { this.preventiveRecommendations = preventiveRecommendations; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }

    public Instant getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(Instant finalizedAt) { this.finalizedAt = finalizedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

