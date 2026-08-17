/**
 * Represents a strength exercise with sets, repetitions, and weight.
 */
public class StrengthEntry extends ExerciseEntry {
    private final int sets;
    private final int reps;
    private final double weightKg;

    /**
     * Creates a strength exercise entry.
     *
     * @param name the exercise name
     * @param sets the number of sets performed
     * @param reps the number of repetitions in each set
     * @param weightKg the weight used in kilograms
     */
    public StrengthEntry(String name, int sets, int reps, double weightKg) {
        super(name);
        this.sets = sets;
        this.reps = reps;
        this.weightKg = weightKg;
    }

    @Override
    public String getTypeLabel() {
        return "Strength";
    }

    @Override
    public String getDetails() {
        return sets + " sets x " + reps + " reps @ " + formatNumber(weightKg) + "kg";
    }

    /**
     * Formats a measurement without an unnecessary decimal fraction.
     *
     * @param value the measurement to format
     * @return the formatted measurement
     */
    private String formatNumber(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
