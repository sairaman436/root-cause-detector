/*
 * Purpose: Validates uploaded evidence files.
 * Why it exists: Evidence ingestion must enforce allowed MIME types, maximum size, checksum duplication, and safe file names.
 * Architecture fit: Application validation service for the Evidence module.
 */
package com.airural.platform.core.evidence.application;

import com.airural.platform.core.evidence.domain.EvidenceType;
import com.airural.platform.core.evidence.infrastructure.EvidenceRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Validates upload input and derives trusted file metadata. */
@Service
public class EvidenceValidationService {
    private final EvidenceRepository evidenceRepository;
    private final long maxSizeBytes;
    private final Set<String> allowedMimeTypes;

    public EvidenceValidationService(
            EvidenceRepository evidenceRepository,
            @Value("${airural.evidence.max-file-size-bytes:52428800}") long maxSizeBytes,
            @Value("${airural.evidence.allowed-mime-types:image/jpeg,image/png,image/webp,video/mp4,audio/mpeg,audio/wav,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,application/gpx+xml,application/geo+json,text/plain,application/octet-stream}") String allowedMimeTypes) {
        this.evidenceRepository = evidenceRepository;
        this.maxSizeBytes = maxSizeBytes;
        this.allowedMimeTypes = Arrays.stream(allowedMimeTypes.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    /** Validates and normalizes upload metadata. */
    public ValidatedEvidenceUpload validate(MultipartFile file, UUID organizationId) {
        if (file == null || file.isEmpty()) {
            throw new EvidenceException("EVIDENCE_FILE_EMPTY", "Evidence file is required", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > maxSizeBytes) {
            throw new EvidenceException("EVIDENCE_FILE_TOO_LARGE", "Evidence file exceeds maximum size", HttpStatus.BAD_REQUEST);
        }
        String mimeType = file.getContentType() == null ? "application/octet-stream" : file.getContentType().toLowerCase();
        if (!allowedMimeTypes.contains(mimeType)) {
            throw new EvidenceException("EVIDENCE_MIME_TYPE_NOT_ALLOWED", "Evidence MIME type is not allowed", HttpStatus.BAD_REQUEST);
        }
        byte[] content;
        try {
            content = file.getBytes();
        } catch (java.io.IOException ex) {
            throw new EvidenceException("EVIDENCE_FILE_READ_FAILED", "Evidence file could not be read", HttpStatus.BAD_REQUEST);
        }
        String checksum = checksum(content);
        if (evidenceRepository.existsByOrganizationIdAndSha256ChecksumAndIsActiveTrue(organizationId, checksum)) {
            throw new EvidenceException("EVIDENCE_DUPLICATE", "Evidence file already exists for this organization", HttpStatus.CONFLICT);
        }
        String sanitizedFileName = sanitize(file.getOriginalFilename());
        return new ValidatedEvidenceUpload(content, sanitizedFileName, mimeType, file.getSize(), checksum, classify(mimeType, sanitizedFileName));
    }

    private String checksum(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(content);
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new EvidenceException("EVIDENCE_CHECKSUM_FAILED", "SHA-256 checksum could not be calculated", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String sanitize(String fileName) {
        String candidate = fileName == null || fileName.isBlank() ? "evidence.bin" : fileName;
        candidate = candidate.replace('\\', '/');
        candidate = candidate.substring(candidate.lastIndexOf('/') + 1);
        candidate = candidate.replaceAll("[^A-Za-z0-9._-]", "_");
        candidate = candidate.replaceAll("_+", "_");
        if (candidate.isBlank() || candidate.equals(".") || candidate.equals("..")) {
            return "evidence.bin";
        }
        return candidate.length() > 255 ? candidate.substring(candidate.length() - 255) : candidate;
    }

    private EvidenceType classify(String mimeType, String fileName) {
        String lowerName = fileName.toLowerCase();
        if (mimeType.startsWith("image/")) return EvidenceType.IMAGE;
        if (mimeType.startsWith("video/")) return EvidenceType.VIDEO;
        if (mimeType.startsWith("audio/")) return EvidenceType.AUDIO;
        if ("application/pdf".equals(mimeType)) return EvidenceType.PDF;
        if (mimeType.contains("word") || mimeType.contains("excel") || mimeType.contains("powerpoint") || lowerName.endsWith(".doc") || lowerName.endsWith(".docx") || lowerName.endsWith(".xls") || lowerName.endsWith(".xlsx") || lowerName.endsWith(".ppt") || lowerName.endsWith(".pptx")) return EvidenceType.OFFICE_DOCUMENT;
        if (mimeType.contains("gpx") || mimeType.contains("geo+json") || lowerName.endsWith(".gpx") || lowerName.endsWith(".geojson")) return EvidenceType.GPS_FILE;
        return EvidenceType.GENERIC_ATTACHMENT;
    }

    /** Trusted upload metadata derived after validation. */
    public record ValidatedEvidenceUpload(byte[] content, String sanitizedFileName, String mimeType, long sizeBytes, String checksum, EvidenceType evidenceType) {
    }
}
