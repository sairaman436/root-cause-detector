/*
 * Purpose: Covers the media-validation boundary for the authenticated vision endpoint.
 * Why it exists: A filename or multipart MIME header must never allow non-image bytes to reach Moondream.
 * Architecture fit: Regression coverage for the AI application boundary without requiring Ollama or PostgreSQL.
 */
package com.airural.platform.core.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.airural.platform.core.ai.application.AiException;
import com.airural.platform.core.ai.application.VisionImageUploadValidator;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

/** Unit tests for signature, MIME, decode, and image-size validation. */
class VisionImageUploadValidatorTests {
    private final VisionImageUploadValidator validator = new VisionImageUploadValidator(1024 * 1024, 2048, 2048);

    @Test
    void acceptsDecodableJpegWithMatchingMimeType() throws Exception {
        var result = validator.validate(file("photo.jpg", "image/jpeg", encodedImage("jpeg", 32, 32)));

        assertThat(result.mimeType()).isEqualTo("image/jpeg");
    }

    @Test
    void acceptsDecodablePngWithMatchingMimeType() throws Exception {
        var result = validator.validate(file("photo.png", "image/png", encodedImage("png", 32, 32)));

        assertThat(result.mimeType()).isEqualTo("image/png");
    }

    @Test
    void rejectsHtmlRenamedToJpegBeforeProviderCall() {
        assertInvalid(file("page.jpg", "image/jpeg", "<!DOCTYPE html><html>error</html>".getBytes()));
    }

    @Test
    void rejectsCorruptJpeg() {
        assertInvalid(file("corrupt.jpg", "image/jpeg", new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00}));
    }

    @Test
    void rejectsUnsupportedImageFormat() throws Exception {
        assertInvalid(file("animated.gif", "image/gif", encodedImage("gif", 32, 32)));
    }

    @Test
    void rejectsPayloadThatExceedsFileSizeLimit() throws Exception {
        var smallValidator = new VisionImageUploadValidator(100, 2048, 2048);

        assertThatThrownBy(() -> smallValidator.validate(file("large.jpg", "image/jpeg", encodedImage("jpeg", 256, 256))))
                .isInstanceOf(AiException.class)
                .satisfies(error -> assertThat(((AiException) error).code()).isEqualTo("VISION_IMAGE_TOO_LARGE"));
    }

    @Test
    void rejectsImageThatExceedsDimensionLimit() throws Exception {
        assertThatThrownBy(() -> validator.validate(file("wide.jpg", "image/jpeg", encodedImage("jpeg", 2049, 1))))
                .isInstanceOf(AiException.class)
                .satisfies(error -> assertThat(((AiException) error).code()).isEqualTo("VISION_IMAGE_DIMENSIONS_EXCEEDED"));
    }

    private void assertInvalid(MockMultipartFile file) {
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(AiException.class)
                .hasMessage(VisionImageUploadValidator.INVALID_IMAGE_MESSAGE)
                .satisfies(error -> assertThat(((AiException) error).code()).isEqualTo("VISION_INVALID_IMAGE"));
    }

    private MockMultipartFile file(String name, String mimeType, byte[] bytes) {
        return new MockMultipartFile("image", name, mimeType, bytes);
    }

    private byte[] encodedImage(String format, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThat(ImageIO.write(image, format, output)).isTrue();
        return output.toByteArray();
    }
}
