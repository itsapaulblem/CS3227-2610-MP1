# 08 – Ui and WorkoutLog extraction

## What was requested

The user wanted the growing `FitLog` class split incrementally, with a written
console regression baseline before each extraction rather than a large rewrite.

## What landed

- `docs/pre-refactor-transcript.md` documented valid commands, validation,
  edit/delete, EOF, PRs, persistence, and later stats/volume scenarios.
- `Ui` initially took ownership of console input/output while FitLog retained
  parsing and domain work.
- `WorkoutLog` took ownership of the entry list, add/delete/replace/get/list
  operations, normalised name matching, and PR collection logic.

## Notable feedback

The user requested that list rendering consistently call `WorkoutLog.size()`
and `get()` rather than reach into its exposed list. That cleanup was made.

## Verification approach

After each small extraction, the full transcript was replayed to ensure output
remained unchanged.

## Prompt record and Codex outcome

> "Before any extraction, let's lock in current behavior. Write a written
> transcript ... This is our regression baseline ... Then implement step 1:
> extract the Ui class only."

> "Proceed to step 2: extract WorkoutLog ... Move the PR-detection logic ...
> into WorkoutLog too."

Codex created the transcript and made the two extractions without changing the
printed console text. The student then requested the list-accessor cleanup.
