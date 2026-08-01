/*
 * Purpose: Provides persistence access to task plans.
 * Why it exists: Planned work must be inspectable before and after execution.
 * Architecture fit: Repository adapter for planner output.
 */
package com.airural.platform.core.agents.infrastructure;

import com.airural.platform.core.agents.domain.TaskPlanEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for task plans. */
public interface TaskPlanRepository extends JpaRepository<TaskPlanEntity, UUID> {
}
