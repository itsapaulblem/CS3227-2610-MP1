# Developer Guide

## Architecture

FitLog is organised into a small command-line application with the following
responsibilities:

- `FitLog` starts the application, loads saved entries, and owns the command
  parsing and dispatch flow. `resolveCommand` routes a trimmed input line to the
  relevant parser, while `executeCommand` performs the parsed command and decides
  whether the application should exit. It also contains the shared log and edit
  validation helpers.
- `Ui` owns the `Scanner`, prints the `> ` prompt, reads a command, and prints all
  user-facing messages. It returns `null` at end-of-file so `FitLog` can exit
  gracefully.
- `WorkoutLog` owns the in-memory `List<ExerciseEntry>`. It provides add, delete,
  replace, lookup, and listing operations, case-insensitive substring search with
  original list positions, normalised exact-name lookup for progression, and
  collection-level PR comparison.
- `Storage` owns file I/O only. It loads valid entries with per-line malformed-data
  warnings and saves a supplied list to disk; it does not print user messages.
- `ExerciseEntry` is the shared abstraction for immutable `StrengthEntry` and
  `CardioEntry` values. Each subtype supplies display details and its PR metric.
- `Command` is a sealed interface. The command records are `ByeCommand`,
  `ListCommand`, `DeleteCommand`, `EditCommand`, `LogStrengthCommand`,
  `LogCardioCommand`, `FindCommand`, `StatsCommand`, and `VolumeCommand`. They
  represent successfully parsed commands; validation errors are reported during
  parsing before a command record is created.

At runtime, `FitLog` reads through `Ui`, resolves the input into a `Command`, uses
`WorkoutLog` to perform collection operations, asks `Storage` to save successful
mutations, and returns output through `Ui`.

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

The numeric parsers are `FitLog`'s `parsePositiveWholeNumber` and
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
  original positions.
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
