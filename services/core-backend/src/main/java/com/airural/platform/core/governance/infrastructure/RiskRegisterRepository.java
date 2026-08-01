/*
 * Purpose: Persists active and historical AI risk register entries.
 * Why it exists: Risk APIs need durable risk ownership, status, and severity data.
 * Architecture fit: JPA adapter for AI-9 risk management.
 */
package com.airural.platform.core.governance.infrastructure;

import com.airural.platform.core.governance.domain.RiskRegisterEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for risk register entries. */
public interface RiskRegisterRepository extends JpaRepository<RiskRegisterEntity, UUID> {
    long countByStatusNot(String status);
    List<RiskRegisterEntity> findTop20ByOrderByUpdatedAtDesc();
}
