# 要求内容

## 対象

- 対象バージョン: 1.21.1
- 対象ディレクトリ: `C:\Users\htv432\IdeaProjects\psitweaks\1.21.1-worktree`
- 関連する既存ドキュメント: `docs/README.md`, `docs/1.21.1/development-guidelines.md`

## 概要

`SpellParamContextualValue` を追加し、プレイヤー向けに Entity と `ContextualValue` 実装型を同じ対象カテゴリとして扱えるようにする。

## 背景

`演算子: タグリスト` は Entity、Item、Block などの複雑なゲーム内対象を扱うため、`ParamAny` ではなく専用の入力データタイプで接続可能な型を明示したい。

## 実装対象の機能

### 1. `SpellParamContextualValue`
- `ContextualValue` を実装した評価型と `Entity` 系評価型を受け付ける。
- 必須型表示は `psitweaks.datatype.contextual_value` とする。

### 2. `演算子: タグリスト` の入力差し替え
- `ParamAny` ではなく `SpellParamContextualValue` を使用する。
- 受け付ける対象は Entity と `ContextualValue` 実装型に限定する。
- Vector 単体入力は新データタイプの対象外とする。

## スコープ外

- 1.20.1 への反映。
- 新しい visible spell piece の追加。
- Entity を `ContextualValue` interface 実装に変更すること。

## 検証方針

- 対象ディレクトリ: `C:\Users\htv432\IdeaProjects\psitweaks\1.21.1-worktree`
- 必須検証: `.\gradlew runData`, `.\gradlew compileJava`
- 条件付き検証: ゲーム内 `runClient` は手動確認が必要になった場合のみ。

## 成功指標

- `演算子: タグリスト` の入力 required type が `Contextual Value` になる。
- Entity、Item、Block などは接続可能で、String/Number/Vector 単体は接続不可になる。
- `runData` と `compileJava` が成功する。
