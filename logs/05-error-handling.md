# 05 – Command validation and parsing cleanup

## What was requested

The user asked to enumerate validation failures before implementing them, then
approved explicit handling for incomplete and malformed strength/cardio logs.

## Validation agreed and implemented

- Missing exercise name or required flags.
- Unknown and duplicate flags, missing flag values, and unexpected trailing text.
- Non-numeric values, integer overflow, zero, negative, `NaN`, and infinity.
- Strictly positive weight: the user explicitly rejected `0kg` as a bodyweight
  shortcut.
- Unknown top-level commands.

Each failure produces a specific corrective message instead of crashing or
silently accepting invalid data. This was committed as `79ab0d0`.

## Refactoring and verification

The user identified duplicated strength/cardio parsing, duplicated number
formatting, and a missing EOF guard. The implementation restored EOF handling,
introduced shared `parseLogDetails`/`LogDetails` parsing, and moved
`formatNumber` to `ExerciseEntry`; commit `04833d4` records that cleanup.

## Prompt record and Codex outcome

> "Before implementing: list all the invalid/edge-case inputs you can think of
> for log strength and log cardio ... Don't implement anything yet."

> "No a weight of 0kg should not be valid for bodyweight exercises. stricty
> positive only similar to running distance."

> "Implement error handling for the cases we discussed ... Keep them specific
> enough that the user knows exactly what to fix."

Initially, Codex implemented the validation immediately despite my instruction
not to make code changes. I reverted the changes and repeated the prompt with a
clearer constraint: “Do not implement anything yet.” On the second attempt,
Codex produced an error table for my review and implemented only the messages I
approved. A later follow-up removed duplicated parsing and formatting logic and
restored graceful EOF handling through shared helper methods.

## Acceptance condition

I accepted the validation change only after reviewing the second prompt's error
table, approving the user-facing messages, confirming that malformed inputs were
rejected, and checking that the shared-helper cleanup preserved graceful EOF
handling.
