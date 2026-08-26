# 06 – Editing and deleting entries

## What was requested

The user requested `delete <index>` and one-field `edit` commands, including
clear messages for empty logs, invalid indices, and edits using a field from the
wrong exercise type.

## Design decision

Entries remained immutable. An edit validates the new value, constructs a new
entry with the changed field, and replaces the original at its existing index.
This avoided mutable setters and reused the existing field sets and number
parsers rather than duplicating validation.

## What landed

- `delete` removes an entry by one-based list number and confirms it.
- `edit` updates exactly one supported strength or cardio field.
- The edited entry is rebuilt, not mutated.
- The user selected the known limitation that `/distance` cannot be cleared by
  edit; `/distance 0` remains invalid.

## Verification

The supplied weight-edit and delete examples, out-of-range indices, and
strength-field-on-cardio errors were checked against the console transcript.

## Prompt record and Codex outcome

> "Add delete and edit commands for logged entries ... Before implementing:
> the existing StrengthEntry/CardioEntry classes are currently immutable-looking
> ... propose how edit should work ... Don't implement yet."

> "This design looks good. go ahead and implement it as proposed: immutable
> reconstruction on edit, shared field/type validation reused from log ..."

The student chose option (a) for the distance gap: an edit cannot clear an
existing cardio distance yet. Codex implemented reconstruction and reused the
log field sets/parsers, then demonstrated the requested outputs.

## Acceptance condition

I accepted edit and delete only after the transcript confirmed immutable entry
replacement, correct numbering, shared field and type validation, successful
deletion, and the agreed error when trying to clear an existing cardio distance.
