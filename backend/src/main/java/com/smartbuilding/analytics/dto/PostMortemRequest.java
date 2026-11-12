package com.smartbuilding.analytics.dto;

import com.smartbuilding.analytics.model.PostMortemStatus;

public class PostMortemRequest {

    private String reportTitle;
    private PostMortemStatus reportStatus;
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

    public PostMortemRequest() {}

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public PostMortemStatus getReportStatus() { return reportStatus; }
    public void setReportStatus(PostMortemStatus reportStatus) { this.reportStatus = reportStatus; }

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
}

