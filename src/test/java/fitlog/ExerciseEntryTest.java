package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * Tests formatting and personal-record values supplied by exercise entry types.
 */
class ExerciseEntryTest {

    @Test
    void loggedAtDisplayUsesReadableMinutePrecision() {
        StrengthEntry entry = new StrengthEntry("bench press", 3, 10, 80.0,
                LocalDateTime.of(2026, 8, 17, 17, 30, 45));

        String loggingTime = entry.getLoggedAtDisplay();

        assertEquals("2026-08-17 17:30", loggingTime);
    }

    @Test
    void legacyEntryDisplaysUnknownLoggingTime() {
        CardioEntry entry = new CardioEntry("run", 30, null, null);

        String loggingTime = entry.getLoggedAtDisplay();

        assertEquals("time not recorded", loggingTime);
    }

    @Test
    void newEntryReceivesAutomaticLoggingTime() {
        StrengthEntry entry = new StrengthEntry("bench press", 3, 10, 80.0);

        assertNotNull(entry.getLoggedAt());
    }

    @Test
    void strengthDetailsFormatsWholeNumberWeightWithoutDecimal() {
        StrengthEntry entry = new StrengthEntry("bench press", 3, 10, 80.0);

        String details = entry.getDetails();

        assertEquals("3 sets x 10 reps @ 80kg", details);
    }

    @Test
    void strengthDetailsFormatsDecimalWeight() {
        StrengthEntry entry = new StrengthEntry("bench press", 3, 10, 82.5);

        String details = entry.getDetails();

        assertEquals("3 sets x 10 reps @ 82.5kg", details);
    }

    @Test
    void strengthPrMetricIsWeight() {
        StrengthEntry entry = new StrengthEntry("bench press", 3, 10, 82.5);

        double metric = entry.getPrMetric();

        assertEquals(82.5, metric);
    }

    @Test
    void strengthPrDescriptionUsesHeaviestLabel() {
        StrengthEntry entry = new StrengthEntry("bench press", 3, 10, 80.0);

        String description = entry.getPrDescription();

        assertEquals("Heaviest bench press: 80kg", description);
    }

    @Test
    void cardioDetailsIncludesDistanceWhenPresent() {
        CardioEntry entry = new CardioEntry("run", 30, 5.0);

        String details = entry.getDetails();

        assertEquals("30 min, 5km", details);
    }

    @Test
    void cardioDetailsOmitsDistanceWhenNull() {
        CardioEntry entry = new CardioEntry("run", 30, null);

        String details = entry.getDetails();

        assertEquals("30 min", details);
    }

    @Test
    void cardioDetailsFormatsDecimalDistance() {
        CardioEntry entry = new CardioEntry("run", 30, 5.5);

        String details = entry.getDetails();

        assertEquals("30 min, 5.5km", details);
    }

    @Test
    void cardioPrMetricIsDuration() {
        CardioEntry entry = new CardioEntry("run", 30, 5.0);

        double metric = entry.getPrMetric();

        assertEquals(30.0, metric);
    }

    @Test
    void cardioPrDescriptionUsesLongestLabel() {
        CardioEntry entry = new CardioEntry("run", 30, 5.0);

        String description = entry.getPrDescription();

        assertEquals("Longest run: 30 min", description);
    }

    @Test
    void entriesRejectNullOrBlankNames() {
        assertThrows(IllegalArgumentException.class,
                () -> new StrengthEntry(null, 3, 10, 80.0));
        assertThrows(IllegalArgumentException.class,
                () -> new CardioEntry("   ", 30, null));
    }

    @Test
    void strengthEntryRejectsNonPositiveCounts() {
        assertThrows(IllegalArgumentException.class,
                () -> new StrengthEntry("bench press", 0, 10, 80.0));
        assertThrows(IllegalArgumentException.class,
                () -> new StrengthEntry("bench press", 3, -1, 80.0));
    }

    @Test
    void strengthEntryRejectsInvalidWeight() {
        assertThrows(IllegalArgumentException.class,
                () -> new StrengthEntry("bench press", 3, 10, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new StrengthEntry("bench press", 3, 10, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new StrengthEntry("bench press", 3, 10, Double.POSITIVE_INFINITY));
    }

    @Test
    void cardioEntryRejectsNonPositiveDuration() {
        assertThrows(IllegalArgumentException.class,
                () -> new CardioEntry("run", 0, null));
    }

    @Test
    void cardioEntryRejectsInvalidSuppliedDistance() {
        assertThrows(IllegalArgumentException.class,
                () -> new CardioEntry("run", 30, -1.0));
        assertThrows(IllegalArgumentException.class,
                () -> new CardioEntry("run", 30, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new CardioEntry("run", 30, Double.POSITIVE_INFINITY));
    }
}
