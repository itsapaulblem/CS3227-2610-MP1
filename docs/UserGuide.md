# FitLog User Guide

Introducing FitLog: a workout logger for recording strength and cardio exercise,
reviewing progress, and tracking personal records.

## Interfaces

FitLog can run as either a JavaFX conversation-style GUI or a command-line interface (CLI). Both interfaces support the same commands and display the same FitLog responses.

In the GUI, user commands are displayed in blue conversation bubbles, while FitLog's responses appear in categorised response bubbles.

![FitLog's JavaFX conversation interface](<../images/Screenshot 2026-08-17 182220.png>)

## Getting started

### Requirements

Install a Java 25 **JDK**. A JRE alone is not sufficient because Gradle must
compile the application. Confirm that Java 25 is active:

```text
java --version
javac --version
```

Both commands should report version 25. If they report another version, update
`JAVA_HOME` and your `PATH` before continuing.

Download or clone the FitLog repository, then open a terminal in its project
root. This is the folder containing `gradlew`, `gradlew.bat`, and
`build.gradle.kts`. All commands below must be run from this folder.

FitLog includes the Gradle wrapper, so you do not need to install Gradle. The
wrapper downloads Gradle and the project's dependencies on its first run, which
can take longer and requires an internet connection.

### Start the GUI (recommended)

The `run` task compiles and starts FitLog. You do **not** need to run
`clean`, `test`, or `shadowJar` first.

#### Windows (PowerShell)

```powershell
.\gradlew.bat run
```

#### macOS or Linux

The wrapper may need executable permission after the repository is downloaded:

```bash
chmod +x gradlew
./gradlew run
```

The GUI has a header, a full-width scrollable conversation view, and a command
field with a **Send** button. Press Enter or select **Send** to submit a command.
The terminal remains occupied while FitLog is open; this is normal. Enter `bye`
in FitLog or close its window to return to the terminal.

### Build and run a packaged GUI JAR (optional)

Use this alternative when you want to run the tests and create a packaged JAR.
Run the Gradle command first and wait for `BUILD SUCCESSFUL`. Then run the
separate `java -jar` command.

#### Windows (PowerShell)

```powershell
.\gradlew.bat clean test shadowJar
```

```powershell
java -jar build\libs\fitlog.jar
```

#### macOS or Linux

```bash
./gradlew clean test shadowJar
```

```bash
java -jar build/libs/fitlog.jar
```

The Gradle command removes old build output, runs the tests, and creates
`build/libs/fitlog.jar`. The `java` command then launches that JAR. Do not append
`java -jar ...` to the Gradle command: it is a different command, not a Gradle
task. `clean` and `test` are useful build checks, but neither is required every
time you start FitLog.

The packaged JAR includes the JavaFX native libraries required by 64-bit
Windows, macOS, and Linux. The same `fitlog.jar` can therefore be built on any
of those operating systems and run on all three.

### Start the console command-line interface (optional)

The CLI supports the same FitLog commands without opening a window.

#### Windows (PowerShell)

```powershell
.\gradlew.bat classes
```

```powershell
java -cp build\classes\java\main fitlog.FitLog
```

#### macOS or Linux

```bash
./gradlew classes
```

```bash
java -cp build/classes/java/main fitlog.FitLog
```

Wait for the Gradle command to finish before running the `java` command. The
console displays a `> ` prompt before each FitLog command. Enter `bye` to print
the farewell and exit normally.

### Setup troubleshooting

- If Gradle says a task such as `java` or `-jar` does not exist, run the Gradle
  and `java -jar` commands separately as shown above.
- If a command cannot find `gradlew`, return to the project root. Use
  `.\gradlew.bat` in Windows PowerShell and `./gradlew` on macOS or Linux.
- If macOS or Linux reports `Permission denied` for `./gradlew`, run
  `chmod +x gradlew` once and try again.
- If the build reports an incompatible Java version, run `./gradlew --version`
  on macOS or Linux, or `.\gradlew.bat --version` on Windows. The `JVM` line
  should report Java 25.
- If the first build appears to pause while downloading Gradle or dependencies,
  keep the terminal open and check the internet connection.
- If `run` does not return to the terminal after the GUI opens, FitLog is still
  running. Submit `bye`; the farewell is displayed and the window closes after
  about 1.2 seconds.

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

Expected outcome: displays every entry in logging order with a number,
type label, exercise name, and details.

Console output:

```text
> list
1. [Strength] bench press - 3 sets x 10 reps @ 80kg (logged 2026-08-17 09:30)
2. [Cardio] run - 30 min, 5km (logged 2026-08-17 10:15)
```

The GUI displays the same entry lines as information bubbles.

## Editing an entry

To change one field of an existing entry, use `edit` with its list
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

To search exercise names, use `find` followed by any part of a name. The search
term does not need to be the complete exercise name. For example, `find press`
matches both `bench press` and `overhead press`. Capitalisation is ignored, so
`find PRESS` produces the same matches. A search term may also be part of a
single word: `find ch` matches `bench press` because `ch` appears within
`bench`.

The search term must still appear as a continuous sequence of characters in the
exercise name. `find bench prss` does not match `bench press`, because `find`
does not correct spelling mistakes. Use `stats` instead when you want entries
for one complete exercise name only.

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
`stats` followed by the complete exercise name. For example, an entry named
`bench press` will not match `bench` or the misspelling `bench prss`. Differences
in capitalisation, leading or trailing spaces, and repeated spaces between words
are ignored.

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
does not yet group or filter entries by workout date. Totals include every valid
entry currently loaded, including large accumulated values.

Console output:

```text
> volume
Totals for all currently loaded entries:
Strength volume: 3900 kg
Cardio duration: 75 min
```

The GUI displays these total lines as information bubbles.

## Viewing command help

To see every supported command, its syntax, and a short example, use `help`.

Syntax:

```text
help
```

Example:

```text
help
```

Expected outcome: displays each command syntax followed by a separate example
line. The console indents examples; the GUI gives them a distinct monospace style.

Console output begins as follows:

```text
> help
log strength <name> /sets <n> /reps <n> /weight <kg>
  Example: log strength bench press /sets 3 /reps 10 /weight 80
log cardio <name> /duration <min> [/distance <km>]
  Example: log cardio run /duration 30 /distance 5
...
```

Commands are case-sensitive, so `Help` is not recognised. An unrecognised command
suggests using `help` to see the available commands. Viewing help does not change
or save workout data.

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
- Use `help` to see all supported commands and examples.


## Personal records

A PR is checked when a new entry is logged and again after a successful edit.

- Strength PRs compare weight in kilograms.
- Cardio PRs compare duration in minutes.
- Entries are compared only with entries of the same type and exercise name.
  Names are matched case-insensitively after trimming and collapsing internal
  whitespace.
- A metric must be strictly greater than every matching entry to be a PR. A first
  entry and a tied metric are not PRs.

For example:

```text
New PR! Heaviest bench press: 82.5kg
New PR! Longest run: 45 min
```

## Logging times

Every new strength or cardio entry records the local time when its `log` command
is submitted. An `edit` preserves that original logging time. `list` and `stats`
display the timestamp as `yyyy-MM-dd HH:mm`.

The recorded time is when FitLog received the command, which
may differ from the time the activity was performed.

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

Saved entries follow the same validation rules as commands: names cannot be
blank, counts and durations must be positive whole numbers, and supplied weights
and distances must be finite numbers greater than zero.

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
| Whole number outside Java's supported integer range | `/reps needs a positive whole number, not '2147483648'.` |
| Zero or negative whole-number value | `/sets must be a whole number greater than zero.` |
| Non-numeric decimal value | `/weight needs a positive number, not 'heavy'.` |
| Zero, negative, `NaN`, or infinity decimal value | `/weight must be a finite number greater than zero.` |
| Missing `find` term | `Specify a search term to find.` |
| Missing `stats` exercise name | `Specify an exercise name to view stats.` |
| No search matches | `No entries match 'squat'.` |
| Invalid entry number | `Entry number must be a whole number, not 'one'.` |
| Out-of-range entry number | `Entry 4 does not exist. Use list to view entry numbers.` |
| Field belongs to the other entry type | `'/duration' applies to cardio entries, but entry 1 is strength.` |
| Unrecognised command | `I don't recognise that command. Use help to see the available commands.` |

The error text is case-sensitive and reflects the supplied flag, entry number, or
search term where applicable.
