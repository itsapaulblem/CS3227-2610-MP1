package fitlog;

/**
 * Represents a recognised command that FitLog can execute.
 */
public sealed interface Command permits ByeCommand, ListCommand, DeleteCommand, EditCommand,
        LogStrengthCommand, LogCardioCommand, FindCommand, StatsCommand, VolumeCommand, HelpCommand {
}
