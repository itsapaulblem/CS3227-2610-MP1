package fitlog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Coordinates startup, command parsing, execution, persistence, and user feedback.
 */
public class FitLogController {
    private static final Set<String> STRENGTH_FIELDS = Set.of("/sets", "/reps", "/weight");
    private static final Set<String> CARDIO_FIELDS = Set.of("/duration", "/distance");
    private final Ui ui;
    private final WorkoutLog entries;
    private final Storage storage;

    /**
     * Creates a controller that uses the supplied UI and storage service.
     *
     * @param ui the destination for user feedback
     * @param storage the service used to load and save entries
     */
    public FitLogController(Ui ui, Storage storage) {
        this.ui = ui;
        this.storage = storage;
        entries = new WorkoutLog();
    }

    /**
     * Loads saved entries, reports load warnings, and displays the startup greeting.
     */
    public void start() {
        Storage.LoadResult loadResult = null;
        IOException loadFailure = null;
        try {
            loadResult = storage.load();
            for (ExerciseEntry entry : loadResult.entries()) {
                entries.add(entry);
            }
        } catch (IOException exception) {
            loadFailure = exception;
        }

        ui.showInfo("Welcome to FitLog!");
        if (loadFailure != null) {
            ui.showWarning("Warning: could not load saved entries: " + loadFailure.getMessage());
        } else {
            for (String warning : loadResult.warnings()) {
                ui.showWarning(warning);
            }
        }
        ui.showInfo("What would you like to log today?");
    }

    /**
     * Parses and executes one user command.
     *
     * @param input the raw user input
     * @return whether the caller should end the interaction
     */
    public boolean submit(String input) {
        Command resolvedCommand = resolveCommand(input.trim(), entries, ui);
        return resolvedCommand != null && executeCommand(resolvedCommand, entries, storage, ui);
    }

    /**
     * Reports graceful termination when the input stream ends.
     */
    public void handleEndOfInput() {
        ui.showInfo("");
        ui.showInfo("Goodbye! Keep training.");
    }

    /**
     * Routes a raw command to the appropriate parser and reports errors for unrecognised input.
     *
     * @param command the raw user command
     * @param entries the current session's entries
     * @param ui the console UI for parse error messages
     * @return the parsed command, or {@code null} after reporting an error
     */
    private static Command resolveCommand(String command, WorkoutLog entries, Ui ui) {
        if (command.equals("delete") || command.startsWith("delete ")) {
            return parseDeleteCommand(command, entries, ui);
        }
        if (command.equals("edit") || command.startsWith("edit ")) {
            return parseEditCommand(command, entries, ui);
        }
        if (command.equals("log strength") || command.startsWith("log strength ")) {
            return parseLogStrengthCommand(command, ui);
        }
        if (command.equals("log cardio") || command.startsWith("log cardio ")) {
            return parseLogCardioCommand(command, ui);
        }
        if (command.equals("find") || command.startsWith("find ")) {
            return parseFindCommand(command, ui);
        }
        if (command.equals("stats") || command.startsWith("stats ")) {
            return parseStatsCommand(command, ui);
        }

        Command simpleCommand = parseSimpleCommand(command);
        if (simpleCommand != null) {
            return simpleCommand;
        }
        if (command.equals("log")) {
            ui.showError("Choose an exercise type after 'log': strength or cardio.");
        } else if (command.startsWith("log ")) {
            ui.showError("'" + command.substring("log ".length()).split("\\s+")[0]
                    + "' is not an exercise type. Use strength or cardio.");
        } else {
            ui.showError("I don't recognise that command. Use log, list, edit, delete, or bye.");
        }
        return null;
    }

    /**
     * Parses a find command while preserving its established validation messages.
     *
     * @param command the raw find command
     * @param ui the console UI for parse error messages
     * @return the parsed find command, or {@code null} when invalid
     */
    private static FindCommand parseFindCommand(String command, Ui ui) {
        String searchTerm = command.substring("find".length()).trim();
        if (searchTerm.isEmpty()) {
            ui.showError("Specify a search term to find.");
            return null;
        }
        return new FindCommand(searchTerm);
    }

