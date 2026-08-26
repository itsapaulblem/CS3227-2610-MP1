# 03 – Logging and listing entries

## What was requested

The next increment added session-only exercise logging: `log <exercise name>`
should store an entry and `list` should display numbered entries. Persistence,
sets, reps, weight, and cardio/strength distinctions were explicitly deferred.

## Design discussion

The user asked whether to use `ArrayList<String>` or introduce an
`ExerciseEntry` class with only a name. The recommendation was to introduce the
class early to avoid a later collection-wide migration when exercise attributes
were added. The user chose that option.

## What landed

- `log` stored an entry in an in-memory list and confirmed the name.
- `list` printed entries with one-based numbering.
- No file persistence or exercise measurements were introduced at this point.

## Later outcome

The simple generic log syntax was superseded by explicit `log strength` and
`log cardio` commands when exercise types were added.

## Prompt record and Codex outcome

> "Add the ability to log an exercise and list logged exercises in the current
> session ... Store entries in an in-memory list for now. no file persistence
> yet."

> "I'm deciding between ... (a) a simple ArrayList<String> ... or (b)
> introducing an ExerciseEntry class now ... Give a recommendation, but don't
> implement anything yet."

Codex compared the options and recommended the class. The follow-up, "Go ahead
and implement option (b)", led to `ExerciseEntry` storage and numbered list
output. Measurements and persistence remained deferred.

### Preserved Codex outcome

> "Changed: `FitLog.java`: keeps an in-memory `List<ExerciseEntry>`, supports
> `log <exercise name>` with confirmation, and numbers entries for `list` ...
> `ExerciseEntry.java`: a minimal class with one immutable `String name` field,
> constructor, and getter."

The pasted history also records testing with `bench press`, `squat`, `list`, and
`bye`.

## Acceptance condition

I accepted the change only after the console transcript showed that
`bench press` and `squat` could be logged, `list` displayed both entries in
numbered insertion order, and `bye` still exited normally.
