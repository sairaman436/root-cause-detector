/*
 * Purpose: Validates uploaded image bytes before they reach a vision provider.
 * Why it exists: Multipart filenames and MIME headers are caller-controlled and cannot prove that a payload is a decodable image.
 * Architecture fit: Keeps media validation at the AI application boundary, before provider calls and before observations enter RAG.
 */
package com.airural.platform.core.ai.application;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** Validates the actual bytes, media type, decodability, and dimensions of vision uploads. */
public final class VisionImageUploadValidator {
    public static final String INVALID_IMAGE_MESSAGE =
            "Invalid image file. The uploaded content is not a supported image.";

    private final long maxImageBytes;
    private final int maxWidth;
    private final int maxHeight;

    public VisionImageUploadValidator(long maxImageBytes, int maxWidth, int maxHeight) {
        this.maxImageBytes = maxImageBytes;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    /** Validates and returns an immutable byte snapshot for the provider request. */
    public ValidatedImage validate(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new AiException("VISION_IMAGE_REQUIRED", "An image is required.", HttpStatus.BAD_REQUEST);
        }
        if (image.getSize() > maxImageBytes) {
            throw new AiException("VISION_IMAGE_TOO_LARGE", "The image exceeds the configured size limit.", HttpStatus.PAYLOAD_TOO_LARGE);
        }

        String declaredMimeType = normalizeMimeType(image.getContentType());
        byte[] bytes;
        try {
            bytes = image.getBytes();
        } catch (IOException ex) {
            throw invalidImage();
        }

        String detectedMimeType = detectMimeType(bytes);
        if (detectedMimeType == null || !declaredMimeType.equals(detectedMimeType)) {
            throw invalidImage();
        }

        validateDecodedImage(bytes);
        return new ValidatedImage(bytes, detectedMimeType);
    }

    private String normalizeMimeType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        String normalized = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return "image/jpg".equals(normalized) ? "image/jpeg" : normalized;
    }

    private String detectMimeType(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff) {
            return "image/jpeg";
        }
        if (bytes.length >= 8
                && (bytes[0] & 0xff) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4e
                && bytes[3] == 0x47
                && bytes[4] == 0x0d
                && bytes[5] == 0x0a
                && bytes[6] == 0x1a
                && bytes[7] == 0x0a) {
            return "image/png";
        }
        return null;
    }

    private void validateDecodedImage(byte[] bytes) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (input == null) {
                throw invalidImage();
            }
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalidImage();
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > maxWidth || height > maxHeight) {
                    throw new AiException(
                            "VISION_IMAGE_DIMENSIONS_EXCEEDED",
                            "The image exceeds the configured dimension limit.",
                            HttpStatus.PAYLOAD_TOO_LARGE);
                }
                BufferedImage decoded = reader.read(0);
                if (decoded == null) {
                    throw invalidImage();
                }
            } finally {
                reader.dispose();
            }
        } catch (AiException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            throw invalidImage();
        }
    }

    private AiException invalidImage() {
        return new AiException("VISION_INVALID_IMAGE", INVALID_IMAGE_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    /** Immutable validated payload passed to the provider adapter. */
    public record ValidatedImage(byte[] bytes, String mimeType) {
        public ValidatedImage {
            bytes = bytes.clone();
        }

        @Override
        public byte[] bytes() {
            return bytes.clone();
        }
    }
}
