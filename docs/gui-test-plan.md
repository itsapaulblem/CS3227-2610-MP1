# FitLog GUI manual test plan (draft)

This plan checks behaviour specific to the JavaFX interface. Command parsing and
domain behaviour remain covered by `pre-refactor-transcript.md` and the JUnit
suite. Start each scenario with a known `data/fitlog.txt` state as noted, so
saved entries do not affect the expected result.

## Test environment

- Use Java 25.
- Launch the GUI from the project root with:

  ```powershell
  .\gradlew.bat run
  ```

- Unless a scenario says otherwise, begin with no `data/fitlog.txt` file (or an
  empty file) and close the previous test window before beginning the next one.

## 1. Launch and initial layout

1. Start the application.
2. Observe the window before entering a command.

Expected:

- The window title is `FitLog` and the header displays `FitLog` with the
  subtitle `Your training command centre`.
- The full-width conversation card is scrollable and the command composer is
  at the bottom.
- The initial conversation contains `Welcome to FitLog!` and `What would you
  like to log today?` as left-aligned grey information bubbles.
- The command field has the strength-command example as placeholder text and
  receives keyboard focus.

## 2. Submit with Enter and with Send

1. Type `volume` in the command field and press Enter.
2. Type `list` in the command field and click **Send**.

Expected for both submissions:

- The submitted text appears once as a right-aligned blue user bubble.
- The input field clears after submission.
- `volume` produces the two-line totals information response; `list` produces
  no additional response when there are no entries.
- The conversation scrolls to the latest message if necessary.

## 3. Blank input is not submitted

1. Leave the command field empty, then type only spaces.
2. Observe **Send** and try pressing Enter.

Expected:

- **Send** is disabled while the trimmed input is empty.
- Pressing Enter produces no user bubble and no FitLog response.

## 4. Success, information, and user-message styling

1. Submit `log strength bench press /sets 3 /reps 10 /weight 80`.
2. Submit `list`.

Expected:

- Each submitted command is a right-aligned blue bubble with white text.
- `Logged: bench press - 3 sets x 10 reps @ 80kg` is a left-aligned green
  success bubble.
- The list row is a left-aligned grey information bubble and reads
  `1. [Strength] bench press - 3 sets x 10 reps @ 80kg`.

## 5. Validation-error styling

1. Submit `log strength squat /sets 3`.

Expected:

- The user command is shown in blue.
- FitLog shows a left-aligned red error bubble reading
  `Strength entries require /sets, /reps, and /weight.`
- No entry is added; submitting `list` still shows only any entry intentionally
  created earlier in this scenario.

## 6. Personal-record styling

1. In a fresh data state, submit
   `log strength GUI PR Test /sets 3 /reps 8 /weight 80`.
2. Submit
   `log strength gui pr test /sets 3 /reps 8 /weight 82.5`.

Expected:

- The first log produces only its green success bubble; a first entry is not a
  PR.
- The second log produces a green success bubble followed by a left-aligned
  gold personal-record bubble reading
  `New PR! Heaviest gui pr test: 82.5kg`.
- The PR bubble has visually stronger/bold text and an amber/gold border.

## 7. Startup warning styling for malformed storage

1. Close FitLog.
2. Create `data/fitlog.txt` containing:

   ```text
   strength\tbench press\t3\t10\t80.0
   this is not a valid entry
   ```

   In a text editor, replace each visible `\t` in the first line with an actual
   tab character.
3. Start FitLog again.

Expected:

- The greeting appears first.
- A left-aligned amber warning bubble appears next and reads
  `Warning: skipped malformed entry on line 2.`
- The normal prompt appears after the warning.
- `list` shows the valid bench-press entry and does not show a malformed entry.

After the scenario, remove or restore `data/fitlog.txt` so it does not affect
other tests.

## 8. Farewell and automatic close

1. Type `bye` and submit it.

Expected:

- A right-aligned blue `bye` bubble and a left-aligned grey
  `Goodbye! Keep training.` bubble appear.
- The command field becomes disabled immediately. **Send** is also disabled
  because the cleared input is blank.
- The window closes automatically after roughly 1.2 seconds. It must not remain
  open as an apparently unresponsive window.

## Completion record

Record the operating system, Java version, date, and pass/fail result for each
section when executing this plan. Attach a screenshot for any visual mismatch.
