# 09 – Command hierarchy and routing

## What was requested

The next refactor introduced a sealed `Command` hierarchy while keeping changes
small and testable. Simple commands were migrated before the denser log paths.

## What landed

- `ByeCommand`, `ListCommand`, and `DeleteCommand` were introduced first.
- `EditCommand` then carried a validated index, field, and raw value.
- `LogStrengthCommand` and `LogCardioCommand` carried validated logging values.
- Finally, a router resolved raw input to a command so the main loop became
  read → resolve → execute → exit check.

## Notable feedback

The user accepted a temporary special case for delete during the first migration
and explicitly noted it would be cleaned up in the controller step. Validation
messages and PR behaviour were preserved throughout.

## Cross-reference

The command-router extraction is recorded in commit `949e761`.

## Prompt record and Codex outcome

> "Proceed to the next step: introduce a Command sealed interface and migrate
> the simplest commands first, ByeCommand, ListCommand, and DeleteCommand."

> "Proceed to migrate edit into the Command hierarchy ... Keep all existing
> validation messages and PR-recheck behavior identical."

> "Proceed to migrate log strength and log cardio into the Command hierarchy."

> "main() now has four near-identical dispatch blocks ... Extract a single
> command-router step ... Explain your approach before implementing."

Codex migrated command types in that order and preserved transcript checks. The
final router unified parser choice and command execution.

## Acceptance condition

I accepted the command migration only after each incremental step preserved the
regression transcript, validation messages, delete and edit behaviour, and PR
rechecking, and the final router removed the repeated dispatch blocks.
