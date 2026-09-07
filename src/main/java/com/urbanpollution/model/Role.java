package com.urbanpollution.model;

/**
 * User roles for Access Control.
 * CITIZEN: View-only access to dashboard, map, charts, readings, and suggestions.
 * AUTHORITY: Full CRUD access to locations, readings, pollution types, and configuration.
 */
public enum Role {
    CITIZEN,
    AUTHORITY
}
