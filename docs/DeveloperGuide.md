# Developer Guide

## Document scope

This guide describes the current FitLog release and should be updated whenever
the product's architecture, behavior, dependencies, or development process
changes.

## Architecture

FitLog is organised into console and JavaFX entry points over a shared controller
and domain layer:

- `FitLog` is the console entry point. It reads console commands and passes them
  to a controller until the user exits or input ends.
- `Launcher` is the plain Java entry point used by Gradle's `run` task. It calls
  `Application.launch(FitLogGui.class, args)` rather than directly launching a
  JavaFX `Application` subclass.
- `FitLogGui` creates the JavaFX scene, wires Enter and Send-button events to the
  controller, and closes the window shortly after a `bye` response.
- `FitLogController` owns startup loading, command parsing, dispatch, the current
  `WorkoutLog`, and the `Storage` service. Its `start()` method emits the startup
  feedback, and `submit(String)` resolves and executes one command.
- `Ui` is an output interface with separate information, example, success, error,
  warning, and PR feedback methods. `ConsoleUi` implements it with plain console output,
  owns the `Scanner`, prints the `> ` prompt, and returns `null` at end-of-file.
- `GuiUi` implements `Ui` by rendering categorised messages as styled JavaFX
  conversation bubbles and scrolling to the newest message.
- `WorkoutLog` owns the in-memory `List<ExerciseEntry>`. It provides add, delete,
  replace, lookup, and listing operations, case-insensitive substring search with
  original list positions, normalised exact-name lookup for progression, and
  collection-level PR comparison and volume totals.
- `Storage` owns file I/O only. It loads valid entries with per-line malformed-data
  warnings and saves a supplied list to disk; it does not print user messages.
- `ExerciseEntry` is the shared abstraction for immutable `StrengthEntry` and
  `CardioEntry` values. It stores the immutable exercise name and logging time;
  each subtype supplies display details and its PR metric.
- `Command` is a sealed interface. The command records are `ByeCommand`,
  `ListCommand`, `DeleteCommand`, `EditCommand`, `LogStrengthCommand`,
  `LogCardioCommand`, `FindCommand`, `StatsCommand`, `VolumeCommand`, and
  `HelpCommand`. They
  represent successfully parsed commands; validation errors are reported during
  parsing before a command record is created.

At runtime, either `FitLog`/`ConsoleUi` or `Launcher`/`FitLogGui` supplies input
to `FitLogController`. The controller resolves the input into a `Command`, uses
`WorkoutLog` to perform collection operations, asks `Storage` to save successful
mutations, and returns categorised feedback through the selected `Ui`.

### GUI reuse after the controller refactor

The `start()`/`submit(String)` controller design was deliberately introduced
before the GUI. It replaces the former blocking console read loop with one-command
operations that JavaFX event handlers can call directly. This allows the GUI to
reuse the existing parsers, command records, validation messages, persistence, and
command execution without duplicating command logic.

`WorkoutLog`, `Storage`, `ExerciseEntry` and its subclasses, and the sealed
`Command` hierarchy required zero changes to support the GUI. Only the UI layer
and entry points differ between console and JavaFX execution.

### JavaFX and Gradle setup

`build.gradle.kts` configures a Java 25 toolchain. It applies the
`org.openjfx.javafxplugin` Gradle plugin at version `0.1.0`, configures JavaFX
`26.0.1`, and enables the `javafx.controls` module. The application main class is
`fitlog.Launcher`. The separate launcher avoids the classpath issues that can
occur when the Java launcher is asked to start a JavaFX `Application` subclass
directly.

The GradleUp Shadow plugin is configured at version `9.6.1` and participates in
the normal `build` lifecycle. Packaging produces two deliberately distinct JARs
in `build/libs/`:

- `fitlog.jar` is the all-in-one Shadow JAR containing runtime dependencies.
- `FitLog-plain.jar` is the thin JAR produced by Gradle's standard `jar` task.

The standard JAR uses the `plain` archive classifier so it does not share the
same output path as `shadowJar`. Without distinct output names, Gradle detects
implicit dependencies between `shadowJar`, `startScripts`, `distTar`, and
`distZip` because those tasks would read and write the same `fitlog.jar` path.

## Design decisions

### Immutable entries

`StrengthEntry` and `CardioEntry` fields are final. An `edit` command validates the
new value and reconstructs a replacement entry, which `WorkoutLog.replace` places
at the same index. This avoids partially updated values and preserves each entry's
simple value-object behaviour.

### PRs are computed on demand

`WorkoutLog.isPersonalRecord` scans the current collection when an entry is logged
or edited. It compares only same-type entries with the same normalised exercise
name, excludes the edited entry itself, and requires a strictly greater metric.
During an edit, the candidate occupies an existing position in the log, so the
controller passes that position as `excludedIndex`; logging passes `-1` because
there is no existing entry to exclude. This prevents an edited entry from being
compared against its previous value.

The result is calculated from the current in-memory log and is not stored on an
entry. Consequently, deleting a former PR cannot leave stale PR state; a later
log or edit always evaluates the then-current history.

### Help output presentation

`HelpCommand` does not mutate the workout log or trigger a storage save. Its
execution emits each command syntax through `Ui.showInfo`, followed by its
example through `Ui.showExample`. Keeping examples as a separate UI feedback
category lets `ConsoleUi` indent them and lets `GuiUi` apply the
`example-message` CSS class, which uses a muted monospace font, without placing
JavaFX styling concerns in `FitLogController`.

### Tab-separated storage

`Storage` writes one entry per line in UTF-8:

```text
strength<TAB>name<TAB>sets<TAB>reps<TAB>weightKg<TAB>loggedAt
cardio<TAB>name<TAB>durationMinutes<TAB>distanceKm<TAB>loggedAt
```

`loggedAt` is an ISO-8601 `LocalDateTime` representing when FitLog received the
log command. For cardio entries without a distance, the distance field is empty.
`formatLoggedAt` serialises known timestamps with `LocalDateTime.toString()` and
writes an empty final field when the time is unknown. `parseLoggedAt` uses
`LocalDateTime.parse`, which reads the same ISO-8601 representation.

Backward compatibility is handled by accepting both the current field counts
(six fields for strength and five for cardio) and the legacy counts without a
timestamp (five and four respectively). Legacy entries receive a `null`
`loggedAt` value and display `time not recorded`; no historical time is invented.

Tabs make the file both human-readable and simple to parse. This is safe under
the current name parser because it rebuilds names from whitespace-split tokens,
so names cannot contain literal tabs. `Storage.save` writes a temporary file and
then replaces the data file atomically when supported, falling back to a normal
replacement when it is not.

### Malformed storage lines

`Storage.load` reads the UTF-8 file one line at a time and passes each line to
`parseEntry`. A line is malformed when it has an unknown entry type, the wrong
number of tab-separated fields, a blank exercise name, an invalid numeric or
timestamp value, or a non-positive/non-finite measurement. The type-specific
parsers return `null` for failed structural or range checks, while
`parseEntry` converts `NumberFormatException` and `DateTimeParseException` into
the same `null` result.

Loading continues after a malformed line. Each `null` result produces a warning
containing the one-based source line number, while valid lines before and after
it remain in the returned `LoadResult`.

## Testing

FitLog's numeric option parsers are intentionally tested through transcript-based
integration scenarios rather than direct unit tests. They report validation errors
through `Ui`, so isolating them would require a wider result-object refactor across
command parsing and editing. Selected behavior is covered by the documented
command transcripts, while focused unit tests cover the collection, persistence,
and entry-formatting logic. The remaining numeric-parser coverage gaps are listed
below.

### Numeric parser coverage

The numeric parsers are `FitLogController`'s `parsePositiveWholeNumber` and
`parsePositiveNumber` methods. The current regression transcript exercises the
following numeric-parser edge case:

- Non-numeric whole number: `log strength bench press /sets 3 /reps ten /weight 80`
  in [Log validation errors](pre-refactor-transcript.md#log-validation-errors)
  expects `/reps needs a positive whole number, not 'ten'.`.

The following cases are supported by the parsers but are **not currently covered**
by `pre-refactor-transcript.md`. Add them to the transcript before relying on it as
complete numeric-parser regression coverage:

- Zero values, for example `/sets 0` and `/weight 0`.
- Negative values, for example `/duration -1` and `/distance -5`.
- Integer overflow, for example `/reps 2147483648`.
- `NaN`, for example `/weight NaN`.
- Infinity, for example `/distance Infinity`.

When changing numeric parsing or validation messages, run the complete
`pre-refactor-transcript.md` scenarios in addition to `./gradlew test`.

### Automated tests

JUnit tests under `src/test/java/fitlog` cover the highest-value domain and
persistence behaviour:

- `WorkoutLogTest` covers PR comparison (first entry, ties, lower values,
  normalised names, type separation, and edit exclusion) and search matching with
  original positions, normalised progression matching, and volume totals.
- `StorageTest` covers loading valid strength/cardio lines, skipping malformed
  lines with warnings, save/load round trips, and parent-directory creation.
- `ExerciseEntryTest` covers strength/cardio detail formatting and PR metric and
  description formatting, including whole and decimal measurements and logging-
  time display.
- `StorageTest` also covers timestamp round trips and backward-compatible loading
  of timestamp-less legacy lines.
- `FitLogControllerTest` covers timestamp preservation during edits, timestamp
  display in `list` and `stats` output, help output, exact lowercase help matching,
  and the guarantee that help does not create a storage file.

`docs/pre-refactor-transcript.md` is the manual regression baseline for console
behaviour: command parsing, user-facing validation messages, edit/delete flows,
EOF handling, PR notifications, and persistence startup behaviour. Numeric
validation coverage in that transcript is currently incomplete; see the gaps in
the Numeric parser coverage section above.

## Software engineering process

FitLog was developed in small, working increments. Features were first scoped
and their edge cases agreed before implementation; larger structural changes
were then extracted in stages while keeping the application runnable. For
example, `Ui`, `WorkoutLog`, and the command hierarchy were introduced one at a
time, with the console regression transcript used to confirm that user-visible
behaviour did not change.

The current verification approach combines focused JUnit tests for domain and
storage behaviour with the console regression transcript and the GUI manual test
plan. Before submitting a change, run `./gradlew test` and replay the relevant
manual scenario when it changes command output or JavaFX interaction.

## Acknowledgements

This project was built with AI assistance from Codex. The student reviewed and
guided the resulting design, implementation, tests, and documentation.
Development logs in the [`logs/`](../logs/) directory record the prompts, design
discussions, and code or documentation produced with Codex assistance.

The project structure and command-oriented interaction style draw on the
CS2103/T individual-project conventions referenced in the assignment brief.
The graphical interface uses [OpenJFX](https://openjfx.io/) and its Gradle
plugin. The build uses [Gradle](https://gradle.org/), automated tests use
[JUnit 5](https://junit.org/junit5/), and distributable JAR packaging uses the
[GradleUp Shadow plugin](https://gradleup.com/shadow/).

Except for the tools, libraries, course conventions, and AI assistance cited
above, no external code or documentation was reused.
