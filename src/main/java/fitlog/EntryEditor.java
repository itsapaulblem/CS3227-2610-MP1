package fitlog;

/**
 * Rebuilds immutable exercise entries with one validated field change.
 */
final class EntryEditor {
    private EntryEditor() {
    }

    /**
     * Creates an updated copy of an entry while preserving its unchanged fields
     * and original logging time.
     *
     * @param entry the entry to copy
     * @param field the field to update
     * @param value the user-supplied replacement value
     * @param ui the destination for value-validation errors
     * @return the updated entry, or {@code null} when the value is invalid
     */
    static ExerciseEntry createUpdatedEntry(ExerciseEntry entry, String field, String value, Ui ui) {
        if (entry instanceof StrengthEntry strengthEntry) {
            return updateStrengthEntry(strengthEntry, field, value, ui);
        }
        if (entry instanceof CardioEntry cardioEntry) {
            return updateCardioEntry(cardioEntry, field, value, ui);
        }
        throw new IllegalArgumentException("Unsupported exercise entry type: " + entry.getClass().getName());
    }

    private static StrengthEntry updateStrengthEntry(StrengthEntry entry, String field, String value, Ui ui) {
        return switch (field) {
            case "/sets" -> withSets(entry, value, ui);
            case "/reps" -> withReps(entry, value, ui);
            case "/weight" -> withWeight(entry, value, ui);
            default -> throw new IllegalArgumentException("Unsupported strength field: " + field);
        };
    }

    private static CardioEntry updateCardioEntry(CardioEntry entry, String field, String value, Ui ui) {
        return switch (field) {
            case "/duration" -> withDuration(entry, value, ui);
            case "/distance" -> withDistance(entry, value, ui);
            default -> throw new IllegalArgumentException("Unsupported cardio field: " + field);
        };
    }

    private static StrengthEntry withSets(StrengthEntry entry, String value, Ui ui) {
        Integer sets = CommandParser.parsePositiveWholeNumber(value, "/sets", ui);
        return sets == null ? null
                : new StrengthEntry(entry.getName(), sets, entry.getReps(), entry.getWeightKg(), entry.getLoggedAt());
    }

    private static StrengthEntry withReps(StrengthEntry entry, String value, Ui ui) {
        Integer reps = CommandParser.parsePositiveWholeNumber(value, "/reps", ui);
        return reps == null ? null
                : new StrengthEntry(entry.getName(), entry.getSets(), reps, entry.getWeightKg(), entry.getLoggedAt());
    }

    private static StrengthEntry withWeight(StrengthEntry entry, String value, Ui ui) {
        Double weightKg = CommandParser.parsePositiveNumber(value, "/weight", ui);
        return weightKg == null ? null : new StrengthEntry(entry.getName(), entry.getSets(), entry.getReps(), weightKg,
                entry.getLoggedAt());
    }

    private static CardioEntry withDuration(CardioEntry entry, String value, Ui ui) {
        Integer durationMinutes = CommandParser.parsePositiveWholeNumber(value, "/duration", ui);
        return durationMinutes == null ? null
                : new CardioEntry(entry.getName(), durationMinutes, entry.getDistanceKm(), entry.getLoggedAt());
    }

    private static CardioEntry withDistance(CardioEntry entry, String value, Ui ui) {
        Double distanceKm = CommandParser.parsePositiveNumber(value, "/distance", ui);
        return distanceKm == null ? null
                : new CardioEntry(entry.getName(), entry.getDurationMinutes(), distanceKm, entry.getLoggedAt());
    }
}
