package fitlog;

import java.io.IOException;

/**
 * Coordinates application startup and delegates submitted commands for parsing
 * and execution.
 */
public class FitLogController {
    private final Ui ui;
    private final WorkoutLog entries;
    private final EntryStorage storage;
    private final CommandRegistry commandRegistry;

    /**
     * Creates a controller that uses the supplied UI and storage service.
     *
     * @param ui the destination for user feedback
     * @param storage the service used to load and save entries
     */
    public FitLogController(Ui ui, EntryStorage storage) {
        this(ui, storage, CommandRegistry.createDefault());
    }

    /** Creates a controller with an explicitly supplied command registry. */
    FitLogController(Ui ui, EntryStorage storage, CommandRegistry commandRegistry) {
        this.ui = ui;
        this.storage = storage;
        this.commandRegistry = commandRegistry;
        entries = new WorkoutLog();
    }

    /**
     * Loads saved entries, reports load warnings, and displays the startup greeting.
     */
    public void start() {
        EntryStorage.LoadResult loadResult = null;
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
        Command command = commandRegistry.parse(input, entries, ui);
        return command != null && commandRegistry.execute(command, entries, storage, ui);
    }

    /**
     * Reports graceful termination when the input stream ends.
     */
    public void handleEndOfInput() {
        ui.showInfo("");
        ui.showInfo("Goodbye! Keep training.");
    }
}
