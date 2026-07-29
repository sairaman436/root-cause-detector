/*
 * Purpose: Stores evidence binaries on the local filesystem.
 * Why it exists: Local development and test deployments need a concrete storage adapter while preserving cloud-ready abstraction.
 * Architecture fit: Infrastructure implementation of the evidence storage port.
 */
package com.airural.platform.core.evidence.infrastructure;

import com.airural.platform.core.evidence.application.*;
import com.airural.platform.core.evidence.domain.StorageProvider;
import java.nio.file.*;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Local filesystem evidence storage adapter. */
@Service
public class LocalEvidenceStorageService implements EvidenceStorageService {
    private final Path rootPath;

    public LocalEvidenceStorageService(@Value("${airural.evidence.local-storage-path:./var/evidence}") String rootPath) {
        this.rootPath = Paths.get(rootPath).toAbsolutePath().normalize();
    }

    @Override
    public StoredEvidenceObject store(byte[] content, String sanitizedFileName) {
        try {
            Files.createDirectories(rootPath);
            String storedFileName = UUID.randomUUID() + "-" + sanitizedFileName;
            Path target = rootPath.resolve(storedFileName).normalize();
            if (!target.startsWith(rootPath)) {
                throw new EvidenceException("EVIDENCE_STORAGE_PATH_INVALID", "Evidence storage path is invalid", HttpStatus.BAD_REQUEST);
            }
            Files.write(target, content, StandardOpenOption.CREATE_NEW);
            return new StoredEvidenceObject(StorageProvider.LOCAL, storedFileName, storedFileName);
        } catch (java.io.IOException ex) {
            throw new EvidenceException("EVIDENCE_STORAGE_WRITE_FAILED", "Evidence file could not be stored", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public EvidenceBinary load(String storageKey, String mimeType, String fileName) {
        try {
            Path target = rootPath.resolve(storageKey).normalize();
            if (!target.startsWith(rootPath) || !Files.exists(target)) {
                throw new EvidenceException("EVIDENCE_BINARY_NOT_FOUND", "Evidence binary was not found", HttpStatus.NOT_FOUND);
            }
            return new EvidenceBinary(Files.readAllBytes(target), mimeType, fileName);
        } catch (java.io.IOException ex) {
            throw new EvidenceException("EVIDENCE_STORAGE_READ_FAILED", "Evidence file could not be read", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Optional<String> createSignedUrl(String storageKey, Duration ttl) {
        return Optional.empty();
    }
}
