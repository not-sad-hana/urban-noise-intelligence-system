-- =============================================================================
-- URBAN POLLUTION MONITORING MVP - SEED DATA
-- SCOPE: Ramapuram, Chennai (Lat: 13.010 to 13.050, Lng: 80.165 to 80.200)
-- Database: PostgreSQL (urban_pollution_db)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Initial Users
-- Password for 'citizen': password123 (BCrypt hash)
-- Password for 'authority': admin123 (BCrypt hash)
-- -----------------------------------------------------------------------------
INSERT INTO users (id, username, password_hash, role, full_name) VALUES
(1, 'citizen', '$2a$10$wK1Ww6a4h4YF9Q51m3K2eeT/p04i3Fm1U8n4kX85fO0k1w3n2x9Zy', 'CITIZEN', 'Ramapuram Resident'),
(2, 'authority', '$2a$10$y58yq0sJkW0YJ4ZgL5kQeeE1wH3c3u3j3F6n0m1k2l3p4q5r6s7Tu', 'AUTHORITY', 'TNPCB Ward Officer')
ON CONFLICT (id) DO UPDATE SET username = EXCLUDED.username;

-- -----------------------------------------------------------------------------
-- 2. Pollution Types (Noise & Air Quality implemented; generic for future extension)
-- -----------------------------------------------------------------------------
INSERT INTO pollution_type (id, name, unit, safe_threshold, hazard_threshold, description) VALUES
(1, 'NOISE', 'dB', 55.0, 75.0, 'Ambient sound pressure level in decibels (CPCB Residential Day: 55dB, Industrial/Hazard: 75dB)'),
(2, 'AIR_QUALITY', 'AQI', 100.0, 250.0, 'National Air Quality Index (Good/Satisfactory <= 100, Poor/Hazardous >= 250)')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

-- -----------------------------------------------------------------------------
-- 3. Initial Locations (Clamped inside Ramapuram bounding box)
-- Bounding box: lat 13.010-13.050, lng 80.165-80.200
-- -----------------------------------------------------------------------------
INSERT INTO locations (id, name, lat, lng, description) VALUES
(1, 'SRM IST Campus Gate', 13.032500, 80.181200, 'Bharathi Salai, Ramapuram educational zone'),
(2, 'MIOT Hospital Junction', 13.024500, 80.186500, 'Mount-Poonamallee Road & Ramapuram access junction'),
(3, 'Rayala Nagar Bus Terminal', 13.036000, 80.178000, 'Major public transit and residential hub'),
(4, 'DLF Cybercity South Gate', 13.038500, 80.188000, 'Commercial IT corridor border with peak vehicular density'),
(5, 'Arignar Anna Main Road Bazaar', 13.031000, 80.175000, 'Dense commercial high-street with heavy market traffic'),
(6, 'Ramapuram Lake Park', 13.028000, 80.172000, 'Residential eco-buffer zone')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

-- -----------------------------------------------------------------------------
-- 4. Suggestions Table (Rule-based actionable recommendations)
-- -----------------------------------------------------------------------------
INSERT INTO suggestions (id, type_id, threshold_range, min_value, max_value, recommendation_text) VALUES
-- Noise Suggestions (Type ID 1)
(1, 1, 'SAFE', 0.0, 55.0, 'Ambient noise is well within permissible CPCB limits. Suitable for outdoor recreational and residential rest.'),
(2, 1, 'MODERATE', 55.1, 75.0, 'Elevated noise levels observed. Recommend deploying acoustic banners and monitoring peak delivery vehicle traffic.'),
(3, 1, 'HAZARD', 75.1, 140.0, 'CRITICAL NOISE HAZARD! Enforce immediate ''No Honking'' compliance, inspect nearby construction diesel generators, and redirect heavy transport vehicles away from residential lanes.'),

-- Air Quality Suggestions (Type ID 2)
(4, 2, 'SAFE', 0.0, 100.0, 'Air quality index is satisfactory. Ideal conditions for outdoor activities and natural ventilation.'),
(5, 2, 'MODERATE', 100.1, 250.0, 'Moderate pollution level. Sensitive groups (children and elderly) should reduce prolonged heavy outdoor exertion.'),
(6, 2, 'HAZARD', 250.1, 500.0, 'SEVERE AQI HAZARD! Deploy mobile water misting guns along Mount-Poonamallee and Anna Salai, temporarily halt open dust-generating construction, and advise residents to wear N95 filtration masks.')
ON CONFLICT (id) DO UPDATE SET recommendation_text = EXCLUDED.recommendation_text;

-- Advance sequences if BIGSERIAL was used
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM users));
SELECT setval('pollution_type_id_seq', (SELECT COALESCE(MAX(id), 1) FROM pollution_type));
SELECT setval('locations_id_seq', (SELECT COALESCE(MAX(id), 1) FROM locations));
SELECT setval('suggestions_id_seq', (SELECT COALESCE(MAX(id), 1) FROM suggestions));
