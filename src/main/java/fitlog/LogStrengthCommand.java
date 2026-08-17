package fitlog;

/**
 * Represents a validated request to log a strength exercise.
 *
 * @param name     the exercise name
 * @param sets     the number of sets
 * @param reps     the repetitions in each set
 * @param weightKg the weight in kilograms
 */
public record LogStrengthCommand(String name, int sets, int reps, double weightKg) implements Command {
}
