package com.mycompany.pengelolaanproject.core;

/**
 * Enumeration representing roles in the project management system.
 */
public enum UserRole {
    PM,
    DEV,
    UIUX;

    /**
     * Parse role from String value. Safe parsing fallback to null or throw exception.
     */
    public static UserRole fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return UserRole.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
