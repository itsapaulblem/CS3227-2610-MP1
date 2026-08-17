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
            String command = scanner.nextLine();

            if (command.equals("bye")) {
                System.out.println("Goodbye! Keep training.");
                break;
            }

            if (command.startsWith("log strength ")) {
                StrengthEntry entry = createStrengthEntry(command);
                entries.add(entry);
                System.out.println("Logged: " + entry.getName() + " - " + entry.getDetails());
            } else if (command.startsWith("log cardio ")) {
                CardioEntry entry = createCardioEntry(command);
                entries.add(entry);
                System.out.println("Logged: " + entry.getName() + " - " + entry.getDetails());
            } else if (command.equals("list")) {
                for (int index = 0; index < entries.size(); index++) {
                    ExerciseEntry entry = entries.get(index);
                    System.out.println((index + 1) + ". [" + entry.getTypeLabel() + "] "
                            + entry.getName() + " - " + entry.getDetails());
                }
            } else {
                System.out.println(command);
            }
        }
    }

    /**
     * Creates a strength entry from a command in the documented strength command format.
     *
     * @param command the complete strength logging command
     * @return the parsed strength entry
     */
    private static StrengthEntry createStrengthEntry(String command) {
        String[] parts = command.split(" /sets | /reps | /weight ");
        String name = parts[0].substring("log strength ".length());
        int sets = Integer.parseInt(parts[1]);
        int reps = Integer.parseInt(parts[2]);
        double weightKg = Double.parseDouble(parts[3]);
        return new StrengthEntry(name, sets, reps, weightKg);
    }

    /**
     * Creates a cardio entry from a command in the documented cardio command format.
     *
     * @param command the complete cardio logging command
     * @return the parsed cardio entry
     */
    private static CardioEntry createCardioEntry(String command) {
        String[] parts = command.split(" /duration | /distance ");
        String name = parts[0].substring("log cardio ".length());
        int durationMinutes = Integer.parseInt(parts[1]);
        Double distanceKm = parts.length == 3 ? Double.parseDouble(parts[2]) : null;
        return new CardioEntry(name, durationMinutes, distanceKm);
    }
}
