package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests command effects separately from recognition and syntax validation. */
class CommandExecutorTest {
    private WorkoutLog workoutLog;
    private RecordingStorage storage;
    private TestUi ui;

    @BeforeEach
    void setUp() {
        workoutLog = new WorkoutLog();
        storage = new RecordingStorage();
        ui = new TestUi();
    }

    @Test
    void byeRequestsExitAndDisplaysFarewell() {
        assertTrue(CommandExecutor.executeBye(ui));
        assertEquals("Goodbye! Keep training.", ui.onlyMessage());
    }

    @Test
    void listDisplaysEntriesInLoggingOrderAndDoesNotSave() {
        workoutLog.add(new StrengthEntry("bench", 3, 10, 80, LocalDateTime.of(2026, 8, 17, 9, 0)));
        workoutLog.add(new CardioEntry("run", 30, null, null));

        assertFalse(CommandExecutor.executeList(workoutLog, ui));

        assertEquals(List.of(
                "1. [Strength] bench - 3 sets x 10 reps @ 80kg (logged 2026-08-17 09:00)",
                "2. [Cardio] run - 30 min (logged time not recorded)"), ui.messages());
        assertEquals(0, storage.saveCount());
    }

    @Test
    void listReportsWhenNoExercisesHaveBeenLogged() {
        assertFalse(CommandExecutor.executeList(workoutLog, ui));

        assertEquals("No exercises logged yet.", ui.onlyMessage());
        assertEquals(0, storage.saveCount());
    }

    @Test
    void loggingStrengthSavesAndReportsNewPersonalRecord() {
        workoutLog.add(new StrengthEntry("Bench  Press", 3, 10, 80));

        CommandExecutor.executeLogStrength(
                new LogStrengthCommand("bench press", 3, 8, 82.5), workoutLog, storage, ui);

        assertEquals(List.of(
                "Logged: bench press - 3 sets x 8 reps @ 82.5kg",
                "New PR! Heaviest bench press: 82.5kg"), ui.messages());
        assertEquals(1, storage.saveCount());
        assertEquals(2, storage.lastSavedEntries().size());
    }

    @Test
    void loggingFirstCardioEntryDoesNotReportPersonalRecord() {
        CommandExecutor.executeLogCardio(new LogCardioCommand("run", 30, null), workoutLog, storage, ui);

        assertEquals(List.of("Logged: run - 30 min"), ui.messages());
        assertInstanceOf(CardioEntry.class, workoutLog.get(0));
        assertEquals(1, storage.saveCount());
    }

    @Test
    void longerCardioEntryReportsPersonalRecord() {
        workoutLog.add(new CardioEntry("run", 30, 5.0));

        CommandExecutor.executeLogCardio(new LogCardioCommand("RUN", 45, null), workoutLog, storage, ui);

        assertEquals(List.of("Logged: RUN - 45 min", "New PR! Longest RUN: 45 min"), ui.messages());
    }

    @Test
    void deleteRemovesSelectedEntryAndSavesRemainingEntries() {
        workoutLog.add(new StrengthEntry("bench", 3, 10, 80));
        workoutLog.add(new CardioEntry("run", 30, null));

        CommandExecutor.executeDelete(new DeleteCommand(0), workoutLog, storage, ui);

        assertEquals("Removed: bench - 3 sets x 10 reps @ 80kg", ui.onlyMessage());
        assertEquals(List.of("run"), storage.lastSavedEntries().stream().map(ExerciseEntry::getName).toList());
    }

    @Test
    void findDisplaysMatchesWithOriginalPositionsOrNoMatchMessage() {
        workoutLog.add(new StrengthEntry("bench press", 3, 10, 80));
        workoutLog.add(new CardioEntry("run", 30, null));
        workoutLog.add(new StrengthEntry("overhead press", 3, 8, 40));

        CommandExecutor.executeFind(new FindCommand("PRESS"), workoutLog, ui);
        assertEquals(List.of(
                "1. [Strength] bench press - 3 sets x 10 reps @ 80kg",
                "3. [Strength] overhead press - 3 sets x 8 reps @ 40kg"), ui.messages());
        ui.clear();

        CommandExecutor.executeFind(new FindCommand("swim"), workoutLog, ui);
        assertEquals("No entries match 'swim'.", ui.onlyMessage());
    }

    @Test
    void statsUsesExactNormalisedNameAndSupportsBothEntryTypes() {
        workoutLog.add(new StrengthEntry("Bench  Press", 3, 10, 80, null));
        workoutLog.add(new CardioEntry("bench press", 30, null, null));
        workoutLog.add(new StrengthEntry("overhead press", 3, 8, 40, null));

        CommandExecutor.executeStats(new StatsCommand(" bench PRESS "), workoutLog, ui);

        assertEquals(List.of(
                "Progression for  bench PRESS :",
                "1. [Strength] 80kg (logged time not recorded)",
                "2. [Cardio] 30 min (logged time not recorded)"), ui.messages());
    }

    @Test
    void statsReportsWhenExactNameDoesNotMatch() {
        workoutLog.add(new StrengthEntry("bench press", 3, 10, 80));

        CommandExecutor.executeStats(new StatsCommand("bench"), workoutLog, ui);

        assertEquals("No entries match 'bench'.", ui.onlyMessage());
    }

    @Test
    void volumeDisplaysStrengthAndCardioTotalsWithoutSaving() {
        workoutLog.add(new StrengthEntry("bench", 3, 10, 82.5));
        workoutLog.add(new CardioEntry("run", 30, null));

        CommandExecutor.executeVolume(workoutLog, ui);

        assertEquals(List.of(
                "Totals for all currently loaded entries:",
                "Strength volume: 2475 kg",
                "Cardio duration: 30 min"), ui.messages());
        assertEquals(0, storage.saveCount());
    }

    @Test
    void successfulEditReplacesEntrySavesAndCanReportPersonalRecord() {
        workoutLog.add(new StrengthEntry("bench", 3, 10, 80));
        workoutLog.add(new StrengthEntry("bench", 3, 8, 70));

        CommandExecutor.executeEdit(new EditCommand(1, "/weight", "85"), workoutLog, storage, ui);

        assertEquals(List.of(
                "Updated: bench - 3 sets x 8 reps @ 85kg",
                "New PR! Heaviest bench: 85kg"), ui.messages());
        assertEquals(85.0, assertInstanceOf(StrengthEntry.class, workoutLog.get(1)).getWeightKg());
        assertEquals(1, storage.saveCount());
    }

    @Test
    void invalidEditDoesNotReplaceOrSaveEntry() {
        StrengthEntry original = new StrengthEntry("bench", 3, 10, 80);
        workoutLog.add(original);

        CommandExecutor.executeEdit(new EditCommand(0, "/weight", "0"), workoutLog, storage, ui);

        assertEquals("/weight must be a finite number greater than zero.", ui.onlyMessage());
        assertEquals(original, workoutLog.get(0));
        assertEquals(0, storage.saveCount());
    }

    /** Records immutable snapshots of each save request. */
    private static final class RecordingStorage implements EntryStorage {
        private final List<List<ExerciseEntry>> saves = new ArrayList<>();

        @Override
        public LoadResult load() {
            return new LoadResult(List.of(), List.of());
        }

        @Override
        public void save(List<ExerciseEntry> entries) throws IOException {
            saves.add(List.copyOf(entries));
        }

        private int saveCount() {
            return saves.size();
        }

        private List<ExerciseEntry> lastSavedEntries() {
            return saves.get(saves.size() - 1);
        }
    }
}
