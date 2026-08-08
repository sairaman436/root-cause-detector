/*
 * Purpose: Stabilizes paginated REST response serialization.
 * Why it exists: Spring's direct PageImpl JSON structure is not a stable public API contract across framework versions.
 * Architecture fit: Cross-cutting web configuration for RC1 API reliability without changing bounded-context services.
 */
package com.airural.platform.core.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/** Configures Spring Data web pagination serialization through DTO mode. */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class PageSerializationConfiguration {}
