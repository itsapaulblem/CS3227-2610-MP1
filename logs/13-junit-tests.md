# 13 – JUnit tests

## What was requested

The user asked for focused tests of high-value logic rather than blanket getter
coverage, with one clear Arrange/Act/Assert per test and no shared mutable test
state.

## What landed

- `WorkoutLogTest`: PR first-entry, tie, lower-metric, normalised-name,
  type-separation, edit-exclusion, find, stats, and volume behaviour.
- `StorageTest`: valid loading, malformed-line warnings, save/load round trips,
  and directory creation using `@TempDir`.
- `ExerciseEntryTest`: details, PR metric/description, and whole/decimal number
  formatting for strength and cardio.

The first sets landed in `1ecf6da` and `771ca77`; volume tests later brought the
suite to 36 tests.

## Deliberate decision

Numeric parser helpers remained directly un-unit-tested because they report
through `Ui`. The user chose transcript-based testing over a late ParseResult
refactor that would touch every parser call site.

## Prompt record and Codex outcome

> "Implement JUnit tests for WorkoutLog and Storage ... one clear Arrange/Act/
> Assert per test, descriptive method names ... no shared mutable state."

> "Implement the entry-formatting tests ... strengthDetailsFormatsWholeNumber
> WeightWithoutDecimal ... cardioPrDescriptionUsesLongestLabel ..."

> "Decision needed on FitLog's private numeric parsers ... (a) Leave them as-is
> ... (b) Refactor them ... Given I'm working toward a deadline ... recommend."

Codex added the requested focused test classes and recommended option (a). The
Developer Guide documents both the decision and the numeric edge cases still
missing from the transcript.
