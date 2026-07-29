-- Purpose: Adds Milestone 6 geography compatibility primitives and permission aliases.
-- Why it exists: Geography workflows need normalized GeoLocation, GeoZone, GeoRegion, GeoAudit, WKT, GeoJSON, and compatibility permissions.
-- Architecture fit: Extends the approved geospatial schema without redesigning the existing hierarchy, survey, identity, or evidence modules.

CREATE TABLE geospatial.geo_locations (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID NOT NULL,
    coordinate_system VARCHAR(40) NOT NULL,
    shape_type VARCHAR(40) NOT NULL,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    elevation_meters DECIMAL(10,2),
    geojson TEXT,
    wkt TEXT,
    min_latitude DECIMAL(10,7),
    min_longitude DECIMAL(10,7),
    max_latitude DECIMAL(10,7),
    max_longitude DECIMAL(10,7),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_geo_locations_entity UNIQUE (entity_type, entity_id),
    CONSTRAINT ck_geo_locations_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT ck_geo_locations_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180)),
    CONSTRAINT ck_geo_locations_bbox CHECK (
        min_latitude IS NULL OR max_latitude IS NULL OR min_longitude IS NULL OR max_longitude IS NULL
        OR (min_latitude <= max_latitude AND min_longitude <= max_longitude)
    )
);

CREATE TABLE geospatial.geo_zones (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(220) NOT NULL,
    description VARCHAR(1000),
    coordinate_system VARCHAR(40) NOT NULL,
    geojson TEXT,
    wkt TEXT,
    min_latitude DECIMAL(10,7),
    min_longitude DECIMAL(10,7),
    max_latitude DECIMAL(10,7),
    max_longitude DECIMAL(10,7),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_geo_zones_code UNIQUE (code)
);

CREATE TABLE geospatial.geo_regions (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(220) NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_geo_regions_code UNIQUE (code)
);

CREATE TABLE geospatial.geo_audit (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(60) NOT NULL,
    actor_user_id UUID REFERENCES identity.users(id),
    details VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_geo_locations_entity ON geospatial.geo_locations (entity_type, entity_id);
CREATE INDEX idx_geo_locations_point ON geospatial.geo_locations (latitude, longitude);
CREATE INDEX idx_geo_locations_bbox ON geospatial.geo_locations (min_latitude, min_longitude, max_latitude, max_longitude);
CREATE INDEX idx_geo_zones_bbox ON geospatial.geo_zones (min_latitude, min_longitude, max_latitude, max_longitude);
CREATE INDEX idx_geo_audit_entity ON geospatial.geo_audit (entity_type, entity_id, created_at);
CREATE INDEX idx_geo_audit_actor ON geospatial.geo_audit (actor_user_id, created_at);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000601', 'GEO_ADMIN', 'GEOGRAPHY', 'ADMIN', 'Administer geography hierarchy, locations, zones, and boundaries', NOW(), NOW()),
('00000000-0000-0000-0000-000000000602', 'GEO_EDITOR', 'GEOGRAPHY', 'EDIT', 'Create and edit geography hierarchy and spatial records', NOW(), NOW()),
('00000000-0000-0000-0000-000000000603', 'GEO_VIEWER', 'GEOGRAPHY', 'VIEW', 'View geography hierarchy and spatial records', NOW(), NOW()),
('00000000-0000-0000-0000-000000000604', 'INFRASTRUCTURE_MANAGE', 'GEOGRAPHY_INFRASTRUCTURE', 'MANAGE', 'Create and manage mapped infrastructure assets', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('GEO_ADMIN', 'GEO_EDITOR', 'GEO_VIEWER', 'INFRASTRUCTURE_MANAGE');

INSERT INTO identity.role_permissions (role_id, permission_id)
VALUES
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000603'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000603');
