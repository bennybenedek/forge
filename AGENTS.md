# AGENTS.md

Agent guidance for `D:\Projekte\Forge - Rogue Commander`.

This repository is a Java 17+ multi-module Maven project (Forge / Rogue Commander fork).

## Sources of truth

- Primary agent rules are in `CLAUDE.md` at repo root.
- `checkstyle.xml` defines enforced lint checks.
- Build and CI behavior is visible in `.github/workflows/test-build.yaml` and related workflow files.
- No Cursor rules were found (`.cursor/rules/` and `.cursorrules` are absent).
- No Copilot instructions file was found (`.github/copilot-instructions.md` is absent).

## High-priority operating rules

- Do not compile or run builds/tests unless the user explicitly asks.
- Do not add unit tests by default. Only add tests when the user explicitly asks for them, or when test additions are part of the agreed implementation plan.
- Do not implement beyond explicit user requirements; if ambiguous, ask one focused question.
- If the user asks for a specific change that appears already done, say so and wait for confirmation before substituting a different change.
- If completing a request appears to require an implementation step that was not in the plan or discussion, stop and discuss it before adding that step.
- If a seemingly small request turns out to require a broad or cross-module implementation (for example touching many files, adding new shared state, changing public/shared method signatures, or plumbing data through UI layers) and there was no preceding plan or implementation discussion, do not implement it automatically. Pause and ask the user to initiate or approve a planning step first.
- Keep changes lean and localized; prefer existing patterns over new abstractions.
- Preserve existing naming conventions when extending code; rename or extend within the established local vocabulary instead of introducing a new conceptual naming pattern for the same behavior.
- Avoid method overloads by default. Prefer one explicit method signature with nullable/empty parameters when appropriate, and only add overloads if the user explicitly wants them or there is a strong established pattern nearby.
- Check nearby code before editing; match the established approach in that package/module.
- Reuse existing mechanisms for UI, loading, events, persistence, and rendering before adding helpers.
- Avoid changes to shared/core Forge code (`forge-core`, `forge-game`, and generic shared desktop infrastructure) when a Rogue Commander-local fix can solve the issue. Only touch shared/core code when there is no reasonable Rogue-scoped solution, and call that out explicitly.
- Use the least code that correctly solves the task; avoid speculative cleanup or side improvements.
- Do not add one-time migrations by default. Ask the user for permission before adding any migration, especially if it was not part of the original plan.
- Do not add backward-compatibility, legacy-save compatibility, fallback migration behavior, or similar compatibility code unless the user explicitly asks for it.
- Do not introduce non-ASCII, mojibake, or typographic symbol replacements into source code, comments, tooltips, or user-facing strings unless the user explicitly asks for them. Preserve existing plain ASCII text by default.
- Preserve each file's existing encoding and BOM state. Do not add, remove, or change BOM/encoding unless the user explicitly asks for it.
- In plans, explicitly name the classes/files expected to change whenever that set is knowable, and during implementation do not expand beyond that set unless the need is explained to the user first.
- Do not introduce noisy class-level static constants or properties for values used in only one method. Keep one-off values local; use local constants/variables inside the method when repeated values improve readability.
- Use proper imports rather than fully qualified names in code.
- If adding multiple related fields, prefer grouping them into a record or small inner type when that fits local style.
- Extract duplicated logic when it is truly shared, but do not over-abstract simple or one-off code.
- Never revert unrelated local changes in a dirty worktree.
- Never use destructive git commands unless explicitly requested.
- Do not create commits unless explicitly requested.

## Project layout (quick map)

- `forge-core`: core rules engine and shared game primitives.
- `forge-game`: gameplay flow, abilities, triggers, replacement/static systems.
- `forge-ai`: AI behavior and simulation logic.
- `forge-gui`: resources and shared GUI assets (including card scripts in `res/cardsfolder`).
- `forge-gui-desktop`: Swing desktop app and many tests.
- `forge-gui-mobile`, `forge-gui-mobile-dev`, `forge-gui-android`, `forge-gui-ios`: mobile stacks.

## Build / lint / test commands

Run from repository root unless noted.

### Setup

- `mvn -v` - verify Maven and Java toolchain.
- `mvn -U -B clean -P windows-linux install` - initial dependency/bootstrap build profile.

