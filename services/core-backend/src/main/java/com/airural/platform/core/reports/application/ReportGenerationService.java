/*
 * Purpose: Generates executive, village, and district reports from completed decision intelligence outputs.
 * Why it exists: Sprint 1 requires durable PDF and CSV reports as the final step of the MVP workflow.
 * Architecture fit: Application service for the Reports bounded context that consumes the Decision API contract without owning decision logic.
 */
package com.airural.platform.core.reports.application;

import com.airural.platform.core.decision.application.DecisionIntelligenceService;
import com.airural.platform.core.decision.web.dto.DecisionDtos.*;
import com.airural.platform.core.identity.application.AuditService;
import com.airural.platform.core.identity.domain.AuditOutcome;
import com.airural.platform.core.reports.domain.*;
import com.airural.platform.core.reports.infrastructure.GeneratedReportRepository;
import com.airural.platform.core.reports.web.dto.ReportDtos.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for report generation and export. */
@Service
public class ReportGenerationService {
    private final GeneratedReportRepository reportRepository;
    private final DecisionIntelligenceService decisionService;
    private final AuditService auditService;

    public ReportGenerationService(GeneratedReportRepository reportRepository, DecisionIntelligenceService decisionService, AuditService auditService) {
        this.reportRepository = reportRepository;
        this.decisionService = decisionService;
        this.auditService = auditService;
    }

    /** Generates a durable report from an existing decision. */
    @Transactional
    public ReportResponse generate(GenerateReportRequest request, UUID userId) {
        DecisionResponse decision = decisionService.decision(request.decisionId());
        String title = value(request.title(), defaultTitle(request.reportType(), decision));
        String summary = executiveSummary(decision, request.reportType());
        String markdown = markdown(title, request.reportType(), decision, summary);
        String csv = csv(decision);
        GeneratedReportEntity report = reportRepository.save(new GeneratedReportEntity(
                request.decisionId(),
                request.surveyId(),
                request.organizationId(),
                request.reportType(),
                title,
                summary,
                markdown,
                csv,
                userId));
        auditService.record(userId, "REPORT_GENERATED", AuditOutcome.SUCCESS, null, null, report.id().toString());
        return response(report);
    }

    /** Lists generated reports. */
    @Transactional(readOnly = true)
    public Page<ReportResponse> reports(Pageable pageable) {
        return reportRepository.findByDeletedAtIsNull(pageable).map(this::response);
    }

    /** Gets one generated report. */
    @Transactional(readOnly = true)
    public ReportResponse report(UUID id) {
        return response(entity(id));
    }

    /** Lists reports generated for one decision. */
    @Transactional(readOnly = true)
    public List<ReportResponse> byDecision(UUID decisionId) {
        return reportRepository.findByDecisionIdAndDeletedAtIsNullOrderByGeneratedAtDesc(decisionId).stream().map(this::response).toList();
    }

    /** Generates CSV bytes for download. */
    @Transactional(readOnly = true)
    public byte[] csv(UUID id) {
        return entity(id).csvContent().getBytes(StandardCharsets.UTF_8);
    }

    /** Generates a compact PDF file from stored report content. */
    @Transactional(readOnly = true)
    public byte[] pdf(UUID id) {
        GeneratedReportEntity report = entity(id);
        String text = report.title() + "\n\n" + report.contentMarkdown();
        return SimplePdf.write(report.title(), wrap(text, 92));
    }

    private GeneratedReportEntity entity(UUID id) {
        return reportRepository.findById(id)
                .filter(report -> report.deletedAt() == null)
                .orElseThrow(() -> new ReportException("REPORT_NOT_FOUND", "Report was not found", HttpStatus.NOT_FOUND));
    }

    private String defaultTitle(ReportType type, DecisionResponse decision) {
        return switch (type) {
            case EXECUTIVE -> "Executive Root Cause Report";
            case VILLAGE -> "Village Root Cause Report";
            case DISTRICT -> "District Root Cause Report";
        } + " - " + decision.id();
    }

    private String executiveSummary(DecisionResponse decision, ReportType type) {
        return "The " + type.name().toLowerCase(Locale.ROOT) + " report summarizes " + decision.rootCauses().size()
                + " root cause(s), " + decision.recommendations().size() + " recommendation(s), and confidence "
                + String.format(Locale.ROOT, "%.2f", decision.confidence()) + ".";
    }

    private String markdown(String title, ReportType type, DecisionResponse decision, String summary) {
        StringBuilder out = new StringBuilder();
        out.append("# ").append(title).append("\n\n");
        out.append("Generated At: ").append(DateTimeFormatter.ISO_INSTANT.format(decision.createdAt())).append("\n\n");
        out.append("Report Type: ").append(type).append("\n\n");
        out.append("## Executive Summary\n").append(summary).append("\n\n");
        out.append("## Final Decision\n").append(decision.finalDecision()).append("\n\n");
        out.append("## Root Causes\n");
        for (RootCauseResponse rootCause : decision.rootCauses()) {
            out.append("- ").append(rootCause.rank()).append(". ").append(rootCause.title()).append(" (confidence ")
                    .append(String.format(Locale.ROOT, "%.2f", rootCause.confidence())).append("): ")
                    .append(rootCause.description()).append("\n");
        }
        out.append("\n## Recommendations\n");
        for (RecommendationResponse recommendation : decision.recommendations()) {
            out.append("- P").append(recommendation.priority()).append(" ").append(recommendation.title()).append(": ")
                    .append(recommendation.description()).append(" Expected impact ")
                    .append(String.format(Locale.ROOT, "%.2f", recommendation.impactScore())).append(".\n");
        }
        out.append("\n## Citations\n");
        for (String citation : decision.citations()) {
            out.append("- ").append(citation).append("\n");
        }
        return out.toString();
    }

    private String csv(DecisionResponse decision) {
        StringBuilder out = new StringBuilder("section,title,description,confidence,priority\n");
        for (RootCauseResponse rootCause : decision.rootCauses()) {
            out.append("root_cause,").append(escape(rootCause.title())).append(',').append(escape(rootCause.description())).append(',')
                    .append(rootCause.confidence()).append(',').append(rootCause.rank()).append('\n');
        }
        for (RecommendationResponse recommendation : decision.recommendations()) {
            out.append("recommendation,").append(escape(recommendation.title())).append(',').append(escape(recommendation.description())).append(',')
                    .append(recommendation.confidence()).append(',').append(recommendation.priority()).append('\n');
        }
        return out.toString();
    }

    private ReportResponse response(GeneratedReportEntity report) {
        String base = "/api/v1/reports/" + report.id();
        return new ReportResponse(report.id(), report.decisionId(), report.surveyId(), report.organizationId(), report.reportType(), report.title(), report.status(), report.executiveSummary(), report.contentMarkdown(), base + "/csv", base + "/pdf", report.generatedBy(), report.generatedAt());
    }

    private String value(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text;
    }

    private String escape(String text) {
        String safe = text == null ? "" : text.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        for (String raw : text.split("\\R")) {
            String line = raw.trim();
            while (line.length() > width) {
                int cut = line.lastIndexOf(' ', width);
                if (cut < 24) cut = width;
                lines.add(line.substring(0, cut));
                line = line.substring(cut).trim();
            }
            lines.add(line);
        }
        return lines;
    }
}
