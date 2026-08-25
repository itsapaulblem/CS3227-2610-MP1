package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Tests command recognition and validation independently from command execution. */
class CommandParserTest {
    private CommandRegistry registry;
    private WorkoutLog workoutLog;
    private TestUi ui;

    @BeforeEach
    void setUp() {
        registry = CommandRegistry.createDefault();
        workoutLog = new WorkoutLog();
        ui = new TestUi();
    }

    @Test
    void validCommandsProduceTheirTypedValues() {
        LogStrengthCommand strength = assertInstanceOf(LogStrengthCommand.class,
                registry.parse("  log strength bench press /weight 82.5 /sets 3 /reps 8  ", workoutLog, ui));
        LogCardioCommand cardio = assertInstanceOf(LogCardioCommand.class,
                registry.parse("log cardio interval run /distance 5.5 /duration 30", workoutLog, ui));

        assertEquals(new LogStrengthCommand("bench press", 3, 8, 82.5), strength);
        assertEquals(new LogCardioCommand("interval run", 30, 5.5), cardio);
        assertEquals(new FindCommand("press"), registry.parse("find press", workoutLog, ui));
        assertEquals(new StatsCommand("bench press"), registry.parse("stats bench press", workoutLog, ui));
    }

    @Test
    void simpleCommandsAreRecognisedAfterOuterWhitespaceIsTrimmed() {
        assertInstanceOf(ListCommand.class, registry.parse("  list  ", workoutLog, ui));
        assertInstanceOf(VolumeCommand.class, registry.parse("volume", workoutLog, ui));
        assertInstanceOf(HelpCommand.class, registry.parse("help", workoutLog, ui));
        assertInstanceOf(ByeCommand.class, registry.parse("bye", workoutLog, ui));
    }

    @Test
    void validEditAndDeleteUseZeroBasedCommandIndices() {
        workoutLog.add(new StrengthEntry("bench", 3, 10, 80));

        assertEquals(new EditCommand(0, "/weight", "82.5"),
                registry.parse("edit 1 /weight 82.5", workoutLog, ui));
        assertEquals(new DeleteCommand(0), registry.parse("delete 1", workoutLog, ui));
    }

    @ParameterizedTest
    @MethodSource("unrecognisedCommands")
    void unrecognisedCommandsReportSpecificErrors(String input, String expectedMessage) {
        assertNull(registry.parse(input, workoutLog, ui));
        assertEquals(expectedMessage, ui.onlyMessage());
    }

    private static Stream<Arguments> unrecognisedCommands() {
        return Stream.of(
                Arguments.of("log", "Choose an exercise type after 'log': strength or cardio."),
                Arguments.of("log yoga flow", "'yoga' is not an exercise type. Use strength or cardio."),
                Arguments.of("Help", "I don't recognise that command. Use help to see the available commands."),
                Arguments.of("listing", "I don't recognise that command. Use help to see the available commands."));
    }

    @ParameterizedTest
    @MethodSource("invalidLogCommands")
    void invalidLogCommandsReportValidationError(String input, String expectedMessage) {
        assertNull(registry.parse(input, workoutLog, ui));
        assertEquals(expectedMessage, ui.onlyMessage());
    }

    private static Stream<Arguments> invalidLogCommands() {
        return Stream.of(
                Arguments.of("log strength", "Add an exercise name before the strength options."),
                Arguments.of("log strength /sets 3 /reps 10 /weight 80",
                        "Add an exercise name before the strength options."),
                Arguments.of("log strength bench press", "Strength entries need /sets, /reps, and /weight values."),
                Arguments.of("log strength bench press /sets 3 /reps 10",
                        "Strength entries require /sets, /reps, and /weight."),
                Arguments.of("log cardio run /distance 5", "Cardio entries require a /duration value."),
                Arguments.of("log cardio run /duration", "Provide a value after /duration."),
                Arguments.of("log cardio run /duration /distance 5", "Provide a value after /duration."),
                Arguments.of("log cardio run /duration 30 extra 5",
                        "Unexpected text 'extra'. Each option needs a /flag."),
                Arguments.of("log strength bench /duration 30",
                        "'/duration' is not a strength option. Use /sets, /reps, and /weight."),
                Arguments.of("log cardio run /duration 30 /duration 40",
                        "Use /duration only once in a cardio entry."));
    }