### Full project

- `mvn clean install` - full clean build with tests.
- `mvn test` - run tests only.
- `mvn validate` - run validation phase (includes checkstyle execution in this repo).
- `mvn checkstyle:check` - run checkstyle directly.
- `mvn clean install -DskipTests` - package while skipping test execution.
- `mvn clean install -Dmaven.test.skip=true` - skip test compilation and execution.

### Module-targeted builds

- `mvn -pl forge-gui-desktop -am clean install` - desktop module with required dependencies.
- `mvn -pl forge-gui-android -am clean install` - android module with dependencies.
- `mvn -pl forge-game -am test` - test a specific module and upstream modules.
- Preferred default when explicitly asked to run tests from an IntelliJ PowerShell terminal:
  `mvn --% -pl forge-gui-desktop -am -U -B clean test`

### Run a single test class (important)

- Direct single-class execution in this repo can be unreliable with the current TestNG/PowerMock setup, especially from IntelliJ's built-in TestNG runner.
- Prefer the module-scoped default above unless the user specifically asks for narrower targeting.
- If narrowing to a class from a PowerShell terminal, start from:
  `mvn --% -pl forge-gui-desktop -am -Dtest=GameEventSerializationTest -Dsurefire.failIfNoSpecifiedTests=false test`

### Run a single test method (important)

- If method-level targeting is explicitly requested from a PowerShell terminal, start from:
  `mvn --% -pl forge-gui-desktop -am -Dtest=GameEventSerializationTest#testAllGameEventFieldsAreSerializable -Dsurefire.failIfNoSpecifiedTests=false test`

### Test framework notes

- Tests use TestNG annotations (`org.testng.annotations.Test`) in this codebase.
- Surefire is configured in root `pom.xml` and adds required JVM `--add-opens` flags.
- CI runs `mvn -U -B clean test` (see `.github/workflows/test-build.yaml`).
- IntelliJ's built-in TestNG runner may fail to discover or execute some Forge tests; prefer Maven/Surefire-based runs from the terminal.

### Test placement and scope

- Existing tests are concentrated in `forge-gui-desktop/src/test/java`.
- Current distribution is heavily skewed toward `forge-gui-desktop`; `forge-gui` currently has no test tree, and `forge-game` only has a very small number of tests.
- When adding tests for logic in `forge-gui` or Rogue Commander code, prefer placing them in `forge-gui-desktop/src/test/java` unless there is already a stronger module-local test pattern in the touched module.
- Mirror the production package in the test package when possible (for example `forge.gamemodes.rogue.*` tests under `forge-gui-desktop/src/test/java/forge/gamemodes/rogue/`).
- Many files under `src/test/java` are shared test infrastructure or harness utilities, not standalone test classes; do not assume every file there should contain `@Test` methods.
- The existing suite mainly covers simulation/integration flows, AI behavior, card database loading/mocking, deck parsing/generation, helper/logic classes, and contract-style checks such as serialization.
- Direct Swing/UI coverage exists, but it is sparse and not the dominant pattern; do not default to button-click or window-visibility tests when a logic-level test would cover the change more reliably.
- Minimal-logic or data-oriented classes are usually tested only when they protect an important contract or regression surface (for example serialization/network behavior), not by default just because a class exists.
- If a test needs loaded cards, `FModel`, or card database access, prefer extending the existing card test bases such as `CardMockTestCase` or `CardDbCardMockTestCase` instead of building custom bootstrap code.

### Running the application

- Desktop entry point: `forge.view.Main` in `forge-gui-desktop`.
- Mobile dev entry point: `forge.app.Main` in `forge-gui-mobile-dev`.
- If manual IDE launches fail because of Java module access, check `CLAUDE.md` for the current recommended VM `--add-opens` set.

## Code style and conventions

Follow existing file-local conventions first, then these defaults.

### Imports

- Use explicit `import` statements; do not use fully qualified class names inline.
- Keep imports at top, grouped by package style already used in the file.
- `checkstyle.xml` enforces at least:
  - no redundant imports
  - no unused imports

### Formatting

- Match surrounding file formatting exactly (legacy style varies by package).
- Keep line wrapping and brace style consistent with nearby code.
- Avoid reformat-only diffs unless requested.

