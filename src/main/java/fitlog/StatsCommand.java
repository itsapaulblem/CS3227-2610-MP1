package fitlog;

/**
 * Represents a request to view the logged progression for one exercise name.
 *
 * @param exerciseName the exercise name to match after normalization
 */
public record StatsCommand(String exerciseName) implements Command {
}
