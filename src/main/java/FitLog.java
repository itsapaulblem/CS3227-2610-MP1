import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Starts the FitLog command-line application and manages the current session's
 * exercise entries.
 */
public class FitLog {
    private static final Set<String> STRENGTH_FIELDS = Set.of("/sets", "/reps", "/weight");
    private static final Set<String> CARDIO_FIELDS = Set.of("/duration", "/distance");

    /**
     * Greets the user, reads commands, and ends when the user enters {@code bye}.
     *
     * @param args command-line arguments, which are not used
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<ExerciseEntry> entries = new ArrayList<>();

        System.out.println("Welcome to FitLog!");
        System.out.println("What would you like to log today?");

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                System.out.println();
                System.out.println("Goodbye! Keep training.");
                break;
            }
            String command = scanner.nextLine().trim();

            if (command.equals("bye")) {
                System.out.println("Goodbye! Keep training.");
                break;
            }

            if (command.equals("log strength") || command.startsWith("log strength ")) {
                logStrength(command, entries);
            } else if (command.equals("log cardio") || command.startsWith("log cardio ")) {
                logCardio(command, entries);
            } else if (command.equals("delete") || command.startsWith("delete ")) {
                deleteEntry(command, entries);
            } else if (command.equals("edit") || command.startsWith("edit ")) {
                editEntry(command, entries);
            } else if (command.equals("list")) {
                for (int index = 0; index < entries.size(); index++) {
                    ExerciseEntry entry = entries.get(index);
                    System.out.println((index + 1) + ". [" + entry.getTypeLabel() + "] "
                            + entry.getName() + " - " + entry.getDetails());
                }
            } else if (command.equals("log")) {
                System.out.println("Choose an exercise type after 'log': strength or cardio.");
            } else if (command.startsWith("log ")) {
                System.out.println("'" + command.substring("log ".length()).split("\\s+")[0]
                        + "' is not an exercise type. Use strength or cardio.");
            } else {
                System.out.println("I don't recognise that command. Use log, list, edit, delete, or bye.");
            }
        }
    }

    /**
     * Parses and records a strength command, reporting an explanation when it is
     * invalid.
     *
     * @param command the complete strength logging command
     * @param entries the current session's entries
     */
    private static void logStrength(String command, List<ExerciseEntry> entries) {
        LogDetails details = parseLogDetails(command, "log strength", "strength", STRENGTH_FIELDS,
                "/sets, /reps, and /weight");
        if (details == null) {
            return;
        }
        if (!details.hasValue("/sets") || !details.hasValue("/reps") || !details.hasValue("/weight")) {
            System.out.println("Strength entries require /sets, /reps, and /weight.");
            return;
        }
        Integer sets = parsePositiveWholeNumber(details.getValue("/sets"), "/sets");
        Integer reps = parsePositiveWholeNumber(details.getValue("/reps"), "/reps");
        Double weightKg = parsePositiveNumber(details.getValue("/weight"), "/weight");
        if (sets == null || reps == null || weightKg == null) {
            return;
        }

        StrengthEntry entry = new StrengthEntry(details.name(), sets, reps, weightKg);
        entries.add(entry);
        System.out.println("Logged: " + entry.getName() + " - " + entry.getDetails());
    }

