/**
 * Represents a request to search entry names for a case-insensitive substring.
 *
 * @param searchTerm the text to search for
 */
public record FindCommand(String searchTerm) implements Command {
}
