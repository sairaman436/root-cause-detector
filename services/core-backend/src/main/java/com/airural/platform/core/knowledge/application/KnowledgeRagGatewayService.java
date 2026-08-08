/*
 * Purpose: Proxies trusted RAG document and retrieval operations to the dedicated RAG service.
 * Why it exists: Backend APIs must remain the secured system boundary while retrieval execution stays in the AI service tier.
 * Architecture fit: Thin application gateway that preserves Clean Architecture by avoiding retrieval logic inside REST controllers.
 */
package com.airural.platform.core.knowledge.application;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/** Application gateway for the enterprise RAG service. */
@Service
public class KnowledgeRagGatewayService {
    private final RestTemplate restTemplate;
    private final String ragServiceUrl;

    public KnowledgeRagGatewayService(@Value("${airural.ai.gateway.rag-service-url:http://localhost:8102}") String ragServiceUrl, RestTemplateBuilder builder) {
        this.ragServiceUrl = ragServiceUrl;
        this.restTemplate = builder.setConnectTimeout(Duration.ofSeconds(3)).setReadTimeout(Duration.ofSeconds(120)).build();
    }

    /** Ingests a trusted JSON document into the RAG service. */
    public Map<String, Object> ingestJson(Map<String, Object> body) {
        return exchange("/v1/documents", HttpMethod.POST, new HttpEntity<>(body, jsonHeaders()));
    }

    /** Ingests a trusted file document into the RAG service with provenance metadata. */
    public Map<String, Object> ingestMultipart(MultipartFile file, Map<String, String> metadata) {
        try {
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(Optional.ofNullable(file.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)));
            parts.add("file", new HttpEntity<>(new NamedByteArrayResource(file.getBytes(), file.getOriginalFilename()), fileHeaders));
            metadata.forEach(parts::add);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            return exchange("/v1/documents", HttpMethod.POST, new HttpEntity<>(parts, headers));
        } catch (IOException ex) {
            throw new KnowledgeException(HttpStatus.BAD_REQUEST, "KNOWLEDGE_FILE_READ_FAILED", "Unable to read uploaded knowledge document");
        }
    }

    /** Lists trusted RAG documents. */
    public Map<String, Object> documents() {
        return exchange("/v1/documents", HttpMethod.GET, new HttpEntity<>(jsonHeaders()));
    }

    /** Gets one trusted RAG document with chunk metadata. */
    public Map<String, Object> document(String id) {
        return exchange("/v1/documents/" + id, HttpMethod.GET, new HttpEntity<>(jsonHeaders()));
    }

    /** Executes evidence-only hybrid retrieval. */
    public Map<String, Object> search(Map<String, Object> body) {
        return exchange("/v1/search", HttpMethod.POST, new HttpEntity<>(body, jsonHeaders()));
    }

    /** Requests a RAG collection reindex. */
    public Map<String, Object> reindexRag(Map<String, Object> body) {
        return exchange("/v1/reindex", HttpMethod.POST, new HttpEntity<>(body, jsonHeaders()));
    }

    /** Lists recent validated citation records. */
    public Map<String, Object> citations() {
        return exchange("/v1/citations", HttpMethod.GET, new HttpEntity<>(jsonHeaders()));
    }

    private Map<String, Object> exchange(String path, HttpMethod method, HttpEntity<?> entity) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    ragServiceUrl + path,
                    method,
                    entity,
                    new ParameterizedTypeReference<>() {});
            return Optional.ofNullable(response.getBody()).orElseGet(Map::of);
        } catch (RestClientException ex) {
            throw new KnowledgeException(HttpStatus.BAD_GATEWAY, "RAG_SERVICE_UNAVAILABLE", "RAG service request failed: " + ex.getMessage());
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        private NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename == null || filename.isBlank() ? "knowledge-document" : filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
