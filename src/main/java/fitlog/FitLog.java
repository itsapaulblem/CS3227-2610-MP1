package fitlog;

import java.nio.file.Path;

/**
 * Starts FitLog's console application.
 */
public class FitLog {

    /**
     * Starts the console UI and submits each entered command to the controller.
     *
     * @param args command-line arguments, which are not used
     */
    public static void main(String[] args) {
        ConsoleUi ui = new ConsoleUi();
        FitLogController controller = new FitLogController(ui, new Storage(Path.of("data", "fitlog.txt")));
        controller.start();

        while (true) {
            String input = ui.readCommand();
            if (input == null) {
                controller.handleEndOfInput();
                break;
            }
            if (controller.submit(input)) {
                break;
            }
        }
    }
}
