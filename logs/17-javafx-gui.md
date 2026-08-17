# 17 – JavaFX GUI

## What was requested

The user wanted a polished Java desktop GUI without reimplementing command
validation. They preferred a chat-style command interface that reused the same
controller and command hierarchy as the console.

## Two-stage implementation

Stage 1 extracted `FitLogController.start()` and `submit(String)`, and changed
`Ui` into a categorised interface implemented by `ConsoleUi`. Console output was
kept byte-for-byte unchanged and the transcript was replayed.

Stage 2 added JavaFX Gradle configuration, `Launcher`, `FitLogGui`, `GuiUi`, and
CSS. The GUI shows a header, scrollable conversation, command field, and Send
button. Information, success, errors, warnings, PRs, and user commands have
distinct styles.

## Notable feedback and fixes

- `bye` initially looked frozen because the GUI correctly disabled input but
  stayed open. The user selected a 1.2-second farewell delay followed by
  `stage.close()`.
- The user later removed the storage-status label, quick actions, and command
  sidebar to keep the final layout focused.
- User and developer guides were updated for both console and GUI modes.

## Verification

The GUI has a manual test plan at `docs/gui-test-plan.md`; automated TestFX was
deferred because of late-project dependency and JavaFX-thread/headless risk.

## Prompt record and Codex outcome

> "I want to add a JavaFX GUI to FitLog: not minimal, should show real effort
> ... Before implementing, propose ... chat-style/command-reuse ... or a
> form-based GUI ... Don't implement anything yet."

> "Let's do this in two stages. Stage 1: refactor only, no JavaFX yet ...
> Extract FitLogController ... Change Ui ... console output must be byte-for-byte
> unchanged."

> "Stage 2: implement the JavaFX GUI ... Add Launcher ... GuiUi ... FitLogGui
> extends Application ... CSS stylesheet ..."

The student later reported that `bye` looked hung and selected automatic close.
They also requested removal of the sidebar and quick actions; Codex simplified
the layout accordingly and updated guides.

### Preserved Codex outcome

The pasted history includes the later GUI-testing response:

> "Created the draft manual plan: `docs/gui-test-plan.md` ... Recommendation:
> do not add TestFX now ... I can't reliably see and click through a live desktop
> window in this environment."

That response is why GUI verification remains a documented manual test plan
rather than a late TestFX dependency.
