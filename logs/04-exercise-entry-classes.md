# 04 – Strength and cardio entry classes

## What was requested

FitLog needed two exercise kinds: strength (name, sets, reps, kg) and cardio
(name, duration, optional km), with typed list formatting.

## Design decision

An abstract `ExerciseEntry` base class was chosen with immutable
`StrengthEntry` and `CardioEntry` subclasses. Type-specific display information
belongs to each subtype, while the base abstraction exposes the common name,
details, PR metric, and type label. The user asked to shorten the originally
proposed subtype names to `StrengthEntry` and `CardioEntry`.

## What landed

- `log strength <name> /sets <n> /reps <n> /weight <kg>`.
- `log cardio <name> /duration <min> [/distance <km>]`.
- `list` output such as `[Strength] ...` and `[Cardio] ...`.
- The type label was implemented through `ExerciseEntry.getTypeLabel()` rather
  than being hard-coded in the command loop.

## Cross-reference

The hierarchy arrived in commit `67fb8b7`.

## Prompt record and Codex outcome

> "Two kinds of exercises need to be logged: Strength: name, sets, reps, weight
> (kg); Cardio: name, duration (minutes), distance (km). distance optional."

> "Before implementing: propose a class design ... under a shared abstraction
> ... don't implement yet."

After reviewing the proposal, the student asked, "Can the names
StrengthExerciseEntry and CardioExerciseEntry be shorten to StrengthEntry and
CardioEntry", then approved implementation. Codex created the base/subclass
design and placed the type label in `ExerciseEntry.getTypeLabel()`.

### Preserved Codex outcome

> "I chose `getTypeLabel()` in `ExerciseEntry`, so each subtype owns its own
> label ... and FitLog only handles generic numbering and display."

The pasted result lists the changed `ExerciseEntry.java`, `StrengthEntry.java`,
`CardioEntry.java`, and `FitLog.java`, and preserves strength, cardio, list, and
cardio-without-distance verification examples.
