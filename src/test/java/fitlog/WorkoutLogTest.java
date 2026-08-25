package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests collection-level workout logging, personal-record, and search behavior.
 */
class WorkoutLogTest {

    @Test
    void basicCollectionOperationsPreserveOrder() {
        WorkoutLog log = new WorkoutLog();
        StrengthEntry strength = new StrengthEntry("bench", 3, 10, 80);
        CardioEntry cardio = new CardioEntry("run", 30, null);

        assertTrue(log.isEmpty());
        log.add(strength);
        log.add(cardio);

        assertEquals(2, log.size());
        assertFalse(log.isEmpty());
        assertSame(strength, log.get(0));
        assertSame(cardio, log.get(1));
    }

    @Test
    void deleteAndReplaceUpdateTheSelectedPosition() {
        WorkoutLog log = new WorkoutLog();
        StrengthEntry first = new StrengthEntry("bench", 3, 10, 80);
        CardioEntry second = new CardioEntry("run", 30, null);
        StrengthEntry replacement = new StrengthEntry("squat", 3, 5, 100);
        log.add(first);
        log.add(second);

        log.replace(0, replacement);
        ExerciseEntry removed = log.delete(1);

        assertSame(replacement, log.get(0));
        assertSame(second, removed);
        assertEquals(1, log.size());
    }

    @Test
    void exposedEntriesCannotMutateWorkoutLog() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench", 3, 10, 80));

