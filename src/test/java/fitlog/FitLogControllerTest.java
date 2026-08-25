package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests timestamp behavior at the controller boundary, including displayed output.
 */
class FitLogControllerTest {

    @Test
    void editPreservesOriginalLoggingTime(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("fitlog.txt");
        Storage storage = new Storage(file);
        TestUi ui = new TestUi();
        FitLogController controller = new FitLogController(ui, storage);
        controller.start();
        controller.submit("log strength bench press /sets 3 /reps 10 /weight 80");
        LocalDateTime originalTime = storage.load().entries().get(0).getLoggedAt();

        controller.submit("edit 1 /weight 82.5");
        LocalDateTime updatedTime = storage.load().entries().get(0).getLoggedAt();

        assertEquals(originalTime, updatedTime);
    }

    @Test
    void listDisplaysLoggingTime(@TempDir Path tempDir) {
        TestUi ui = new TestUi();
        FitLogController controller = new FitLogController(ui, new Storage(tempDir.resolve("fitlog.txt")));
        controller.start();
        controller.submit("log strength bench press /sets 3 /reps 10 /weight 80");
        ui.clear();

        controller.submit("list");

        assertEquals(1, ui.messages().size());
        assertTrue(ui.messages().get(0).matches("1\\. \\[Strength] bench press - 3 sets x 10 reps @ 80kg "
                + "\\(logged \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)"));
    }

    @Test
    void statsDisplaysLoggingTime(@TempDir Path tempDir) {
        TestUi ui = new TestUi();
        FitLogController controller = new FitLogController(ui, new Storage(tempDir.resolve("fitlog.txt")));
        controller.start();
        controller.submit("log strength bench press /sets 3 /reps 10 /weight 80");
        ui.clear();

        controller.submit("stats bench press");

        assertEquals("Progression for bench press:", ui.messages().get(0));
        assertTrue(ui.messages().get(1).matches("1\\. \\[Strength] 80kg "
                + "\\(logged \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)"));
    }

    @Test
    void helpDisplaysEveryCommandWithoutSaving(@TempDir Path tempDir) {
        Path file = tempDir.resolve("fitlog.txt");
        TestUi ui = new TestUi();
        FitLogController controller = new FitLogController(ui, new Storage(file));
        controller.start();
        ui.clear();

        controller.submit("help");

        assertEquals(expectedHelpMessages(), ui.messages());
        assertTrue(java.nio.file.Files.notExists(file));
    }

    private static List<String> expectedHelpMessages() {
        return List.of(
                "log strength <name> /sets <n> /reps <n> /weight <kg>",
                "Example: log strength bench press /sets 3 /reps 10 /weight 80",
                "log cardio <name> /duration <min> [/distance <km>]",
                "Example: log cardio run /duration 30 /distance 5",
                "list",
                "Example: list",
                "edit <index> /sets <n> | edit <index> /reps <n> | edit <index> /weight <kg> | "
                        + "edit <index> /duration <min> | edit <index> /distance <km>",
                "Example: edit 1 /weight 82.5",
                "delete <index>",
                "Example: delete 2",
                "find <search term>",
                "Example: find press",
                "stats <exercise name>",
                "Example: stats bench press",
                "volume",
                "Example: volume",
                "help",
                "Example: help",
                "bye",
                "Example: bye");
    }

    @Test
    void startReportsLoadFailureFromStorageAbstraction() {
        TestUi ui = new TestUi();
        EntryStorage storage = new ConfigurableEntryStorage(true, false);
        FitLogController controller = new FitLogController(ui, storage);

        controller.start();

        assertEquals(List.of(
                "Welcome to FitLog!",
                "Warning: could not load saved entries: simulated load failure",
                "What would you like to log today?"), ui.messages());
    }

    @Test
    void mutationReportsSaveFailureFromStorageAbstraction() {
        TestUi ui = new TestUi();
        EntryStorage storage = new ConfigurableEntryStorage(false, true);
        FitLogController controller = new FitLogController(ui, storage);
        controller.start();
        ui.clear();

        controller.submit("log cardio run /duration 30");

        assertEquals(List.of(
                "Logged: run - 30 min",
                "Warning: could not save entries: simulated save failure"), ui.messages());
    }

    @Test
    void startReportsWarningsAndMakesLoadedEntriesAvailable() {
        ExerciseEntry loadedEntry = new StrengthEntry("bench", 3, 10, 80, null);
        EntryStorage storage = new LoadedEntryStorage(new EntryStorage.LoadResult(
                List.of(loadedEntry), List.of("Warning: skipped malformed entry on line 2.")));
        TestUi ui = new TestUi();
        FitLogController controller = new FitLogController(ui, storage);

        controller.start();
        controller.submit("list");

        assertEquals(List.of(
                "Welcome to FitLog!",
                "Warning: skipped malformed entry on line 2.",
                "What would you like to log today?",
                "1. [Strength] bench - 3 sets x 10 reps @ 80kg (logged time not recorded)"), ui.messages());
    }

    @Test
    void endOfInputDisplaysBlankLineAndFarewell() {
        TestUi ui = new TestUi();
        FitLogController controller = new FitLogController(ui, new ConfigurableEntryStorage(false, false));

        controller.handleEndOfInput();

        assertEquals(List.of("", "Goodbye! Keep training."), ui.messages());
    }

    /** Test double that can fail either storage operation without filesystem setup. */
    private static final class ConfigurableEntryStorage implements EntryStorage {
        private final boolean failLoad;
        private final boolean failSave;

        private ConfigurableEntryStorage(boolean failLoad, boolean failSave) {
            this.failLoad = failLoad;
            this.failSave = failSave;
        }

        @Override
        public LoadResult load() throws IOException {
            if (failLoad) {
                throw new IOException("simulated load failure");
            }
            return new LoadResult(List.of(), List.of());
        }

        @Override
        public void save(List<ExerciseEntry> entries) throws IOException {
            if (failSave) {
                throw new IOException("simulated save failure");
            }
        }
    }

    /** Returns a predetermined successful load result. */
    private record LoadedEntryStorage(LoadResult result) implements EntryStorage {
        @Override
        public LoadResult load() {
            return result;
        }

        @Override
        public void save(List<ExerciseEntry> entries) {
        }
    }

}
