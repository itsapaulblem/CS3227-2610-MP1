package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests timestamp behaviour at the controller boundary, including displayed output.
 */
class FitLogControllerTest {

    @Test
    void editPreservesOriginalLoggingTime(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("fitlog.txt");
        Storage storage = new Storage(file);
        RecordingUi ui = new RecordingUi();
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
        RecordingUi ui = new RecordingUi();
        FitLogController controller = new FitLogController(ui, new Storage(tempDir.resolve("fitlog.txt")));
        controller.start();
        controller.submit("log strength bench press /sets 3 /reps 10 /weight 80");
        ui.clearMessages();

        controller.submit("list");

        assertEquals(1, ui.messages().size());
        assertTrue(ui.messages().get(0).matches("1\\. \\[Strength] bench press - 3 sets x 10 reps @ 80kg "
                + "\\(logged \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)"));
    }

    @Test
    void statsDisplaysLoggingTime(@TempDir Path tempDir) {
        RecordingUi ui = new RecordingUi();
        FitLogController controller = new FitLogController(ui, new Storage(tempDir.resolve("fitlog.txt")));
        controller.start();
        controller.submit("log strength bench press /sets 3 /reps 10 /weight 80");
        ui.clearMessages();

        controller.submit("stats bench press");

        assertEquals("Progression for bench press:", ui.messages().get(0));
        assertTrue(ui.messages().get(1).matches("1\\. \\[Strength] 80kg "
                + "\\(logged \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)"));
    }

    /**
     * Captures controller feedback without using either the console or JavaFX UI.
     */
    private static class RecordingUi implements Ui {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void showInfo(String message) {
            messages.add(message);
        }

        @Override
        public void showSuccess(String message) {
            messages.add(message);
        }

        @Override
        public void showError(String message) {
            messages.add(message);
        }

        @Override
        public void showWarning(String message) {
            messages.add(message);
        }

        @Override
        public void showPersonalRecord(String message) {
            messages.add(message);
        }

        /**
         * Returns all captured feedback in display order.
         *
         * @return the captured feedback
         */
        private List<String> messages() {
            return messages;
        }

        /**
         * Removes startup and command feedback before checking a later response.
         */
        private void clearMessages() {
            messages.clear();
        }
    }
}
