package fitlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Stores the complete loaded workout history and performs collection-level operations.
 */
public class WorkoutLog {
    private final List<ExerciseEntry> entries = new ArrayList<>();

    /**
     * Adds an exercise entry to the workout history.
     *
     * @param entry the entry to add
     */
    public void add(ExerciseEntry entry) {
        entries.add(entry);
    }

    /**
     * Removes and returns the entry at a zero-based index.
     *
     * @param index the zero-based entry index
     * @return the removed entry
     */
    public ExerciseEntry delete(int index) {
        return entries.remove(index);
    }

    /**
     * Replaces the entry at a zero-based index.
     *
     * @param index the zero-based entry index
     * @param updatedEntry the replacement entry
     */
    public void replace(int index, ExerciseEntry updatedEntry) {
        entries.set(index, updatedEntry);
    }

    /**
     * Returns the entry at a zero-based index.
     *
     * @param index the zero-based entry index
     * @return the matching entry
     */
    public ExerciseEntry get(int index) {
        return entries.get(index);
    }

    /**
     * Returns the number of entries in the workout history.
     *
     * @return the entry count
     */
    public int size() {
        return entries.size();
    }

    /**
     * Checks whether the workout history contains no entries.
     *
     * @return whether the session is empty
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Returns a read-only view of the workout history for listing and persistence.
     *
     * @return the entries in logging order
     */
    public List<ExerciseEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Finds entries whose names contain the specified text, ignoring letter case.
     *
     * @param term the text to search for
     * @return matching entries paired with their one-based positions in the full list
     */
    public List<EntryMatch> findByName(String term) {
        String normalisedTerm = term.toLowerCase(Locale.ROOT);
        List<EntryMatch> matches = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) {
            ExerciseEntry entry = entries.get(index);
            if (entry.getName().toLowerCase(Locale.ROOT).contains(normalisedTerm)) {
                matches.add(new EntryMatch(index + 1, entry));
            }
        }
        return matches;
    }

    /**
     * Finds entries with the specified exercise name after applying the same
     * normalisation used for personal-record comparison.
     *
     * @param name the exercise name to match
     * @return matching entries paired with their one-based positions in logging order
     */
    public List<EntryMatch> findByExerciseName(String name) {
        String normalisedName = normaliseExerciseName(name);
        List<EntryMatch> matches = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) {
            ExerciseEntry entry = entries.get(index);
            if (normaliseExerciseName(entry.getName()).equals(normalisedName)) {
                matches.add(new EntryMatch(index + 1, entry));
            }
        }
        return matches;
    }

    /**
     * Calculates training totals across every entry currently held by this log.
     *
     * @return the total strength volume and cardio duration
     */
    public TrainingTotals calculateTotals() {
        double strengthVolume = 0;
        int cardioDuration = 0;
        for (ExerciseEntry entry : entries) {
            if (entry instanceof StrengthEntry strengthEntry) {
                strengthVolume += strengthEntry.getSets() * strengthEntry.getReps() * strengthEntry.getWeightKg();
            } else if (entry instanceof CardioEntry cardioEntry) {
                cardioDuration += cardioEntry.getDurationMinutes();
            }
        }
        return new TrainingTotals(strengthVolume, cardioDuration);
    }

    /**
     * Checks whether a candidate strictly improves on every other matching entry.
     *
     * @param candidate the entry being logged or edited
     * @param excludedIndex the entry to exclude during an edit, or {@code -1} when logging
     * @return whether the candidate is a new personal record
     * @throws IllegalArgumentException if the exclusion index is neither {@code -1}
     *                                  nor the index of an existing entry
     */
    public boolean isPersonalRecord(ExerciseEntry candidate, int excludedIndex) {
        if (excludedIndex < -1 || excludedIndex >= entries.size()) {
            throw new IllegalArgumentException(
                    "Excluded PR index must be -1 or refer to an existing entry.");
        }
        boolean hasPriorMatchingEntry = false;
        String candidateName = normaliseExerciseName(candidate.getName());
        for (int index = 0; index < entries.size(); index++) {
            ExerciseEntry existingEntry = entries.get(index);
            if (index == excludedIndex || existingEntry.getClass() != candidate.getClass()
                    || !normaliseExerciseName(existingEntry.getName()).equals(candidateName)) {
                continue;
            }
            hasPriorMatchingEntry = true;
            if (candidate.getPrMetric() <= existingEntry.getPrMetric()) {
                return false;
            }
        }
        return hasPriorMatchingEntry;
    }

    /**
     * Converts an exercise name into a consistent comparison key.
     *
     * @param name the exercise name to normalise
     * @return a trimmed, whitespace-normalised, lowercase comparison key
     */
    private String normaliseExerciseName(String name) {
        return name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Associates a matching entry with its one-based position in the full list.
     *
     * @param position the entry's one-based list position
     * @param entry the matching entry
     */
    public record EntryMatch(int position, ExerciseEntry entry) {
    }

    /**
     * Holds calculated totals for all entries in a workout log.
     *
     * @param strengthVolume the sum of sets × reps × weight for strength entries
     * @param cardioDurationMinutes the sum of minutes for cardio entries
     */
    public record TrainingTotals(double strengthVolume, int cardioDurationMinutes) {
    }
}
