package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies that commands can extend FitLog through registration alone.
 */
class CommandRegistryTest {

    @Test
    void registeredCommandIsParsedExecutedAndIncludedInHelp(@TempDir Path tempDir) {
        CommandDefinition<PingCommand> pingDefinition = new CommandDefinition<>(
                PingCommand.class,
                input -> input.equals("ping"),
                (input, entries, ui) -> new PingCommand(),
                (command, entries, storage, ui, registry) -> {
                    ui.showInfo("Pong!");
                    return false;
                },
                "ping",
                "ping");
        CommandRegistry registry = CommandRegistry.createDefault().with(pingDefinition);
        RecordingUi ui = new RecordingUi();
        FitLogController controller = new FitLogController(
                ui, new Storage(tempDir.resolve("fitlog.txt")), registry);
        controller.start();
        ui.clearMessages();

        boolean shouldExit = controller.submit("ping");

        assertFalse(shouldExit);
        assertEquals(List.of("Pong!"), ui.messages());

        ui.clearMessages();
        controller.submit("help");

        assertTrue(ui.messages().contains("ping"));
        assertTrue(ui.messages().contains("Example: ping"));
    }

    /** Test-only command proving that the production command hierarchy is open. */
    private record PingCommand() implements Command {
    }

    /** Captures every feedback category without imposing presentation differences. */
    private static final class RecordingUi implements Ui {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void showInfo(String message) {
            messages.add(message);
        }

        @Override
        public void showExample(String message) {
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

        private List<String> messages() {
            return List.copyOf(messages);
        }

        private void clearMessages() {
            messages.clear();
        }
    }
}