    /**
     * Parses a stats command while preserving its established validation messages.
     *
     * @param command the raw stats command
     * @param ui the console UI for parse error messages
     * @return the parsed stats command, or {@code null} when invalid
     */
    private static StatsCommand parseStatsCommand(String command, Ui ui) {
        String exerciseName = command.substring("stats".length()).trim();
        if (exerciseName.isEmpty()) {
            ui.showError("Specify an exercise name to view stats.");
            return null;
        }
        return new StatsCommand(exerciseName);
    }

    /**
     * Parses commands that have already been migrated to the command hierarchy.
     *
     * @param command the raw user command
     * @return a parsed command, or {@code null} when another command path should handle it
     */
    private static Command parseSimpleCommand(String command) {
        if (command.equals("bye")) {
            return new ByeCommand();
        }
        if (command.equals("list")) {
            return new ListCommand();
        }
        if (command.equals("volume")) {
            return new VolumeCommand();
        }
        return null;
    }

    /**
     * Parses a delete command while preserving its established validation messages.
     *
     * @param command the raw delete command
     * @param entries the current session's entries
     * @param ui the console UI for parse error messages
     * @return the parsed delete command, or {@code null} when invalid
     */
    private static DeleteCommand parseDeleteCommand(String command, WorkoutLog entries, Ui ui) {
        if (entries.isEmpty()) {
            ui.showError("There are no entries to delete.");
            return null;
        }

        String[] parts = command.split("\\s+");
        if (parts.length == 1) {
            ui.showError("Specify the entry number to delete.");
            return null;
        }
        if (parts.length != 2) {
            ui.showError("Delete accepts exactly one entry number.");
            return null;
        }

        Integer index = parseEntryIndex(parts[1], entries.size(), ui);
        return index == null ? null : new DeleteCommand(index);
    }

    /**
     * Executes a parsed command.
     *
     * @param command the command to execute
     * @param entries the current session's entries
     * @param ui the console UI for output
     * @return whether FitLog should exit after execution
     */
    private static boolean executeCommand(Command command, WorkoutLog entries, Storage storage, Ui ui) {
        return switch (command) {
        case ByeCommand ignored -> {
            ui.showInfo("Goodbye! Keep training.");
            yield true;
        }
        case ListCommand ignored -> {
            for (int index = 0; index < entries.size(); index++) {
                ExerciseEntry entry = entries.get(index);
                ui.showInfo((index + 1) + ". [" + entry.getTypeLabel() + "] "
                        + entry.getName() + " - " + entry.getDetails());
            }
            yield false;
        }
        case DeleteCommand deleteCommand -> {
            ExerciseEntry removedEntry = entries.delete(deleteCommand.index());
            ui.showSuccess("Removed: " + removedEntry.getName() + " - " + removedEntry.getDetails());
            saveEntries(storage, entries, ui);
            yield false;
        }
        case EditCommand editCommand -> {
            executeEditCommand(editCommand, entries, storage, ui);
            yield false;
        }
        case LogStrengthCommand logCommand -> {
            StrengthEntry entry = new StrengthEntry(logCommand.name(), logCommand.sets(), logCommand.reps(),
                    logCommand.weightKg());
            boolean isPersonalRecord = entries.isPersonalRecord(entry, -1);
            entries.add(entry);
            ui.showSuccess("Logged: " + entry.getName() + " - " + entry.getDetails());
            if (isPersonalRecord) {
                printPrNotification(entry, ui);
            }
            saveEntries(storage, entries, ui);
            yield false;
        }
        case LogCardioCommand logCommand -> {
            CardioEntry entry = new CardioEntry(logCommand.name(), logCommand.durationMinutes(),
                    logCommand.distanceKm());
            boolean isPersonalRecord = entries.isPersonalRecord(entry, -1);
            entries.add(entry);
            ui.showSuccess("Logged: " + entry.getName() + " - " + entry.getDetails());
            if (isPersonalRecord) {
                printPrNotification(entry, ui);
            }
            saveEntries(storage, entries, ui);
            yield false;
        }
        case FindCommand findCommand -> {
            var matches = entries.findByName(findCommand.searchTerm());
            if (matches.isEmpty()) {
                ui.showInfo("No entries match '" + findCommand.searchTerm() + "'.");
            } else {
                for (WorkoutLog.EntryMatch match : matches) {
                    ExerciseEntry entry = match.entry();
                    ui.showInfo(match.position() + ". [" + entry.getTypeLabel() + "] "
                            + entry.getName() + " - " + entry.getDetails());
                }
            }
            yield false;
        }
        case StatsCommand statsCommand -> {
            var matches = entries.findByExerciseName(statsCommand.exerciseName());
            if (matches.isEmpty()) {
                ui.showInfo("No entries match '" + statsCommand.exerciseName() + "'.");
            } else {
                ui.showInfo("Progression for " + statsCommand.exerciseName() + ":");
                for (WorkoutLog.EntryMatch match : matches) {
                    ExerciseEntry entry = match.entry();
                    ui.showInfo(match.position() + ". [" + entry.getTypeLabel() + "] "
                            + formatStatsMetric(entry));
                }
            }
            yield false;
        }
        case VolumeCommand ignored -> {
            WorkoutLog.TrainingTotals totals = entries.calculateTotals();
            ui.showInfo("Totals for all currently loaded entries:");
            ui.showInfo("Strength volume: " + ExerciseEntry.formatNumber(totals.strengthVolume()) + " kg");
            ui.showInfo("Cardio duration: " + totals.cardioDurationMinutes() + " min");
            yield false;
        }
        };
    }

