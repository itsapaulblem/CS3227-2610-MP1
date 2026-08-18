package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/** Tests console input, prompting, and plain-text feedback rendering. */
class ConsoleUiTest {

    @Test
    void readCommandReturnsInputThenNullAtEndOfStream() {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream("list\n".getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            ConsoleUi ui = new ConsoleUi();

            assertEquals("list", ui.readCommand());
            assertNull(ui.readCommand());
            assertEquals("> > ", output.toString(StandardCharsets.UTF_8));
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }
    }

    @Test
    void feedbackIsPrintedAsPlainTextAndExamplesAreIndented() {
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            ConsoleUi ui = new ConsoleUi();
            ui.showInfo("info");
            ui.showExample("example");
            ui.showSuccess("success");
            ui.showError("error");
            ui.showWarning("warning");
            ui.showPersonalRecord("record");

            assertEquals("info\n  example\nsuccess\nerror\nwarning\nrecord\n",
                    output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"));
        } finally {
            System.setOut(originalOutput);
        }
    }
}
