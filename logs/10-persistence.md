# 10 – Persistent storage

## What was requested

FitLog was to load saved entries at startup and save after successful mutations,
without mixing file I/O into `WorkoutLog` or UI output into storage code.

## Decisions chosen

- Tab-separated, human-readable lines in `data/fitlog.txt`.
- Directory creation on first save.
- Write to a temporary file and replace the data file.
- Save after successful log/edit/delete, not only at exit.
- Skip malformed lines, retain valid entries, and show one startup warning per
  skipped line.

## What landed

`Storage` owns only `load()` and `save()`. `FitLogController` displays load and
save warnings through the UI. A comment explains why tabs are safe while names
are reconstructed from whitespace-split tokens. Persistence was committed as
`615774a` and regression cases were added to the transcript.

## Prompt record and Codex outcome

> "Persist WorkoutLog entries to disk automatically, and load them on startup.
> Before implementing, propose ... file format ... Storage class design ... when
> saving happens ... corrupted line ... Don't implement yet."

> "go ahead and implement it as proposed: tab-separated format,
> data/fitlog.txt with directory auto-creation ... write-temp-then-replace ..."

The follow-up required load warnings after the welcome message and save warnings
immediately. Codex implemented those behaviours and added transcript scenarios
for restart persistence and malformed files.

## Acceptance condition

I accepted persistence only after restart scenarios restored saved entries,
malformed lines were skipped without losing valid data, load warnings appeared
after the welcome message, and save failures were reported immediately.
