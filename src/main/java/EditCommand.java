/**
 * Represents a request to update one field of an entry at a zero-based index.
 *
 * @param index the zero-based index of the entry to update
 * @param field the field to update
 * @param value the new field value
 */
public record EditCommand(int index, String field, String value) implements Command {
}
