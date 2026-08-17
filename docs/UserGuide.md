# FitLog User Guide

FitLog is a workout logging application with both a JavaFX graphical interface
and a console interface. It keeps entries in a saved workout log, lets you list,
edit, delete, and search them, and identifies personal records (PRs).

## Build

FitLog requires Java 25. From the project root, build the application with:

```text
./gradlew build
```

## Run the graphical interface

The default Gradle run task launches the JavaFX GUI through `fitlog.Launcher`:

```text
./gradlew run
```

On PowerShell, use `./gradlew.bat build` and `./gradlew.bat run` if needed.

The GUI contains:

- a header with the FitLog title and subtitle;
- a full-width, scrollable conversation view;
- a command input field and **Send** button at the bottom.

Enter commands in the input field and press Enter or click **Send**. The button is
disabled while the input is blank. The GUI has no separate command language: use
the exact same syntax documented in the [Commands](#commands) section below.

Conversation cards provide feedback by category:

- Green: successful log, edit, or delete operation.
- Red: validation or command error.
- Amber: non-fatal storage warning.
- Gold: personal-record notification.
- Blue: your submitted command.

Typing `bye` displays the goodbye message, disables the input, and closes the
window automatically after a short delay.

## Run the console interface

The console remains available, but `./gradlew run` no longer launches it. After
building the project, start the `FitLog` class explicitly:

```text
java -cp build/classes/java/main fitlog.FitLog
```

The console displays a `> ` prompt for each command. Ensure the `java` command
uses Java 25.

## Commands

Exercise names may contain spaces. Option flags and their values are separated by
whitespace.

### Log a strength exercise

Syntax:

```text
log strength <name> /sets <n> /reps <n> /weight <kg>
```

`/sets` and `/reps` must be positive whole numbers. `/weight` must be a finite
number greater than zero.

Example:

```text
> log strength bench press /sets 3 /reps 10 /weight 80
Logged: bench press - 3 sets x 10 reps @ 80kg
```

### Log a cardio exercise

Syntax:

```text
log cardio <name> /duration <min> [/distance <km>]
```

`/duration` is required and must be a positive whole number. `/distance` is
optional; when supplied, it must be a finite number greater than zero.

Example:

```text
> log cardio run /duration 30 /distance 5
Logged: run - 30 min, 5km
```

### List all entries

Syntax:

```text
list
```

Entries are shown in logging order with one-based numbers. Use those numbers with
`edit` and `delete`.

Example:

```text
> list
1. [Strength] bench press - 3 sets x 10 reps @ 80kg
2. [Cardio] run - 30 min, 5km
```

### Edit one field

Syntax:

```text
edit <index> /sets <n>
edit <index> /reps <n>
edit <index> /weight <kg>
edit <index> /duration <min>
edit <index> /distance <km>
```

`/sets`, `/reps`, and `/weight` apply only to strength entries. `/duration` and
`/distance` apply only to cardio entries. The index is the entry's one-based
number in `list`; exactly one field can be changed in each command. A cardio
distance cannot currently be cleared through `edit`.

Example:

```text
> edit 1 /weight 82.5
Updated: bench press - 3 sets x 10 reps @ 82.5kg
```

### Delete an entry

Syntax:

```text
delete <index>
```

The index is the entry's one-based number in `list`.

Example:

```text
> delete 2
Removed: run - 30 min, 5km
```

### Find entries by name

Syntax:

```text
find <search term>
```

FitLog uses case-insensitive substring matching. Results retain their actual
one-based positions in the full log, so the displayed number can be used directly
with `edit` or `delete`.

Example:

```text
> find press
1. [Strength] bench press - 3 sets x 10 reps @ 80kg
3. [Strength] overhead press - 3 sets x 8 reps @ 40kg
```

### View an exercise's progression

Syntax:

```text
stats <exercise name>
```

FitLog finds entries whose names match after trimming, collapsing internal
whitespace, and ignoring letter case. It displays matching entries in logged order
with their actual list positions and type-specific PR metrics.

Example:

```text
> stats bench press
Progression for bench press:
1. [Strength] 80kg
2. [Strength] 82.5kg
```

### View all loaded totals

Syntax:

```text
volume
```

This is not a weekly report: FitLog has no date or timestamp fields. It reports
totals across all entries currently loaded from the log. Strength volume is the
sum of `sets × reps × weight`; cardio duration is the sum of recorded minutes.

Example:

```text
> volume
Totals for all currently loaded entries:
Strength volume: 3900 kg
Cardio duration: 75 min
```

### Exit FitLog

Syntax:

```text
bye
```

Example:

```text
> bye
Goodbye! Keep training.
```

If standard input ends instead of receiving `bye`, FitLog also exits gracefully
with the same goodbye message.

## Testing

Run FitLog's automated JUnit tests from the project root with:

```text
.\gradlew.bat test
```

For manual regression testing of console commands and their exact output, follow
[the console regression transcript](pre-refactor-transcript.md). For JavaFX
layout, message styling, Enter/Send behaviour, startup warnings, and the delayed
`bye` close, follow [the GUI test plan](gui-test-plan.md).

## Personal records

A PR is checked when a new entry is logged and again after a successful edit.

- Strength PRs compare weight in kilograms.
- Cardio PRs compare duration in minutes.
- Entries are compared only with entries of the same type and exercise name.
  Names are matched case-insensitively after trimming and collapsing internal
  whitespace.
- A metric must be strictly greater than every matching entry to be a PR. A first
  entry and a tied metric are not PRs.
- During an edit, the entry being replaced is excluded from its own comparison.

For example:

```text
New PR! Heaviest bench press: 82.5kg
New PR! Longest run: 45 min
```

PR status is calculated from the current in-memory log each time; it is not stored
on an entry. Deleting a former PR therefore does not leave stale PR state behind.

## Data persistence

FitLog stores its data in `data/fitlog.txt`, relative to the directory where you
run the application. The `data/` directory is created automatically when an entry
is first saved.

Successful `log`, `edit`, and `delete` commands save the complete log immediately.
`list` and `find` do not save because they do not change data.

If a saved line is malformed, FitLog skips that line, keeps valid entries, and
prints a startup warning such as:

```text
Warning: skipped malformed entry on line 2.
```

If the storage file itself cannot be loaded, startup reports `Warning: could not
load saved entries: <reason>`. If saving fails, FitLog reports `Warning: could not
save entries: <reason>` immediately after the successful in-memory operation.

## Common errors

The following messages are emitted by the current command parser and are useful
guides to correcting input:

| Cause | Message |
| --- | --- |
| No type after `log` | `Choose an exercise type after 'log': strength or cardio.` |
| Unsupported type, such as `log yoga` | `'yoga' is not an exercise type. Use strength or cardio.` |
| Missing exercise name | `Add an exercise name before the strength options.` (or `cardio`) |
| Required strength fields omitted | `Strength entries require /sets, /reps, and /weight.` |
| Required cardio duration omitted | `Cardio entries require a /duration value.` |
| Unsupported option | `'/pace' is not a cardio option. Use /duration and optional /distance.` |
| Repeated option | `Use /distance only once in a cardio entry.` |
| Missing option value | `Provide a value after /weight.` |
| Non-numeric whole-number value | `/reps needs a positive whole number, not 'ten'.` |
| Zero or negative whole-number value | `/sets must be a whole number greater than zero.` |
| Non-numeric decimal value | `/weight needs a positive number, not 'heavy'.` |
| Zero, negative, `NaN`, or infinity decimal value | `/weight must be a finite number greater than zero.` |
| Missing `find` term | `Specify a search term to find.` |
| Missing `stats` exercise name | `Specify an exercise name to view stats.` |
| No search matches | `No entries match 'squat'.` |
| Invalid entry number | `Entry number must be a whole number, not 'one'.` |
| Out-of-range entry number | `Entry 4 does not exist. Use list to view entry numbers.` |
| Field belongs to the other entry type | `'/duration' applies to cardio entries, but entry 1 is strength.` |
| Unrecognised command | `I don't recognise that command. Use log, list, edit, delete, or bye.` |

The error text is case-sensitive and reflects the supplied flag, entry number, or
search term where applicable.
