import java.util.Scanner;

/**
 * Handles console input and output for the FitLog application.
 */
public class Ui {
    private final Scanner scanner;

    /**
     * Creates a UI that reads commands from standard input.
     */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /**
     * Displays the command prompt and reads the next line of input.
     *
     * @return the next line, or {@code null} when input has ended
     */
    public String readCommand() {
        System.out.print("> ");
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }

    /**
     * Displays one message followed by a line break.
     *
     * @param message the message to display
     */
    public void showMessage(String message) {
        System.out.println(message);
    }
}
