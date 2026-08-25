package fitlog;

import java.io.IOException;
import java.util.List;

/**
 * Executes validated commands against a workout log and persists mutations.
 */
final class CommandExecutor {
    private CommandExecutor() {
    }

    static boolean executeBye(Ui ui) {
        ui.showInfo("Goodbye! Keep training.");
        return true;
    }

    static boolean executeList(WorkoutLog workoutLog, Ui ui) {
        if (workoutLog.isEmpty()) {
            ui.showInfo("No exercises logged yet.");
            return false;
        }

        for (int index = 0; index < workoutLog.size(); index++) {
            ui.showInfo(EntryFormatter.formatListEntry(index + 1, workoutLog.get(index)));
        }
        return false;
    }

    static boolean executeDelete(DeleteCommand command, WorkoutLog workoutLog, EntryStorage storage, Ui ui) {
        ExerciseEntry removedEntry = workoutLog.delete(command.index());
        ui.showSuccess("Removed: " + removedEntry.getName() + " - " + removedEntry.getDetails());
        saveEntries(storage, workoutLog, ui);
        return false;
    }

    static boolean executeLogStrength(LogStrengthCommand command, WorkoutLog workoutLog,
            EntryStorage storage, Ui ui) {
        StrengthEntry entry = new StrengthEntry(command.name(), command.sets(), command.reps(), command.weightKg());
        addEntry(entry, workoutLog, storage, ui);
        return false;
    }

    static boolean executeLogCardio(LogCardioCommand command, WorkoutLog workoutLog,
            EntryStorage storage, Ui ui) {
        CardioEntry entry = new CardioEntry(command.name(), command.durationMinutes(), command.distanceKm());
        addEntry(entry, workoutLog, storage, ui);
        return false;
    }

    /** Performs the behavior shared by strength and cardio logging. */
    private static void addEntry(ExerciseEntry entry, WorkoutLog workoutLog, EntryStorage storage, Ui ui) {
        boolean isPersonalRecord = workoutLog.isPersonalRecord(entry);
        workoutLog.add(entry);
        ui.showSuccess("Logged: " + entry.getName() + " - " + entry.getDetails());
        if (isPersonalRecord) {
            printPrNotification(entry, ui);
        }
        saveEntries(storage, workoutLog, ui);
    }

    static boolean executeFind(FindCommand command, WorkoutLog workoutLog, Ui ui) {
        List<WorkoutLog.EntryMatch> matches = workoutLog.findByName(command.searchTerm());
        if (matches.isEmpty()) {
            ui.showInfo("No entries match '" + command.searchTerm() + "'.");
            return false;
        }
        for (WorkoutLog.EntryMatch match : matches) {
            ui.showInfo(EntryFormatter.formatFindMatch(match));
        }
        return false;
    }

    static boolean executeStats(StatsCommand command, WorkoutLog workoutLog, Ui ui) {
        List<WorkoutLog.EntryMatch> matches = workoutLog.findByExerciseName(command.exerciseName());
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

    static boolean executeVolume(WorkoutLog workoutLog, Ui ui) {
        WorkoutLog.TrainingTotals totals = workoutLog.calculateTotals();
        ui.showInfo("Totals for all currently loaded entries:");
        ui.showInfo("Strength volume: " + ExerciseEntry.formatNumber(totals.strengthVolume()) + " kg");
        ui.showInfo("Cardio duration: " + totals.cardioDurationMinutes() + " min");
        return false;
    }

    static boolean executeEdit(EditCommand command, WorkoutLog workoutLog, EntryStorage storage, Ui ui) {
        ExerciseEntry existingEntry = workoutLog.get(command.index());
        ExerciseEntry updatedEntry = EntryEditor.createUpdatedEntry(
                existingEntry, command.field(), command.value(), ui);
        if (updatedEntry == null) {
            return false;
        }
        boolean isPersonalRecord = workoutLog.isPersonalRecord(updatedEntry, command.index());
        workoutLog.replace(command.index(), updatedEntry);
        ui.showSuccess("Updated: " + updatedEntry.getName() + " - " + updatedEntry.getDetails());
        if (isPersonalRecord) {
            printPrNotification(updatedEntry, ui);
        }
        saveEntries(storage, workoutLog, ui);
        return false;
    }

    private static void saveEntries(EntryStorage storage, WorkoutLog workoutLog, Ui ui) {
        try {
            storage.save(workoutLog.getEntries());
        } catch (IOException exception) {
            ui.showWarning("Warning: could not save entries: " + exception.getMessage());
        }
    }

    private static void printPrNotification(ExerciseEntry entry, Ui ui) {
        ui.showPersonalRecord("New PR! " + entry.getPrDescription());
    }
}
