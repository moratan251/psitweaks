# Design

Add a default `ContextualValue#getTagIds(SpellContext)` method returning an empty set, then override it for value types that can expose registry tags.

`BlockValue` will return the snapshot block state tags it already stores. `SpellItemValue` will return item registry tags from the stored item stack snapshot.

`PieceOperatorTagList` will use `ParamAny` so Entity, BlockValue, SpellItemValue, Vector, and future contextual value types can share one UI-facing piece. A helper will normalize targets into sorted `namespace:path` strings:

- Entity: verify the entity, then return entity type tags.
- ContextualValue: call `getTagIds`.
- Vector: snapshot the vector as a block value and return its block tags.
- Unsupported target: return an empty string list.

The old `PieceOperatorBlockHasTag` class, registration, client material, language entries, and generated assets will be removed or replaced with `operator_tag_list`.
