/*
 * Purpose: Persists laboratory publications.
 * Why it exists: Publication APIs require durable paper, report, RFC, whitepaper, benchmark, and review records.
 * Architecture fit: JPA adapter for Research-1 publication system.
 */
package com.airural.platform.core.research.infrastructure;

import com.airural.platform.core.research.domain.PublicationEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for publications. */
public interface PublicationRepository extends JpaRepository<PublicationEntity, UUID> {
    List<PublicationEntity> findTop20ByOrderByPublishedAtDesc();
}
