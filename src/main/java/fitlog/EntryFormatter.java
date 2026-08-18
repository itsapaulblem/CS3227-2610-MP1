package fitlog;

/**
 * Formats workout entries for FitLog's list, search, and progression views.
 */
final class EntryFormatter {
    private EntryFormatter() {
    }

    /** Formats an entry with its position, details, and logging time. */
    static String formatListEntry(int position, ExerciseEntry entry) {
        return position + ". [" + entry.getTypeLabel() + "] " + entry.getName() + " - " + entry.getDetails()
                + " (logged " + entry.getLoggedAtDisplay() + ")";
    }

    /** Formats a search match while retaining its position in the complete log. */
    static String formatFindMatch(WorkoutLog.EntryMatch match) {
        ExerciseEntry entry = match.entry();
        return match.position() + ". [" + entry.getTypeLabel() + "] "
                + entry.getName() + " - " + entry.getDetails();
    }

    /** Formats one exercise-progression result with its PR metric and logging time. */
    static String formatStatsMatch(WorkoutLog.EntryMatch match) {
        ExerciseEntry entry = match.entry();
        return match.position() + ". [" + entry.getTypeLabel() + "] "
                + formatPrMetric(entry) + " (logged " + entry.getLoggedAtDisplay() + ")";
    }

    /** Formats the type-specific metric used to compare personal records. */
    private static String formatPrMetric(ExerciseEntry entry) {
        String metric = ExerciseEntry.formatNumber(entry.getPrMetric());
        return entry instanceof StrengthEntry ? metric + "kg" : metric + " min";
    }
}
