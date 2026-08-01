/*
 * Purpose: Persists generated decision intelligence reports.
 * Why it exists: PDF and CSV exports must be reproducible from durable report records.
 * Architecture fit: Aggregate root for the Reports bounded context.
 */
package com.airural.platform.core.reports.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for generated reports. */
@Entity
@Table(name = "generated_reports", schema = "reports")
public class GeneratedReportEntity {
    @Id private UUID id;
    private UUID decisionId;
    private UUID surveyId;
    private UUID organizationId;
    @Enumerated(EnumType.STRING) private ReportType reportType;
    private String title;
    private String status;
    @Column(columnDefinition = "TEXT") private String executiveSummary;
    @Column(columnDefinition = "TEXT") private String contentMarkdown;
    @Column(columnDefinition = "TEXT") private String csvContent;
    private UUID generatedBy;
    private Instant generatedAt;
    private Instant updatedAt;
    private Instant deletedAt;

    protected GeneratedReportEntity() {}

    public GeneratedReportEntity(UUID decisionId, UUID surveyId, UUID organizationId, ReportType reportType, String title, String executiveSummary, String contentMarkdown, String csvContent, UUID generatedBy) {
        this.id = UUID.randomUUID();
        this.decisionId = decisionId;
        this.surveyId = surveyId;
        this.organizationId = organizationId;
        this.reportType = reportType;
        this.title = title;
        this.status = "GENERATED";
        this.executiveSummary = executiveSummary;
        this.contentMarkdown = contentMarkdown;
        this.csvContent = csvContent;
        this.generatedBy = generatedBy;
        this.generatedAt = Instant.now();
        this.updatedAt = this.generatedAt;
    }

    public UUID id() { return id; }
    public UUID decisionId() { return decisionId; }
    public UUID surveyId() { return surveyId; }
    public UUID organizationId() { return organizationId; }
    public ReportType reportType() { return reportType; }
    public String title() { return title; }
    public String status() { return status; }
    public String executiveSummary() { return executiveSummary; }
    public String contentMarkdown() { return contentMarkdown; }
    public String csvContent() { return csvContent; }
    public UUID generatedBy() { return generatedBy; }
    public Instant generatedAt() { return generatedAt; }
    public Instant updatedAt() { return updatedAt; }
    public Instant deletedAt() { return deletedAt; }
}
