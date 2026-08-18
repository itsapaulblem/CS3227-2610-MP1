package fitlog;

import java.io.IOException;
import java.util.List;

/**
 * Loads and saves exercise entries independently of a particular persistence
 * format or location.
 */
public interface EntryStorage {
    /**
     * Loads all valid entries and any non-fatal warnings.
     *
     * @return the loaded entries and warnings
     * @throws IOException if the underlying data source cannot be read
     */
    LoadResult load() throws IOException;

    /**
     * Persists the supplied entries.
     *
     * @param entries the entries to save
     * @throws IOException if the underlying data destination cannot be written
     */
    void save(List<ExerciseEntry> entries) throws IOException;

    /**
     * Contains entries loaded from storage and non-fatal load warnings.
     *
     * @param entries the valid loaded entries
     * @param warnings warnings generated while loading
     */
    record LoadResult(List<ExerciseEntry> entries, List<String> warnings) {
    }
}
