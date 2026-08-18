package fitlog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents one strength or cardio exercise logged in FitLog.
 * The hierarchy is sealed because these are the only exercise categories in the
 * FitLog domain.
 */
public abstract sealed class ExerciseEntry permits StrengthEntry, CardioEntry {
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final String name;
    private final LocalDateTime loggedAt;

    /**
     * Creates an entry for an exercise with the specified name.
     *
     * @param name the exercise name supplied by the user
     * @param loggedAt the local time at which the entry was logged, or {@code null}
     *                 for a legacy entry whose saved time is unknown
     * @throws IllegalArgumentException if the exercise name is {@code null} or blank
     */
    public ExerciseEntry(String name, LocalDateTime loggedAt) {
        if (!ExerciseValueValidator.isValidName(name)) {
            throw new IllegalArgumentException("Exercise name must not be blank.");
        }
        this.name = name;
        this.loggedAt = loggedAt;
    }

    /**
     * Returns the name of the logged exercise.
     *
     * @return the exercise name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the local time at which this entry was logged.
     *
     * @return the logging time, or {@code null} for a legacy entry without a saved time
     */
    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    /**
     * Returns the logging time in a compact display format.
     *
     * @return the formatted logging time, or {@code "time not recorded"} for a legacy entry
     */
    public String getLoggedAtDisplay() {
        return loggedAt == null ? "time not recorded" : DISPLAY_TIME_FORMAT.format(loggedAt);
    }

    /**
     * Returns the label identifying this entry's exercise category.
     *
     * @return the exercise category label
     */
    public abstract String getTypeLabel();

    /**
     * Returns the entry's type-specific details for display after its name.
     *
     * @return the formatted exercise details
     */
    public abstract String getDetails();

    /**
     * Returns the numeric value used to compare entries for personal records.
     *
     * @return the personal-record comparison value
     */
    public abstract double getPrMetric();

    /**
     * Returns the type-specific text used in a personal-record notification.
     *
     * @return the personal-record notification text after its prefix
     */
    public abstract String getPrDescription();

    /**
     * Formats a measurement without an unnecessary decimal fraction.
     *
     * @param value the measurement to format
     * @return the formatted measurement
     */
    protected static String formatNumber(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
