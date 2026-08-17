import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Starts the FitLog command-line application and manages the current session's exercise entries.
 */
public class FitLog {

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
            String command = scanner.nextLine().trim();

            if (command.equals("bye")) {
                System.out.println("Goodbye! Keep training.");
                break;
            }

            if (command.equals("log strength") || command.startsWith("log strength ")) {
                logStrength(command, entries);
            } else if (command.equals("log cardio") || command.startsWith("log cardio ")) {
                logCardio(command, entries);
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
                System.out.println("I don't recognise that command. Use log, list, or bye.");
            }
        }
    }

    /**
     * Parses and records a strength command, reporting an explanation when it is invalid.
     *
     * @param command the complete strength logging command
     * @param entries the current session's entries
     */
    private static void logStrength(String command, List<ExerciseEntry> entries) {
        String remainder = command.substring("log strength".length()).trim();
        if (remainder.isEmpty()) {
            System.out.println("Add an exercise name before the strength options.");
            return;
        }
        String[] parts = remainder.split("\\s+");
        int firstFlagIndex = findFirstFlag(parts);
        if (firstFlagIndex == 0) {
            System.out.println("Add an exercise name before the strength options.");
            return;
        }
        if (firstFlagIndex == -1) {
            System.out.println("Strength entries need /sets, /reps, and /weight values.");
            return;
        }

        String name = join(parts, 0, firstFlagIndex);
        String setsValue = null;
        String repsValue = null;
        String weightValue = null;
        for (int index = firstFlagIndex; index < parts.length; index += 2) {
            String flag = parts[index];
            if (!flag.startsWith("/")) {
                System.out.println("Unexpected text '" + flag + "'. Each option needs a /flag.");
                return;
            }
            if (index + 1 == parts.length || parts[index + 1].startsWith("/")) {
                System.out.println("Provide a value after " + flag + ".");
                return;
            }
            String value = parts[index + 1];
            switch (flag) {
            case "/sets":
                if (setsValue != null) {
                    System.out.println("Use /sets only once in a strength entry.");
                    return;
                }
                setsValue = value;
                break;
            case "/reps":
                if (repsValue != null) {
                    System.out.println("Use /reps only once in a strength entry.");
                    return;
                }
                repsValue = value;
                break;
            case "/weight":
                if (weightValue != null) {
                    System.out.println("Use /weight only once in a strength entry.");
                    return;
                }
                weightValue = value;
                break;
            default:
                System.out.println("'" + flag + "' is not a strength option. Use /sets, /reps, and /weight.");
                return;
            }
        }

        if (setsValue == null || repsValue == null || weightValue == null) {
            System.out.println("Strength entries require /sets, /reps, and /weight.");
            return;
        }
        Integer sets = parsePositiveWholeNumber(setsValue, "/sets");
        Integer reps = parsePositiveWholeNumber(repsValue, "/reps");
        Double weightKg = parsePositiveNumber(weightValue, "/weight");
        if (sets == null || reps == null || weightKg == null) {
            return;
        }

        StrengthEntry entry = new StrengthEntry(name, sets, reps, weightKg);
        entries.add(entry);
        System.out.println("Logged: " + entry.getName() + " - " + entry.getDetails());
    }

    /**
     * Parses and records a cardio command, reporting an explanation when it is invalid.
     *
     * @param command the complete cardio logging command
     * @param entries the current session's entries
     */
    private static void logCardio(String command, List<ExerciseEntry> entries) {
        String remainder = command.substring("log cardio".length()).trim();
        if (remainder.isEmpty()) {
            System.out.println("Add an exercise name before the cardio options.");
            return;
        }
        String[] parts = remainder.split("\\s+");
        int firstFlagIndex = findFirstFlag(parts);
        if (firstFlagIndex == 0) {
            System.out.println("Add an exercise name before the cardio options.");
            return;
        }
        if (firstFlagIndex == -1) {
            System.out.println("Cardio entries need a /duration value.");
            return;
        }

        String name = join(parts, 0, firstFlagIndex);
        String durationValue = null;
        String distanceValue = null;
        for (int index = firstFlagIndex; index < parts.length; index += 2) {
            String flag = parts[index];
            if (!flag.startsWith("/")) {
                System.out.println("Unexpected text '" + flag + "'. Each option needs a /flag.");
                return;
            }
            if (index + 1 == parts.length || parts[index + 1].startsWith("/")) {
                System.out.println("Provide a value after " + flag + ".");
                return;
            }
            String value = parts[index + 1];
            switch (flag) {
            case "/duration":
                if (durationValue != null) {
                    System.out.println("Use /duration only once in a cardio entry.");
                    return;
                }
                durationValue = value;
                break;
            case "/distance":
                if (distanceValue != null) {
                    System.out.println("Use /distance only once in a cardio entry.");
                    return;
                }
                distanceValue = value;
                break;
            default:
                System.out.println("'" + flag + "' is not a cardio option. Use /duration and optional /distance.");
                return;
            }
        }

        if (durationValue == null) {
            System.out.println("Cardio entries require a /duration value.");
            return;
        }
        Integer durationMinutes = parsePositiveWholeNumber(durationValue, "/duration");
        Double distanceKm = distanceValue == null ? null : parsePositiveNumber(distanceValue, "/distance");
        if (durationMinutes == null || (distanceValue != null && distanceKm == null)) {
            return;
        }

        CardioEntry entry = new CardioEntry(name, durationMinutes, distanceKm);
        entries.add(entry);
        System.out.println("Logged: " + entry.getName() + " - " + entry.getDetails());
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
     * @param parts the tokens to join
     * @param fromInclusive the first token index to include
     * @param toExclusive the first token index to exclude
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
     * Parses a positive integer option value and reports a useful error when invalid.
     *
     * @param value the supplied option value
     * @param flag the option the value belongs to
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
     * Parses a finite positive decimal option value and reports a useful error when invalid.
     *
     * @param value the supplied option value
     * @param flag the option the value belongs to
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
}
