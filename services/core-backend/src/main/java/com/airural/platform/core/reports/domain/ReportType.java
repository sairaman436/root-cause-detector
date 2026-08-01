/*
 * Purpose: Enumerates supported Sprint 1 report types.
 * Why it exists: Report generation must produce explicit executive, village, and district report variants.
 * Architecture fit: Domain primitive for the Reports bounded context.
 */
package com.airural.platform.core.reports.domain;

/** Supported generated report types. */
public enum ReportType {
    EXECUTIVE,
    VILLAGE,
    DISTRICT
}
