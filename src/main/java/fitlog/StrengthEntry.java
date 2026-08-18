package fitlog;

import java.time.LocalDateTime;

/**
 * Represents a strength exercise with sets, repetitions, and weight.
 */
public final class StrengthEntry extends ExerciseEntry {
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
        this(name, sets, reps, weightKg, LocalDateTime.now());
    }

    /**
     * Creates a strength exercise entry with a specified logging time.
     *
     * @param name the exercise name
     * @param sets the number of sets performed
     * @param reps the number of repetitions in each set
     * @param weightKg the weight used in kilograms
     * @param loggedAt the local logging time, or {@code null} for a legacy entry
     */
    public StrengthEntry(String name, int sets, int reps, double weightKg, LocalDateTime loggedAt) {
        super(name, loggedAt);
        this.sets = sets;
        this.reps = reps;
        this.weightKg = weightKg;
    }

    @Override
    public String getTypeLabel() {
        return "Strength";
    }

    /**
     * Returns the number of sets performed.
     *
     * @return the number of sets
     */
    public int getSets() {
        return sets;
    }

    /**
     * Returns the number of repetitions performed in each set.
     *
     * @return the number of repetitions
     */
    public int getReps() {
        return reps;
    }

    /**
     * Returns the weight used in kilograms.
     *
     * @return the weight in kilograms
     */
    public double getWeightKg() {
        return weightKg;
    }

    @Override
    public String getDetails() {
        return sets + " sets x " + reps + " reps @ " + formatNumber(weightKg) + "kg";
    }

    @Override
    public double getPrMetric() {
        return weightKg;
    }

    @Override
    public String getPrDescription() {
        return "Heaviest " + getName() + ": " + formatNumber(weightKg) + "kg";
    }

}
