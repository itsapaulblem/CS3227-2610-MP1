package fitlog;

import java.util.function.Predicate;

/**
 * Describes how one command is recognised, parsed, executed, and documented.
 *
 * @param commandType the command object produced by the parser
 * @param matchesInput whether raw input belongs to this command
 * @param parser the command-specific parser
 * @param executor the command-specific behaviour
 * @param syntax the syntax displayed by {@code help}
 * @param example a valid example displayed by {@code help}
 * @param <C> the command type handled by this definition
 */
record CommandDefinition<C extends Command>(Class<C> commandType, Predicate<String> matchesInput,
        CommandDefinition.Parser<C> parser, CommandDefinition.Executor<C> executor,
        String syntax, String example) {

    /** Parses matched input into a validated command. */
    @FunctionalInterface
    interface Parser<C extends Command> {
        C parse(String input, WorkoutLog entries, Ui ui);
    }

    /** Executes a command using the application's shared services. */
    @FunctionalInterface
    interface Executor<C extends Command> {
        boolean execute(C command, WorkoutLog entries, Storage storage, Ui ui, CommandRegistry registry);
    }
}
