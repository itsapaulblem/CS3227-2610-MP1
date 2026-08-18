package fitlog;

import java.io.IOException;

/**
 * Executes validated commands against a workout log and persists mutations.
 */
final class CommandExecutor {
    private CommandExecutor() {
    }

    /**
     * Executes one parsed command.
     *
     * @param command the validated command to execute
     * @param entries the workout log to query or mutate
     * @param storage the persistence service used after mutations
     * @param ui the destination for command feedback
     * @return whether FitLog should end the interaction
     */
    static boolean execute(Command command, WorkoutLog entries, Storage storage, Ui ui) {
        return switch (command) {
        case ByeCommand ignored -> executeBye(ui);
        case ListCommand ignored -> executeList(entries, ui);
        case DeleteCommand deleteCommand -> executeDelete(deleteCommand, entries, storage, ui);
        case EditCommand editCommand -> executeEdit(editCommand, entries, storage, ui);
        case LogStrengthCommand logCommand -> executeLogStrength(logCommand, entries, storage, ui);
        case LogCardioCommand logCommand -> executeLogCardio(logCommand, entries, storage, ui);
        case FindCommand findCommand -> executeFind(findCommand, entries, ui);
        case StatsCommand statsCommand -> executeStats(statsCommand, entries, ui);
        case VolumeCommand ignored -> executeVolume(entries, ui);
        case HelpCommand ignored -> executeHelp(ui);
        };
    }

    private static boolean executeBye(Ui ui) {
        ui.showInfo("Goodbye! Keep training.");
        return true;
    }

    private static boolean executeList(WorkoutLog entries, Ui ui) {
        for (int index = 0; index < entries.size(); index++) {
            ui.showInfo(EntryFormatter.formatListEntry(index + 1, entries.get(index)));
        }
        return false;
    }

    private static boolean executeDelete(DeleteCommand command, WorkoutLog entries, Storage storage, Ui ui) {
        ExerciseEntry removedEntry = entries.delete(command.index());
        ui.showSuccess("Removed: " + removedEntry.getName() + " - " + removedEntry.getDetails());
        saveEntries(storage, entries, ui);
        return false;
    }

    private static boolean executeLogStrength(LogStrengthCommand command, WorkoutLog entries,
            Storage storage, Ui ui) {
        StrengthEntry entry = new StrengthEntry(command.name(), command.sets(), command.reps(), command.weightKg());
        addEntry(entry, entries, storage, ui);
        return false;
    }

    private static boolean executeLogCardio(LogCardioCommand command, WorkoutLog entries,
            Storage storage, Ui ui) {
        CardioEntry entry = new CardioEntry(command.name(), command.durationMinutes(), command.distanceKm());
        addEntry(entry, entries, storage, ui);
        return false;
    }

    /** Performs the behaviour shared by strength and cardio logging. */
    private static void addEntry(ExerciseEntry entry, WorkoutLog entries, Storage storage, Ui ui) {
        boolean isPersonalRecord = entries.isPersonalRecord(entry, -1);
        entries.add(entry);
        ui.showSuccess("Logged: " + entry.getName() + " - " + entry.getDetails());
        if (isPersonalRecord) {
            printPrNotification(entry, ui);
        }
        saveEntries(storage, entries, ui);
    }

    private static boolean executeFind(FindCommand command, WorkoutLog entries, Ui ui) {
        var matches = entries.findByName(command.searchTerm());
        if (matches.isEmpty()) {
            ui.showInfo("No entries match '" + command.searchTerm() + "'.");
            return false;
        }
        for (WorkoutLog.EntryMatch match : matches) {
            ui.showInfo(EntryFormatter.formatFindMatch(match));
        }
        return false;
    }

    private static boolean executeStats(StatsCommand command, WorkoutLog entries, Ui ui) {
        var matches = entries.findByExerciseName(command.exerciseName());
        if (matches.isEmpty()) {
            ui.showInfo("No entries match '" + command.exerciseName() + "'.");
            return false;
        }
        ui.showInfo("Progression for " + command.exerciseName() + ":");
        for (WorkoutLog.EntryMatch match : matches) {
            ui.showInfo(EntryFormatter.formatStatsMatch(match));
        }
        return false;
    }

    private static boolean executeVolume(WorkoutLog entries, Ui ui) {
        WorkoutLog.TrainingTotals totals = entries.calculateTotals();
        ui.showInfo("Totals for all currently loaded entries:");
        ui.showInfo("Strength volume: " + ExerciseEntry.formatNumber(totals.strengthVolume()) + " kg");
        ui.showInfo("Cardio duration: " + totals.cardioDurationMinutes() + " min");
        return false;
    }

    private static boolean executeHelp(Ui ui) {
        HelpFormatter.showHelp(ui);
        return false;
    }

    private static boolean executeEdit(EditCommand command, WorkoutLog entries, Storage storage, Ui ui) {
        ExerciseEntry existingEntry = entries.get(command.index());
        ExerciseEntry updatedEntry = EntryEditor.createUpdatedEntry(
                existingEntry, command.field(), command.value(), ui);
        if (updatedEntry == null) {
            return false;
        }
        boolean isPersonalRecord = entries.isPersonalRecord(updatedEntry, command.index());
        entries.replace(command.index(), updatedEntry);
        ui.showSuccess("Updated: " + updatedEntry.getName() + " - " + updatedEntry.getDetails());
        if (isPersonalRecord) {
            printPrNotification(updatedEntry, ui);
        }
        saveEntries(storage, entries, ui);
        return false;
    }

    private static void saveEntries(Storage storage, WorkoutLog entries, Ui ui) {
        try {
            storage.save(entries.getEntries());
        } catch (IOException exception) {
            ui.showWarning("Warning: could not save entries: " + exception.getMessage());
        }
    }

    private static void printPrNotification(ExerciseEntry entry, Ui ui) {
        ui.showPersonalRecord("New PR! " + entry.getPrDescription());
    }
}
