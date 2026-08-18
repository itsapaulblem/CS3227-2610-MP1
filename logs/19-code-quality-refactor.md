# 19 – Refactor code to ensure code quality

## What was requested

The user supplied a code-quality and SOLID-principles audit and asked if any of the code violated
these principles. The work covered responsibility
separation, command extensibility, the exercise-entry hierarchy, domain
invariants, assertion use, dependency inversion, duplicated validation, JavaFX
magic values, and inaccurate naming. The user then requested a final audit for
any remaining violations.

## Incremental refactors

### Single Responsibility Principle

Commit `f61d192` reduced `FitLogController` from a large multi-purpose class to a
small coordinator. Parsing, execution, immutable editing, formatting, and help
behaviour were extracted into focused collaborators, including `CommandParser`,
`CommandExecutor`, `EntryEditor`, and `EntryFormatter`.

### Open/Closed Principle

Commit `a6bd308` introduced `CommandDefinition` and `CommandRegistry` as the
single registration mechanism for recognition, parsing, execution, syntax, and
help examples. `Command` became an extensible interface, so a test-only command
could be added through registration without changing the central parser or an
execution switch.

### Honest exercise-entry hierarchy

Commit `5291afe` sealed `ExerciseEntry` to the final `StrengthEntry` and
`CardioEntry` classes. This made the code's two-type assumptions explicit and
prevented unsupported subclasses from being silently ignored, misformatted, or
cast incorrectly.

### Domain invariants and preconditions

Commit `492c545` moved name and measurement validation into the public entry
constructors so every creation path produces a valid object. Commit `4e03828`
replaced the public `WorkoutLog.isPersonalRecord` assertion precondition with an
always-on `IllegalArgumentException`, because Java assertions may be disabled.

### Dependency Inversion Principle

Commit `8694154` introduced the `EntryStorage` interface. The controller and
command pipeline now depend on that abstraction, while `Storage` remains the
file-backed production implementation. Controller tests can therefore use
in-memory results and precise load/save failure test doubles.

### Remaining readability and duplication findings

Commit `0e537b9` completed the lower-severity cleanup:

- Added `ExerciseValueValidator` as the shared source for name, positive integer,
  and finite positive decimal rules.
- Made storage loading rely on constructor validation instead of duplicating
  strength/cardio range checks.
- Replaced significant GUI dimensions, message width, and farewell delay with
  descriptive constants.
- Renamed the controller field from `entries` to `workoutLog` and corrected
  documentation from "current session" to the complete loaded workout history.
- Split the remaining long parser, registry, and startup methods into focused
  helpers.
- Checked for unused Java files; none were safe or necessary to remove.

## Final audit outcome

At the end of the refactor, the supplied checklist had no clear unresolved code
quality violation:

- Methods over 30 lines: 0.
- Production Java assertions: 0.
- Empty catch blocks: 0.
- Unreferenced Java files: 0.
- Inaccurate current-session terminology: 0.
- The then-current 56 tests passed with no failures; the later log 13 expansion
  increased this to 156 passing tests.

The audit retained several explicit boundaries rather than presenting them as
completed work:

- `WorkoutSession` is not modelled, even though the project domain says entries
  on the same date form a session. It should be introduced with actual grouping
  behaviour rather than as an unused placeholder.
- Small local JavaFX spacing values remain inline; major dimensions and timing
  values have descriptive constants.
- Some command keywords remain in command-specific parsing as well as registry
  matchers, although recognition, execution, and help metadata are centralised.
- GUI appearance remains manually verified.

## Prompt record and Codex outcome

> "Don't edit any code, but can you check for code quality, SOLID principles,
> check if there are any violations and flag them out for me? Think step by step"

> "FitLogController has too many responsibilities — SRP ... This should be done
> incrementally, not as a single large refactor."

> "I changed the code, how was the overall audit, any violations still?"

Codex first reported the design findings without modifying code. After receiving
separate implementation requests, it applied each refactor in small commits,
ran the Java 25 test suite after the increments, and finished with the automated
checks and explicit limitations recorded above.