    /**
     * Formats an entry's type-specific personal-record metric for progression output.
     *
     * @param entry the entry whose metric is being displayed
     * @return the formatted metric and its unit
     */
    private static String formatStatsMetric(ExerciseEntry entry) {
        String metric = ExerciseEntry.formatNumber(entry.getPrMetric());
        return entry instanceof StrengthEntry ? metric + "kg" : metric + " min";
    }

    /**
     * Parses an edit command while preserving its established validation messages.
     *
     * @param command the raw edit command
     * @param entries the current session's entries
     * @param ui the console UI for parse error messages
     * @return the parsed edit command, or {@code null} when invalid
     */
    private static EditCommand parseEditCommand(String command, WorkoutLog entries, Ui ui) {
        if (entries.isEmpty()) {
            ui.showError("There are no entries to edit.");
            return null;
        }

        String[] parts = command.split("\\s+");
        if (parts.length == 1) {
            ui.showError("Specify the entry number to edit.");
            return null;
        }

        Integer index = parseEntryIndex(parts[1], entries.size(), ui);
        if (index == null) {
            return null;
        }
        if (parts.length == 2) {
            ui.showError("Specify one field and its new value, for example /weight 82.5.");
            return null;
        }
        if (parts.length == 3) {
            ui.showError("Provide a value after " + parts[2] + ".");
            return null;
        }
        if (parts.length != 4) {
            ui.showError("Edit one field at a time.");
            return null;
        }

        String field = parts[2];
        ExerciseEntry existingEntry = entries.get(index);
        if (!STRENGTH_FIELDS.contains(field) && !CARDIO_FIELDS.contains(field)) {
            ui.showError("'" + field + "' cannot be edited. Choose a field supported by this entry type.");
            return null;
        }
        if (existingEntry instanceof StrengthEntry && !STRENGTH_FIELDS.contains(field)) {
            ui.showError("'" + field + "' applies to cardio entries, but entry " + (index + 1) + " is strength.");
            return null;
        }
        if (existingEntry instanceof CardioEntry && !CARDIO_FIELDS.contains(field)) {
            ui.showError("'" + field + "' applies to strength entries, but entry " + (index + 1) + " is cardio.");
            return null;
        }
        return new EditCommand(index, field, parts[3]);
    }

    /**
     * Parses a strength log command while preserving its established validation messages.
     *
     * @param command the raw strength log command
     * @param ui the console UI for parse error messages
     * @return the parsed strength command, or {@code null} when invalid
     */
    private static LogStrengthCommand parseLogStrengthCommand(String command, Ui ui) {
        LogDetails details = parseLogDetails(command, "log strength", "strength", STRENGTH_FIELDS,
                "/sets, /reps, and /weight", ui);
        if (details == null) {
            return null;
        }
        if (!details.hasValue("/sets") || !details.hasValue("/reps") || !details.hasValue("/weight")) {
            ui.showError("Strength entries require /sets, /reps, and /weight.");
            return null;
        }
        Integer sets = parsePositiveWholeNumber(details.getValue("/sets"), "/sets", ui);
        Integer reps = parsePositiveWholeNumber(details.getValue("/reps"), "/reps", ui);
        Double weightKg = parsePositiveNumber(details.getValue("/weight"), "/weight", ui);
        if (sets == null || reps == null || weightKg == null) {
            return null;
        }
        return new LogStrengthCommand(details.name(), sets, reps, weightKg);
    }

