# Reflections on AI-assisted Software Engineering

## Overview

I used Codex 5.6 Terra throughout FitLog's development as a design partner,
implementation assistant, troubleshooting aid and never treated its
output as automatically correct. All AI-generated code and design
suggestions were reviewed and verified before being accepted.

The most useful pattern was to avoid immediately asking Codex to implement
a feature. Instead, I first described the problem and asked it to identify
approaches, assumptions, edge cases, and trade-offs. I made the design
decision myself, then asked it to implement the chosen approach. This kept
engineering and product decisions under my control while using AI to
accelerate implementation and repetitive work.

The three examples below were chosen because each surfaces a different
failure mode or strength: (1) using the LLM to surface hidden requirements
before implementation, (2) the LLM silently dropping correct behaviour
during later rework, and (3) the LLM ignoring an explicit multi-step
instruction.

---

## Example 1: Defining Personal-Record Behaviour Before Implementation

**Prompt and context.** Before implementing personal records (PRs), I
asked Codex to propose how a PR should be defined for strength and cardio
exercises, without writing any code, covering how names should be
matched, whether editing should trigger re-evaluation.

After reviewing its proposals, I decided: strength PRs use heaviest
weight, cardio PRs use longest duration; a new value must be *strictly*
greater (a tie is not a PR); a first-ever entry for an exercise is never a
PR; names are matched after trimming, collapsing whitespace, and
case-folding; editing an entry re-checks PR status, excluding that entry
from its own comparison; PR status is computed from history on demand,
never stored on an entry. Only then did I ask Codex to implement it.

**Why formulated this way.** "Personal record" has no single meaning: a
strength PR could be weight, reps, or volume; a cardio PR could be
duration, distance, or pace. If I'd simply asked Codex to "implement
personal records," it would have picked one of these on my behalf, and I
might not have caught behaviour I didn't actually want. Separating the
product decision from the implementation kept the choice mine while still
using AI to explore the space of options quickly.

**What the LLM assumed.** Codex proposed plausible metrics, but they were
assumptions, not requirements. It could identify *possible* domain rules
but not which one was correct for FitLog. It also raised the
stored versus computed question for PR status; I chose computed on demand
specifically so deleting a former PR-holder cannot leave stale state.

**How the prompt evolved.** It moved from a broad "how should a PR be defined"
question to a precise specification of a PR, only becoming an implementation request after
every ambiguity (ties, first entries, name matching, edit behaviour) was
resolved. This iterative approach avoided the wasted rework of coding
against an assumption I would have rejected anyway.

**Verification.** I reviewed the code and backed it with JUnit tests
covering: first entry is not a PR, a strictly heavier entry is, a
strictly longer cardio duration is, an equal weight is not, differently
formatted versions of the same name match correctly, and an edited entry
is compared against other entries, not itself. These edge cases were not
obvious from the main implementation path. An easy bug would be comparing
an edited entry against its own old value.

**Engineering judgement required.** The LLM could enumerate alternatives
and implement a chosen one, but deciding what a PR meant in FitLog, which
metrics mattered, and which possibilities to exclude (for example, no
time period specific PRs, despite having enough data to support them) was
mine. Syntactically correct code is not the same as code that satisfies
the actual requirement.

**When prompting was less effective.** Once the rules were settled,
re-prompting about small details (such as `>` versus `>=`) was slower than
just reading the code myself. Generated tests also still needed manual
review against my actual requirements. A test that only confirms the
LLM's own interpretation is not useful.

**What I would do differently.** I would convert selected rules into
explicit acceptance criteria before the first implementation prompt, to
reduce clarification rounds while still forcing the design decisions to
happen upfront. The effective workflow here was:

problem -> alternatives -> engineering decision -> explicit requirements -> implementation -> verification

not `prompt -> code`.

---

## Example 2: An LLM Correctly Implementing Something, Then Silently Losing It During Later Rework

**Context.** Early on, I asked Codex for a minimal FitLog console loop:
greet, read commands, echo, exit on `bye`, deliberately scoped narrow (no
extra classes yet). Codex's first version correctly guarded against
end-of-input:

```java
if (!scanner.hasNextLine()) {
    break;
}
```

