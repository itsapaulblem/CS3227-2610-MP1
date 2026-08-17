/**
 * Represents a cardio exercise with duration and an optional distance.
 */
public class CardioEntry extends ExerciseEntry {
    private final int durationMinutes;
    private final Double distanceKm;

    /**
     * Creates a cardio exercise entry.
     *
     * @param name the exercise name
     * @param durationMinutes the exercise duration in minutes
     * @param distanceKm the distance in kilometres, or {@code null} when not recorded
     */
    public CardioEntry(String name, int durationMinutes, Double distanceKm) {
        super(name);
        this.durationMinutes = durationMinutes;
        this.distanceKm = distanceKm;
    }

    @Override
    public String getTypeLabel() {
        return "Cardio";
    }

    @Override
    public String getDetails() {
        String details = durationMinutes + " min";
        return distanceKm == null ? details : details + ", " + formatNumber(distanceKm) + "km";
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
