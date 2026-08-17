import java.util.Scanner;

/**
 * Starts the FitLog command-line application and echoes commands until the user exits.
 */
public class FitLog {

    /**
     * Greets the user, reads commands, and ends when the user enters {@code bye}.
     *
     * @param args command-line arguments, which are not used
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to FitLog!");
        System.out.println("What would you like to log today?");

        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine();

            if (command.equals("bye")) {
                System.out.println("Goodbye! Keep training.");
                break;
            }

            System.out.println(command);
        }
    }
}
