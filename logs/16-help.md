# 16 – Help command

## What was requested

The user requested an exact lowercase `help` command that lists every supported
command with syntax and an example. The syntax and examples had to reflect the
implemented commands and `docs/UserGuide.md`, and viewing help had to avoid
mutating or saving workout data.

After reviewing the first output, the user requested clearer presentation: each
example should appear on the line below its syntax and use a distinct font in the
JavaFX interface. They also requested that unrecognised commands direct users to
`help` instead of showing an incomplete command list.

## What Codex implemented

- Added `HelpCommand` to the sealed `Command` hierarchy and exact matching in
  `parseSimpleCommand`; capitalised `Help` remains unrecognised.
- Added non-mutating help execution covering log strength, log cardio, list,
  edit, delete, find, stats, volume, help, and bye.
- Added `Ui.showExample` so presentation remains a UI-layer responsibility.
  `ConsoleUi` indents examples, while `GuiUi` applies a muted, italic monospace
  `example-message` style.
- Changed the unrecognised-command response to direct users to `help` for the
  current list of available commands.
- Updated the root README, User Guide, Developer Guide, and console regression
  transcript.

## Testing and verification

`FitLogControllerTest` verifies the complete help output, exact case-sensitive
matching, and that `help` does not create a storage file. The Java 25 Gradle test
suite and `git diff --check` passed. The console help and capitalised `Help`
scenario was replayed against the updated transcript.

## Acceptance condition

I accepted `help` only after the exact output matched the implemented commands
and User Guide, case sensitivity and no-save behaviour were tested, the console
transcript was replayed, and the Java 25 suite and diff check passed.
