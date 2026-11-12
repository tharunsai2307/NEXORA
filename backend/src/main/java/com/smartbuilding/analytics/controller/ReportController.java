package com.smartbuilding.analytics.controller;

import com.smartbuilding.analytics.dto.GeneratedReportResponse;
import com.smartbuilding.analytics.dto.PostMortemRequest;
import com.smartbuilding.analytics.dto.PostMortemResponse;
import com.smartbuilding.analytics.service.PostMortemService;
import com.smartbuilding.analytics.service.ReportExportService;
import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.security.UserDetailsImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportExportService reportExportService;
    private final PostMortemService postMortemService;

    public ReportController(ReportExportService reportExportService, PostMortemService postMortemService) {
        this.reportExportService = reportExportService;
        this.postMortemService = postMortemService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GeneratedReportResponse>>> listGeneratedReports(
            @RequestParam(required = false) String buildingId) {
        List<GeneratedReportResponse> list = reportExportService.listReports(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Reports catalog retrieved", list));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam(defaultValue = "EXECUTIVE_SUMMARY") String reportType,
            @RequestParam(required = false) String buildingId,
            @RequestParam(defaultValue = "PDF") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {

        String generatedBy = userPrincipal != null ? userPrincipal.getFullName() : "SYSTEM";
        byte[] data = reportExportService.exportReport(reportType, buildingId, format, start, end, generatedBy);

        boolean isPdf = "PDF".equalsIgnoreCase(format);
        String filename = reportType.toLowerCase() + "-report-" + Instant.now().getEpochSecond() + (isPdf ? ".pdf" : ".csv");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(isPdf ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("text/csv; charset=utf-8"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);

        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/incidents/{incidentId}/postmortem")
    public ResponseEntity<ApiResponse<PostMortemResponse>> getIncidentPostMortem(
            @PathVariable String incidentId) {
        PostMortemResponse data = postMortemService.getPostMortemByIncident(incidentId);
        return ResponseEntity.ok(ApiResponse.success("Post-mortem report retrieved", data));
    }

    @PostMapping("/incidents/{incidentId}/postmortem")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<PostMortemResponse>> saveIncidentPostMortem(
            @PathVariable String incidentId,
            @RequestBody PostMortemRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {

        String userId = userPrincipal != null ? userPrincipal.getId() : null;
        String userName = userPrincipal != null ? userPrincipal.getFullName() : "Safety Officer";

        PostMortemResponse data = postMortemService.createOrUpdatePostMortem(incidentId, request, userId, userName);
        return ResponseEntity.ok(ApiResponse.success("Post-mortem report updated", data));
    }

    @PostMapping("/incidents/{incidentId}/postmortem/finalize")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<PostMortemResponse>> finalizePostMortem(
            @PathVariable String incidentId,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {

        String reviewer = userPrincipal != null ? userPrincipal.getFullName() : "Safety Director";
        PostMortemResponse data = postMortemService.finalizePostMortem(incidentId, reviewer);
        return ResponseEntity.ok(ApiResponse.success("Post-mortem report finalized", data));
    }

    @GetMapping("/postmortems")
    public ResponseEntity<ApiResponse<List<PostMortemResponse>>> listPostMortems(
            @RequestParam(required = false) String status) {
        List<PostMortemResponse> data = postMortemService.listPostMortems(status);
        return ResponseEntity.ok(ApiResponse.success("Post-mortem list retrieved", data));
    }

    @GetMapping("/postmortems/{postMortemId}/export")
    public ResponseEntity<byte[]> exportPostMortem(
            @PathVariable String postMortemId,
            @RequestParam(defaultValue = "PDF") String format) {

        byte[] data = reportExportService.exportPostMortem(postMortemId, format);
        boolean isPdf = "PDF".equalsIgnoreCase(format);
        String filename = "incident-post-mortem-" + postMortemId.substring(0, Math.min(postMortemId.length(), 8)) + (isPdf ? ".pdf" : ".csv");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(isPdf ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("text/csv; charset=utf-8"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);

        return ResponseEntity.ok().headers(headers).body(data);
    }
}

