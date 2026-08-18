package fitlog;

/**
 * Defines the value rules shared by all exercise-entry creation paths.
 */
final class ExerciseValueValidator {
    private ExerciseValueValidator() {
    }

    /** Returns whether an exercise name contains at least one non-whitespace character. */
    static boolean isValidName(String name) {
        return name != null && !name.isBlank();
    }

    /** Returns whether a whole-number exercise measurement is positive. */
    static boolean isPositiveWholeNumber(int value) {
        return value > 0;
    }

    /** Returns whether a decimal exercise measurement is finite and positive. */
    static boolean isFinitePositiveNumber(double value) {
        return Double.isFinite(value) && value > 0;
    }
}
