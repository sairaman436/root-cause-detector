/*
 * Purpose: Represents the authenticated platform principal.
 * Why it exists: Controllers and services need access to the user ID and granted authorities from JWT authentication.
 * Architecture fit: Security adapter for the identity module.
 */
package com.airural.platform.core.identity.security;

import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Spring Security principal for authenticated users. */
public record AuthenticatedUser(UUID userId, String email, Collection<? extends GrantedAuthority> authorities)
        implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }
}
