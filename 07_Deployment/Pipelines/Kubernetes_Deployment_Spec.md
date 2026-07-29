# Kubernetes Deployment Specification

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** DevOps & Container Orchestration Engineering Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Kubernetes Infrastructure Manifest Specification  

---

# Kubernetes Deployment Specification

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | Kubernetes Deployment Specification |
| Domain | Container Orchestration & Cloud Infrastructure |
| Version | 1.0 |
| Status | Approved |
| Owner | DevOps Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This document provides the canonical production Kubernetes manifest specifications for deploying API Gateway, Survey Ingestion, AI Inference, and Database Proxy pods within the AI Rural Root Cause Discovery System.

---

# Production Kubernetes Deployment Manifest (API Gateway)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: csp-api-gateway
  namespace: production
  labels:
    app.kubernetes.io/name: csp-api-gateway
    app.kubernetes.io/part-of: rural-root-cause-system
spec:
  replicas: 4
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 25%
      maxUnavailable: 0
  selector:
    matchLabels:
      app: csp-api-gateway
  template:
    metadata:
      labels:
        app: csp-api-gateway
    spec:
      containers:
        - name: api-gateway
          image: 123456789012.dkr.ecr.us-east-1.amazonaws.com/csp-api:v1.0.0
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
              name: http
          resources:
            requests:
              cpu: "500m"
              memory: "1Gi"
            limits:
              cpu: "2000m"
              memory: "4Gi"
          livenessProbe:
            httpGet:
              path: /healthz
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /readyz
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
          securityContext:
            allowPrivilegeEscalation: false
            readOnlyRootFilesystem: true
            runAsNonRoot: true
            runAsUser: 10001
```

---

# Horizontal Pod Autoscaler (HPA) Specification

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: csp-api-gateway-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: csp-api-gateway
  minReplicas: 4
  maxReplicas: 40
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

---

# Approval

| Role | Name | Date |
|------|------|------|
| Lead DevOps Architect | Samantha Chen | 2026-07-28 |
| Infrastructure Lead | Robert Sterling | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of K8s Deployment Spec | DevOps Team |

---

# End of Document
