# Requirements

- Target directory: `C:\Users\htv432\IdeaProjects\psitweaks\1.21.1-worktree`
- Add a new spell piece `operator_tag_list` named `演算子: タグリスト`.
- The piece accepts a target value that players can understand as a contextual game object, including Entity, Item, Block, and other `ContextualValue` implementations.
- The piece returns a `String List` containing registry tag IDs for the target.
- Treat Vector inputs as block positions, matching existing Block value compatibility behavior.
- Entity outputs must use entity type registry tags, not scoreboard/custom entity tags.
- Remove the development-only spell piece `operator_block_has_tag` / `演算子: ブロックタグ判定`.
- Reuse the old block-tag-check texture for `operator_tag_list`.
- Do not keep a compatibility alias for `operator_block_has_tag`.

# Out Of Scope

- No 1.20.1 backport.
- No gameplay/client manual test in this task unless requested.
- No support for non-registry/freeform tags.
