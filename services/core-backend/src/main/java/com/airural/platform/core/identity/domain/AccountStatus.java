/*
 * Purpose: Defines allowed user account lifecycle states.
 * Why it exists: Keeps authentication and administration logic from relying on free-form status strings.
 * Architecture fit: Supports identity governance and account lifecycle controls.
 */
package com.airural.platform.core.identity.domain;

/** User account lifecycle state. */
public enum AccountStatus {
    /** Account can authenticate and access authorized resources. */
    ACTIVE,
    /** Account exists but cannot authenticate. */
    SUSPENDED,
    /** Account has been deactivated for retention or offboarding. */
    DEACTIVATED
}
