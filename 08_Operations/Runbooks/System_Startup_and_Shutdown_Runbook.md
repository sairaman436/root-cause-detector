# System Startup and Shutdown Runbook

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Systems Engineering & Operations Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Operational Procedure Runbook  

---

# System Startup and Shutdown Runbook

---

# Document Information

| Field | Value |
|---------|---------|
| Runbook Name | System Startup and Shutdown Runbook |
| System Component | Entire System Infrastructure |
| Estimated Execution Time | 25 Minutes (Startup) / 15 Minutes (Shutdown) |
| Execution Frequency | Event-Driven (Maintenance / Disaster Failover) |
| Access Requirements | Kubernetes Cluster Admin / AWS Infrastructure IAM |

---

# Purpose

This runbook defines the strict dependency-ordered sequence for performing a clean, graceful shutdown or cold startup of the entire AI Rural Root Cause Discovery System infrastructure to prevent database corruption, message loss, or service deadlocks.

---

# System Dependency Hierarchy

```text
Layer 1: Core Networking & KMS Secret Vault
   │
   ▼
Layer 2: Database Storage (PostgreSQL Primary/Replicas) & Redis Cache
   │
   ▼
Layer 3: Message Queue Cluster (RabbitMQ / Kafka)
   │
   ▼
Layer 4: AI Inference Engine & Feature Store Services
   │
   ▼
Layer 5: Business API Gateway & Frontend UI Client
```

---

# Controlled System Shutdown Procedure

### Step 1: Drain Ingress Web Traffic
```bash
# Set Ingress Routing to Maintenance Page
kubectl apply -f k8s/manifests/maintenance-ingress.yaml -n production
```

### Step 2: Scale Down Business API Pods
```bash
kubectl scale deployment/csp-api-gateway --replicas=0 -n production
kubectl scale deployment/csp-survey-service --replicas=0 -n production
```

### Step 3: Scale Down AI Inference & Message Consumers
```bash
kubectl scale deployment/csp-ai-inference --replicas=0 -n production
kubectl scale deployment/csp-recommendation-engine --replicas=0 -n production
```

### Step 4: Flush Redis Cache & Checkpoint Database WAL
```bash
redis-cli -h cache.csp.gov.in SAVE
psql -h db.csp.gov.in -U postgres -c "CHECKPOINT;"
```

---

# Cold System Startup Procedure

Execute in exact reverse order (Layer 1 → Layer 5):

1. **Start Core Databases & Redis**: Verify PostgreSQL Master accepts write connections.
2. **Start Message Broker**: Confirm RabbitMQ queues are active.
3. **Start AI Inference Pods**: Verify GPU memory initialization and model artifact loading (`RC-XGB-v2`).
4. **Start API Gateway**: Scale API Gateway pods to 4 replicas.
5. **Restore Production Ingress Routing**: Switch traffic from maintenance page to live gateway.

---

# Approval

| Role | Name | Date |
|------|------|------|
| SRE Lead | Jonathan Vance | 2026-07-28 |
| Operations Director | Helen Brody | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of System Startup and Shutdown Runbook | Operations Team |

---

# End of Document
