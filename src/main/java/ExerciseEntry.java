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
}
