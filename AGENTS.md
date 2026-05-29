# AGENTS.md

Use this file when an agent starts inside the `1.21.1` branch or local worktree `1.21.1-worktree/`.

## Read First

1. This `AGENTS.md`
2. `../AGENTS.md`
3. `../docs/README.md`
4. `../docs/1.21.1/development-guidelines.md`
5. Add `../docs/1.21.1/architecture.md`, `repository-structure.md`, `porting-decisions.md`, `memory-leak-checklist.md`, or `glossary.md` only when relevant.

## Role

- This is the primary NeoForge 1.21.1 development worktree.
- Treat this dir as the Git/project root for 1.21.1 work.
- Use `../1.20.1/` only as legacy Forge reference or when explicitly porting/backporting.
- Store shared task state in `../.steering/`; use skills from `../.agents/skills/`.

## Notes

- Run Gradle commands in this worktree.
- Minimum verification after implementation: `.\gradlew compileJava`.
- Run `.\gradlew runData` for datagen changes.
- Do not copy Forge 1.20.1 code blindly; verify NeoForge 1.21.1 API differences first.
