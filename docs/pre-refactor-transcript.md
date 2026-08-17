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
