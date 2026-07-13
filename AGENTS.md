# AGENTS.md

Use this file when an agent starts inside `1.21.1-worktree/`. The workspace-level rules in `../AGENTS.md` still apply.

## Read First

1. `../AGENTS.md`
2. `../docs/README.md`
3. `../docs/1.21.1/development-guidelines.md`
4. Add `architecture.md`, `repository-structure.md`, `porting-decisions.md`, `memory-leak-checklist.md`, or `glossary.md` from `../docs/1.21.1/` only when relevant.

## Role

- This is the main Minecraft 1.21.1 / NeoForge development worktree.
- Use `../1.20.1/` only for an explicit 1.20.1 fix, backport, or porting reference.
- Store task state in `../.steering/`; never create a worktree-local `.steering/`.
- Use shared skills from `../.agents/skills/`.

## Local Rules

- Use Java 21 and the current NeoForge 21.1.x APIs present in this worktree.
- Follow the existing `api` / `common` / `client` / `datagen` / `mixin` layout and keep client-only code isolated.
- Register capabilities through `RegisterCapabilitiesEvent`, prefer data components for item state, and register network payloads through the existing payload handler flow.
- Inspect 1.21.1 source or dependency classes before using an API. Do not infer NeoForge behavior from `../1.20.1/` alone.
- Do not edit JSON under `src/generated/resources/` directly. Change the provider or its hand-written input, run `.\gradlew runData`, and inspect the generated diff.
- Before `runData`, check for existing provider/generated changes and do not overwrite unrelated work.
- For a 1.20.1 backport, follow `../docs/1.21.1/porting-decisions.md`.

## Verification

- Run Gradle commands in this directory.
- Minimum implementation verification: `.\gradlew compileJava`.
- Datagen changes: `.\gradlew runData`, followed by a generated-resource diff review.
- Visible/client changes: `.\gradlew runClient` when practical.
- GameTest additions: `.\gradlew runGameTestServer`.
- Do not commit `run/`, logs, crash reports, or unrelated generated output.
