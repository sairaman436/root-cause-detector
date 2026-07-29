/*
 * Purpose: Defines supported survey assignment targets.
 * Why it exists: Surveys can be assigned to organizations, teams, users, or geographic regions.
 * Architecture fit: Domain vocabulary for the assignment model.
 */
package com.airural.platform.core.survey.domain;

/** Survey assignment target type. */
public enum AssignmentType {
    ORGANIZATION,
    TEAM,
    USER,
    GEOGRAPHIC_REGION
}
