package com.smartbuilding.analytics.model;

import com.smartbuilding.emergency.model.Incident;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incident_post_mortems")
public class IncidentPostMortem {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false, unique = true)
    private Incident incident;

    @Column(name = "report_title", length = 255, nullable = false)
    private String reportTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", length = 32, nullable = false)
    private PostMortemStatus reportStatus = PostMortemStatus.DRAFT;

    @Column(name = "author_id", length = 64)
    private String authorId;

    @Column(name = "author_name", length = 128)
    private String authorName;

    @Column(name = "executive_summary", columnDefinition = "TEXT")
    private String executiveSummary;

    @Column(name = "incident_overview", columnDefinition = "TEXT")
    private String incidentOverview;

    @Column(name = "timeline_narrative", columnDefinition = "TEXT")
    private String timelineNarrative;

    @Column(name = "sensor_analysis", columnDefinition = "TEXT")
    private String sensorAnalysis;

    @Column(name = "ai_assessment_summary", columnDefinition = "TEXT")
    private String aiAssessmentSummary;

    @Column(name = "response_evaluation", columnDefinition = "TEXT")
    private String responseEvaluation;

    @Column(name = "evacuation_summary", columnDefinition = "TEXT")
    private String evacuationSummary;

    @Column(name = "root_cause_assessment", columnDefinition = "TEXT")
    private String rootCauseAssessment;

    @Column(name = "corrective_actions", columnDefinition = "TEXT")
    private String correctiveActions;

    @Column(name = "preventive_recommendations", columnDefinition = "TEXT")
    private String preventiveRecommendations;

    @Column(name = "reviewed_by", length = 128)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "finalized_at")
    private Instant finalizedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public IncidentPostMortem() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Incident getIncident() { return incident; }
    public void setIncident(Incident incident) { this.incident = incident; }

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