    /**
     * Parses a cardio log command while preserving its established validation messages.
     *
     * @param command the raw cardio log command
     * @param ui the console UI for parse error messages
     * @return the parsed cardio command, or {@code null} when invalid
     */
    private static LogCardioCommand parseLogCardioCommand(String command, Ui ui) {
        LogDetails details = parseLogDetails(command, "log cardio", "cardio", CARDIO_FIELDS,
                "/duration and optional /distance", ui);
        if (details == null) {
            return null;
        }
        if (!details.hasValue("/duration")) {
            ui.showError("Cardio entries require a /duration value.");
            return null;
        }
        Integer durationMinutes = parsePositiveWholeNumber(details.getValue("/duration"), "/duration", ui);
        Double distanceKm = details.hasValue("/distance")
                ? parsePositiveNumber(details.getValue("/distance"), "/distance", ui) : null;
        if (durationMinutes == null || (details.hasValue("/distance") && distanceKm == null)) {
            return null;
        }
        return new LogCardioCommand(details.name(), durationMinutes, distanceKm);
    }

    /**
     * Parses the shared name-and-option structure of a strength or cardio log command.
     *
     * @param command the complete logging command
     * @param commandPrefix the command text before the exercise name
     * @param exerciseType the display name of the exercise type
     * @param allowedFields the option flags supported by the exercise type
     * @param allowedFieldsDescription the supported options for use in error messages
     * @return parsed details, or {@code null} after reporting an error
     */
    private static LogDetails parseLogDetails(String command, String commandPrefix, String exerciseType,
            Set<String> allowedFields, String allowedFieldsDescription, Ui ui) {
        String remainder = command.substring(commandPrefix.length()).trim();
        if (remainder.isEmpty()) {
            ui.showError("Add an exercise name before the " + exerciseType + " options.");
            return null;
        }
        String[] parts = remainder.split("\\s+");
        int firstFlagIndex = findFirstFlag(parts);
        if (firstFlagIndex == 0) {
            ui.showError("Add an exercise name before the " + exerciseType + " options.");
            return null;
        }
        if (firstFlagIndex == -1) {
            ui.showError(capitalise(exerciseType) + " entries need " + allowedFieldsDescription + " values.");
            return null;
        }

        Map<String, String> values = new HashMap<>();
        for (int index = firstFlagIndex; index < parts.length; index += 2) {
            String flag = parts[index];
            if (!flag.startsWith("/")) {
                ui.showError("Unexpected text '" + flag + "'. Each option needs a /flag.");
                return null;
            }
            if (index + 1 == parts.length || parts[index + 1].startsWith("/")) {
                ui.showError("Provide a value after " + flag + ".");
                return null;
            }
            if (!allowedFields.contains(flag)) {
                ui.showError("'" + flag + "' is not a " + exerciseType + " option. Use "
                        + allowedFieldsDescription + ".");
                return null;
            }
            if (values.containsKey(flag)) {
                ui.showError("Use " + flag + " only once in a " + exerciseType + " entry.");
                return null;
            }
            values.put(flag, parts[index + 1]);
        }
        return new LogDetails(join(parts, 0, firstFlagIndex), values);
    }

    /**
     * Replaces one immutable entry with the update described by a parsed command.
     *
     * @param command the validated edit command
     * @param entries the current session's entries
     * @param ui the console UI for output and value-validation errors
     */
    private static void executeEditCommand(EditCommand command, WorkoutLog entries, Storage storage, Ui ui) {
        ExerciseEntry existingEntry = entries.get(command.index());
        ExerciseEntry updatedEntry = createUpdatedEntry(existingEntry, command.field(), command.value(), ui);
        if (updatedEntry == null) {
            return;
        }
        boolean isPersonalRecord = entries.isPersonalRecord(updatedEntry, command.index());
        entries.replace(command.index(), updatedEntry);
        ui.showSuccess("Updated: " + updatedEntry.getName() + " - " + updatedEntry.getDetails());
        if (isPersonalRecord) {
            printPrNotification(updatedEntry, ui);
        }
        saveEntries(storage, entries, ui);
    }