    @ParameterizedTest
    @MethodSource("invalidNumericCommands")
    void invalidNumbersAreRejected(String input, String expectedMessage) {
        assertNull(registry.parse(input, workoutLog, ui));
        assertEquals(expectedMessage, ui.onlyMessage());
    }

    private static Stream<Arguments> invalidNumericCommands() {
        return Stream.of(
                Arguments.of("log strength bench /sets ten /reps 10 /weight 80",
                        "/sets needs a positive whole number, not 'ten'."),
                Arguments.of("log strength bench /sets 0 /reps 10 /weight 80",
                        "/sets must be a whole number greater than zero."),
                Arguments.of("log strength bench /sets 3.5 /reps 10 /weight 80",
                        "/sets needs a positive whole number, not '3.5'."),
                Arguments.of("log cardio run /duration -1", "/duration must be a whole number greater than zero."),
                Arguments.of("log strength bench /sets 3 /reps 10 /weight heavy",
                        "/weight needs a positive number, not 'heavy'."),
                Arguments.of("log strength bench /sets 3 /reps 10 /weight 0",
                        "/weight must be a finite number greater than zero."),
                Arguments.of("log cardio run /duration 30 /distance NaN",
                        "/distance must be a finite number greater than zero."),
                Arguments.of("log cardio run /duration 30 /distance Infinity",
                        "/distance must be a finite number greater than zero."));
    }

    @ParameterizedTest
    @MethodSource("emptyQueryCommands")
    void queryCommandsRequireSearchText(String input, String expectedMessage) {
        assertNull(registry.parse(input, workoutLog, ui));
        assertEquals(expectedMessage, ui.onlyMessage());
    }

    private static Stream<Arguments> emptyQueryCommands() {
        return Stream.of(
                Arguments.of("find", "Specify a search term to find."),
                Arguments.of("stats", "Specify an exercise name to view stats."));
    }

    @ParameterizedTest
    @MethodSource("invalidDeleteCommands")
    void deleteRejectsInvalidTargets(String input, String expectedMessage) {
        workoutLog.add(new StrengthEntry("bench", 3, 10, 80));

        assertNull(registry.parse(input, workoutLog, ui));
        assertEquals(expectedMessage, ui.onlyMessage());
    }

    private static Stream<Arguments> invalidDeleteCommands() {
        return Stream.of(
                Arguments.of("delete", "Specify the entry number to delete."),
                Arguments.of("delete 1 now", "Delete accepts exactly one entry number."),
                Arguments.of("delete one", "Entry number must be a whole number, not 'one'."),
                Arguments.of("delete 0", "Entry number must be greater than zero."),
                Arguments.of("delete 2", "Entry 2 does not exist. Use list to view entry numbers."));
    }

    @Test
    void deleteAndEditRejectEmptyLogBeforeOtherValidation() {
        assertNull(registry.parse("delete nonsense", workoutLog, ui));
        assertEquals("There are no entries to delete.", ui.onlyMessage());
        ui.clear();

        assertNull(registry.parse("edit nonsense", workoutLog, ui));
        assertEquals("There are no entries to edit.", ui.onlyMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidEditCommands")
    void editRejectsInvalidStructureOrField(String input, String expectedMessage) {
        workoutLog.add(new StrengthEntry("bench", 3, 10, 80));

        assertNull(registry.parse(input, workoutLog, ui));
        assertEquals(expectedMessage, ui.onlyMessage());
    }

    private static Stream<Arguments> invalidEditCommands() {
        return Stream.of(
                Arguments.of("edit", "Specify the entry number to edit."),
                Arguments.of("edit one /weight 90", "Entry number must be a whole number, not 'one'."),
                Arguments.of("edit 1", "Specify one field and its new value, for example /weight 82.5."),
                Arguments.of("edit 1 /weight", "Provide a value after /weight."),
                Arguments.of("edit 1 /weight 90 /sets 4", "Edit one field at a time."),
                Arguments.of("edit 1 /name squat",
                        "'/name' cannot be edited. Choose a field supported by this entry type."),
                Arguments.of("edit 1 /duration 30",
                        "'/duration' applies to cardio entries, but entry 1 is strength."));
    }

    @Test
    void editRejectsStrengthFieldForCardioEntry() {
        workoutLog.add(new CardioEntry("run", 30, null));

        assertNull(registry.parse("edit 1 /sets 4", workoutLog, ui));
        assertEquals("'/sets' applies to strength entries, but entry 1 is cardio.", ui.onlyMessage());
    }
}
