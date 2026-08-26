# 07 – Personal-record detection

## What was requested

Before implementation, the user asked to decide PR rules, name matching, edit
behaviour, and notification output.

## Rules chosen

- Strength PR: heaviest weight only.
- Cardio PR: longest duration only.
- A metric must be strictly greater; first entries and ties are not PRs.
- Exercise names are compared after trimming, collapsing whitespace, and
  lowercasing.
- Edits recheck against all other same-name, same-type entries while excluding
  the entry being replaced.
- PR state is computed by scanning current history, never stored on an entry;
  deleting a prior best therefore cannot leave stale state.

## What landed

Log and edit responses add `New PR! Heaviest ...` or `New PR! Longest ...` when
applicable. The implementation is recorded in `6f5965a`.

## Prompt record and Codex outcome

> "When a new entry is logged, detect whether it's a personal record (PR) ...
> Before implementing, propose ... Don't implement yet — I want to decide."

> "go ahead and implement it as proposed: strictly-greater-only, no PR on first
> entry, normalized name matching ... recheck on edit ..."

The student specifically asked, "so a pr for strength is only heaviest weight?"
and confirmed that rule. Codex implemented on-demand collection scanning and
demonstrated first, higher, tied, and edited entries.

## Acceptance condition

I accepted PR detection only after the examples confirmed no PR for a first
entry, a PR for a strictly higher value, no PR for a tie, normalized same-name
matching, and correct rechecking on edit while excluding the edited entry.
