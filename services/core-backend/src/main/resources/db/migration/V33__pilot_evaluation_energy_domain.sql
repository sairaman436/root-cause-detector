-- Purpose: Extends the governed pilot domain vocabulary for dataset v0.5 diversity coverage.
-- Why it exists: Energy and electricity scenarios require an explicit domain value rather than
-- being hidden under a generic infrastructure label.
-- Architecture fit: Keeps the evaluation bounded context aligned with the approved rural
-- domain taxonomy without changing existing rows or review decisions.

ALTER TABLE evaluation.pilot_scenarios
    DROP CONSTRAINT IF EXISTS ck_pilot_scenario_domain;

ALTER TABLE evaluation.pilot_scenarios
    ADD CONSTRAINT ck_pilot_scenario_domain CHECK (domain IN (
        'WATER', 'AGRICULTURE', 'EDUCATION', 'EMPLOYMENT',
        'HEALTHCARE', 'SANITATION', 'ENERGY', 'INFRASTRUCTURE', 'LIVELIHOOD',
        'MULTI_DOMAIN', 'ADVERSARIAL'
    ));
