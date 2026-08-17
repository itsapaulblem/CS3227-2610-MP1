package fitlog;

import javafx.application.Application;

/**
 * Plain Java entry point that launches the JavaFX application.
 */
public final class Launcher {

    private Launcher() {
    }

    /**
     * Launches FitLog's graphical user interface.
     *
     * @param args command-line arguments passed to JavaFX
     */
    public static void main(String[] args) {
        Application.launch(FitLogGui.class, args);
    }
}
