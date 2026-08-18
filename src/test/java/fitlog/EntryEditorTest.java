package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Tests immutable entry rebuilding for every editable field. */
class EntryEditorTest {
    private static final LocalDateTime LOGGED_AT = LocalDateTime.of(2026, 8, 17, 12, 30);

    @ParameterizedTest
    @MethodSource("strengthEdits")
    void strengthFieldsCanBeEdited(String field, String value, int sets, int reps, double weight) {
        StrengthEntry original = new StrengthEntry("bench press", 3, 10, 80, LOGGED_AT);

        StrengthEntry updated = assertInstanceOf(StrengthEntry.class,
                EntryEditor.createUpdatedEntry(original, field, value, new TestUi()));

        assertEquals(sets, updated.getSets());
        assertEquals(reps, updated.getReps());
        assertEquals(weight, updated.getWeightKg());
        assertEquals(LOGGED_AT, updated.getLoggedAt());
    }

    private static Stream<Arguments> strengthEdits() {
        return Stream.of(
                Arguments.of("/sets", "4", 4, 10, 80.0),
                Arguments.of("/reps", "8", 3, 8, 80.0),
                Arguments.of("/weight", "82.5", 3, 10, 82.5));
    }

    @ParameterizedTest
    @MethodSource("cardioEdits")
    void cardioFieldsCanBeEdited(String field, String value, int duration, double distance) {
        CardioEntry original = new CardioEntry("run", 30, 5.0, LOGGED_AT);

        CardioEntry updated = assertInstanceOf(CardioEntry.class,
                EntryEditor.createUpdatedEntry(original, field, value, new TestUi()));

        assertEquals(duration, updated.getDurationMinutes());
        assertEquals(distance, updated.getDistanceKm());
        assertEquals(LOGGED_AT, updated.getLoggedAt());
    }

    private static Stream<Arguments> cardioEdits() {
        return Stream.of(
                Arguments.of("/duration", "45", 45, 5.0),
                Arguments.of("/distance", "7.5", 30, 7.5));
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    void invalidEditValuesLeaveEntryUnchanged(ExerciseEntry entry, String field, String value,
            String expectedMessage) {
        TestUi ui = new TestUi();

        assertNull(EntryEditor.createUpdatedEntry(entry, field, value, ui));
        assertEquals(expectedMessage, ui.onlyMessage());
    }

    private static Stream<Arguments> invalidValues() {
        return Stream.of(
                Arguments.of(new StrengthEntry("bench", 3, 10, 80), "/sets", "0",
                        "/sets must be a whole number greater than zero."),
                Arguments.of(new StrengthEntry("bench", 3, 10, 80), "/reps", "ten",
                        "/reps needs a positive whole number, not 'ten'."),
                Arguments.of(new StrengthEntry("bench", 3, 10, 80), "/weight", "NaN",
                        "/weight must be a finite number greater than zero."),
                Arguments.of(new CardioEntry("run", 30, null), "/duration", "-1",
                        "/duration must be a whole number greater than zero."),
                Arguments.of(new CardioEntry("run", 30, null), "/distance", "far",
                        "/distance needs a positive number, not 'far'."));
    }

    @Test
    void directCallsRejectFieldsUnsupportedByEntryType() {
        TestUi ui = new TestUi();

        assertThrows(IllegalArgumentException.class,
                () -> EntryEditor.createUpdatedEntry(new StrengthEntry("bench", 3, 10, 80),
                        "/duration", "30", ui));
        assertThrows(IllegalArgumentException.class,
                () -> EntryEditor.createUpdatedEntry(new CardioEntry("run", 30, null),
                        "/weight", "80", ui));
    }
}
