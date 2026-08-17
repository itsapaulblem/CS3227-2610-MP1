# FitLog pre-refactor regression transcript

This document records the expected console behaviour before the UI extraction.
Each section starts a fresh FitLog session unless stated otherwise. Lines beginning
with `>` are commands entered by the user.

For the non-persistence sections, run each session with a fresh `data/` directory
so entries from an earlier scenario do not affect the result.

## Valid strength and cardio entries

```text
Welcome to FitLog!
What would you like to log today?
> log strength bench press /sets 3 /reps 10 /weight 80
Logged: bench press - 3 sets x 10 reps @ 80kg
> log cardio run /duration 30 /distance 5
Logged: run - 30 min, 5km
> list
1. [Strength] bench press - 3 sets x 10 reps @ 80kg
2. [Cardio] run - 30 min, 5km
> bye
Goodbye! Keep training.
```

## Log validation errors

```text
> log strength /sets 3 /reps 10 /weight 80
Add an exercise name before the strength options.
> log cardio run /pace 5 /duration 30
'/pace' is not a cardio option. Use /duration and optional /distance.
> log cardio run /duration 30 /distance 5 /distance 6
Use /distance only once in a cardio entry.
> log strength bench press /sets 3 /reps ten /weight 80
/reps needs a positive whole number, not 'ten'.
> log strength squat /sets 3
Strength entries require /sets, /reps, and /weight.
```

## Edit and delete success and errors

```text
> log strength bench press /sets 3 /reps 10 /weight 80
Logged: bench press - 3 sets x 10 reps @ 80kg
> log cardio run /duration 30 /distance 5
Logged: run - 30 min, 5km
> edit 1 /weight 82.5
Updated: bench press - 3 sets x 10 reps @ 82.5kg
> delete 2
Removed: run - 30 min, 5km
> edit 1 /duration 20
'/duration' applies to cardio entries, but entry 1 is strength.
> edit 4 /weight 90
Entry 4 does not exist. Use list to view entry numbers.
> edit one /weight 90
Entry number must be a whole number, not 'one'.
> delete 4
Entry 4 does not exist. Use list to view entry numbers.
```

In an empty session:

```text
> delete 1
There are no entries to delete.
> edit 1 /weight 80
There are no entries to edit.
```

## EOF handling

When standard input ends instead of supplying another command:

```text
Welcome to FitLog!
What would you like to log today?
> 
Goodbye! Keep training.
```

## Personal records

```text
> log strength bench press /sets 3 /reps 10 /weight 80
Logged: bench press - 3 sets x 10 reps @ 80kg
> log strength bench press /sets 3 /reps 8 /weight 82.5
Logged: bench press - 3 sets x 8 reps @ 82.5kg
New PR! Heaviest bench press: 82.5kg
> log strength bench press /sets 4 /reps 6 /weight 82.5
Logged: bench press - 4 sets x 6 reps @ 82.5kg
> log strength bench press /sets 3 /reps 5 /weight 70
Logged: bench press - 3 sets x 5 reps @ 70kg
> edit 4 /weight 85
Updated: bench press - 3 sets x 5 reps @ 85kg
New PR! Heaviest bench press: 85kg
```

The first log has no PR notification, tied weight has no notification, and a
strictly heavier logged or edited entry has one.

## Stats and volume

For a strength-only exercise, progression uses normalised-name matching and keeps
the original logged positions:

```text
> log strength bench press /sets 3 /reps 10 /weight 80
Logged: bench press - 3 sets x 10 reps @ 80kg
> log strength Bench   Press /sets 3 /reps 8 /weight 82.5
Logged: Bench Press - 3 sets x 8 reps @ 82.5kg
New PR! Heaviest Bench Press: 82.5kg
> stats bench press
Progression for bench press:
1. [Strength] 80kg
2. [Strength] 82.5kg
```

The same normalised name can have mixed strength and cardio history; the type
label keeps their distinct metrics clear:

```text
> log strength circuit /sets 3 /reps 10 /weight 40
Logged: circuit - 3 sets x 10 reps @ 40kg
> log cardio Circuit /duration 20
Logged: Circuit - 20 min
> stats circuit
Progression for circuit:
1. [Strength] 40kg
2. [Cardio] 20 min
```

Error cases:

```text
> stats squat
No entries match 'squat'.
> stats
Specify an exercise name to view stats.
```

`volume` reports totals across all currently loaded entries, not weekly totals:

```text
> log strength bench press /sets 3 /reps 10 /weight 80
Logged: bench press - 3 sets x 10 reps @ 80kg
> log strength squat /sets 3 /reps 5 /weight 100
Logged: squat - 3 sets x 5 reps @ 100kg
> log cardio run /duration 30
Logged: run - 30 min
> log cardio cycle /duration 45
Logged: cycle - 45 min
> volume
Totals for all currently loaded entries:
Strength volume: 3900 kg
Cardio duration: 75 min
```

In a session with no entries:

```text
> volume
Totals for all currently loaded entries:
Strength volume: 0 kg
Cardio duration: 0 min
```

Decimal strength volume retains its decimal fraction:

```text
> log strength dumbbell curl /sets 1 /reps 1 /weight 82.55
Logged: dumbbell curl - 1 sets x 1 reps @ 82.55kg
> volume
Totals for all currently loaded entries:
Strength volume: 82.55 kg
Cardio duration: 0 min
```

## Persistence

On a first run with no `data/fitlog.txt` file:

```text
Welcome to FitLog!
What would you like to log today?
> log strength bench press /sets 3 /reps 10 /weight 80
Logged: bench press - 3 sets x 10 reps @ 80kg
> log cardio run /duration 30 /distance 5
Logged: run - 30 min, 5km
> bye
Goodbye! Keep training.
```

After restarting from the same directory:

```text
Welcome to FitLog!
What would you like to log today?
> list
1. [Strength] bench press - 3 sets x 10 reps @ 80kg
2. [Cardio] run - 30 min, 5km
> bye
Goodbye! Keep training.
```

If the storage file contains a malformed second line between valid entries:

```text
strength	bench press	3	10	80.0
this is not a valid entry
cardio	run	30	5.0
```

Startup reports the skipped line and keeps the valid ones:

```text
Welcome to FitLog!
Warning: skipped malformed entry on line 2.
What would you like to log today?
> list
1. [Strength] bench press - 3 sets x 10 reps @ 80kg
2. [Cardio] run - 30 min, 5km
> bye
Goodbye! Keep training.
```
