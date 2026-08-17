package fitlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests loading and saving tab-separated workout-entry storage files.
 */
class StorageTest {

    @Test
    void loadMissingFileReturnsNoEntriesAndNoWarnings(@TempDir Path tempDir) throws IOException {
        Storage storage = new Storage(tempDir.resolve("fitlog.txt"));

        Storage.LoadResult result = storage.load();

        assertTrue(result.entries().isEmpty());
        assertTrue(result.warnings().isEmpty());
    }

    @Test
    void loadReadsValidStrengthEntry(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("fitlog.txt");
        Files.write(file, List.of("strength\tbench press\t3\t10\t80.0"));
        Storage storage = new Storage(file);

        Storage.LoadResult result = storage.load();

        assertEquals(1, result.entries().size());
        StrengthEntry entry = assertInstanceOf(StrengthEntry.class, result.entries().get(0));
        assertEquals("bench press", entry.getName());
        assertEquals(3, entry.getSets());
        assertEquals(10, entry.getReps());
        assertEquals(80.0, entry.getWeightKg());
        assertNull(entry.getLoggedAt());
        assertTrue(result.warnings().isEmpty());
    }

    @Test
    void loadReadsCardioEntryWithDistance(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("fitlog.txt");
        Files.write(file, List.of("cardio\trun\t30\t5.0"));
        Storage storage = new Storage(file);

        Storage.LoadResult result = storage.load();

        assertEquals(1, result.entries().size());
        CardioEntry entry = assertInstanceOf(CardioEntry.class, result.entries().get(0));
        assertEquals("run", entry.getName());
        assertEquals(30, entry.getDurationMinutes());
        assertEquals(5.0, entry.getDistanceKm());
        assertNull(entry.getLoggedAt());
        assertTrue(result.warnings().isEmpty());
    }

    @Test
    void loadReadsCardioEntryWithoutDistance(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("fitlog.txt");
        Files.write(file, List.of("cardio\tstationary bike\t45\t"));
        Storage storage = new Storage(file);

        Storage.LoadResult result = storage.load();

        assertEquals(1, result.entries().size());
        CardioEntry entry = assertInstanceOf(CardioEntry.class, result.entries().get(0));
        assertEquals("stationary bike", entry.getName());
        assertEquals(45, entry.getDurationMinutes());
        assertNull(entry.getDistanceKm());
        assertNull(entry.getLoggedAt());
        assertTrue(result.warnings().isEmpty());
    }

    @Test
    void loadSkipsMalformedStrengthLineAndReportsItsLineNumber(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("fitlog.txt");
        Files.write(file, List.of("strength\tbench press\tthree\t10\t80.0"));
        Storage storage = new Storage(file);

        Storage.LoadResult result = storage.load();

        assertTrue(result.entries().isEmpty());
        assertEquals(1, result.warnings().size());
        assertTrue(result.warnings().get(0).contains("line 1"));
    }

    @Test
    void loadSkipsMalformedCardioLineAndReportsItsLineNumber(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("fitlog.txt");
        Files.write(file, List.of("cardio\trun\t30\tfive"));
        Storage storage = new Storage(file);

        Storage.LoadResult result = storage.load();

        assertTrue(result.entries().isEmpty());
        assertEquals(1, result.warnings().size());
        assertTrue(result.warnings().get(0).contains("line 1"));
    }

    @Test
    void loadKeepsValidLinesAroundMalformedLines(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("fitlog.txt");
        Files.write(file, List.of(
                "strength\tbench press\t3\t10\t80.0",
                "this is not a valid entry",
                "cardio\trun\t30\t5.0"));
        Storage storage = new Storage(file);

        Storage.LoadResult result = storage.load();

        assertEquals(2, result.entries().size());
        assertEquals("bench press", result.entries().get(0).getName());
        assertEquals("run", result.entries().get(1).getName());
        assertEquals(1, result.warnings().size());
        assertTrue(result.warnings().get(0).contains("line 2"));
    }

    @Test
    void saveThenLoadPreservesAllEntryData(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("fitlog.txt");
        Storage storage = new Storage(file);
        List<ExerciseEntry> entries = List.of(
                new StrengthEntry("bench press", 3, 10, 82.5),
                new CardioEntry("run", 30, 5.0),
                new CardioEntry("stationary bike", 45, null));

        storage.save(entries);
        Storage.LoadResult result = storage.load();

        assertEquals(3, result.entries().size());
        assertEquals("bench press", result.entries().get(0).getName());
        assertEquals("3 sets x 10 reps @ 82.5kg", result.entries().get(0).getDetails());
        assertEquals("run", result.entries().get(1).getName());
        assertEquals("30 min, 5km", result.entries().get(1).getDetails());
        assertEquals("stationary bike", result.entries().get(2).getName());
        assertEquals("45 min", result.entries().get(2).getDetails());
        assertTrue(result.warnings().isEmpty());
    }

    @Test
    void saveThenLoadPreservesLoggingTimes(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("fitlog.txt");
        Storage storage = new Storage(file);
        LocalDateTime strengthTime = LocalDateTime.of(2026, 8, 17, 9, 15);
        LocalDateTime cardioTime = LocalDateTime.of(2026, 8, 17, 18, 45, 30);
        List<ExerciseEntry> entries = List.of(
                new StrengthEntry("bench press", 3, 10, 82.5, strengthTime),
                new CardioEntry("run", 30, 5.0, cardioTime));

        storage.save(entries);
        Storage.LoadResult result = storage.load();

        assertEquals(strengthTime, result.entries().get(0).getLoggedAt());
        assertEquals(cardioTime, result.entries().get(1).getLoggedAt());
        assertTrue(result.warnings().isEmpty());
    }

    @Test
    void saveCreatesMissingParentDirectory(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("nested").resolve("data").resolve("fitlog.txt");
        Storage storage = new Storage(file);
        List<ExerciseEntry> entries = List.of(new StrengthEntry("bench press", 3, 10, 80.0));

        storage.save(entries);

        assertTrue(Files.exists(file));
        assertFalse(Files.readAllLines(file).isEmpty());
    }
}
