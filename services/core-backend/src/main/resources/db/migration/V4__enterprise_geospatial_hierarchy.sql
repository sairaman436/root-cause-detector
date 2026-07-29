-- Purpose: Creates the Enterprise Geospatial Intelligence and Administrative Hierarchy schema for Milestone 5.
-- Why it exists: Geography is the foundation for future AI, analytics, reporting, root-cause discovery, and simulation workflows.
-- Architecture fit: Adds administrative hierarchy, spatial metadata, infrastructure assets, and household mapping without Kafka, ML, RAG, agents, analytics, reporting, or simulation logic.

CREATE SCHEMA IF NOT EXISTS geospatial;

CREATE TABLE geospatial.countries (
    id UUID PRIMARY KEY,
    code VARCHAR(3) NOT NULL,
    name VARCHAR(180) NOT NULL,
    iso_code VARCHAR(3),
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_countries_code UNIQUE (code),
    CONSTRAINT ck_countries_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT ck_countries_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
);

CREATE TABLE geospatial.states (
    id UUID PRIMARY KEY,
    country_id UUID NOT NULL REFERENCES geospatial.countries(id),
    code VARCHAR(32) NOT NULL,
    name VARCHAR(180) NOT NULL,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_states_country_code UNIQUE (country_id, code),
    CONSTRAINT ck_states_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT ck_states_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
);

CREATE TABLE geospatial.districts (
    id UUID PRIMARY KEY,
    state_id UUID NOT NULL REFERENCES geospatial.states(id),
    code VARCHAR(32) NOT NULL,
    name VARCHAR(180) NOT NULL,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_districts_state_code UNIQUE (state_id, code),
    CONSTRAINT ck_districts_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT ck_districts_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
);

CREATE TABLE geospatial.mandals (
    id UUID PRIMARY KEY,
    district_id UUID NOT NULL REFERENCES geospatial.districts(id),
    code VARCHAR(32) NOT NULL,
    name VARCHAR(180) NOT NULL,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_mandals_district_code UNIQUE (district_id, code),
    CONSTRAINT ck_mandals_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT ck_mandals_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
);

CREATE TABLE geospatial.blocks (
    id UUID PRIMARY KEY,
    mandal_id UUID NOT NULL REFERENCES geospatial.mandals(id),
    code VARCHAR(32) NOT NULL,
    name VARCHAR(180) NOT NULL,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_blocks_mandal_code UNIQUE (mandal_id, code),
    CONSTRAINT ck_blocks_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT ck_blocks_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
);

CREATE TABLE geospatial.gram_panchayats (
    id UUID PRIMARY KEY,
    block_id UUID NOT NULL REFERENCES geospatial.blocks(id),
    code VARCHAR(32) NOT NULL,
    name VARCHAR(180) NOT NULL,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_gram_panchayats_block_code UNIQUE (block_id, code),
    CONSTRAINT ck_gp_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT ck_gp_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
);

CREATE TABLE geospatial.villages (
    id UUID PRIMARY KEY,
    gram_panchayat_id UUID NOT NULL REFERENCES geospatial.gram_panchayats(id),
    code VARCHAR(32) NOT NULL,
    name VARCHAR(180) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    elevation_meters DECIMAL(10,2),
    area_sq_km DECIMAL(12,4),
    population BIGINT,
    household_count BIGINT,
    geojson TEXT,
    min_latitude DECIMAL(10,7),
    min_longitude DECIMAL(10,7),
    max_latitude DECIMAL(10,7),
    max_longitude DECIMAL(10,7),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_villages_gp_code UNIQUE (gram_panchayat_id, code),
    CONSTRAINT ck_villages_latitude CHECK (latitude >= -90 AND latitude <= 90),
    CONSTRAINT ck_villages_longitude CHECK (longitude >= -180 AND longitude <= 180)
);

CREATE TABLE geospatial.wards (
    id UUID PRIMARY KEY,
    village_id UUID NOT NULL REFERENCES geospatial.villages(id),
    code VARCHAR(32) NOT NULL,
    name VARCHAR(180) NOT NULL,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_wards_village_code UNIQUE (village_id, code),
    CONSTRAINT ck_wards_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT ck_wards_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
);

CREATE TABLE geospatial.hamlets (
    id UUID PRIMARY KEY,
    ward_id UUID NOT NULL REFERENCES geospatial.wards(id),
    code VARCHAR(32) NOT NULL,
    name VARCHAR(180) NOT NULL,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_hamlets_ward_code UNIQUE (ward_id, code),
    CONSTRAINT ck_hamlets_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT ck_hamlets_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
);

