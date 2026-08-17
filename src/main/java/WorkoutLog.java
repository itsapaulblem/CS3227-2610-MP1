import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Stores the entries in the current workout session and performs collection-level operations.
 */
public class WorkoutLog {
    private final List<ExerciseEntry> entries = new ArrayList<>();

    /**
     * Adds an exercise entry to the current session.
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
     * Returns the number of entries in the current session.
     *
     * @return the entry count
     */
    public int size() {
        return entries.size();
    }

    /**
     * Checks whether the current session contains no entries.
     *
     * @return whether the session is empty
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Returns a read-only view of the current session's entries for listing.
     *
     * @return the entries in logging order
     */
    public List<ExerciseEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Checks whether a candidate strictly improves on every other matching entry.
     *
     * @param candidate the entry being logged or edited
     * @param excludedIndex the entry to exclude during an edit, or {@code -1} when logging
     * @return whether the candidate is a new personal record
     */
    public boolean isPersonalRecord(ExerciseEntry candidate, int excludedIndex) {
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
}
