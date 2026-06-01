package pengelolaanproject.model;

/**
 * Enumeration representing the current progress status of a task.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    REVIEW,
    DONE;

    /**
     * Helper to safely parse a status from a String value.
     * Maps null or unknown values to TODO by default, or returns null.
     */
    public static TaskStatus fromString(String value) {
        if (value == null) {
            return TODO;
        }
        try {
            return TaskStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return TODO;
        }
    }
}
