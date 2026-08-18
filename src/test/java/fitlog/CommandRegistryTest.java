package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
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
        TestUi ui = new TestUi();
        FitLogController controller = new FitLogController(
                ui, new Storage(tempDir.resolve("fitlog.txt")), registry);
        controller.start();
        ui.clear();

        boolean shouldExit = controller.submit("ping");

        assertFalse(shouldExit);
        assertEquals(List.of("Pong!"), ui.messages());

        ui.clear();
        controller.submit("help");

        assertTrue(ui.messages().contains("ping"));
        assertTrue(ui.messages().contains("Example: ping"));
    }

    @Test
    void executingUnregisteredCommandFailsClearly(@TempDir Path tempDir) {
        CommandRegistry registry = CommandRegistry.createDefault();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> registry.execute(new PingCommand(), new WorkoutLog(),
                        new Storage(tempDir.resolve("fitlog.txt")), new TestUi()));

        assertTrue(exception.getMessage().contains(PingCommand.class.getName()));
    }

    /** Test-only command proving that the production command hierarchy is open. */
    private record PingCommand() implements Command {
    }

}
