package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests collection-level workout logging, personal-record, and search behaviour.
 */
class WorkoutLogTest {

    @Test
    void firstEntryForExerciseIsNotPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        StrengthEntry candidate = new StrengthEntry("bench press", 3, 10, 80.0);

        assertFalse(log.isPersonalRecord(candidate, -1));
    }

    @Test
    void heavierStrengthEntryIsPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));
        StrengthEntry candidate = new StrengthEntry("bench press", 3, 8, 82.5);

        assertTrue(log.isPersonalRecord(candidate, -1));
    }

    @Test
    void tiedStrengthWeightIsNotPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));
        StrengthEntry candidate = new StrengthEntry("bench press", 4, 6, 80.0);

        assertFalse(log.isPersonalRecord(candidate, -1));
    }

    @Test
    void lighterStrengthWeightIsNotPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));
        StrengthEntry candidate = new StrengthEntry("bench press", 3, 12, 70.0);

        assertFalse(log.isPersonalRecord(candidate, -1));
    }

    @Test
    void personalRecordMatchesNamesCaseInsensitivelyAndWithNormalisedWhitespace() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("Bench   Press", 3, 10, 80.0));
        StrengthEntry candidate = new StrengthEntry(" bench press ", 3, 8, 82.5);

        assertTrue(log.isPersonalRecord(candidate, -1));
    }

    @Test
    void differentExerciseNamesDoNotAffectPersonalRecord() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("squat", 3, 5, 100.0));
        StrengthEntry candidate = new StrengthEntry("bench press", 3, 10, 80.0);

        assertFalse(log.isPersonalRecord(candidate, -1));
    }

    @Test
    void differentEntryTypesDoNotCompeteForPersonalRecords() {
        WorkoutLog log = new WorkoutLog();
        log.add(new CardioEntry("run", 30, 5.0));
        StrengthEntry candidate = new StrengthEntry("run", 3, 10, 80.0);

        assertFalse(log.isPersonalRecord(candidate, -1));
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
    void personalRecordRejectsExclusionIndexBelowLoggingSentinel() {
        WorkoutLog log = new WorkoutLog();
        StrengthEntry candidate = new StrengthEntry("bench press", 3, 10, 80.0);

        assertThrows(IllegalArgumentException.class,
                () -> log.isPersonalRecord(candidate, -2));
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
    void calculateTotalsCombinesStrengthAndCardioEntries() {
        WorkoutLog log = new WorkoutLog();
        log.add(new StrengthEntry("bench press", 3, 10, 80.0));
        log.add(new CardioEntry("run", 30, 5.0));

        WorkoutLog.TrainingTotals totals = log.calculateTotals();

        assertEquals(2400.0, totals.strengthVolume());
        assertEquals(30, totals.cardioDurationMinutes());
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
}
