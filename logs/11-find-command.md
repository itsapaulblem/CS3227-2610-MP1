# 11 – Find command

## What was requested

The user requested `find <search term>` using case-insensitive substring
matching, and asked whether results should be renumbered or retain their actual
list positions.

## Decision chosen

Matches show their real one-based position in the complete list. This lets a
user immediately pass the displayed number to `edit` or `delete`; local match
numbering would be less actionable.

## What landed

- `FindCommand` and parser validation for an empty term.
- `WorkoutLog.findByName`, returning entry/real-position pairs.
- No persistence save for searches.
- Output for no matches: `No entries match '<term>'.`

## Cross-reference

The feature was committed as `e1a33ea`.

## Prompt record and Codex outcome

> "Add a find command that searches logged entries by exercise name, using
> case-insensitive substring matching (not exact match)."

> "The numbering shown for find results — should it be the entry's actual
> position in the full list ... or a fresh 1-based numbering of just the
> matches? Tell me the tradeoff before implementing."

The student selected real positions. Codex added `FindCommand`, parser feedback
for an empty term, and `WorkoutLog.findByName`, then demonstrated non-adjacent
matches, no matches, and empty input.
