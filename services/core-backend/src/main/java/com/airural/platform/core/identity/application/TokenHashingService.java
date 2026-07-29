/*
 * Purpose: Hashes refresh tokens before persistence.
 * Why it exists: Stored refresh tokens must not be reusable if database records are exposed.
 * Architecture fit: Implements secure token lifecycle controls.
 */
package com.airural.platform.core.identity.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Service;

/** SHA-256 hashing service for opaque refresh tokens. */
@Service
public class TokenHashingService {

    /** Returns a lowercase hexadecimal SHA-256 hash. */
    public String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : digest) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
