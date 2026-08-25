package fitlog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Provides the single registration point for FitLog's supported commands.
 * Parsing, execution, and help all use the same definitions.
 */
final class CommandRegistry {
    private final List<CommandDefinition<?>> definitions;

    /** Creates a registry from the supplied command definitions. */
    CommandRegistry(List<CommandDefinition<?>> definitions) {
        this.definitions = List.copyOf(definitions);
    }

    /**
     * Returns a new registry containing one additional command definition.
     * Existing registries remain unchanged.
     */
    CommandRegistry with(CommandDefinition<?> definition) {
        List<CommandDefinition<?>> extendedDefinitions = new ArrayList<>(definitions);
        extendedDefinitions.add(definition);
        return new CommandRegistry(extendedDefinitions);
    }

    /** Creates the registry used by the FitLog application. */
    static CommandRegistry createDefault() {
        return new CommandRegistry(List.of(
                strengthLogDefinition(), cardioLogDefinition(), listDefinition(), editDefinition(),
                deleteDefinition(), findDefinition(), statsDefinition(), volumeDefinition(),
                helpDefinition(), byeDefinition()));
    }

    private static CommandDefinition<LogStrengthCommand> strengthLogDefinition() {
        return definition(LogStrengthCommand.class, startsWithCommand("log strength"),
                (input, _, ui) -> CommandParser.parseLogStrengthCommand(input, ui),
                (command, workoutLog, storage, ui, _) ->
                        CommandExecutor.executeLogStrength(command, workoutLog, storage, ui),
                "log strength <name> /sets <n> /reps <n> /weight <kg>",
                "log strength bench press /sets 3 /reps 10 /weight 80");
    }

    private static CommandDefinition<LogCardioCommand> cardioLogDefinition() {
        return definition(LogCardioCommand.class, startsWithCommand("log cardio"),
                (input, _, ui) -> CommandParser.parseLogCardioCommand(input, ui),
                (command, workoutLog, storage, ui, _) ->
                        CommandExecutor.executeLogCardio(command, workoutLog, storage, ui),
                "log cardio <name> /duration <min> [/distance <km>]",
                "log cardio run /duration 30 /distance 5");
    }

    private static CommandDefinition<ListCommand> listDefinition() {
        return definition(ListCommand.class, exactCommand("list"),
                (_, _, _) -> new ListCommand(),
                (_, workoutLog, _, ui, _) ->
                        CommandExecutor.executeList(workoutLog, ui),
                "list", "list");
    }

    private static CommandDefinition<EditCommand> editDefinition() {
        return definition(EditCommand.class, startsWithCommand("edit"), CommandParser::parseEditCommand,
                (command, workoutLog, storage, ui, _) ->
                        CommandExecutor.executeEdit(command, workoutLog, storage, ui),
                "edit <index> /sets <n> | edit <index> /reps <n> | "
                        + "edit <index> /weight <kg> | edit <index> /duration <min> | "
                        + "edit <index> /distance <km>",
                "edit 1 /weight 82.5");
    }

    private static CommandDefinition<DeleteCommand> deleteDefinition() {
        return definition(DeleteCommand.class, startsWithCommand("delete"), CommandParser::parseDeleteCommand,
                (command, workoutLog, storage, ui, _) ->
                        CommandExecutor.executeDelete(command, workoutLog, storage, ui),
                "delete <index>", "delete 2");
    }

    private static CommandDefinition<FindCommand> findDefinition() {
        return definition(FindCommand.class, startsWithCommand("find"),
                (input, _, ui) -> CommandParser.parseFindCommand(input, ui),
                (command, workoutLog, _, ui, _) ->
                        CommandExecutor.executeFind(command, workoutLog, ui),
                "find <search term>", "find press");
    }

    private static CommandDefinition<StatsCommand> statsDefinition() {
        return definition(StatsCommand.class, startsWithCommand("stats"),
                (input, _, ui) -> CommandParser.parseStatsCommand(input, ui),
                (command, workoutLog, _, ui, _) ->
                        CommandExecutor.executeStats(command, workoutLog, ui),
                "stats <exercise name>", "stats bench press");
    }

    private static CommandDefinition<VolumeCommand> volumeDefinition() {
        return definition(VolumeCommand.class, exactCommand("volume"),
                (_, _, _) -> new VolumeCommand(),
                (_, workoutLog, _, ui, _) ->
                        CommandExecutor.executeVolume(workoutLog, ui),
                "volume", "volume");
    }

    private static CommandDefinition<HelpCommand> helpDefinition() {
        return definition(HelpCommand.class, exactCommand("help"),
                (_, _, _) -> new HelpCommand(),
                (_, _, _, ui, registry) -> {
                    registry.showHelp(ui);
                    return false;
                },
                "help", "help");
    }

    private static CommandDefinition<ByeCommand> byeDefinition() {
        return definition(ByeCommand.class, exactCommand("bye"),
                (_, _, _) -> new ByeCommand(),
                (_, _, _, ui, _) ->
                        CommandExecutor.executeBye(ui),
                "bye", "bye");
    }

    /** Parses input using the first matching command definition. */
    Command parse(String input, WorkoutLog workoutLog, Ui ui) {
        String command = input.trim();
        for (CommandDefinition<?> definition : definitions) {
            if (definition.matchesInput().test(command)) {
                return definition.parser().parse(command, workoutLog, ui);
            }
        }
        CommandParser.reportUnrecognizedCommand(command, ui);
        return null;
    }

    /** Executes a command using the definition registered for its runtime type. */
    boolean execute(Command command, WorkoutLog workoutLog, EntryStorage storage, Ui ui) {
        for (CommandDefinition<?> definition : definitions) {
            if (definition.commandType().isInstance(command)) {
                return executeWithDefinition(definition, command, workoutLog, storage, ui);
            }
        }
        throw new IllegalArgumentException("No handler registered for command type: " + command.getClass().getName());
    }

    /** Displays help generated from the same definitions used for parsing. */
    void showHelp(Ui ui) {
        for (CommandDefinition<?> definition : definitions) {
            ui.showInfo(definition.syntax());
            ui.showExample("Example: " + definition.example());
        }
    }

    private <C extends Command> boolean executeWithDefinition(CommandDefinition<C> definition,
            Command command, WorkoutLog workoutLog, EntryStorage storage, Ui ui) {
        C typedCommand = definition.commandType().cast(command);
        return definition.executor().execute(typedCommand, workoutLog, storage, ui, this);
    }

    private static <C extends Command> CommandDefinition<C> definition(Class<C> commandType,
            Predicate<String> matchesInput, CommandDefinition.Parser<C> parser,
            CommandDefinition.Executor<C> executor, String syntax, String example) {
        return new CommandDefinition<>(commandType, matchesInput, parser, executor, syntax, example);
    }

    private static Predicate<String> exactCommand(String commandName) {
        return input -> input.equals(commandName);
    }

    private static Predicate<String> startsWithCommand(String commandName) {
        return input -> input.equals(commandName) || input.startsWith(commandName + " ");
    }

}
