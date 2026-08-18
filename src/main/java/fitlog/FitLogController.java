package fitlog;

import java.io.IOException;
import java.util.List;

/**
 * Coordinates application startup and delegates submitted commands for parsing
 * and execution.
 */
public class FitLogController {
    private final Ui ui;
    private final WorkoutLog workoutLog;
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
        workoutLog = new WorkoutLog();
    }

    /**
     * Loads saved entries, reports load warnings, and displays the startup greeting.
     */
    public void start() {
        List<String> loadWarnings = loadWorkoutHistory();

        ui.showInfo("Welcome to FitLog!");
        for (String warning : loadWarnings) {
            ui.showWarning(warning);
        }
        ui.showInfo("What would you like to log today?");
    }

    /** Loads saved entries and converts a load failure into one user-facing warning. */
    private List<String> loadWorkoutHistory() {
        try {
            EntryStorage.LoadResult loadResult = storage.load();
            for (ExerciseEntry entry : loadResult.entries()) {
                workoutLog.add(entry);
            }
            return loadResult.warnings();
        } catch (IOException exception) {
            return List.of("Warning: could not load saved entries: " + exception.getMessage());
        }
    }

    /**
     * Parses and executes one user command.
     *
     * @param input the raw user input
     * @return whether the caller should end the interaction
     */
    public boolean submit(String input) {
        Command command = commandRegistry.parse(input, workoutLog, ui);
        return command != null && commandRegistry.execute(command, workoutLog, storage, ui);
    }

    /**
     * Reports graceful termination when the input stream ends.
     */
    public void handleEndOfInput() {
        ui.showInfo("");
        ui.showInfo("Goodbye! Keep training.");
    }
}
