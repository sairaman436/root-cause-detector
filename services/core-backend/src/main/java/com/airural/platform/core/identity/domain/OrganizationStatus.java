/*
 * Purpose: Defines allowed organization lifecycle states.
 * Why it exists: Prevents users from being managed under inactive organizations.
 * Architecture fit: Supports organization governance in the identity platform.
 */
package com.airural.platform.core.identity.domain;

/** Organization lifecycle state. */
public enum OrganizationStatus {
    /** Organization can own users and roles. */
    ACTIVE,
    /** Organization is retained but unavailable for new activity. */
    INACTIVE
}
