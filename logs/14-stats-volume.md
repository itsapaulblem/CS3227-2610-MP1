# 14 – Progression statistics and totals

## What was requested

The user requested exercise progression and "weekly" volume, while explicitly
asking that the missing timestamp/date issue be treated as a real scope gap.

## Decisions chosen

- `stats <exercise name>` matches normalised exact names, not substring names,
  and retains logged order and actual list positions.
- Mixed strength/cardio entries with the same normalised name are both shown,
  with labels distinguishing weight from duration.
- `volume` reports totals for all currently loaded entries, not weekly totals,
  because entries have no dates.

## What landed

`StatsCommand`, `VolumeCommand`, matching support in `WorkoutLog`, and volume
totals were added. Volume calculation was then moved from controller dispatch to
`WorkoutLog.calculateTotals()` so it could be tested directly. A decimal-volume
transcript case verified `82.55 kg` formatting. Commits: `6e09d41` and
`2c764ed`.

## Prompt record and Codex outcome

> "Add stats commands ... stats <exercise name> ... volume ... since there's no
> date/timestamp ... flag this as a real gap, don't just guess."

> "go ahead and implement it as proposed ... volume ... totals for all currently
> loaded entries, not \"weekly\" ... Neither mutates state, so neither should
> trigger a save."

The student later asked to move volume logic where it could be unit tested.
Codex moved it into `WorkoutLog` and added tests for strength-only, cardio-only,
mixed, empty, and decimal-volume logs.

## Acceptance condition

I accepted statistics and totals only after they used the complete loaded
history, did not trigger persistence, and the tests covered strength-only,
cardio-only, mixed, empty, and decimal-volume cases.
