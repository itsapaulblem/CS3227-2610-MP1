# 12 – Gradle, Java package, and environment setup

## What was requested

The user chose Gradle over manually managing JUnit JARs. The project needed a
Java 25 toolchain, Gradle wrapper, JUnit 5, standard test directories, and a
move from the default package to `fitlog`, with no behaviour change.

## What landed

- `settings.gradle.kts`, `build.gradle.kts`, and Gradle wrapper.
- Java 25 toolchain and JUnit Platform configuration.
- All application classes moved to `src/main/java/fitlog`.
- Test source root `src/test/java/fitlog`.

The mechanical build/package change is commit `4c28dac`.

## Real problem encountered

Gradle initially failed to download JUnit with a PKIX SSL certificate error.
The user confirmed Java 25, ISRG Root X1/X2 certificates, and a successful
direct JShell HTTPS connection, then stopped old Gradle daemons. A later error
identified the missing JUnit Platform launcher, which was added as a test runtime
dependency. The test task subsequently ran successfully.

## Prompt record and Codex outcome

> "Go ahead and add Gradle: settings.gradle.kts, build.gradle.kts, Gradle
> wrapper, Java 25 toolchain, JUnit 5 ... Also move all existing classes ... into
> a package named fitlog ... no behavior changes."

The student later supplied the actual SSL and JUnit Platform errors from the
terminal. Codex diagnosed them from the output, suggested certificate/daemon
checks, and the configuration gained the JUnit Platform launcher dependency.

## Acceptance condition

I accepted the build migration only after Gradle used the Java 25 toolchain, the
code compiled under the `fitlog` package, the JUnit Platform launcher issue was
resolved, and the test task completed successfully without changing behaviour.
