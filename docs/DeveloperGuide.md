# Developer Guide

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
- `Ui` is an output interface with separate information, success, error, warning,
  and PR feedback methods. `ConsoleUi` implements it with plain console output,
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
  `CardioEntry` values. Each subtype supplies display details and its PR metric.
- `Command` is a sealed interface. The command records are `ByeCommand`,
  `ListCommand`, `DeleteCommand`, `EditCommand`, `LogStrengthCommand`,
  `LogCardioCommand`, `FindCommand`, `StatsCommand`, and `VolumeCommand`. They
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

`build.gradle.kts` applies the `org.openjfx.javafxplugin` Gradle plugin at version
`0.1.0`, configures JavaFX `26.0.1`, and enables the `javafx.controls` module.
The application main class is `fitlog.Launcher`. The separate launcher avoids the
classpath issues that can occur when the Java launcher is asked to start a JavaFX
`Application` subclass directly.

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
The result is not stored on an entry. Consequently, deleting an entry cannot leave
stale PR state; a later log or edit uses the then-current history.

### Tab-separated storage

`Storage` writes one entry per line in UTF-8:

```text
strength<TAB>name<TAB>sets<TAB>reps<TAB>weightKg
cardio<TAB>name<TAB>durationMinutes<TAB>distanceKm
```

For cardio entries without a distance, the final field is empty. Tabs make the
file both human-readable and simple to parse. This is safe under the current name
parser because it rebuilds names from whitespace-split tokens, so names cannot
contain literal tabs. `Storage.save` writes a temporary file and then replaces the
data file atomically when supported, falling back to a normal replacement when it
is not.

## Testing

FitLog's numeric option parsers are intentionally tested through transcript-based
integration scenarios rather than direct unit tests. They report validation errors
through `Ui`, so isolating them would require a wider result-object refactor across
command parsing and editing. Their behavior is covered by the documented command
transcripts, while focused unit tests cover the collection, persistence, and
entry-formatting logic.

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
  description formatting, including whole and decimal measurements.

`docs/pre-refactor-transcript.md` is the manual regression baseline for console
behaviour: command parsing, user-facing validation messages, edit/delete flows,
EOF handling, PR notifications, and persistence startup behaviour. Numeric
validation coverage in that transcript is currently incomplete; see the gaps in
the Numeric parser coverage section above.

## Acknowledgements

This project was built with AI assistance from Codex, as required to be
acknowledged by the CS3227 MP1 requirements. Its project structure follows the
CS2103/T individual-project conventions referenced in the assignment brief.
