/*
 * Purpose: Provides persistence access to embedding records.
 * Why it exists: Local fallback hybrid search and embedding metadata need durable chunk records.
 * Architecture fit: Repository adapter for embedding vector metadata.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.EmbeddingRecordEntity;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/** Repository for embedding records. */
public interface EmbeddingRecordRepository extends JpaRepository<EmbeddingRecordEntity, UUID> {
    List<EmbeddingRecordEntity> findTop10ByCollectionNameOrderByChunkIndexAsc(String collectionName);

    @Query("select e from EmbeddingRecordEntity e where e.collectionName = :collection and lower(e.chunkText) like lower(concat('%', :term, '%'))")
    List<EmbeddingRecordEntity> searchByText(@Param("collection") String collection, @Param("term") String term);
}
