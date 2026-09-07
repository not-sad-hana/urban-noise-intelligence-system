-- =============================================================================
-- URBAN POLLUTION MONITORING MVP - DATABASE SCHEMA
-- SCOPE: Ramapuram, Chennai, India
-- Bounding Box: Latitude 13.010 - 13.050, Longitude 80.165 - 80.200
-- Database: PostgreSQL (urban_pollution_db)
-- =============================================================================

-- Drop tables if exists (in reverse FK dependency order)
DROP TABLE IF EXISTS suggestions CASCADE;
DROP TABLE IF EXISTS readings CASCADE;
DROP TABLE IF EXISTS pollution_type CASCADE;
DROP TABLE IF EXISTS locations CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- -----------------------------------------------------------------------------
-- 1. Users Table (Role-based access: citizen vs authority)
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CITIZEN' CHECK (role IN ('CITIZEN', 'AUTHORITY')),
    full_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- 2. Locations Table (Strictly clamped to Ramapuram bounding box)
-- -----------------------------------------------------------------------------
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    lat DECIMAL(9, 6) NOT NULL,
    lng DECIMAL(9, 6) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_ramapuram_lat CHECK (lat >= 13.010000 AND lat <= 13.050000),
    CONSTRAINT chk_ramapuram_lng CHECK (lng >= 80.165000 AND lng <= 80.200000)
);

-- Compound index on coordinates for rapid geo-queries
CREATE INDEX idx_locations_lat_lng ON locations (lat, lng);

-- -----------------------------------------------------------------------------
-- 3. PollutionType Table (Extensible: noise & air now, water/soil/light later)
-- -----------------------------------------------------------------------------
CREATE TABLE pollution_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    unit VARCHAR(20) NOT NULL,
    safe_threshold DOUBLE PRECISION NOT NULL,
    hazard_threshold DOUBLE PRECISION NOT NULL,
    description VARCHAR(255)
);

-- -----------------------------------------------------------------------------
-- 4. Readings Table (Time-stamped pollutant observations)
-- -----------------------------------------------------------------------------
CREATE TABLE readings (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT NOT NULL,
    type_id BIGINT NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    reading_date DATE NOT NULL,
    reading_time TIME NOT NULL,
    weather VARCHAR(50) DEFAULT 'Clear',
    source VARCHAR(50) DEFAULT 'SIMULATED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE,
    FOREIGN KEY (type_id) REFERENCES pollution_type(id) ON DELETE CASCADE
);

CREATE INDEX idx_readings_loc_type_date ON readings (location_id, type_id, reading_date, reading_time);

-- -----------------------------------------------------------------------------
-- 5. Suggestions Table (Threshold rule-based lookup for health & environmental action)
-- -----------------------------------------------------------------------------
CREATE TABLE suggestions (
    id BIGSERIAL PRIMARY KEY,
    type_id BIGINT NOT NULL,
    threshold_range VARCHAR(50) NOT NULL CHECK (threshold_range IN ('SAFE', 'MODERATE', 'HAZARD')),
    min_value DOUBLE PRECISION NOT NULL,
    max_value DOUBLE PRECISION NOT NULL,
    recommendation_text TEXT NOT NULL,
    FOREIGN KEY (type_id) REFERENCES pollution_type(id) ON DELETE CASCADE
);