        assertThrows(UnsupportedOperationException.class,
                () -> log.getEntries().add(new CardioEntry("run", 30, null)));
        assertEquals(1, log.size());
    }

    @Test
    void firstEntryForExerciseIsNotPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        StrengthEntry candidate = new StrengthEntry("bench press", 3, 10, 80.0);

        assertFalse(log.isPersonalRecord(candidate));
    }

    @Test
    void heavierStrengthEntryIsPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));
        StrengthEntry candidate = new StrengthEntry("bench press", 3, 8, 82.5);

        assertTrue(log.isPersonalRecord(candidate));
    }

    @Test
    void longerCardioEntryIsPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        log.add(new CardioEntry("run", 30, 5.0));

        assertTrue(log.isPersonalRecord(new CardioEntry("RUN", 45, null)));
    }

    @Test
    void tiedOrShorterCardioEntryIsNotPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        log.add(new CardioEntry("run", 30, 5.0));

        assertFalse(log.isPersonalRecord(new CardioEntry("run", 30, 6.0)));
        assertFalse(log.isPersonalRecord(new CardioEntry("run", 20, 6.0)));
    }

    @Test
    void tiedStrengthWeightIsNotPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));
        StrengthEntry candidate = new StrengthEntry("bench press", 4, 6, 80.0);

        assertFalse(log.isPersonalRecord(candidate));
    }

    @Test
    void lighterStrengthWeightIsNotPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));
        StrengthEntry candidate = new StrengthEntry("bench press", 3, 12, 70.0);

        assertFalse(log.isPersonalRecord(candidate));
    }

    @Test
    void personalRecordMatchesNamesCaseInsensitivelyAndWithNormalisedWhitespace() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("Bench   Press", 3, 10, 80.0));
        StrengthEntry candidate = new StrengthEntry(" bench press ", 3, 8, 82.5);

        assertTrue(log.isPersonalRecord(candidate));
    }

    @Test
    void differentExerciseNamesDoNotAffectPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("squat", 3, 5, 100.0));
        StrengthEntry candidate = new StrengthEntry("bench press", 3, 10, 80.0);

        assertFalse(log.isPersonalRecord(candidate));
    }

    @Test
    void differentEntryTypesDoNotCompeteForPersonalRecords() {
        WorkoutLog log = new WorkoutLog();
        log.add(new CardioEntry("run", 30, 5.0));
        StrengthEntry candidate = new StrengthEntry("run", 3, 10, 80.0);

        assertFalse(log.isPersonalRecord(candidate));
    }

    @Test
    void editingOnlyMatchingEntryIsNotPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));
        StrengthEntry candidate = new StrengthEntry("bench press", 3, 10, 82.5);

        assertFalse(log.isPersonalRecord(candidate, 0));
    }

    @Test
    void editingEntryThatBeatsAnotherMatchingEntryIsPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));
        log.add(new StrengthEntry("bench press", 3, 8, 70.0));
        StrengthEntry candidate = new StrengthEntry("bench press", 3, 8, 85.0);

        assertTrue(log.isPersonalRecord(candidate, 1));
    }

    @Test
    void editingEntryThatDoesNotBeatEveryOtherMatchIsNotPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 90.0));
        log.add(new StrengthEntry("bench press", 3, 8, 70.0));

        assertFalse(log.isPersonalRecord(new StrengthEntry("bench press", 3, 8, 85.0), 1));
    }

    @Test
    void personalRecordRejectsNegativeExclusionIndex() {
        WorkoutLog log = new WorkoutLog();
        StrengthEntry candidate = new StrengthEntry("bench press", 3, 10, 80.0);

        assertThrows(IllegalArgumentException.class,
                () -> log.isPersonalRecord(candidate, -1));
    }

    @Test
    void personalRecordRejectsExclusionIndexOutsideLog() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));
        StrengthEntry candidate = new StrengthEntry("bench press", 3, 10, 82.5);

        assertThrows(IllegalArgumentException.class,
                () -> log.isPersonalRecord(candidate, log.size()));
    }

    @Test
    void findByNameMatchesCaseInsensitiveSubstringsWithOriginalPositions() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));
        log.add(new CardioEntry("run", 30, null));
        log.add(new StrengthEntry("overhead press", 3, 8, 40.0));

        List<WorkoutLog.EntryMatch> matches = log.findByName("PRESS");

        assertEquals(2, matches.size());
        assertEquals(1, matches.get(0).position());
        assertEquals("bench press", matches.get(0).entry().getName());
        assertEquals(3, matches.get(1).position());
        assertEquals("overhead press", matches.get(1).entry().getName());
    }

    @Test
    void findByNameReturnsEmptyWhenNothingMatches() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));
        log.add(new CardioEntry("run", 30, null));

        List<WorkoutLog.EntryMatch> matches = log.findByName("squat");

        assertTrue(matches.isEmpty());
    }

    @Test
    void findByExerciseNameNormalisesNamesAndKeepsMixedTypesInLoggedOrder() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("Bench   Press", 3, 10, 80.0));
        log.add(new CardioEntry("run", 30, 5.0));
        log.add(new CardioEntry(" bench press ", 20, null));

        List<WorkoutLog.EntryMatch> matches = log.findByExerciseName("BENCH PRESS");

        assertEquals(2, matches.size());
        assertEquals(1, matches.get(0).position());
        assertInstanceOf(StrengthEntry.class, matches.get(0).entry());
        assertEquals(3, matches.get(1).position());
        assertInstanceOf(CardioEntry.class, matches.get(1).entry());
    }

    @Test
    void findByExerciseNameReturnsEmptyWhenNothingMatches() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));

        List<WorkoutLog.EntryMatch> matches = log.findByExerciseName("squat");

        assertTrue(matches.isEmpty());
    }

    @Test
    void calculateTotalsSumsAllStrengthVolume() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));
        log.add(new StrengthEntry("squat", 3, 5, 100.0));

        WorkoutLog.TrainingTotals totals = log.calculateTotals();

        assertEquals(3900.0, totals.strengthVolume());
        assertEquals(0, totals.cardioDurationMinutes());
    }

    @Test
    void calculateTotalsSumsAllCardioDuration() {
        WorkoutLog log = new WorkoutLog();
        log.add(new CardioEntry("run", 30, 5.0));
        log.add(new CardioEntry("cycle", 45, null));

        WorkoutLog.TrainingTotals totals = log.calculateTotals();

        assertEquals(0.0, totals.strengthVolume());
        assertEquals(75, totals.cardioDurationMinutes());
    }

    @Test
    void calculateTotalsReturnsZeroForAnEmptyLog() {
        WorkoutLog log = new WorkoutLog();

        WorkoutLog.TrainingTotals totals = log.calculateTotals();

        assertEquals(0.0, totals.strengthVolume());
        assertEquals(0, totals.cardioDurationMinutes());
    }

    @Test
    void calculateTotalsPreservesDecimalStrengthVolume() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("dumbbell curl", 1, 1, 82.55));

        WorkoutLog.TrainingTotals totals = log.calculateTotals();

        assertEquals(82.55, totals.strengthVolume());
        assertEquals(0, totals.cardioDurationMinutes());
    }

    @Test
    void calculateTotalsDoesNotOverflowDuringStrengthMultiplication() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("leg press", Integer.MAX_VALUE, 2, 1.0));

        WorkoutLog.TrainingTotals totals = log.calculateTotals();

        assertEquals(4_294_967_294.0, totals.strengthVolume());
    }

    @Test
    void calculateTotalsDoesNotOverflowAccumulatedCardioDuration() {
        WorkoutLog log = new WorkoutLog();
        log.add(new CardioEntry("cycle", Integer.MAX_VALUE, null));
        log.add(new CardioEntry("run", Integer.MAX_VALUE, null));

        WorkoutLog.TrainingTotals totals = log.calculateTotals();

        assertEquals(4_294_967_294L, totals.cardioDurationMinutes());
    }
}
