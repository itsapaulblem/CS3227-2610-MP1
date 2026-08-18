package fitlog;

import java.time.LocalDateTime;

/**
 * Represents a cardio exercise with duration and an optional distance.
 */
public final class CardioEntry extends ExerciseEntry {
    private final int durationMinutes;
    private final Double distanceKm;

    /**
     * Creates a cardio exercise entry.
     *
     * @param name the exercise name
     * @param durationMinutes the exercise duration in minutes
     * @param distanceKm the distance in kilometres, or {@code null} when not recorded
     * @throws IllegalArgumentException if any exercise value is invalid
     */
    public CardioEntry(String name, int durationMinutes, Double distanceKm) {
        this(name, durationMinutes, distanceKm, LocalDateTime.now());
    }

    /**
     * Creates a cardio exercise entry with a specified logging time.
     *
     * @param name the exercise name
     * @param durationMinutes the exercise duration in minutes
     * @param distanceKm the distance in kilometres, or {@code null} when not recorded
     * @param loggedAt the local logging time, or {@code null} for a legacy entry
     * @throws IllegalArgumentException if the name is blank, the duration is not
     *                                  positive, or a supplied distance is not finite and positive
     */
    public CardioEntry(String name, int durationMinutes, Double distanceKm, LocalDateTime loggedAt) {
        super(name, loggedAt);
        if (!ExerciseValueValidator.isPositiveWholeNumber(durationMinutes)) {
            throw new IllegalArgumentException("Duration must be greater than zero.");
        }
        if (distanceKm != null && !ExerciseValueValidator.isFinitePositiveNumber(distanceKm)) {
            throw new IllegalArgumentException("Distance must be finite and greater than zero.");
        }
        this.durationMinutes = durationMinutes;
        this.distanceKm = distanceKm;
    }

    @Override
    public String getTypeLabel() {
        return "Cardio";
    }

    /**
     * Returns the exercise duration in minutes.
     *
     * @return the duration in minutes
     */
    public int getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * Returns the distance in kilometres, if it was recorded.
     *
     * @return the distance in kilometres, or {@code null} when not recorded
     */
    public Double getDistanceKm() {
        return distanceKm;
    }

    @Override
    public String getDetails() {
        String details = durationMinutes + " min";
        return distanceKm == null ? details : details + ", " + formatNumber(distanceKm) + "km";
    }

    @Override
    public double getPrMetric() {
        return durationMinutes;
    }

    @Override
    public String getPrDescription() {
        return "Longest " + getName() + ": " + durationMinutes + " min";
    }

}
