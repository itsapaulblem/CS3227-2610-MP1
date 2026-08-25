# 20 – Documentation audit and design diagrams

## What was requested

The user asked Codex to review the completed FitLog repository against the MP1
grading and submission criteria. The review focused on documentation accuracy,
internal consistency, evidence of testing, and design documentation. The user
then requested targeted documentation updates rather than new application
features.

The main requests were to:

1. identify any grading or submission criteria that were still missing;
2. reconcile a contradiction between `docs/Reflections.md` and
   `logs/05-error-handling.md`;
3. remove stale internal requirements for `WorkoutSession` and weekly volume,
   because FitLog intentionally reports totals across the complete loaded
   history;
4. update and manually execute all eight scenarios in the GUI test plan;
5. update stale test counts in the logs; and
6. add an architecture diagram and a sequence diagram to the Developer Guide,
   with captions and explanations following the CS2103/T design guidance.

## Documentation audit findings

Codex compared the repository contents with the assignment write-up and found
that the required source code, User Guide, Developer Guide, reflection document,
and numbered prompt summaries were present. The most important remaining risks
were inconsistencies within the documentation rather than missing application
functionality.

Three high-priority issues were examined:

* The reflection originally said that Codex ignored a request not to implement
  error handling, while the error-handling log described a review-first flow.
* An internal project instruction and an older code-quality log referred to
  `WorkoutSession` grouping and weekly volume even though those concepts were
  not part of the implemented product.
* The GUI test plan was still labelled as a draft, described an outdated empty
  `list` response, and had no completed execution record.

The Developer Guide also described the architecture only in prose. This was not
a correctness defect, but diagrams would provide clearer design evidence for
the Documentation Quality and Basic Software Engineering Practices criteria.

## Decisions and corrections

The user clarified that the first error-handling implementation was premature:
it was reverted, the prompt was issued again, Codex then produced an error table
for review, and implementation proceeded only after approval. The reflection
was revised to preserve that full sequence and agree with the prompt log.

The user also confirmed that FitLog intentionally stores individual exercise
entries and calculates statistics and volume across the complete loaded
history. It does not group entries into `WorkoutSession` objects and does not
promise weekly volume. Project-specific instructions and logs were aligned with
that implemented scope instead of documenting an unimplemented requirement.

The GUI test plan was updated to match the current empty-state message,
`No exercises logged yet.` All eight scenarios were then executed manually
against the packaged application in isolated data directories. This avoided
altering the user's normal FitLog data.

## Verification recorded

The completed GUI test record states:

* **Date:** 25 August 2026
* **OS:** Windows 11 25H2, build 26200.9168, AMD64
* **Java:** Eclipse Temurin 25.0.4+7
* **Result:** all eight scenarios passed

The broader repository verification used Java 25.0.4 and reported 157 automated
tests with zero failures or errors. The shadow JAR build also completed
successfully. Stale references to the previous test count were corrected in the
relevant logs.

## Developer Guide diagrams

Codex first drafted the diagrams without editing the repository so that their
accuracy and level of abstraction could be reviewed. After the user approved
the approach, two Mermaid diagrams were added to `docs/DeveloperGuide.md`:

1. an **architecture diagram** showing the JavaFX UI, command parser, command
   objects, model, and storage components, with arrows indicating which
   component initiates each runtime interaction; and
2. a **sequence diagram** showing a successful strength-log command that is
   parsed, checked for a personal record, added to the model, saved to disk, and
   reported to the user.

The diagrams were checked against the current code rather than inferred only
from the prose documentation. Captions explicitly identify the diagram types,
and surrounding paragraphs explain the responsibilities and interaction flow.
The Mermaid sequence-diagram activation bars were styled near-white with a dark
border so that they remain visible without rendering as heavy black blocks.

## Prompt record and Codex outcome

### Submission and grading audit

> "I have already done this project. Is there any criteria of the grading that
> I have missed out?"

Codex inspected the repository against the supplied submission instructions and
grading categories, distinguishing definite submission requirements from
optional improvements. The audit prioritised inaccurate or unverified
documentation because the assignment explicitly treats documentation
inaccuracies as bugs.

### GUI test plan and consistency work

> "Update the GUI test plan, manually execute all 8 scenarios, and record OS,
> Java version, date, and results."

> "Make Reflections.md not contradict; I reverted it and prompted again."

> "Remove all instances of WorkoutSession implementation ... FitLog only
> calculates all-history totals. Ensure the repository's internal project
> specification does not contradict the implementation."

Codex updated the relevant documentation, performed the eight manual GUI
scenarios, recorded the environment and outcomes, and checked the repository
for stale `WorkoutSession`, weekly-volume, and test-count claims. No new
`WorkoutSession` or weekly-statistics implementation was introduced because the
user explicitly defined those concepts as outside the product scope.

### Design-diagram drafting and implementation

> "Can you draw Mermaid diagrams ... don't implement yet, just explain to me
> and show me a draft. Check they accurately represent the current code."

> "Are you following CS2103/T standard? See this textbook."

> "Put the two diagrams in the Developer Guide, put captions and explanations
> of the diagrams in the guide; don't just leave them there."

Codex reviewed the current implementation and the referenced CS2103/T design
guidance, then kept the architecture diagram at the component/responsibility
level and the sequence diagram focused on one concrete runtime scenario. Once
approved, both diagrams and their explanations were added. A final presentation
follow-up changed the activation-bar styling for readability.

## Preserved outcome

This work changed documentation and verification records only. It did not alter
FitLog's runtime behaviour. The repository now presents a consistent product
scope, an executed GUI test record, current automated-test evidence, and visual
design documentation that corresponds to the implemented system.
