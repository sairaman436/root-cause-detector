-- Purpose: Creates the AI-6 enterprise model optimization and packaging schema.
-- Why it exists: Production-ready models need optimized artifacts, deployment bundles, compatibility reports, signatures, benchmarks, and release candidates after evaluation gates.
-- Architecture fit: Adds the governed optimization platform without retraining, evaluation, or deployment execution.

CREATE SCHEMA IF NOT EXISTS optimization;

CREATE TABLE optimization.optimization_runs (
    id UUID PRIMARY KEY,
    evaluation_run_id UUID NOT NULL,
    model_run_id UUID NOT NULL,
    model_name VARCHAR(180) NOT NULL,
    model_family VARCHAR(120) NOT NULL,
    status VARCHAR(60) NOT NULL,
    release_recommendation VARCHAR(80) NOT NULL,
    immutable_hash VARCHAR(128) NOT NULL,
    requested_formats_json TEXT NOT NULL,
    requested_targets_json TEXT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_optimization_runs_evaluation FOREIGN KEY (evaluation_run_id) REFERENCES evaluation.evaluation_runs(id)
);

CREATE TABLE optimization.optimization_profiles (
    id UUID PRIMARY KEY,
    profile_key VARCHAR(120) NOT NULL UNIQUE,
    export_format VARCHAR(80) NOT NULL,
    quantization_mode VARCHAR(80) NOT NULL,
    precision_mode VARCHAR(80) NOT NULL,
    target_runtime VARCHAR(120) NOT NULL,
    status VARCHAR(60) NOT NULL,
    parameters_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE optimization.hardware_profiles (
    id UUID PRIMARY KEY,
    hardware_key VARCHAR(120) NOT NULL UNIQUE,
    hardware_class VARCHAR(80) NOT NULL,
    min_ram_gb INTEGER NOT NULL,
    min_vram_gb INTEGER NOT NULL,
    min_cpu_cores INTEGER NOT NULL,
    accelerator VARCHAR(80) NOT NULL,
    status VARCHAR(60) NOT NULL,
    notes_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE optimization.optimization_artifacts (
    id UUID PRIMARY KEY,
    optimization_run_id UUID NOT NULL,
    profile_id UUID NOT NULL,
    artifact_name VARCHAR(240) NOT NULL,
    export_format VARCHAR(80) NOT NULL,
    quantization_mode VARCHAR(80) NOT NULL,
    precision_mode VARCHAR(80) NOT NULL,
    storage_uri VARCHAR(500) NOT NULL,
    artifact_size_bytes BIGINT NOT NULL,
    checksum_sha256 VARCHAR(128) NOT NULL,
    validation_status VARCHAR(60) NOT NULL,
    validation_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_optimization_artifacts_run FOREIGN KEY (optimization_run_id) REFERENCES optimization.optimization_runs(id),
    CONSTRAINT fk_optimization_artifacts_profile FOREIGN KEY (profile_id) REFERENCES optimization.optimization_profiles(id)
);

CREATE TABLE optimization.deployment_packages (
    id UUID PRIMARY KEY,
    optimization_run_id UUID NOT NULL,
    package_type VARCHAR(120) NOT NULL,
    target_environment VARCHAR(120) NOT NULL,
    package_uri VARCHAR(500) NOT NULL,
    manifest_type VARCHAR(120) NOT NULL,
    checksum_sha256 VARCHAR(128) NOT NULL,
    status VARCHAR(60) NOT NULL,
    manifest_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_deployment_packages_run FOREIGN KEY (optimization_run_id) REFERENCES optimization.optimization_runs(id)
);

CREATE TABLE optimization.compatibility_reports (
    id UUID PRIMARY KEY,
    optimization_run_id UUID NOT NULL,
    artifact_id UUID NOT NULL,
    hardware_profile_id UUID NOT NULL,
    runtime_target VARCHAR(120) NOT NULL,
    compatibility_status VARCHAR(60) NOT NULL,
    compatibility_matrix_json TEXT NOT NULL,
    failure_details_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_compatibility_reports_run FOREIGN KEY (optimization_run_id) REFERENCES optimization.optimization_runs(id),
    CONSTRAINT fk_compatibility_reports_artifact FOREIGN KEY (artifact_id) REFERENCES optimization.optimization_artifacts(id),
    CONSTRAINT fk_compatibility_reports_hardware FOREIGN KEY (hardware_profile_id) REFERENCES optimization.hardware_profiles(id)
);

CREATE TABLE optimization.performance_benchmarks (
    id UUID PRIMARY KEY,
    optimization_run_id UUID NOT NULL,
    artifact_id UUID NOT NULL,
    first_token_latency_ms NUMERIC(12, 2) NOT NULL,
    tokens_per_second NUMERIC(12, 2) NOT NULL,
    peak_memory_gb NUMERIC(8, 2) NOT NULL,
    average_memory_gb NUMERIC(8, 2) NOT NULL,
    gpu_utilization_percent NUMERIC(5, 2) NOT NULL,
    cpu_utilization_percent NUMERIC(5, 2) NOT NULL,
    cold_start_ms NUMERIC(12, 2) NOT NULL,
    warm_start_ms NUMERIC(12, 2) NOT NULL,
    concurrent_requests INTEGER NOT NULL,
    status VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_performance_benchmarks_run FOREIGN KEY (optimization_run_id) REFERENCES optimization.optimization_runs(id),
    CONSTRAINT fk_performance_benchmarks_artifact FOREIGN KEY (artifact_id) REFERENCES optimization.optimization_artifacts(id)
);

CREATE TABLE optimization.artifact_signatures (
    id UUID PRIMARY KEY,
    optimization_run_id UUID NOT NULL,
    artifact_id UUID NOT NULL,
    checksum_sha256 VARCHAR(128) NOT NULL,
    signature_algorithm VARCHAR(120) NOT NULL,
    signature_value VARCHAR(256) NOT NULL,
    signer VARCHAR(180) NOT NULL,
    integrity_status VARCHAR(60) NOT NULL,
    license_status VARCHAR(60) NOT NULL,
    tamper_evidence_json TEXT NOT NULL,
    signed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_artifact_signatures_run FOREIGN KEY (optimization_run_id) REFERENCES optimization.optimization_runs(id),
    CONSTRAINT fk_artifact_signatures_artifact FOREIGN KEY (artifact_id) REFERENCES optimization.optimization_artifacts(id)
);

CREATE TABLE optimization.release_candidates (
    id UUID PRIMARY KEY,
    optimization_run_id UUID NOT NULL,
    candidate_version VARCHAR(120) NOT NULL,
    status VARCHAR(80) NOT NULL,
    promoted_by VARCHAR(180),
    review_json TEXT NOT NULL,
    release_notes TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    promoted_at TIMESTAMP,
    CONSTRAINT fk_release_candidates_run FOREIGN KEY (optimization_run_id) REFERENCES optimization.optimization_runs(id)
);

CREATE INDEX idx_optimization_runs_evaluation_status ON optimization.optimization_runs(evaluation_run_id, status, release_recommendation);
CREATE INDEX idx_optimization_artifacts_run_format ON optimization.optimization_artifacts(optimization_run_id, export_format, validation_status);
CREATE INDEX idx_deployment_packages_run_target ON optimization.deployment_packages(optimization_run_id, target_environment, status);
CREATE INDEX idx_compatibility_reports_artifact_runtime ON optimization.compatibility_reports(artifact_id, runtime_target, compatibility_status);
CREATE INDEX idx_performance_benchmarks_run_status ON optimization.performance_benchmarks(optimization_run_id, status);
CREATE INDEX idx_artifact_signatures_artifact_integrity ON optimization.artifact_signatures(artifact_id, integrity_status, license_status);
CREATE INDEX idx_release_candidates_run_status ON optimization.release_candidates(optimization_run_id, status);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000001701', 'OPTIMIZATION_READ', 'OPTIMIZATION', 'READ', 'Read optimization jobs, artifacts, benchmarks, packages, compatibility, and release reports', NOW(), NOW()),
('00000000-0000-0000-0000-000000001702', 'OPTIMIZATION_RUN', 'OPTIMIZATION', 'RUN', 'Start model optimization and packaging after evaluation gates', NOW(), NOW()),
('00000000-0000-0000-0000-000000001703', 'OPTIMIZATION_PROMOTE', 'OPTIMIZATION', 'PROMOTE', 'Record optimized artifact release promotion without deployment', NOW(), NOW()),
('00000000-0000-0000-0000-000000001704', 'MODEL_PACKAGE_REVIEW', 'OPTIMIZATION_PACKAGE', 'REVIEW', 'Review optimized model deployment packages', NOW(), NOW()),
('00000000-0000-0000-0000-000000001705', 'PERFORMANCE_REVIEW', 'OPTIMIZATION_PERFORMANCE', 'REVIEW', 'Review performance benchmark evidence', NOW(), NOW()),
('00000000-0000-0000-0000-000000001706', 'MODEL_SECURITY_REVIEW', 'OPTIMIZATION_SECURITY', 'REVIEW', 'Review artifact signing, integrity, tamper, and license validation', NOW(), NOW()),
('00000000-0000-0000-0000-000000001707', 'AI_RELEASE_REVIEW', 'OPTIMIZATION_RELEASE', 'REVIEW', 'Review optimized artifact release candidate decisions', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('OPTIMIZATION_READ', 'OPTIMIZATION_RUN', 'OPTIMIZATION_PROMOTE', 'MODEL_PACKAGE_REVIEW', 'PERFORMANCE_REVIEW', 'MODEL_SECURITY_REVIEW', 'AI_RELEASE_REVIEW');

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000205', id
FROM identity.permissions
WHERE name IN ('OPTIMIZATION_READ', 'MODEL_PACKAGE_REVIEW', 'PERFORMANCE_REVIEW', 'MODEL_SECURITY_REVIEW');

INSERT INTO optimization.optimization_profiles (id, profile_key, export_format, quantization_mode, precision_mode, target_runtime, status, parameters_json, created_at)
VALUES
('00000000-0000-0000-0000-000000001751', 'GGUF', 'GGUF', 'NONE', 'FP16_COMPATIBLE', 'LLAMA_CPP', 'ACTIVE', '{"exporter":"llama.cpp","version":"v1"}', NOW()),
('00000000-0000-0000-0000-000000001752', 'SAFETENSORS', 'SAFETENSORS', 'NONE', 'FP16_COMPATIBLE', 'ENTERPRISE_SERVER', 'ACTIVE', '{"format":"safetensors","version":"v1"}', NOW()),
('00000000-0000-0000-0000-000000001753', 'ONNX', 'ONNX', 'STATIC', 'FP16_COMPATIBLE', 'ONNX_RUNTIME', 'ACTIVE', '{"opset":"enterprise-default"}', NOW()),
('00000000-0000-0000-0000-000000001754', 'VLLM', 'VLLM', 'NONE', 'MIXED_PRECISION', 'VLLM', 'ACTIVE', '{"runtime":"vllm"}', NOW()),
('00000000-0000-0000-0000-000000001755', 'TENSORRT_LLM', 'TENSORRT_LLM', 'NONE', 'MIXED_PRECISION', 'TENSORRT_LLM', 'ACTIVE', '{"runtime":"tensorrt-llm-ready"}', NOW()),
('00000000-0000-0000-0000-000000001756', 'OLLAMA_MANIFEST', 'OLLAMA_MANIFEST', 'NONE', 'FP16_COMPATIBLE', 'OLLAMA', 'ACTIVE', '{"manifest":"Modelfile"}', NOW()),
('00000000-0000-0000-0000-000000001757', 'QUANT_4BIT', 'GGUF', '4BIT', 'FP16_COMPATIBLE', 'LLAMA_CPP', 'ACTIVE', '{"bits":4}', NOW()),
('00000000-0000-0000-0000-000000001758', 'QUANT_5BIT', 'GGUF', '5BIT', 'FP16_COMPATIBLE', 'LLAMA_CPP', 'ACTIVE', '{"bits":5}', NOW()),
('00000000-0000-0000-0000-000000001759', 'QUANT_8BIT', 'GGUF', '8BIT', 'FP16_COMPATIBLE', 'LLAMA_CPP', 'ACTIVE', '{"bits":8}', NOW());

INSERT INTO optimization.hardware_profiles (id, hardware_key, hardware_class, min_ram_gb, min_vram_gb, min_cpu_cores, accelerator, status, notes_json, created_at)
VALUES
('00000000-0000-0000-0000-000000001771', 'cloud-gpu-24gb', 'CLOUD_GPU', 64, 24, 16, 'CUDA', 'ACTIVE', '{"recommendedFor":["vLLM","TensorRT-LLM","Kubernetes"]}', NOW()),
('00000000-0000-0000-0000-000000001772', 'local-gpu-12gb', 'LOCAL_GPU', 32, 12, 8, 'CUDA_OR_ROCM', 'ACTIVE', '{"recommendedFor":["developer-workstation"]}', NOW()),
('00000000-0000-0000-0000-000000001773', 'edge-cpu-16gb', 'EDGE_DEVICE', 16, 0, 8, 'NONE', 'ACTIVE', '{"recommendedFor":["GGUF","llama.cpp","air-gapped"]}', NOW());
