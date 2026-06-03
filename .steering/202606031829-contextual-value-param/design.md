# 設計書

## 対象

- 対象バージョン: 1.21.1
- 対象ディレクトリ: `C:\Users\htv432\IdeaProjects\psitweaks\1.21.1-worktree`
- 前提条件: 前回作業で `ContextualValue#getTagIds` と `PieceOperatorTagList` が追加済み。

## アーキテクチャ概要

Psi の `SpellParam` 接続判定に合わせ、`Object` を required type にしつつ `canAccept` を明示的に絞る専用 Param を追加する。

## コンポーネント設計

### 1. `SpellParamContextualValue`

**責務**:
- Entity と `ContextualValue` 実装型だけを接続可能にする。
- UI 上の required type を `Contextual Value` として表示する。

**実装の要点**:
- `SpellParam<Object>` を継承する。
- `canAccept` で `Entity.class.isAssignableFrom(type)` または `ContextualValue.class.isAssignableFrom(type)` を判定する。

### 2. `PieceOperatorTagList`

**責務**:
- `演算子: タグリスト` の入力型を新 Param に差し替える。

**実装の要点**:
- `ParamAny` を削除し、`SpellParamContextualValue` を使用する。
- helper 側も Vector 単体分岐を削除して、受け付ける型と実行時処理を一致させる。

## ディレクトリ構造

```text
src/main/java/com/moratan251/psitweaks/common/spells/param/SpellParamContextualValue.java
src/main/java/com/moratan251/psitweaks/common/spells/operator/PieceOperatorTagList.java
src/main/java/com/moratan251/psitweaks/common/spells/operator/TagTargetHelper.java
src/main/java/com/moratan251/psitweaks/datagen/providers/PsitweaksLanguageProvider.java
src/generated/resources/assets/psitweaks/lang/*.json
```

## 実装の順序

1. `SpellParamContextualValue` を追加する。
2. `PieceOperatorTagList` と `TagTargetHelper` を差し替える。
3. 言語 provider と生成済み lang を更新する。
4. `runData` と `compileJava` を実行する。

## 移植メモ / 互換性リスク

- Forge / NeoForge API差分: 該当なし。
- Minecraftバージョン差分: 該当なし。
- 依存Mod差分: Psi の `SpellParam` 接続判定に依存する。
- Mixin / network / datagen 差分: datagen のみ更新。
- 一時的な無効化・隔離方針: 該当なし。

## パフォーマンス考慮事項

- 接続判定は評価型 `Class` の assignability 判定のみで軽量。

## 将来の拡張性

新しい複雑値型を `ContextualValue` として実装すれば、追加登録なしでこの Param に接続可能になる。
