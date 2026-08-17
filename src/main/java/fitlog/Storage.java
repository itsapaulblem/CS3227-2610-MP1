package fitlog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and saves workout entries using a tab-separated text file.
 */
public class Storage {
    private final Path filePath;

    /**
     * Creates storage backed by the specified file.
     *
     * @param filePath the path of the workout data file
     */
    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads all valid entries and records a warning for every malformed line.
     *
     * @return the loaded entries and warnings
     * @throws IOException if the data file cannot be read
     */
    public LoadResult load() throws IOException {
        List<ExerciseEntry> entries = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return new LoadResult(entries, warnings);
        }

        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        for (int index = 0; index < lines.size(); index++) {
            ExerciseEntry entry = parseEntry(lines.get(index));
            if (entry == null) {
                warnings.add("Warning: skipped malformed entry on line " + (index + 1) + ".");
            } else {
                entries.add(entry);
            }
        }
        return new LoadResult(entries, warnings);
    }

    /**
     * Saves entries by writing a temporary file before replacing the data file.
     *
     * @param entries the entries to save
     * @throws IOException if the entries cannot be written
     */
    public void save(List<ExerciseEntry> entries) throws IOException {
        Path parentDirectory = filePath.getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }
        Path temporaryFile = Files.createTempFile(parentDirectory, "fitlog-", ".tmp");
        try {
            Files.write(temporaryFile, formatEntries(entries), StandardCharsets.UTF_8);
            replaceFile(temporaryFile);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    /**
     * Parses one tab-separated entry line.
     *
     * @param line the line to parse
     * @return the parsed entry, or {@code null} when malformed
     */
    private ExerciseEntry parseEntry(String line) {
        String[] fields = line.split("\\t", -1);
        try {
            return switch (fields[0]) {
            case "strength" -> parseStrengthEntry(fields);
            case "cardio" -> parseCardioEntry(fields);
            default -> null;
            };
        } catch (NumberFormatException | DateTimeParseException exception) {
            return null;
        }
    }

    /**
     * Parses a strength entry line.
     *
     * @param fields the tab-separated fields
     * @return the strength entry, or {@code null} when malformed
     */
    private StrengthEntry parseStrengthEntry(String[] fields) {
        if ((fields.length != 5 && fields.length != 6) || fields[1].isBlank()) {
            return null;
        }
        int sets = Integer.parseInt(fields[2]);
        int reps = Integer.parseInt(fields[3]);
        double weightKg = Double.parseDouble(fields[4]);
        if (sets <= 0 || reps <= 0 || !Double.isFinite(weightKg) || weightKg <= 0) {
            return null;
        }
        LocalDateTime loggedAt = fields.length == 6 ? parseLoggedAt(fields[5]) : null;
        return new StrengthEntry(fields[1], sets, reps, weightKg, loggedAt);
    }

    /**
     * Parses a cardio entry line.
     *
     * @param fields the tab-separated fields
     * @return the cardio entry, or {@code null} when malformed
     */
    private CardioEntry parseCardioEntry(String[] fields) {
        if ((fields.length != 4 && fields.length != 5) || fields[1].isBlank()) {
            return null;
        }
        int durationMinutes = Integer.parseInt(fields[2]);
        Double distanceKm = fields[3].isEmpty() ? null : Double.parseDouble(fields[3]);
        if (durationMinutes <= 0 || (distanceKm != null && (!Double.isFinite(distanceKm) || distanceKm <= 0))) {
            return null;
        }
        LocalDateTime loggedAt = fields.length == 5 ? parseLoggedAt(fields[4]) : null;
        return new CardioEntry(fields[1], durationMinutes, distanceKm, loggedAt);
    }

    /**
     * Parses a saved logging time, allowing an empty field for a legacy entry.
     *
     * @param value the saved ISO-8601 timestamp
     * @return the parsed time, or {@code null} when the historical time is unknown
     */
    private LocalDateTime parseLoggedAt(String value) {
        return value.isEmpty() ? null : LocalDateTime.parse(value);
    }

    /**
     * Formats every entry for writing to the storage file.
     *
     * @param entries the entries to format
     * @return the formatted file lines
     */
    private List<String> formatEntries(List<ExerciseEntry> entries) {
        List<String> lines = new ArrayList<>();
        for (ExerciseEntry entry : entries) {
            // Names cannot contain literal tabs because FitLog rebuilds names by joining whitespace-split tokens.
            if (entry instanceof StrengthEntry strengthEntry) {
                lines.add("strength\t" + entry.getName() + "\t" + strengthEntry.getSets() + "\t"
                        + strengthEntry.getReps() + "\t" + strengthEntry.getWeightKg() + "\t"
                        + formatLoggedAt(entry));
            } else if (entry instanceof CardioEntry cardioEntry) {
                String distance = cardioEntry.getDistanceKm() == null ? "" : cardioEntry.getDistanceKm().toString();
                lines.add("cardio\t" + entry.getName() + "\t" + cardioEntry.getDurationMinutes() + "\t" + distance
                        + "\t" + formatLoggedAt(entry));
            }
        }
        return lines;
    }

    /**
     * Formats a timestamp for persistence while retaining unknown legacy times as empty fields.
     *
     * @param entry the entry being persisted
     * @return an ISO-8601 timestamp, or an empty string when the timestamp is unknown
     */
    private String formatLoggedAt(ExerciseEntry entry) {
        return entry.getLoggedAt() == null ? "" : entry.getLoggedAt().toString();
    }

    /**
     * Replaces the data file with the completed temporary file.
     *
     * @param temporaryFile the file containing the new data
     * @throws IOException if replacement fails
     */
    private void replaceFile(Path temporaryFile) throws IOException {
        try {
            Files.move(temporaryFile, filePath, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Contains entries loaded from storage and warnings about skipped lines.
     *
     * @param entries the valid loaded entries
     * @param warnings warnings generated while loading
     */
    public record LoadResult(List<ExerciseEntry> entries, List<String> warnings) {
    }
}
