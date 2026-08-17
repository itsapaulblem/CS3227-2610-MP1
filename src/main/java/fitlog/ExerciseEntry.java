package fitlog;

/**
 * Represents one exercise logged in the current workout session.
 */
public abstract class ExerciseEntry {
    private final String name;

    /**
     * Creates an entry for an exercise with the specified name.
     *
     * @param name the exercise name supplied by the user
     */
    public ExerciseEntry(String name) {
        this.name = name;
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
    protected String formatNumber(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