Without this, piped or redirected input reaching EOF throws
`NoSuchElementException` instead of exiting gracefully.

Several increments later, I asked Codex to add `delete`/`edit` commands.
That rewrite reintroduced `scanner.nextLine().trim()` with no guard at
all. The already implemented, already verified EOF handling was silently
dropped. Nothing in Codex's summary of the change flagged this; I only
caught it by re-reading `main()` against the earlier version and testing
with piped input.

**Why this mattered.** This is a concrete instance of a failure mode the
course material (lecture) warns about directly: an LLM can implement something
correctly, then quietly regress it during later, seemingly unrelated
work, without announcing that a previously-solved concern reappeared. The
delete/edit diff looked reasonable in isolation; nothing about it read as
"this removes error handling" unless compared against the prior version
specifically.

**What I did differently afterward.** I restored the guard, then made it
standard practice: later refactors (the `Ui`/`WorkoutLog`/`Command`
extraction, the GUI `Ui`-interface change) were verified against a full
written regression transcript (`docs/pre-refactor-transcript.md`) after
every step, not just the new feature. This was not solely a reaction to
this bug; the later refactors touched multiple classes and were
inherently more regression-prone. But this incident is why I stopped
trusting "the new feature works" as sufficient verification and started
checking that everything previously working still worked too.

---

## Example 3: An LLM Skipping an Explicit "Propose, Don't Implement Yet" Instruction

**Context.** For error handling on `log strength`/`log cardio`, I gave
Codex a two-part instruction: enumerate edge cases and propose messages
first; do not implement anything yet, I want to review the list first.
Codex skipped the review gate entirely and directly implemented
field-aware validation across `FitLog`, describing the change only after
the fact.

**Why this is more concerning than Example 2.** Example 2 was a passive
regression: something correct got lost during unrelated work. This was
different: an explicit, unambiguous process constraint (a human checkpoint
before implementation) was simply not followed. The danger is that this
kind of instruction exists specifically to keep a decision point in the
loop before code changes happen. If the LLM collapses that gate whenever
it judges the implementation "straightforward enough," the reviewer has
to actively notice a two part instruction got executed as one, rather
than trusting it was honoured. This is a harder failure to catch than a
regression, because nothing about the output looks wrong on its own; it
is only wrong relative to the process I asked for. On review, the
validation it produced was actually solid, catching cases like integer
overflow and NaN that were not on my own list. But good output does not
retroactively justify skipping the checkpoint, since the entire point was
to let me catch a bad proposal before it became code.

**What I did.** I noted the skipped instruction but reviewed the
implementation on its merits rather than reverting it, since the coverage
was genuinely good. But it changed how I structure this kind of request
going forward: a process constraint embedded inside a larger prompt is
not reliably followed. Next time, I would split it into two separate
prompts, one that only asks for analysis with implementation explicitly
prohibited, and a second, separate implementation prompt sent only after
I have reviewed and approved the first response. That creates an actual
checkpoint instead of relying on the model to stop itself mid-task.

---

## What I Would Do Differently Overall

Codex was most effective when a task was clearly scoped and I used it to
explore alternatives or implement decisions I had already made. It was
less reliable when a task required domain judgement it could not have,
when a change needed to preserve behaviour outside the immediate prompt,
or when I expected it to hold a strict process like "propose first,
implement later."

If repeating this project, I would separate the workflow more
deliberately into exploration, decision, implementation, and
verification, use separate prompts (not one combined instruction)
whenever I need a real human checkpoint, and verify both the requested
feature and existing functionality via tests, transcript replays, and
manual diff review, not just the former.

Not every problem benefited equally from more prompting. Straightforward
code corrections, and environment issues like `JAVA_HOME` configuration
or SSL certificate failures (caused by accidentally staying connected to
the SoC VPN), were often faster to fix by directly inspecting the error
than by refining prompts further. I also learned to stop a refactor once
the design was clear enough, rather than continuing simply because Codex
could always suggest one more abstraction.

The consistent lesson: AI increased development speed but did not remove
the need for engineering judgement. Codex was most valuable for expanding
the alternatives and edge cases I considered. I remained responsible for
narrowing those possibilities, controlling scope, and verifying the final
system was actually correct.