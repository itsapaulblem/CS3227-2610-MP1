# 02 – Basic command loop

## What was requested

FitLog was to begin as a minimal console application: greet the user, accept
commands repeatedly, echo non-`bye` input, and exit cleanly on `bye`. The user
explicitly asked not to add extra classes or workout logging yet.

## What landed

- A single `FitLog` main class implemented the console loop and greeting.
- The interaction matched the requested `Welcome to FitLog!`, prompt, echo, and
  `Goodbye! Keep training.` flow.
- This baseline was recorded by commit `9d5b306`.

## Notable follow-up

The user later noticed that an EOF guard had been lost during subsequent work.
It was restored so end-of-input exits gracefully rather than calling
`Scanner.nextLine()` when no line remains.

## Prompt record and Codex outcome

> "Create a minimal Java console application called FitLog that starts, greets
> the user, reads commands in a loop, and exits when the user types \"bye\" ...
> just echo back whatever command the user types (except \"bye\")."

Codex implemented the one-class console loop and later confirmed its sample
interaction. The follow-up requested restoring the missing EOF guard, which
produced the documented graceful EOF behaviour.

### Preserved Codex outcome

> "Implemented the minimal FitLog console app in `FitLog.java` ... Greets the
> user and shows a `> ` prompt ... Echoes every command except exact `bye` ...
> Verified by compiling and running it with Java 25 (`javac 25.0.4`). No other
> classes were added."

## Acceptance condition

I accepted the command loop only after a Java 25 compile-and-run check showed the
expected greeting and prompt, echoed ordinary input, exited on `bye`, and handled
EOF gracefully after the follow-up fix.