### Types and APIs

- Prefer concrete domain types already used in the module over generic catch-all types.
- Use enums/records when existing code in that area already relies on them.
- Avoid introducing new abstraction layers unless necessary for repeated logic.
- Reuse existing utility methods before adding new helpers.
- Preserve existing collection and utility choices in the touched area (Guava, Forge utility types, etc.).

### Naming

- Use clear, direct names that describe behavior.
- Prefer action verbs for methods (`loadCard`, `handleMatchData`, `incrementNpcLevel`).
- Keep names consistent with neighboring classes and package patterns.

### Error handling

- Use guard clauses and early returns for invalid state checks.
- Catch narrow exceptions only when recovery/fallback is intentional.
- Do not swallow exceptions silently; either handle with context or propagate.
- Preserve existing nullability patterns in the touched area.

### Comments and docs

- Add comments only for non-obvious behavior or rule constraints.
- Prefer self-explanatory code over explanatory comments.
- Keep public API docs concise and aligned with established style.

## Rogue Commander-specific architecture rules

These are mandatory when touching Rogue Commander code.

- Keep effect logic inside the `RogueEffect` system.
- Keep NPC progression/dialog logic inside the `NPCEncounter` system.
- `RogueEffect` implementations should own their trigger-specific behavior (`onRunStart`, `onMatchStart`, `onMatchWin`, `onDefeat`, etc.).
- `NPCEncounter` implementations should own level-gated NPC logic and return `NPCContext` where appropriate.
- Controllers should only:
  - persist raw run/match data
  - call composite trigger dispatchers
  - render/display resulting context/dialog objects
- Do not place effect-specific or NPC-specific logic directly in controllers.
- If an effect needs game data, persist that data generically on `RogueRun`, then let the effect read it itself.

## Broader architecture notes

- `forge-game` contains the ability system, triggers, replacement effects, static abilities, and zone/game flow logic.
- The ability system is centered on `AbilityFactory` and effect implementations under `forge-game/.../ability/effects/`.
- AI behavior is concentrated in `forge-ai`, especially `AiController`, `ComputerUtil*`, `SpellAbilityAi`, and `simulation/`.
- Card interactions are usually implemented through triggers, static abilities, replacement effects, and state-based handling rather than ad hoc controller logic.

## Card scripting rules (when editing `forge-gui/res/cardsfolder`)

- Plane static abilities must include `EffectZone$ Command`.
- Plane chaos triggers should use `TriggerZones$ Command`.
- Zone-qualified `Valid...` SVar tokens must use correct compact syntax (e.g., `ValidGraveyard`).
- For triggered targeting tied to the triggering card's controller, use the established `TargetingPlayer$ TriggeredCardController` pattern when appropriate.
- Keep script files concise and aligned with existing card script conventions.

## Platform and environment notes

- Java 17+ is required.
- Maven 3.8.1+ is required.
- Android work must respect current SDK/tooling expectations visible in `.github/workflows/test-android-build.yml`.
- Be careful with Android API compatibility; apparently available JDK methods may still be unsupported on Android targets.
- Proguard/tooling details in `CONTRIBUTING.md` and CI files are relevant when touching Android packaging.
- Art assets added to the project should be copyright-free / public domain per repo guidance.

## Agent workflow recommendations

- Before editing, inspect nearby implementations in the same package.
- For Rogue UI changes, check patterns under `forge-gui-desktop/src/main/java/forge/screens/home/rogue/` first.
- Prefer minimal diffs that solve the exact requested problem.
- After edits, run only the narrowest verification command the user requested.
- When summarizing results, include touched file paths and any commands executed.
- For setup or IDE-specific troubleshooting, `CONTRIBUTING.md` and `CLAUDE.md` contain more detail than this summary file.

## Environment baseline

- Java: 17+
- Maven: 3.8.1+
- Main branch: `master`
- Desktop entry point: `forge.view.Main`
- Mobile dev entry point: `forge.app.Main`

## Additional local references

- `C:\Users\BEBENEDE\Documents\Mtg Rogue Contents_v2.0.0.pdf`
- `C:\Users\BEBENEDE\Documents\Mtg Rogue Rules_v2.0.0.pdf`
