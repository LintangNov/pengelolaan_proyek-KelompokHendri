package pengelolaanproject.model;

/**
 * Enumeration representing the current lifecycle status of a project.
 */
public enum ProjectStatus {
    AKTIF,
    SELESAI;

    /**
     * Helper to safely parse a status from a String value.
     * Maps null or unknown values to AKTIF by default.
     */
    public static ProjectStatus fromString(String value) {
        if (value == null) {
            return AKTIF;
        }
        try {
            return ProjectStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return AKTIF;
        }
    }
}
