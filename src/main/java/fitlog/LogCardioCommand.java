package fitlog;

/**
 * Represents a validated request to log a cardio exercise.
 *
 * @param name the exercise name
 * @param durationMinutes the duration in minutes
 * @param distanceKm the optional distance in kilometers
 */
public record LogCardioCommand(String name, int durationMinutes, Double distanceKm) implements Command {
}
