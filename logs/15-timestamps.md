# 15 – Automatic logging timestamps

## Design discussion and decision

Timestamp support followed a discussion about what a time-based workout record
should mean. The key decisions were whether FitLog should store the time a
command is submitted or require the user to enter the actual workout time, and
whether the first increment should only display timestamps or also support date
range filtering.

The chosen first increment records the local command-submission time
automatically, preserves it on edit, and displays it in `list` and `stats`.
Date-range filtering and user-entered workout times were deliberately deferred.
This kept the data model and command syntax small while still making the timing
of each logged entry visible.

## What Codex implemented

- Added an immutable `loggedAt` value to `ExerciseEntry`.
- New strength and cardio entries use `LocalDateTime.now()` when created.
- Edits rebuild an entry with its original timestamp.
- `list` and `stats` show the timestamp in `yyyy-MM-dd HH:mm` format.
- Storage writes an ISO-8601 timestamp as the final tab-separated field.
- Older saved lines without a timestamp still load and display `time not
  recorded`, instead of inventing a historical time.

## Documentation and verification

`UserGuide.md`, `DeveloperGuide.md`, `Reflections.md`, the regression transcript,
and the root README were updated to distinguish current timestamps from the
future date-range-filtering feature. New JUnit tests cover timestamp display,
legacy entries, and storage round trips. `./gradlew.bat test --no-daemon` passed
with 39 tests and no failures.