CREATE TABLE geospatial.households (
    id UUID PRIMARY KEY,
    hamlet_id UUID NOT NULL REFERENCES geospatial.hamlets(id),
    household_code VARCHAR(64) NOT NULL,
    head_of_household VARCHAR(180),
    address VARCHAR(500) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    survey_id UUID REFERENCES survey.surveys(id),
    evidence_id UUID REFERENCES evidence.evidence(id),
    iot_metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_households_hamlet_code UNIQUE (hamlet_id, household_code),
    CONSTRAINT ck_households_latitude CHECK (latitude >= -90 AND latitude <= 90),
    CONSTRAINT ck_households_longitude CHECK (longitude >= -180 AND longitude <= 180)
);

CREATE TABLE geospatial.geo_boundaries (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID NOT NULL,
    geojson TEXT NOT NULL,
    min_latitude DECIMAL(10,7) NOT NULL,
    min_longitude DECIMAL(10,7) NOT NULL,
    max_latitude DECIMAL(10,7) NOT NULL,
    max_longitude DECIMAL(10,7) NOT NULL,
    area_sq_km DECIMAL(12,4),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_geo_boundaries_entity UNIQUE (entity_type, entity_id),
    CONSTRAINT ck_boundaries_bbox CHECK (min_latitude <= max_latitude AND min_longitude <= max_longitude)
);

CREATE TABLE geospatial.infrastructure_assets (
    id UUID PRIMARY KEY,
    village_id UUID REFERENCES geospatial.villages(id),
    asset_type VARCHAR(60) NOT NULL,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(220) NOT NULL,
    description VARCHAR(1000),
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_infrastructure_assets_code UNIQUE (asset_type, code),
    CONSTRAINT ck_assets_latitude CHECK (latitude >= -90 AND latitude <= 90),
    CONSTRAINT ck_assets_longitude CHECK (longitude >= -180 AND longitude <= 180)
);

CREATE TABLE geospatial.administrative_hierarchy (
    id UUID PRIMARY KEY,
    country_id UUID REFERENCES geospatial.countries(id),
    state_id UUID REFERENCES geospatial.states(id),
    district_id UUID REFERENCES geospatial.districts(id),
    mandal_id UUID REFERENCES geospatial.mandals(id),
    block_id UUID REFERENCES geospatial.blocks(id),
    gram_panchayat_id UUID REFERENCES geospatial.gram_panchayats(id),
    village_id UUID REFERENCES geospatial.villages(id),
    ward_id UUID REFERENCES geospatial.wards(id),
    hamlet_id UUID REFERENCES geospatial.hamlets(id),
    household_id UUID REFERENCES geospatial.households(id),
    path_code VARCHAR(700) NOT NULL,
    path_name VARCHAR(1200) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_administrative_hierarchy_household UNIQUE (household_id),
    CONSTRAINT uq_administrative_hierarchy_path UNIQUE (path_code)
);

CREATE INDEX idx_states_country ON geospatial.states (country_id);
CREATE INDEX idx_districts_state ON geospatial.districts (state_id);
CREATE INDEX idx_mandals_district ON geospatial.mandals (district_id);
CREATE INDEX idx_blocks_mandal ON geospatial.blocks (mandal_id);
CREATE INDEX idx_gp_block ON geospatial.gram_panchayats (block_id);
CREATE INDEX idx_villages_gp ON geospatial.villages (gram_panchayat_id);
CREATE INDEX idx_villages_centroid ON geospatial.villages (latitude, longitude);
CREATE INDEX idx_villages_bbox ON geospatial.villages (min_latitude, min_longitude, max_latitude, max_longitude);
CREATE INDEX idx_wards_village ON geospatial.wards (village_id);
CREATE INDEX idx_hamlets_ward ON geospatial.hamlets (ward_id);
CREATE INDEX idx_households_hamlet ON geospatial.households (hamlet_id);
CREATE INDEX idx_households_location ON geospatial.households (latitude, longitude);
CREATE INDEX idx_boundaries_entity ON geospatial.geo_boundaries (entity_type, entity_id);
CREATE INDEX idx_boundaries_bbox ON geospatial.geo_boundaries (min_latitude, min_longitude, max_latitude, max_longitude);
CREATE INDEX idx_assets_village ON geospatial.infrastructure_assets (village_id);
CREATE INDEX idx_assets_type_location ON geospatial.infrastructure_assets (asset_type, latitude, longitude);
CREATE INDEX idx_assets_location ON geospatial.infrastructure_assets (latitude, longitude);
CREATE INDEX idx_hierarchy_path ON geospatial.administrative_hierarchy (path_code);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000501', 'GEO_READ', 'GEOSPATIAL', 'READ', 'View administrative hierarchy and geospatial records', NOW(), NOW()),
('00000000-0000-0000-0000-000000000502', 'GEO_MANAGE', 'GEOSPATIAL', 'MANAGE', 'Create and maintain administrative hierarchy and geospatial records', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('GEO_READ', 'GEO_MANAGE');

INSERT INTO identity.role_permissions (role_id, permission_id)
VALUES
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000501'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000501');