    /**
     * Saves the current entries and reports a warning if persistence fails.
     *
     * @param storage the storage service to use
     * @param entries the entries to save
     * @param ui the console UI for save failure warnings
     */
    private static void saveEntries(Storage storage, WorkoutLog entries, Ui ui) {
        try {
            storage.save(entries.getEntries());
        } catch (IOException exception) {
            ui.showWarning("Warning: could not save entries: " + exception.getMessage());
        }
    }

    /**
     * Creates a replacement entry with one changed field after validating its new
     * value.
     *
     * @param entry the entry being replaced
     * @param field the field to change
     * @param value the supplied replacement value
     * @return the replacement entry, or {@code null} when the value is invalid
     */
    private static ExerciseEntry createUpdatedEntry(ExerciseEntry entry, String field, String value, Ui ui) {
        if (entry instanceof StrengthEntry strengthEntry) {
            return switch (field) {
            case "/sets" -> createStrengthEntryWithSets(strengthEntry, value, ui);
            case "/reps" -> createStrengthEntryWithReps(strengthEntry, value, ui);
            case "/weight" -> createStrengthEntryWithWeight(strengthEntry, value, ui);
                default -> throw new IllegalStateException("Unsupported strength field: " + field);
            };
        }

        CardioEntry cardioEntry = (CardioEntry) entry;
        return switch (field) {
        case "/duration" -> createCardioEntryWithDuration(cardioEntry, value, ui);
        case "/distance" -> createCardioEntryWithDistance(cardioEntry, value, ui);
            default -> throw new IllegalStateException("Unsupported cardio field: " + field);
        };
    }

    /**
     * Rebuilds a strength entry with a new validated set count.
     *
     * @param entry the existing strength entry
     * @param value the supplied set count
     * @return the rebuilt entry, or {@code null} when the value is invalid
     */
    private static StrengthEntry createStrengthEntryWithSets(StrengthEntry entry, String value, Ui ui) {
        Integer sets = parsePositiveWholeNumber(value, "/sets", ui);
        return sets == null ? null : new StrengthEntry(entry.getName(), sets, entry.getReps(), entry.getWeightKg());
    }

    /**
     * Rebuilds a strength entry with a new validated repetition count.
     *
     * @param entry the existing strength entry
     * @param value the supplied repetition count
     * @return the rebuilt entry, or {@code null} when the value is invalid
     */
    private static StrengthEntry createStrengthEntryWithReps(StrengthEntry entry, String value, Ui ui) {
        Integer reps = parsePositiveWholeNumber(value, "/reps", ui);
        return reps == null ? null : new StrengthEntry(entry.getName(), entry.getSets(), reps, entry.getWeightKg());
    }

    /**
     * Rebuilds a strength entry with a new validated weight.
     *
     * @param entry the existing strength entry
     * @param value the supplied weight
     * @return the rebuilt entry, or {@code null} when the value is invalid
     */
    private static StrengthEntry createStrengthEntryWithWeight(StrengthEntry entry, String value, Ui ui) {
        Double weightKg = parsePositiveNumber(value, "/weight", ui);
        return weightKg == null ? null : new StrengthEntry(entry.getName(), entry.getSets(), entry.getReps(), weightKg);
    }

    /**
     * Rebuilds a cardio entry with a new validated duration.
     *
     * @param entry the existing cardio entry
     * @param value the supplied duration
     * @return the rebuilt entry, or {@code null} when the value is invalid
     */
    private static CardioEntry createCardioEntryWithDuration(CardioEntry entry, String value, Ui ui) {
        Integer durationMinutes = parsePositiveWholeNumber(value, "/duration", ui);
        return durationMinutes == null ? null
                : new CardioEntry(entry.getName(), durationMinutes, entry.getDistanceKm());
    }

