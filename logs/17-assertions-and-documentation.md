# 17 – Assertions, documentation, packaging, and submission preparation

## What was requested

The user asked for Java assertions that document invariants guaranteed by earlier
validation, without replacing existing user-facing validation. Documentation was
then expanded for users, developers, GUI testing, and submission preparation.

## What landed

- Assertions with explanatory comments document validated delete/edit indices,
  type-specific edit fields, and the PR excluded-index invariant.
- The Java assertions are disabled by default, so normal runtime behaviour is
  unchanged; the JUnit suite still passed. This work is commit `32c7408`.
- `UserGuide.md`, `DeveloperGuide.md`, the friendlier root README, and a GUI
  screenshot were updated.
- `docs/gui-test-plan.md` records repeatable visual/manual JavaFX checks.
- The Shadow plugin was configured and `shadowJar` successfully created
  `build/libs/fitlog.jar` for the JavaFX launcher.

## Submission-related observations

The `master` branch name and repository remote were checked. Documentation and
new logs still need to be reviewed, committed, and pushed before submission.

## Prompt record and Codex outcome

> "Add Java assert statements to document important invariants ... Do NOT use
> assertions as a substitute for the existing validation/error-handling."

> "Create a README.md-style user guide for FitLog ... Base every command's syntax
> and example output strictly on FitLog's actual current behavior."

> "How do I update build.gradle.kts to create a fat/shadow JAR ... can you do it
> for me."

Codex added documented assertions, expanded user/developer documentation,
configured the Shadow plugin, and successfully ran `shadowJar`, producing
`build/libs/fitlog.jar`. The GUI test-plan request was also documented here;
TestFX was deliberately deferred after a risk assessment.

### Preserved Codex outcome

The pasted history records the final MP1 audit: FitLog was assessed as a JavaFX
desktop personal utility, distinct from a to-do manager, with 36 passing tests.
It also correctly flagged the mandatory reflection document and complete summary
logs as submission work rather than application-feature defects.
