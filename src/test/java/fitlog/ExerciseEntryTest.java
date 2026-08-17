package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests formatting and personal-record values supplied by exercise entry types.
 */
class ExerciseEntryTest {

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
}