    /**
     * Parses and records a cardio command, reporting an explanation when it is
     * invalid.
     *
     * @param command the complete cardio logging command
     * @param entries the current session's entries
     */
    private static void logCardio(String command, List<ExerciseEntry> entries) {
        LogDetails details = parseLogDetails(command, "log cardio", "cardio", CARDIO_FIELDS,
                "/duration and optional /distance");
        if (details == null) {
            return;
        }
        if (!details.hasValue("/duration")) {
            System.out.println("Cardio entries require a /duration value.");
            return;
        }
        Integer durationMinutes = parsePositiveWholeNumber(details.getValue("/duration"), "/duration");
        Double distanceKm = details.hasValue("/distance")
                ? parsePositiveNumber(details.getValue("/distance"), "/distance") : null;
        if (durationMinutes == null || (details.hasValue("/distance") && distanceKm == null)) {
            return;
        }

        CardioEntry entry = new CardioEntry(details.name(), durationMinutes, distanceKm);
        entries.add(entry);
        System.out.println("Logged: " + entry.getName() + " - " + entry.getDetails());
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
            Set<String> allowedFields, String allowedFieldsDescription) {
        String remainder = command.substring(commandPrefix.length()).trim();
        if (remainder.isEmpty()) {
            System.out.println("Add an exercise name before the " + exerciseType + " options.");
            return null;
        }
        String[] parts = remainder.split("\\s+");
        int firstFlagIndex = findFirstFlag(parts);
        if (firstFlagIndex == 0) {
            System.out.println("Add an exercise name before the " + exerciseType + " options.");
            return null;
        }
        if (firstFlagIndex == -1) {
            System.out.println(capitalise(exerciseType) + " entries need " + allowedFieldsDescription + " values.");
            return null;
        }

        Map<String, String> values = new HashMap<>();
        for (int index = firstFlagIndex; index < parts.length; index += 2) {
            String flag = parts[index];
            if (!flag.startsWith("/")) {
                System.out.println("Unexpected text '" + flag + "'. Each option needs a /flag.");
                return null;
            }
            if (index + 1 == parts.length || parts[index + 1].startsWith("/")) {
                System.out.println("Provide a value after " + flag + ".");
                return null;
            }
            if (!allowedFields.contains(flag)) {
                System.out.println("'" + flag + "' is not a " + exerciseType + " option. Use "
                        + allowedFieldsDescription + ".");
                return null;
            }
            if (values.containsKey(flag)) {
                System.out.println("Use " + flag + " only once in a " + exerciseType + " entry.");
                return null;
            }
            values.put(flag, parts[index + 1]);
        }
        return new LogDetails(join(parts, 0, firstFlagIndex), values);
    }

    /**
     * Removes an entry identified by its one-based list number.
     *
     * @param command the complete delete command
     * @param entries the current session's entries
     */
    private static void deleteEntry(String command, List<ExerciseEntry> entries) {
        if (entries.isEmpty()) {
            System.out.println("There are no entries to delete.");
            return;
        }

        String[] parts = command.split("\\s+");
        if (parts.length == 1) {
            System.out.println("Specify the entry number to delete.");
            return;
        }
        if (parts.length != 2) {
            System.out.println("Delete accepts exactly one entry number.");
            return;
        }

        Integer index = parseEntryIndex(parts[1], entries.size());
        if (index == null) {
            return;
        }
        ExerciseEntry removedEntry = entries.remove(index.intValue());
        System.out.println("Removed: " + removedEntry.getName() + " - " + removedEntry.getDetails());
    }

