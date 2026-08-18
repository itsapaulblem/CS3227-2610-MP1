package fitlog;

import java.util.List;

/**
 * Stores and displays the syntax and example for every supported command.
 */
final class HelpFormatter {
    private static final List<HelpEntry> HELP_ENTRIES = List.of(
            new HelpEntry("log strength <name> /sets <n> /reps <n> /weight <kg>",
                    "log strength bench press /sets 3 /reps 10 /weight 80"),
            new HelpEntry("log cardio <name> /duration <min> [/distance <km>]",
                    "log cardio run /duration 30 /distance 5"),
            new HelpEntry("list", "list"),
            new HelpEntry("edit <index> /sets <n> | edit <index> /reps <n> | "
                    + "edit <index> /weight <kg> | edit <index> /duration <min> | "
                    + "edit <index> /distance <km>", "edit 1 /weight 82.5"),
            new HelpEntry("delete <index>", "delete 2"),
            new HelpEntry("find <search term>", "find press"),
            new HelpEntry("stats <exercise name>", "stats bench press"),
            new HelpEntry("volume", "volume"),
            new HelpEntry("help", "help"),
            new HelpEntry("bye", "bye"));

    private HelpFormatter() {
    }

    /** Displays every command syntax followed by a visually distinct example. */
    static void showHelp(Ui ui) {
        for (HelpEntry entry : HELP_ENTRIES) {
            ui.showInfo(entry.syntax());
            ui.showExample("Example: " + entry.example());
        }
    }

    /** Associates one command syntax description with a valid example. */
    private record HelpEntry(String syntax, String example) {
    }
}
