/*
 * Purpose: Verifies evidence upload validation behavior.
 * Why it exists: Milestone 4 requires MIME validation, size enforcement, duplicate detection, and filename sanitization.
 * Architecture fit: Unit coverage for evidence application validation.
 */
package com.airural.platform.core.evidence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.airural.platform.core.evidence.application.*;
import com.airural.platform.core.evidence.domain.EvidenceType;
import com.airural.platform.core.evidence.infrastructure.EvidenceRepository;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

/** Unit tests for evidence upload validation. */
class EvidenceValidationServiceTests {
    private static final UUID ORGANIZATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void validUploadIsClassifiedAndSanitized() {
        EvidenceRepository repository = mock(EvidenceRepository.class);
        EvidenceValidationService service = new EvidenceValidationService(repository, 1024, "image/png");
        MockMultipartFile file = new MockMultipartFile(
                "file", "..\\unsafe village photo.png", "image/png", "image-bytes".getBytes(StandardCharsets.UTF_8));

        EvidenceValidationService.ValidatedEvidenceUpload upload = service.validate(file, ORGANIZATION_ID);

        assertThat(upload.evidenceType()).isEqualTo(EvidenceType.IMAGE);
        assertThat(upload.sanitizedFileName()).isEqualTo("unsafe_village_photo.png");
        assertThat(upload.checksum()).hasSize(64);
    }

    @Test
    void invalidMimeTypeIsRejected() {
        EvidenceValidationService service = new EvidenceValidationService(mock(EvidenceRepository.class), 1024, "image/png");
        MockMultipartFile file = new MockMultipartFile("file", "script.js", "application/javascript", "alert(1)".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.validate(file, ORGANIZATION_ID))
                .isInstanceOf(EvidenceException.class)
                .hasMessageContaining("MIME type");
    }

    @Test
    void duplicateChecksumIsRejected() {
        EvidenceRepository repository = mock(EvidenceRepository.class);
        when(repository.existsByOrganizationIdAndSha256ChecksumAndIsActiveTrue(eq(ORGANIZATION_ID), anyString())).thenReturn(true);
        EvidenceValidationService service = new EvidenceValidationService(repository, 1024, "application/pdf");
        MockMultipartFile file = new MockMultipartFile("file", "evidence.pdf", "application/pdf", "same".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.validate(file, ORGANIZATION_ID))
                .isInstanceOf(EvidenceException.class)
                .hasMessageContaining("already exists");
    }
}
