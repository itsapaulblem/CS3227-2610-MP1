package fitlog;

import java.util.Scanner;

/**
 * Provides console input and renders every feedback category as plain text.
 */
public class ConsoleUi implements Ui {
    private final Scanner scanner;

    /**
     * Creates a console UI that reads commands from standard input.
     */
    public ConsoleUi() {
        scanner = new Scanner(System.in);
    }

    /**
     * Displays the prompt and reads the next command line.
     *
     * @return the next line, or {@code null} when standard input has ended
     */
    public String readCommand() {
        System.out.print("> ");
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }

    @Override
    public void showInfo(String message) {
        System.out.println(message);
    }

    @Override
    public void showExample(String message) {
        System.out.println("  " + message);
    }

    @Override
    public void showSuccess(String message) {
        System.out.println(message);
    }

    @Override
    public void showError(String message) {
        System.out.println(message);
    }

    @Override
    public void showWarning(String message) {
        System.out.println(message);
    }

    @Override
    public void showPersonalRecord(String message) {
        System.out.println(message);
    }
}
