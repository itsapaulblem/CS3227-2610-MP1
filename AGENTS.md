# Project context
This repository contains FitLog, a personal command-line/chat-based workout
logging application, developed as an individual project for an
undergraduate software engineering course. The app lets a user log
strength and cardio workout entries via text commands, view history,
track personal records, view exercise progression, and compute strength and
cardio totals across the complete loaded workout history.

# Default user context
Unless the user says otherwise, assume you are assisting the student who
owns and is building this project. If the user identifies themselves as
an instructor or other stakeholder, adapt your response to that role.

# Student profile
* Prior knowledge: Comfortable with Java and OOP concepts; has completed
  the CS2103/T individual project (a to-do-list chatbot).
* Level of programming experience: Quite experienced with Java.
* IDE and level of expertise: VS Code, comfortable with its Java tooling
  and terminal-based Git workflows.

# Guidance for interacting with the user
* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through
  responsible use of AI. For example:
  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments to all classes and to nontrivial
    methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include
    explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is
    sufficient for the requirements, while briefly explaining relevant
    more advanced alternatives.
* When implementing a new feature, propose breaking it into small,
  independently testable increments before writing code, unless the
  user explicitly asks for a one-shot implementation.
* After any code change, summarize what changed and flag any
  assumptions made (e.g. about input format, edge cases not yet
  handled).

# Project-specific requirements
## Domain rules
* An `ExerciseEntry` is either a strength entry (sets, reps, weight) or
  a cardio entry (duration, distance). Keep these as distinct types
  sharing a common abstraction rather than one class with unused fields.
* Training statistics use the complete loaded workout history rather than
  grouping or filtering entries by date.
* Personal record (PR) detection must compare a new entry against all
  prior entries for the *same exercise name* only.

## Java version
Ensure Java 25 is used for running the application or build tasks.

## Git
* Use lightweight tags unless the user requests an annotated tag.
* When proposing or creating a commit message, include enough detail in
  the body to explain the rationale for the change, not just what
  changed.
* Do not commit or push unless explicitly asked.

## Testing
* After implementing or changing a feature, propose relevant test cases
  (manual or JUnit) before moving to the next increment.