    /**
     * Replaces one immutable entry with a copy containing one validated changed
     * value.
     *
     * @param command the complete edit command
     * @param entries the current session's entries
     */
    private static void editEntry(String command, List<ExerciseEntry> entries) {
        if (entries.isEmpty()) {
            System.out.println("There are no entries to edit.");
            return;
        }

        String[] parts = command.split("\\s+");
        if (parts.length == 1) {
            System.out.println("Specify the entry number to edit.");
            return;
        }

        Integer index = parseEntryIndex(parts[1], entries.size());
        if (index == null) {
            return;
        }
        if (parts.length == 2) {
            System.out.println("Specify one field and its new value, for example /weight 82.5.");
            return;
        }
        if (parts.length == 3) {
            System.out.println("Provide a value after " + parts[2] + ".");
            return;
        }
        if (parts.length != 4) {
            System.out.println("Edit one field at a time.");
            return;
        }

        String field = parts[2];
        String value = parts[3];
        ExerciseEntry existingEntry = entries.get(index);
        if (!STRENGTH_FIELDS.contains(field) && !CARDIO_FIELDS.contains(field)) {
            System.out.println("'" + field + "' cannot be edited. Choose a field supported by this entry type.");
            return;
        }
        if (existingEntry instanceof StrengthEntry && !STRENGTH_FIELDS.contains(field)) {
            System.out.println("'" + field + "' applies to cardio entries, but entry " + (index + 1) + " is strength.");
            return;
        }
        if (existingEntry instanceof CardioEntry && !CARDIO_FIELDS.contains(field)) {
            System.out.println("'" + field + "' applies to strength entries, but entry " + (index + 1) + " is cardio.");
            return;
        }

        ExerciseEntry updatedEntry = createUpdatedEntry(existingEntry, field, value);
        if (updatedEntry == null) {
            return;
        }
        entries.set(index, updatedEntry);
        System.out.println("Updated: " + updatedEntry.getName() + " - " + updatedEntry.getDetails());
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
    private static ExerciseEntry createUpdatedEntry(ExerciseEntry entry, String field, String value) {
        if (entry instanceof StrengthEntry strengthEntry) {
            return switch (field) {
                case "/sets" -> createStrengthEntryWithSets(strengthEntry, value);
                case "/reps" -> createStrengthEntryWithReps(strengthEntry, value);
                case "/weight" -> createStrengthEntryWithWeight(strengthEntry, value);
                default -> throw new IllegalStateException("Unsupported strength field: " + field);
            };
        }

        CardioEntry cardioEntry = (CardioEntry) entry;
        return switch (field) {
            case "/duration" -> createCardioEntryWithDuration(cardioEntry, value);
            case "/distance" -> createCardioEntryWithDistance(cardioEntry, value);
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
    private static StrengthEntry createStrengthEntryWithSets(StrengthEntry entry, String value) {
        Integer sets = parsePositiveWholeNumber(value, "/sets");
        return sets == null ? null : new StrengthEntry(entry.getName(), sets, entry.getReps(), entry.getWeightKg());
    }

    /**
     * Rebuilds a strength entry with a new validated repetition count.
     *
     * @param entry the existing strength entry
     * @param value the supplied repetition count
     * @return the rebuilt entry, or {@code null} when the value is invalid
     */
    private static StrengthEntry createStrengthEntryWithReps(StrengthEntry entry, String value) {
        Integer reps = parsePositiveWholeNumber(value, "/reps");
        return reps == null ? null : new StrengthEntry(entry.getName(), entry.getSets(), reps, entry.getWeightKg());
    }

    /**
     * Rebuilds a strength entry with a new validated weight.
     *
     * @param entry the existing strength entry
     * @param value the supplied weight
     * @return the rebuilt entry, or {@code null} when the value is invalid
     */
    private static StrengthEntry createStrengthEntryWithWeight(StrengthEntry entry, String value) {
        Double weightKg = parsePositiveNumber(value, "/weight");
        return weightKg == null ? null : new StrengthEntry(entry.getName(), entry.getSets(), entry.getReps(), weightKg);
    }

    /**
     * Rebuilds a cardio entry with a new validated duration.
     *
     * @param entry the existing cardio entry
     * @param value the supplied duration
     * @return the rebuilt entry, or {@code null} when the value is invalid
     */
    private static CardioEntry createCardioEntryWithDuration(CardioEntry entry, String value) {
        Integer durationMinutes = parsePositiveWholeNumber(value, "/duration");
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
    private static CardioEntry createCardioEntryWithDistance(CardioEntry entry, String value) {
        Double distanceKm = parsePositiveNumber(value, "/distance");
        return distanceKm == null ? null : new CardioEntry(entry.getName(), entry.getDurationMinutes(), distanceKm);
    }

    /**
     * Converts a one-based entry number into a validated zero-based list index.
     *
     * @param value      the user-supplied entry number
     * @param entryCount the current number of entries
     * @return the zero-based index, or {@code null} when invalid
     */
    private static Integer parseEntryIndex(String value, int entryCount) {
        final int entryNumber;
        try {
            entryNumber = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            System.out.println("Entry number must be a whole number, not '" + value + "'.");
            return null;
        }
        if (entryNumber <= 0) {
            System.out.println("Entry number must be greater than zero.");
            return null;
        }
        if (entryNumber > entryCount) {
            System.out.println("Entry " + entryNumber + " does not exist. Use list to view entry numbers.");
            return null;
        }
        return entryNumber - 1;
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
    private static Integer parsePositiveWholeNumber(String value, String flag) {
        try {
            int number = Integer.parseInt(value);
            if (number <= 0) {
                System.out.println(flag + " must be a whole number greater than zero.");
                return null;
            }
            return number;
        } catch (NumberFormatException exception) {
            System.out.println(flag + " needs a positive whole number, not '" + value + "'.");
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
    private static Double parsePositiveNumber(String value, String flag) {
        try {
            double number = Double.parseDouble(value);
            if (!Double.isFinite(number) || number <= 0) {
                System.out.println(flag + " must be a finite number greater than zero.");
                return null;
            }
            return number;
        } catch (NumberFormatException exception) {
            System.out.println(flag + " needs a positive number, not '" + value + "'.");
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
