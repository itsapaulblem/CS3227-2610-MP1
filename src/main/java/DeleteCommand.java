/**
 * Represents a request to delete one entry by its zero-based index.
 *
 * @param index the zero-based index of the entry to delete
 */
public record DeleteCommand(int index) implements Command {
}