    /**
     * Rebuilds a cardio entry with a new validated distance.
     *
     * @param entry the existing cardio entry
     * @param value the supplied distance
     * @return the rebuilt entry, or {@code null} when the value is invalid
     */
    private static CardioEntry createCardioEntryWithDistance(CardioEntry entry, String value, Ui ui) {
        Double distanceKm = parsePositiveNumber(value, "/distance", ui);
        return distanceKm == null ? null : new CardioEntry(entry.getName(), entry.getDurationMinutes(), distanceKm);
    }

    /**
     * Converts a one-based entry number into a validated zero-based list index.
     *
     * @param value      the user-supplied entry number
     * @param entryCount the current number of entries
     * @return the zero-based index, or {@code null} when invalid
     */
    private static Integer parseEntryIndex(String value, int entryCount, Ui ui) {
        final int entryNumber;
        try {
            entryNumber = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            ui.showError("Entry number must be a whole number, not '" + value + "'.");
            return null;
        }
        if (entryNumber <= 0) {
            ui.showError("Entry number must be greater than zero.");
            return null;
        }
        if (entryNumber > entryCount) {
            ui.showError("Entry " + entryNumber + " does not exist. Use list to view entry numbers.");
            return null;
        }
        return entryNumber - 1;
    }

    /**
     * Prints a personal-record notification for a newly established record.
     *
     * @param entry the entry that established the personal record
     */
    private static void printPrNotification(ExerciseEntry entry, Ui ui) {
        ui.showPersonalRecord("New PR! " + entry.getPrDescription());
    }

    /**
     * Finds the first token representing a command option.
     *
     * @param parts the command tokens after its type
     * @return the first option's index, or {@code -1} if there is no option
     */
    private static int findFirstFlag(String[] parts) {
        for (int index = 0; index < parts.length; index++) {
            if (parts[index].startsWith("/")) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Joins a consecutive range of tokens with spaces.
     *
     * @param parts         the tokens to join
     * @param fromInclusive the first token index to include
     * @param toExclusive   the first token index to exclude
     * @return the joined text
     */
    private static String join(String[] parts, int fromInclusive, int toExclusive) {
        StringBuilder result = new StringBuilder();
        for (int index = fromInclusive; index < toExclusive; index++) {
            if (index > fromInclusive) {
                result.append(' ');
            }
            result.append(parts[index]);
        }
        return result.toString();
    }

    /**
     * Capitalises the first character of a word for an error message.
     *
     * @param value the word to capitalise
     * @return the word with its first character capitalised
     */
    private static String capitalise(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * Parses a positive integer option value and reports a useful error when
     * invalid.
     *
     * @param value the supplied option value
     * @param flag  the option the value belongs to
     * @return the parsed value, or {@code null} when invalid
     */
    private static Integer parsePositiveWholeNumber(String value, String flag, Ui ui) {
        try {
            int number = Integer.parseInt(value);
            if (number <= 0) {
                ui.showError(flag + " must be a whole number greater than zero.");
                return null;
            }
            return number;
        } catch (NumberFormatException exception) {
            ui.showError(flag + " needs a positive whole number, not '" + value + "'.");
            return null;
        }
    }

    /**
     * Parses a finite positive decimal option value and reports a useful error when
     * invalid.
     *
     * @param value the supplied option value
     * @param flag  the option the value belongs to
     * @return the parsed value, or {@code null} when invalid
     */
    private static Double parsePositiveNumber(String value, String flag, Ui ui) {
        try {
            double number = Double.parseDouble(value);
            if (!Double.isFinite(number) || number <= 0) {
                ui.showError(flag + " must be a finite number greater than zero.");
                return null;
            }
            return number;
        } catch (NumberFormatException exception) {
            ui.showError(flag + " needs a positive number, not '" + value + "'.");
            return null;
        }
    }

    /**
     * Holds the shared name and option values extracted from a log command.
     *
     * @param name the exercise name
     * @param optionValues the values keyed by their option flags
     */
    private record LogDetails(String name, Map<String, String> optionValues) {
        /**
         * Checks whether a value was supplied for an option.
         *
         * @param flag the option flag
         * @return whether the option was supplied
         */
        private boolean hasValue(String flag) {
            return optionValues.containsKey(flag);
        }

        /**
         * Returns the value supplied for an option.
         *
         * @param flag the option flag
         * @return the option value
         */
        private String getValue(String flag) {
            return optionValues.get(flag);
        }
    }
}
