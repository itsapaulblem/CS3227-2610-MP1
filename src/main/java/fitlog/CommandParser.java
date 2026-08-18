package fitlog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Converts raw user input into validated {@link Command} objects.
 */
final class CommandParser {
    private static final Set<String> STRENGTH_FIELDS = Set.of("/sets", "/reps", "/weight");
    private static final Set<String> CARDIO_FIELDS = Set.of("/duration", "/distance");

    private CommandParser() {
    }

    /**
     * Parses one user command and reports any validation error through the UI.
     *
     * @param input the raw user input
     * @param entries the current workout log, used to validate entry indices
     * @param ui the destination for validation errors
     * @return the validated command, or {@code null} when parsing fails
     */
    static Command parse(String input, WorkoutLog entries, Ui ui) {
        String command = input.trim();
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
        reportUnrecognisedCommand(command, ui);
        return null;
    }

    /** Reports the most specific error available for an unrecognised command. */
    private static void reportUnrecognisedCommand(String command, Ui ui) {
        if (command.equals("log")) {
            ui.showError("Choose an exercise type after 'log': strength or cardio.");
        } else if (command.startsWith("log ")) {
            ui.showError("'" + command.substring("log ".length()).split("\\s+")[0]
                    + "' is not an exercise type. Use strength or cardio.");
        } else {
            ui.showError("I don't recognise that command. Use help to see the available commands.");
        }
    }

    private static FindCommand parseFindCommand(String command, Ui ui) {
        String searchTerm = command.substring("find".length()).trim();
        if (searchTerm.isEmpty()) {
            ui.showError("Specify a search term to find.");
            return null;
        }
        return new FindCommand(searchTerm);
    }

    private static StatsCommand parseStatsCommand(String command, Ui ui) {
        String exerciseName = command.substring("stats".length()).trim();
        if (exerciseName.isEmpty()) {
            ui.showError("Specify an exercise name to view stats.");
            return null;
        }
        return new StatsCommand(exerciseName);
    }

    private static Command parseSimpleCommand(String command) {
        return switch (command) {
        case "bye" -> new ByeCommand();
        case "list" -> new ListCommand();
        case "volume" -> new VolumeCommand();
        case "help" -> new HelpCommand();
        default -> null;
        };
    }

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

    /** Parses an edit option value so execution can reuse the same validation messages. */
    static Integer parsePositiveWholeNumber(String value, String flag, Ui ui) {
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

    /** Parses an edit option value so execution can reuse the same validation messages. */
    static Double parsePositiveNumber(String value, String flag, Ui ui) {
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

    private static int findFirstFlag(String[] parts) {
        for (int index = 0; index < parts.length; index++) {
            if (parts[index].startsWith("/")) {
                return index;
            }
        }
        return -1;
    }

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

    private static String capitalise(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    /** Holds the exercise name and option values extracted from a log command. */
    private record LogDetails(String name, Map<String, String> optionValues) {
        private boolean hasValue(String flag) {
            return optionValues.containsKey(flag);
        }

        private String getValue(String flag) {
            return optionValues.get(flag);
        }
    }
}
