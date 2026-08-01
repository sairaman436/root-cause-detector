/*
 * Purpose: Provides persistence access to task execution state.
 * Why it exists: Retry and recovery workflows need durable execution records.
 * Architecture fit: Repository adapter for task execution control.
 */
package com.airural.platform.core.agents.infrastructure;

import com.airural.platform.core.agents.domain.TaskExecutionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for task executions. */
public interface TaskExecutionRepository extends JpaRepository<TaskExecutionEntity, UUID> {
}
