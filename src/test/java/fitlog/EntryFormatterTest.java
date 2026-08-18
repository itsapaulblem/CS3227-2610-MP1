package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Tests list, search, and statistics presentation independently from commands. */
class EntryFormatterTest {
    private static final LocalDateTime LOGGED_AT = LocalDateTime.of(2026, 8, 17, 9, 5);

    @Test
    void listEntryContainsPositionTypeDetailsAndTime() {
        StrengthEntry entry = new StrengthEntry("bench press", 3, 10, 82.5, LOGGED_AT);

        assertEquals("2. [Strength] bench press - 3 sets x 10 reps @ 82.5kg (logged 2026-08-17 09:05)",
                EntryFormatter.formatListEntry(2, entry));
    }

    @Test
    void findMatchKeepsOriginalPositionAndOmitsTime() {
        CardioEntry entry = new CardioEntry("run", 30, 5.5, LOGGED_AT);

        assertEquals("4. [Cardio] run - 30 min, 5.5km",
                EntryFormatter.formatFindMatch(new WorkoutLog.EntryMatch(4, entry)));
    }

    @Test
    void statsFormatsStrengthAndCardioMetricsWithCorrectUnits() {
        StrengthEntry strength = new StrengthEntry("bench", 3, 10, 80, LOGGED_AT);
        CardioEntry cardio = new CardioEntry("run", 30, null, null);

        assertEquals("1. [Strength] 80kg (logged 2026-08-17 09:05)",
                EntryFormatter.formatStatsMatch(new WorkoutLog.EntryMatch(1, strength)));
        assertEquals("2. [Cardio] 30 min (logged time not recorded)",
                EntryFormatter.formatStatsMatch(new WorkoutLog.EntryMatch(2, cardio)));
    }
}
