# FitLog User Guide

Introducing FitLog: a workout logger for recording strength and cardio exercise,
reviewing progress, and tracking personal records.

## UI

FitLog can run as either a JavaFX conversation-style application or a console
application. Both use the same commands and produce the same FitLog response
text. In the GUI, your command appears as a blue bubble and FitLog's response
appears as a categorised conversation bubble.

![FitLog's JavaFX conversation interface](<images/Screenshot 2026-08-17 152324.png>)

### Starting the GUI

Build the project with Java 25, then launch the JavaFX interface:

```text
./gradlew build
./gradlew run
```

On PowerShell, use `./gradlew.bat build` and `./gradlew.bat run` if needed. The
GUI has a header, a full-width scrollable conversation view, and a command field
with a **Send** button. Press Enter or click **Send** to submit a command.

### Starting the console

After building, start the console entry point explicitly:

```text
java -cp build/classes/java/main fitlog.FitLog
```

The console displays a `> ` prompt before each command. Ensure the `java` command
uses Java 25.

## Logging a strength exercise

To log strength work, use `log strength` followed by an exercise name, sets,
reps, and weight in kilograms.

Syntax:

```text
log strength <name> /sets <n> /reps <n> /weight <kg>
```

Example:

```text
log strength bench press /sets 3 /reps 10 /weight 80
```

Expected outcome: stores a strength entry for bench press with three sets of ten
repetitions at 80 kg.

Console output (the GUI shows the same FitLog text in a green success bubble):

```text
> log strength bench press /sets 3 /reps 10 /weight 80
Logged: bench press - 3 sets x 10 reps @ 80kg
```

## Logging a cardio exercise

To log cardio work, use `log cardio` followed by an exercise name and duration in
minutes. Distance in kilometres is optional.

Syntax:

```text
log cardio <name> /duration <min> [/distance <km>]
```

Example:

```text
log cardio run /duration 30 /distance 5
```

Expected outcome: stores a cardio entry for a 30-minute, 5 km run.

Console output (the GUI shows the same FitLog text in a green success bubble):

```text
> log cardio run /duration 30 /distance 5
Logged: run - 30 min, 5km
```

## Listing entries

To view all logged entries, use the `list` command.

Syntax:

```text
list
```

Example:

```text
list
```

Expected outcome: displays every entry in logging order with a one-based number,
type label, exercise name, and details.

Console output:

```text
> list
1. [Strength] bench press - 3 sets x 10 reps @ 80kg (logged 2026-08-17 09:30)
2. [Cardio] run - 30 min, 5km (logged 2026-08-17 10:15)
```

The GUI displays the same entry lines as information bubbles.

## Editing an entry

To change one field of an existing entry, use `edit` with its one-based list
number and exactly one supported field. Strength entries support `/sets`, `/reps`,
and `/weight`; cardio entries support `/duration` and `/distance`.

Syntax:

```text
edit <index> /sets <n>
edit <index> /reps <n>
edit <index> /weight <kg>
edit <index> /duration <min>
edit <index> /distance <km>
```

Example:

```text
edit 1 /weight 82.5
```

Expected outcome: replaces the first entry with an otherwise identical entry that
uses 82.5 kg.

Console output:

```text
> edit 1 /weight 82.5
Updated: bench press - 3 sets x 10 reps @ 82.5kg
```

The GUI displays the update as a green success bubble. If the edit establishes a
PR, FitLog also displays a gold personal-record bubble.

## Deleting an entry

To remove an entry, use `delete` followed by its one-based list number.

Syntax:

```text
delete <index>
```

Example:

```text
delete 2
```

Expected outcome: removes the second logged entry.

Console output:

```text
> delete 2
Removed: run - 30 min, 5km
```

The GUI displays the removal as a green success bubble.

## Finding entries

To search entry names, use `find` followed by a search term. Matching is
case-insensitive and uses substring matching.

Syntax:

```text
find <search term>
```

Example:

```text
find press
```

Expected outcome: lists matching entries with their actual positions in the full
log, so those numbers can be used with `edit` or `delete`.

Console output:

```text
> find press
1. [Strength] bench press - 3 sets x 10 reps @ 80kg
3. [Strength] overhead press - 3 sets x 8 reps @ 40kg
```

The GUI displays the same matching lines as information bubbles.

## Viewing an exercise progression

To see the personal-record metric for one exercise across its logged history, use
`stats` followed by the exercise name. Names match after trimming, collapsing
whitespace, and ignoring case.

Syntax:

```text
stats <exercise name>
```

Example:

```text
stats bench press
```

Expected outcome: shows matching entries in logged order, with weight for
strength entries and duration for cardio entries.

Console output:

```text
> stats bench press
Progression for bench press:
1. [Strength] 80kg (logged 2026-08-17 09:30)
2. [Strength] 82.5kg (logged 2026-08-17 11:00)
```

The GUI displays the progression header and each result as information bubbles.

## Viewing training totals

To view totals across all currently loaded entries, use `volume`.

Syntax:

```text
volume
```

Example:

```text
volume
```

Expected outcome: shows total strength volume (`sets × reps × weight`) and total
cardio duration. These are loaded-log totals, not weekly totals, because FitLog
does not yet filter entries by a date range.

Console output:

```text
> volume
Totals for all currently loaded entries:
Strength volume: 3900 kg
Cardio duration: 75 min
```

The GUI displays these total lines as information bubbles.

## Stopping FitLog

To end a FitLog session, use `bye`.

Syntax:

```text
bye
```

Example:

```text
bye
```

Expected outcome: displays a goodbye message and ends the current interaction. In
the GUI, the input is disabled and the window closes after a short delay.

Console output:

```text
> bye
Goodbye! Keep training.
```

In the GUI, `Goodbye! Keep training.` appears as an information bubble before the
window closes.

## Additional notes

- Exercise names may contain spaces; option flags and their values are separated
  by whitespace.
- `/sets`, `/reps`, and `/duration` must be positive whole numbers. `/weight` and
  `/distance` must be finite numbers greater than zero when supplied.
- FitLog saves successful log, edit, and delete operations to `data/fitlog.txt`.
- Each new entry records its local logging time. `list` and `stats` show it as
  `yyyy-MM-dd HH:mm`; edits preserve the original time.
- The GUI uses green success, red error, amber warning, and gold PR feedback to
  make each response easy to identify.
