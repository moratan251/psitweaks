package com.moratan251.psitweaks.datagen.providers;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class PsitweaksLanguageProvider extends LanguageProvider {
    private final String locale;

    public PsitweaksLanguageProvider(PackOutput output, String locale) {
        super(output, Psitweaks.MOD_ID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        switch (locale) {
            case "en_us" -> addEnglishTranslations();
            case "ja_jp" -> addJapaneseTranslations();
            default -> throw new IllegalStateException("Unsupported locale: " + locale);
        }
        addBackportedStringTranslations();
        addBackportedMatrixTranslations();
    }

    private void addBackportedStringTranslations() {
        add("psitweaks.datatype.string", switch (locale) {
            case "ja_jp" -> "String";
            default -> "String";
        });
        add("psitweaks.datatype.string_list", switch (locale) {
            case "ja_jp" -> "String List";
            default -> "String List";
        });
        add("psitweaks.datatype.number_list", switch (locale) {
            case "ja_jp" -> "Number List";
            default -> "Number List";
        });
        add("psitweaks.datatype.vector_list", switch (locale) {
            case "ja_jp" -> "Vector List";
            default -> "Vector List";
        });
        add("psitweaks.datatype.vector_or_number_list", switch (locale) {
            case "ja_jp" -> "Vector/Number List";
            default -> "Vector/Number List";
        });
        add("psitweaks.datatype.any_list", switch (locale) {
            case "ja_jp" -> "Any List";
            default -> "Any List";
        });
        add("psitweaks.datatype.plain_value", switch (locale) {
            case "ja_jp" -> "Plain Value";
            default -> "Plain Value";
        });
        add("psitweaks.datatype.matrix", switch (locale) {
            case "ja_jp" -> "Matrix";
            default -> "Matrix";
        });
        add("psitweaks.datatype.matrix_list", switch (locale) {
            case "ja_jp" -> "Matrix List";
            default -> "Matrix List";
        });
        add("psitweaks.datatype.item", switch (locale) {
            case "ja_jp" -> "Item";
            default -> "Item";
        });
        add("psitweaks.datatype.item_list", switch (locale) {
            case "ja_jp" -> "Item List";
            default -> "Item List";
        });
        add("psitweaks.datatype.block", switch (locale) {
            case "ja_jp" -> "Block";
            default -> "Block";
        });
        add("psitweaks.datatype.block_list", switch (locale) {
            case "ja_jp" -> "Block List";
            default -> "Block List";
        });
        add("psitweaks.datatype.contextual_value", switch (locale) {
            case "ja_jp" -> "Contextual Value";
            default -> "Contextual Value";
        });
        add("psitweaks.datatype.contextual_value_list", switch (locale) {
            case "ja_jp" -> "Contextual Value List";
            default -> "Contextual Value List";
        });
        add("psitweaks.gui.spell_piece_mode", switch (locale) {
            case "ja_jp" -> "モード: %s";
            default -> "Mode: %s";
        });
        add("psitweaks.gui.spell_piece_mode.title", switch (locale) {
            case "ja_jp" -> "モード選択";
            default -> "Mode Select";
        });
        add("screen.psitweaks.configuration.title", switch (locale) {
            case "ja_jp" -> "Psitweaks コンフィグ";
            default -> "Psitweaks Configuration";
        });
        add("screen.psitweaks.configuration.disable_damage_psi_deduction", switch (locale) {
            case "ja_jp" -> "被ダメージ時のPsi消費を無効化";
            default -> "Disable Damage Psi Deduction";
        });
        add("screen.psitweaks.configuration.disable_regen_cooldown", switch (locale) {
            case "ja_jp" -> "Psi回復クールダウンを無効化";
            default -> "Disable Psi Regeneration Cooldown";
        });
        add("psi.book.page.psitweaks_changes.1", switch (locale) {
            case "ja_jp" -> "$(p)スペルプログラム画面では, Shiftを押しながらグリッドを左ドラッグすると, 複数のスペルピースを範囲選択できます. 選択されたスペルピースは青くハイライトされ, コピーされる範囲は青い枠で示されます.$(p)Ctrlを押しながらスペルピースを左クリックすると, そのピースを選択に追加したり, 選択から外したりできます. 複数選択中は Ctrl+C でコピー, Ctrl+X で切り取り, Ctrl+V で貼り付けを行えます. 貼り付けは, Psiで現在選択されているセルをコピー範囲の左上として行われます.";
            default -> "In the Spell Programmer screen, hold Shift and left-drag across the grid to select multiple spell pieces. Selected spell pieces are highlighted in blue, and the copied range is shown with a blue outline.$(p)Hold Ctrl and left-click a spell piece to add it to or remove it from the selection. While multiple pieces are selected, use Ctrl+C to copy, Ctrl+X to cut, and Ctrl+V to paste. Pasting uses the cell currently selected in Psi as the upper-left corner of the copied range.";
        });
        add("psitweaks.gui.string_constant_input.empty", switch (locale) {
            case "ja_jp" -> "空文字列";
            default -> "Empty string";
        });
        add("psitweaks.gui.string_constant_input.hint", switch (locale) {
            case "ja_jp" -> "Shift+Enter改行 / Enter閉";
            default -> "Shift+Enter newline / Enter closes";
        });
        add("psitweaks.gui.string_constant_input.read_only", switch (locale) {
            case "ja_jp" -> "閲覧のみ";
            default -> "Read only";
        });
        add("psitweaks.gui.string_constant_input.button.copy_all", switch (locale) {
            case "ja_jp" -> "全コピー";
            default -> "Copy All";
        });
        add("psitweaks.gui.string_constant_input.button.clear_all", switch (locale) {
            case "ja_jp" -> "全削除";
            default -> "Clear";
        });
        add("psitweaks.gui.string_constant_input.button.replace_all", switch (locale) {
            case "ja_jp" -> "貼付";
            default -> "Paste";
        });
        add("psitweaks.spellparam.string", switch (locale) {
            case "ja_jp" -> "文字列";
            default -> "String";
        });
        add("psitweaks.spellparam.string1", switch (locale) {
            case "ja_jp" -> "文字列1";
            default -> "String 1";
        });
        add("psitweaks.spellparam.string2", switch (locale) {
            case "ja_jp" -> "文字列2";
            default -> "String 2";
        });
        add("psitweaks.spellparam.string3", switch (locale) {
            case "ja_jp" -> "文字列3";
            default -> "String 3";
        });
        add("psitweaks.spellparam.value1", switch (locale) {
            case "ja_jp" -> "値1";
            default -> "Value 1";
        });
        add("psitweaks.spellparam.value2", switch (locale) {
            case "ja_jp" -> "値2";
            default -> "Value 2";
        });
        add("psitweaks.spellparam.value3", switch (locale) {
            case "ja_jp" -> "値3";
            default -> "Value 3";
        });
        add("psitweaks.spellparam.element1", switch (locale) {
            case "ja_jp" -> "要素1";
            default -> "Element 1";
        });
        add("psitweaks.spellparam.element2", switch (locale) {
            case "ja_jp" -> "要素2";
            default -> "Element 2";
        });
        add("psitweaks.spellparam.element3", switch (locale) {
            case "ja_jp" -> "要素3";
            default -> "Element 3";
        });
        add("psitweaks.spellparam.vector_or_number_list", switch (locale) {
            case "ja_jp" -> "数列";
            default -> "Array";
        });
        add("psitweaks.spellparam.size", switch (locale) {
            case "ja_jp" -> "サイズ";
            default -> "Size";
        });
        add("psitweaks.spellparam.indices", switch (locale) {
            case "ja_jp" -> "成分";
            default -> "Indices";
        });
        add("psitweaks.spellerror.plain_memory_type", switch (locale) {
            case "ja_jp" -> "保存された値を選択中の型として読み取れません";
            default -> "Stored value cannot be read as the selected type";
        });
        add("psitweaks.spellerror.plain_memory_parse", switch (locale) {
            case "ja_jp" -> "保存された文字列を選択中の型へ変換できません";
            default -> "Stored string cannot be converted to the selected type";
        });
        add("psitweaks.spellerror.empty_delimiter", switch (locale) {
            case "ja_jp" -> "区切り文字が空です";
            default -> "Delimiter cannot be empty";
        });
        add("psitweaks.spellerror.expected_three_numbers", switch (locale) {
            case "ja_jp" -> "Number List の要素数は3である必要があります";
            default -> "Number List must contain exactly 3 elements";
        });
        add("psitweaks.spellerror.no_jump_anchor", switch (locale) {
            case "ja_jp" -> "前方に一致するジャンプアンカーがありません";
            default -> "No matching Jump Anchor found ahead";
        });
        add("psitweaks.spellpiece.trick_jump", switch (locale) {
            case "ja_jp" -> "作動式: ジャンプ";
            default -> "Trick: Jump";
        });
        add("psitweaks.spellpiece.trick_jump.desc", switch (locale) {
            case "ja_jp" -> "対象数値が未入力、または絶対値が1未満なら、同じ定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします。";
            default -> "If the target number is unset or its absolute value is less than 1, jumps forward to the next Jump Anchor with the same constant label.";
        });
        add("psitweaks.spellpiece.trick_switch", switch (locale) {
            case "ja_jp" -> "作動式: スイッチ";
            default -> "Trick: Switch";
        });
        add("psitweaks.spellpiece.trick_switch.desc", switch (locale) {
            case "ja_jp" -> "入力Stringと同じ定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします。一致するアンカーがない場合はそのまま次へ進みます。";
            default -> "Jumps forward to the next Jump Anchor with the same constant label as the input String. If no matching anchor exists, execution continues.";
        });
        add("psitweaks.spellpiece.jump_anchor", switch (locale) {
            case "ja_jp" -> "ジャンプアンカー";
            default -> "Jump Anchor";
        });
        add("psitweaks.spellpiece.jump_anchor.desc", switch (locale) {
            case "ja_jp" -> "作動式: ジャンプと作動式: スイッチの到達点になる、何もしない目印です。任意の定数ラベルを指定できます。";
            default -> "A no-op marker used as the destination for Trick: Jump and Trick: Switch. It can take an optional constant label.";
        });
        add("psi.book.page.psitweaks_spellpiece.trick_jump", switch (locale) {
            case "ja_jp" -> "対象数値が未入力、または絶対値が1未満なら, 同じラベルを持つ次のジャンプアンカーまで前方にジャンプします. ラベル入力は任意ですが, 使う場合はString定数である必要があります. 空ラベルは空ラベルのアンカーにのみ一致します. 対象入力なしの場合は無条件の前方ジャンプとして動作します. 後方のジャンプアンカーには飛ぶことができません.";
            default -> "Jumps forward to the next Jump Anchor with the same label when the target number is unset, or when its absolute value is less than 1. The label input is optional, but if used it must be a String constant. Empty labels only match empty-label anchors. With no target input, it acts as an unconditional forward jump. It cannot jump to Jump Anchors behind it.";
        });
        add("psi.book.page.psitweaks_spellpiece.trick_switch", switch (locale) {
            case "ja_jp" -> "入力Stringを評価し, それと等しい定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします. 前方に一致するアンカーがない場合はエラーにならず, そのまま次のスペルピースへ進むため, default相当の処理はスイッチの直後に置いてください.  後方のジャンプアンカーには飛ぶことができません.";
            default -> "Evaluates the input String and jumps forward to the next Jump Anchor whose constant label is equal to it. If no matching anchor exists ahead, it does not error and simply continues to the next spell piece, so place the default branch immediately after the Switch. It cannot jump to Jump Anchors behind it.";
        });
        add("psi.book.page.psitweaks_spellpiece.jump_anchor", switch (locale) {
            case "ja_jp" -> "作動式: ジャンプと作動式: スイッチの到達点になる目印です.  このスペルピース自体は何もしません. 定数文字列をラベルとして入力することができます.";
            default -> "A marker used as the destination for Trick: Jump and Trick: Switch. This spell piece itself does nothing. You can input a constant String as its label.";
        });
        add("psitweaks.spellpiece.trick_store_value", switch (locale) {
            case "ja_jp" -> "作動式: 値を保存";
            default -> "Trick: Store Value";
        });
        add("psitweaks.spellpiece.trick_store_value.desc", switch (locale) {
            case "ja_jp" -> "Plain Value (Number, Vector, String ... )をCADメモリに保存する。Stringは最大128文字まで保存します。";
            default -> "Store a Plain Value (Number, Vector, String ... ) in CAD memory. Strings are stored up to 128 characters.";
        });
        add("psitweaks.spellpiece.selector_stored_value", switch (locale) {
            case "ja_jp" -> "取得子: 保存された値";
            default -> "Selector: Stored Value";
        });
        add("psitweaks.spellpiece.selector_stored_value.desc", switch (locale) {
            case "ja_jp" -> "CADメモリに保存されたPlain Valueを取得する";
            default -> "Retrieve a Plain Value from CAD memory";
        });
        add("psi.book.page.psitweaks_spellpiece.trick_store_value", switch (locale) {
            case "ja_jp" -> "Plain ValueをCADメモリに保存します. メモリ番号はPsi標準と同じく1が最初のスロットです. Number, Vector, Stringは同じスロットで互いに上書きされます. 128文字を超えるStringは保存時に切り捨てられ, 術者のチャット欄に警告が表示されます.";
            default -> "Stores a Plain Value in CAD memory. The memory slot uses Psi's standard numbering: 1 is the first slot. Number, Vector, and String values overwrite each other in the same slot. String values longer than 128 characters are truncated when stored, and a warning is shown in the caster's chat.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_stored_value", switch (locale) {
            case "ja_jp" -> "CADメモリからPlain Valueを取得します. モードボタンでString, Number, Vectorを選択します. String変換は厳格で, NumberとVectorは直接変換できません.";
            default -> "Gets a Plain Value from CAD memory. Use the mode button to choose String, Number, or Vector. String conversions are strict; Number and Vector cannot be converted directly.";
        });
        add("psitweaks.spellpiece.operator_block_position", switch (locale) {
            case "ja_jp" -> "演算子: ブロック座標";
            default -> "Operator: Block Position";
        });
        add("psitweaks.spellpiece.operator_block_position.desc", switch (locale) {
            case "ja_jp" -> "Block 値に保存された座標を通常の Vector として出力します。";
            default -> "Outputs the saved position of a Block value as a plain Vector.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_block_position", switch (locale) {
            case "ja_jp" -> "Block に保存された座標を通常の Vector として出力します. 基本的に Vector を必要とするスペルピースには Block 型をそのまま接続できますが、作動式: デバッグ に接続して座標を確認する際など Vector型 が明示的に必要な場合に使います.";
            default -> "Outputs the position saved in a Block as a plain Vector. A Block can usually be connected directly to spell pieces that require a Vector, but use this when a Vector value is explicitly needed, such as when checking coordinates with Trick: Debug.";
        });
        add("psitweaks.spellpiece.operator_block_position_list", switch (locale) {
            case "ja_jp" -> "演算子: ブロック位置リスト";
            default -> "Operator: Block Position List";
        });
        add("psitweaks.spellpiece.operator_block_position_list.desc", switch (locale) {
            case "ja_jp" -> "Block List のブロックの座標を Vector List として取得します。";
            default -> "Gets the positions of the blocks in a Block List as a Vector List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_block_position_list", switch (locale) {
            case "ja_jp" -> "Block List に保存された座標を Vector List として取得します.";
            default -> "Gets the positions saved in a Block List as a Vector List.";
        });
        add("psitweaks.spellpiece.operator_block_state", switch (locale) {
            case "ja_jp" -> "演算子: ブロックステート";
            default -> "Operator: Block State";
        });
        add("psitweaks.spellpiece.operator_block_state.desc", switch (locale) {
            case "ja_jp" -> "Block 値に保存されたブロックステートを文字列として出力します。例: minecraft:oak_stairs[facing=north]";
            default -> "Outputs the saved block state of a Block value as a String. Example: minecraft:oak_stairs[facing=north]";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_block_state", switch (locale) {
            case "ja_jp" -> "Block 値に保存されたブロックステートを String として出力します. 出力は minecraft:oak_stairs[facing=north,half=bottom] のような Minecraft のコマンド風表記です. 値は Block のスナップショットから取得され, 後からワールドが変化しても変わりません.";
            default -> "Outputs the block state saved in a Block value as a String, using Minecraft's command-style form such as minecraft:oak_stairs[facing=north,half=bottom]. The value comes from the Block snapshot and does not change if the world changes later.";
        });
        add("psitweaks.spellpiece.operator_block_state_entries", switch (locale) {
            case "ja_jp" -> "演算子: ブロックステート項目";
            default -> "Operator: Block State Entries";
        });
        add("psitweaks.spellpiece.operator_block_state_entries.desc", switch (locale) {
            case "ja_jp" -> "Block 値に保存されたブロックステートのプロパティ項目を String List として出力します。";
            default -> "Outputs the saved block state properties of a Block value as a String List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_block_state_entries", switch (locale) {
            case "ja_jp" -> "Block 値に保存されたブロックステートのプロパティを String List として出力します. 各要素は facing:north や waterlogged:false のような property:value 形式です. state property を持たないブロックは空の String List を出力します.";
            default -> "Outputs the properties saved in a Block value's block state as a String List. Each entry uses property:value form, such as facing:north or waterlogged:false. Blocks with no state properties output an empty String List.";
        });
        add("psitweaks.spellpiece.operator_block_state_value", switch (locale) {
            case "ja_jp" -> "演算子: ブロックステート値";
            default -> "Operator: Block State Value";
        });
        add("psitweaks.spellpiece.operator_block_state_value.desc", switch (locale) {
            case "ja_jp" -> "Block 値に保存されたブロックステートから指定したプロパティ値を String として出力します。";
            default -> "Outputs one saved block state property value from a Block value.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_block_state_value", switch (locale) {
            case "ja_jp" -> "Block 値に保存されたブロックステートから指定 property の値を1つ出力します. String 入力には facing や waterlogged などの property 名を指定します. property が存在しない場合は空の String を出力します.";
            default -> "Outputs one property value from the block state saved in a Block value. Use the String input to name a property such as facing or waterlogged. If the property does not exist, it outputs an empty String.";
        });
        add("psitweaks.spellpiece.operator_equal", switch (locale) {
            case "ja_jp" -> "演算子: 等しい";
            default -> "Operator: Equal";
        });
        add("psitweaks.spellpiece.operator_equal.desc", switch (locale) {
            case "ja_jp" -> "値1と値2が等しいなら1、そうでなければ0を出力します。";
            default -> "Outputs 1 if Value 1 equals Value 2, otherwise 0.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_equal", switch (locale) {
            case "ja_jp" -> "値1と値2を Any 入力として比較します. 2つの値が等しいなら1, そうでなければ0を出力します. Number は数値, Vector は座標, Item はアイテムデータと source, Block はディメンション, 座標, ブロック状態を比較します. Block と Vector の比較は座標比較です.";
            default -> "Compares Value 1 and Value 2 as Any inputs. It outputs 1 when both values are equal, otherwise 0. Numbers compare by numeric value, Vectors compare by coordinates, Item values compare their item data and source, and Block values compare dimension, position, and block state. A Block compared with a Vector uses coordinate comparison.";
        });
        add("psitweaks.spellpiece.operator_greater_than", switch (locale) {
            case "ja_jp" -> "演算子: ～より大きい";
            default -> "Operator: Greater Than";
        });
        add("psitweaks.spellpiece.operator_greater_than.desc", switch (locale) {
            case "ja_jp" -> "値1が値2より大きいなら1、そうでなければ0を出力します。Number を入力できます。";
            default -> "Outputs 1 if Value 1 is greater than Value 2, otherwise 0. Number inputs are accepted.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_greater_than", switch (locale) {
            case "ja_jp" -> "値1と値2を Number として比較します. 値1が値2より大きいなら1, そうでなければ0を出力します.";
            default -> "Compares Value 1 and Value 2 as Numbers. It outputs 1 when Value 1 is greater than Value 2, otherwise 0.";
        });
        add("psitweaks.spellpiece.operator_greater_than_or_equal", switch (locale) {
            case "ja_jp" -> "演算子: ～以上";
            default -> "Operator: Greater Than or Equal";
        });
        add("psitweaks.spellpiece.operator_greater_than_or_equal.desc", switch (locale) {
            case "ja_jp" -> "値1が値2以上なら1、そうでなければ0を出力します。Number を入力できます。";
            default -> "Outputs 1 if Value 1 is greater than or equal to Value 2, otherwise 0. Number inputs are accepted.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_greater_than_or_equal", switch (locale) {
            case "ja_jp" -> "値1と値2を Number として比較します. 値1が値2以上なら1, そうでなければ0を出力します.";
            default -> "Compares Value 1 and Value 2 as Numbers. It outputs 1 when Value 1 is greater than or equal to Value 2, otherwise 0.";
        });
        add("psitweaks.spellpiece.operator_get_id", switch (locale) {
            case "ja_jp" -> "演算子: レジストリID";
            default -> "Operator: Registry ID";
        });
        add("psitweaks.spellpiece.operator_get_id.desc", switch (locale) {
            case "ja_jp" -> "Contextual Value のレジストリIDを String として出力します。";
            default -> "Outputs the registry ID of a Contextual Value.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_get_id", switch (locale) {
            case "ja_jp" -> "Contextual Value のレジストリIDを String として出力します. Entity 入力は EntityType ID, Item 入力は Item ID, Block 入力は Block ID を出力します. 対応していない contextual value は空の String を出力します.";
            default -> "Outputs the registry ID of a Contextual Value as a String. Entity inputs output the EntityType ID, Item inputs output the Item ID, and Block inputs output the Block ID. Unsupported contextual values output an empty String.";
        });
        add("psitweaks.spellpiece.operator_get_id_list", switch (locale) {
            case "ja_jp" -> "演算子: レジストリIDリスト";
            default -> "Operator: Registry ID List";
        });
        add("psitweaks.spellpiece.operator_get_id_list.desc", switch (locale) {
            case "ja_jp" -> "Contextual Value List の各要素のレジストリIDを String List として出力します。";
            default -> "Outputs registry IDs from a Contextual Value List as a String List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_get_id_list", switch (locale) {
            case "ja_jp" -> "Contextual Value List から各要素のレジストリIDを String List として出力します. Entity List, Item List, Block List は入力順を維持します. Vector List 入力はそのワールド座標のブロックを読む Block List として変換されます. 対応していない要素や空要素は空文字列になります.";
            default -> "Outputs registry IDs from a Contextual Value List as a String List. Entity List, Item List, and Block List preserve their input order. Vector List input is converted to Block List by reading the blocks at those world coordinates. Unsupported or empty elements become empty strings.";
        });
        add("psitweaks.spellpiece.operator_tag_list", switch (locale) {
            case "ja_jp" -> "演算子: タグリスト";
            default -> "Operator: Tag List";
        });
        add("psitweaks.spellpiece.operator_tag_list.desc", switch (locale) {
            case "ja_jp" -> "対象の Contextual Value(Entity、Item、Block など)のレジストリタグを String List として出力します。";
            default -> "Outputs the registry tags of the target Contextual Value (Entity, Item, Block, etc.) as a String List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_tag_list", switch (locale) {
            case "ja_jp" -> "Contextual Value からレジストリタグを String List として出力します. 各要素は # を付けない namespace:path 形式です. Entity 入力は EntityType のタグを返し, スコアボードタグは含みません.";
            default -> "Outputs registry tags from a Contextual Value as a String List. Entries use namespace:path form without #. Entity inputs return EntityType tags, not scoreboard tags.";
        });
        add("psitweaks.spellpiece.operator_item_count", switch (locale) {
            case "ja_jp" -> "演算子: アイテム個数";
            default -> "Operator: Item Count";
        });
        add("psitweaks.spellpiece.operator_item_count.desc", switch (locale) {
            case "ja_jp" -> "Item 値のスタック個数を Number として出力します。空の Item は0を出力します。";
            default -> "Outputs the stack count of an Item value.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_item_count", switch (locale) {
            case "ja_jp" -> "Item 値のスタック個数を Number として出力します. 空の Item は0を出力します.";
            default -> "Outputs the stack count of an Item value as a Number. Empty Item values output 0.";
        });
        add("psitweaks.spellpiece.operator_item_total_count", switch (locale) {
            case "ja_jp" -> "演算子: アイテム合計個数";
            default -> "Operator: Item Total Count";
        });
        add("psitweaks.spellpiece.operator_item_total_count.desc", switch (locale) {
            case "ja_jp" -> "Item List 内の指定 ID と一致するアイテムの合計個数を Number として出力します。";
            default -> "Outputs the total count of matching items in an Item List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_item_total_count", switch (locale) {
            case "ja_jp" -> "Item List からスタック個数の合計を Number として出力します. String 入力が空または未接続ならすべての Item を数えます. それ以外の場合は String と registry ID が完全一致する Item だけを数えます. 無効な ID は何にも一致せず0を出力します.";
            default -> "Outputs the total stack count from an Item List. If the String input is empty or not connected, all Items are counted. Otherwise only Items whose registry ID exactly matches the String are counted. Invalid IDs simply match nothing and output 0.";
        });
        add("psitweaks.spellpiece.operator_item_slot", switch (locale) {
            case "ja_jp" -> "演算子: アイテムスロット";
            default -> "Operator: Item Slot";
        });
        add("psitweaks.spellpiece.operator_item_slot.desc", switch (locale) {
            case "ja_jp" -> "Item 値に保存された内部インベントリのスロット番号を Number として出力します。スロット番号を取得できない場合は-1です。";
            default -> "Outputs the internal inventory slot number saved on an Item value. Outputs -1 if no slot number is available.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_item_slot", switch (locale) {
            case "ja_jp" -> "Item 値に保存された0始まりのインベントリスロット番号を出力します. Entity またはブロックのインベントリスロットから取得した Item は取得元のスロット番号を保持します. 空の Item, 手や防具から取得した Item, ドロップアイテムなど, インベントリスロット由来でない Item は -1 を出力します.";
            default -> "Outputs the zero-based inventory slot number saved in an Item value. Items obtained from entity or block inventory slots retain this source slot. Items without an inventory-slot source, including empty Items, hand or equipment Items, and dropped item entities, output -1.";
        });
        add("psitweaks.spellpiece.selector_internal_items", switch (locale) {
            case "ja_jp" -> "取得子: 内部アイテム";
            default -> "Selector: Internal Items";
        });
        add("psitweaks.spellpiece.selector_internal_items.desc", switch (locale) {
            case "ja_jp" -> "対象ブロックの内部インベントリを Item List として取得します。";
            default -> "Outputs the target block's internal inventory as an Item List.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_internal_items", switch (locale) {
            case "ja_jp" -> "対象ブロックの内部インベントリを Item List として出力します. ItemHandler と Container をスロット単位で読み取ります. 読み取り可能なインベントリがない場合は空の Item List を出力します.";
            default -> "Outputs items from the target block's internal inventory as an Item List. Item handlers and containers are read slot by slot. Blocks without readable inventories output an empty Item List.";
        });
        add("psitweaks.spellpiece.selector_internal_slot_item", switch (locale) {
            case "ja_jp" -> "取得子: 内部スロットアイテム";
            default -> "Selector: Internal Slot Item";
        });
        add("psitweaks.spellpiece.selector_internal_slot_item.desc", switch (locale) {
            case "ja_jp" -> "指定ブロックの0始まり内部インベントリスロットにある Item を取得します。スロットを読めない場合は空の Item を出力します。";
            default -> "Gets the item in a block's zero-based internal inventory slot. Outputs an empty Item if the slot cannot be read.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_internal_slot_item", switch (locale) {
            case "ja_jp" -> "対象ブロックの0始まりの内部インベントリスロットにある Item を出力します. ItemHandler を優先して使用し, 利用できない場合は Container を使用します. 読み取り可能なインベントリがないブロック, 空スロット, 負の番号, 範囲外の番号は空の Item を出力します.";
            default -> "Outputs the Item in the target block's zero-based internal inventory slot. Item handlers are used first, with Container as a fallback. Blocks without a readable inventory and empty, negative, or out-of-range slots output an empty Item.";
        });
        add("psitweaks.spellpiece.selector_selected_slot_item", switch (locale) {
            case "ja_jp" -> "取得子: 選択スロットアイテム";
            default -> "Selector: Selected Slot Item";
        });
        add("psitweaks.spellpiece.selector_selected_slot_item.desc", switch (locale) {
            case "ja_jp" -> "この術式で選択しているアイテムを取得します。";
            default -> "Gets the item currently selected by this spell.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_selected_slot_item", switch (locale) {
            case "ja_jp" -> "術式で選択しているスロットにある ItemStack を Item 型として出力します. スロットは Psi の 作動式: ブロック設置 などで扱うものと同じであり, 作動式: 指定スロット切替 の影響を受けます. 空スロットは空の Item を出力します.";
            default -> "Outputs the ItemStack in the slot selected by the spell as an Item value. This is the same slot used by Psi pieces such as Trick: Place Block, and is affected by Trick: Switch Target Slot. Empty slots output an empty Item.";
        });
        add("psitweaks.spellpiece.selector_entity_slot_item", switch (locale) {
            case "ja_jp" -> "取得子: エンティティスロットアイテム";
            default -> "Selector: Entity Slot Item";
        });
        add("psitweaks.spellpiece.selector_entity_slot_item.desc", switch (locale) {
            case "ja_jp" -> "対象 Entity の0始まり内部インベントリスロットにある Item を取得します。スロットを読めない場合は空の Item を出力します。";
            default -> "Gets the item in the target entity's zero-based internal inventory slot. Outputs an empty Item if the slot cannot be read.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_entity_slot_item", switch (locale) {
            case "ja_jp" -> "対象 Entity の0始まりのインベントリスロットにある Item を出力します. Player はプレイヤーインベントリを使用し, その他の Entity は利用可能な ItemHandler を使用します. 読み取れないスロット, 空スロット, 負の番号, 範囲外の番号は空の Item を出力します.";
            default -> "Outputs the Item in the target Entity's zero-based inventory slot. Players use their player inventory; other entities use their item handler when available. Unreadable, empty, negative, or out-of-range slots output an empty Item.";
        });
        add("psitweaks.spellpiece.selector_display_name", switch (locale) {
            case "ja_jp" -> "取得子: 表示名";
            default -> "Selector: Display Name";
        });
        add("psitweaks.spellpiece.selector_display_name.desc", switch (locale) {
            case "ja_jp" -> "Contextual Value の表示名を術者の言語に応じた String として出力します。";
            default -> "Outputs the localized display name of a Contextual Value.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_display_name", switch (locale) {
            case "ja_jp" -> "Contextual Value の表示名を, 術者の現在言語に応じた String として出力します. プレイヤーやカスタム名付き Entity は表示されている名前を返し, 通常の Entity は EntityType の名前, Item は hover name, Block はブロック名を返します. 術者の言語を取得できない場合は English を使います. 翻訳がない場合は English, それもなければ翻訳キーそのものを返します.";
            default -> "Outputs the display name of a Contextual Value as a String in the caster's current language. Players and custom-named entities keep their visible names; other entities use their EntityType name, Items use their hover name, and Blocks use their block name. If the caster language cannot be read, English is used. Missing translations fall back to English, then to the translation key itself.";
        });
        add("psitweaks.spellpiece.selector_display_name_list", switch (locale) {
            case "ja_jp" -> "取得子: 表示名リスト";
            default -> "Selector: Display Name List";
        });
        add("psitweaks.spellpiece.selector_display_name_list.desc", switch (locale) {
            case "ja_jp" -> "Contextual Value List の各要素の表示名を術者の言語に応じた String List として出力します。";
            default -> "Outputs localized display names from a Contextual Value List as a String List.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_display_name_list", switch (locale) {
            case "ja_jp" -> "Contextual Value List から各要素の表示名を String List として出力し, 入力順を維持します. Entity List, Item List, Block List に対応します. Vector List 入力はそのワールド座標のブロックを読む Block List として変換されます. 各要素は 取得子: 表示名 と同じ言語 fallback を使います.";
            default -> "Outputs display names from a Contextual Value List as a String List while preserving input order. Entity List, Item List, and Block List are supported. Vector List input is converted to Block List by reading the blocks at those world coordinates. Each element uses the same language fallback as Selector: Display Name.";
        });
        add("psitweaks.spellpiece.selector_nbt", switch (locale) {
            case "ja_jp" -> "取得子: NBT";
            default -> "Selector: NBT";
        });
        add("psitweaks.spellpiece.selector_nbt.desc", switch (locale) {
            case "ja_jp" -> "対象 Contextual Value のNBTトップレベルを key:value 形式の String List として出力します。";
            default -> "Outputs the target Contextual Value's top-level NBT as key:value strings.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_nbt", switch (locale) {
            case "ja_jp" -> "対象 Contextual Value のNBTトップレベルを key:value 形式の String List として出力します. Entity 入力ではエンティティIDキーも他のトップレベルNBTキーと同様に含まれます. Block 入力は, Vector 入力から変換された Block も含めて BlockEntity のNBTを読み取り, BlockEntity がないブロックは空リストになります. 値はSNBT文字列です.";
            default -> "Outputs the target Contextual Value's top-level NBT as key:value strings in a String List. Entity inputs include their entity id key like other top-level NBT keys. Block inputs, including Vector inputs converted to Block values, read BlockEntity NBT; blocks without a BlockEntity output an empty list. Values use SNBT text.";
        });
        add("psitweaks.spellpiece.selector_nbt_keys", switch (locale) {
            case "ja_jp" -> "取得子: NBTキー";
            default -> "Selector: NBT Keys";
        });
        add("psitweaks.spellpiece.selector_nbt_keys.desc", switch (locale) {
            case "ja_jp" -> "対象 Contextual Value のNBTトップレベルキーを String List として出力します。";
            default -> "Outputs the target Contextual Value's top-level NBT keys as a String List.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_nbt_keys", switch (locale) {
            case "ja_jp" -> "対象 Contextual Value のNBTトップレベルキーを String List として出力します. Entity 入力ではエンティティIDキーも含まれます. Block 入力は, Vector 入力から変換された Block も含めて BlockEntity のNBTを読み取り, BlockEntity がないブロックは空リストになります.";
            default -> "Outputs the target Contextual Value's top-level NBT keys as a String List. Entity inputs include the entity id key. Block inputs, including Vector inputs converted to Block values, read BlockEntity NBT; blocks without a BlockEntity output an empty list.";
        });
        add("psitweaks.spellpiece.selector_nbt_value", switch (locale) {
            case "ja_jp" -> "取得子: NBT値";
            default -> "Selector: NBT Value";
        });
        add("psitweaks.spellpiece.selector_nbt_value.desc", switch (locale) {
            case "ja_jp" -> "対象 Contextual Value のNBTから String キーまたはNBTパスに一致する値を出力します。一致しない場合は空文字列です。";
            default -> "Outputs the target Contextual Value's NBT value matching the String key or NBT path. Missing paths output an empty string.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_nbt_value", switch (locale) {
            case "ja_jp" -> "対象 Contextual Value のNBTから StringキーまたはNBTパスに一致する値をSNBT文字列として出力します. Entity 入力のエンティティIDキーを含めて, トップレベルキーの完全一致を先に確認します. 深い値には Inventory[0].id や components.\"minecraft:custom_name\" のようなパスを指定できます. Block 入力は, Vector 入力から変換された Block も含めて BlockEntity のNBTを読み取ります. パスが存在しない場合, 不正なパス, BlockEntity がないブロックは空文字列になります.";
            default -> "Outputs the target Contextual Value's NBT value matching the String key or NBT path as SNBT text. Top-level exact key matches are checked first, including the entity id key on Entity inputs. Use paths such as Inventory[0].id or components.\"minecraft:custom_name\" for nested values. Block inputs, including Vector inputs converted to Block values, read BlockEntity NBT. Missing paths, invalid paths, and blocks without a BlockEntity output an empty String.";
        });
        add("psitweaks.spellpiece.selector_online_players", switch (locale) {
            case "ja_jp" -> "取得子: オンラインプレイヤー";
            default -> "Selector: Online Players";
        });
        add("psitweaks.spellpiece.selector_online_players.desc", switch (locale) {
            case "ja_jp" -> "ワールド内のオンラインプレイヤー名を String List として取得します。";
            default -> "Outputs the names of online players in the world as a String List.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_online_players", switch (locale) {
            case "ja_jp" -> "術者の現在ワールドにいるオンラインプレイヤー全員の名前を String List として出力します.";
            default -> "Outputs the names of all online players in the caster's current world as a String List.";
        });
        add("psitweaks.spellwarning.cad_memory_string_truncated", switch (locale) {
            case "ja_jp" -> "CADメモリスロット%sのStringは%s文字から%s文字に切り捨てられました";
            default -> "String CAD memory value in slot %s was truncated from %s to %s characters";
        });
        add("psitweaks.spellpiece.constant_string", switch (locale) {
            case "ja_jp" -> "定数子: 文字列";
            default -> "Constant: String";
        });
        add("psitweaks.spellpiece.constant_string.desc", switch (locale) {
            case "ja_jp" -> "入力した文字列をそのまま出力します。最大1024文字です。";
            default -> "Outputs the entered string. Limited to 1024 characters.";
        });
        add("psi.book.page.psitweaks_spellpiece.constant_string", switch (locale) {
            case "ja_jp" -> "入力した文字列をString値として出力します. 値は術式に保存され, 最大1024文字です.";
            default -> "Outputs the entered string as a String value. The value is saved with the spell and is limited to 1024 characters.";
        });
        add("psitweaks.spellpiece.operator_format_string", switch (locale) {
            case "ja_jp" -> "演算子: フォーマット文字列";
            default -> "Operator: Format String";
        });
        add("psitweaks.spellpiece.operator_format_string.desc", switch (locale) {
            case "ja_jp" -> "テキストウィンドウで入力された文字列を返します。 任意の入力された値はテキスト中の{1}, {2}, {3}へ埋め込まれます。";
            default -> "Returns the string entered in the text window. Optional input values are embedded into {1}, {2}, and {3} in the text.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_format_string", switch (locale) {
            case "ja_jp" -> "保存したテンプレート中の {1}, {2}, {3} を最大3つの任意 Any 入力で置換して String を出力します. テンプレートは 定数子: 文字列 と同じ入力ウィンドウで編集し, 最大1024文字です. 未接続の入力は空文字になります. Entity, Item, Block などの Contextual Value は 取得子: 表示名 と同じ現在言語表示名を使います. Contextual Value List は 取得子: 表示名リスト と同じ現在言語表示名へ変換し, コンマ区切りで埋め込みます. それ以外の値は通常の String 変換を使います. 最終出力は実行時 String 上限で制限されます.";
            default -> "Outputs a String by replacing {1}, {2}, and {3} in the saved template with up to three optional Any inputs. The template uses the same input window as Constant: String and is limited to 1024 characters. Unconnected inputs become empty text. Contextual Values such as Entity, Item, and Block use the same localized display name as Selector: Display Name. Contextual Value Lists use the same localized names as Selector: Display Name List and are joined with commas. Other values use the normal String conversion. The final output is capped by the runtime String limit.";
        });
        add("psitweaks.spellpiece.operator_from_string", switch (locale) {
            case "ja_jp" -> "演算子: 文字列から変換";
            default -> "Operator: From String";
        });
        add("psitweaks.spellpiece.operator_from_string.desc", switch (locale) {
            case "ja_jp" -> "選択中のモードに応じて String をそのまま返すか、Number または Vector に変換します。";
            default -> "Returns the input String as-is, or converts it into a Number or Vector for the selected mode.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_from_string", switch (locale) {
            case "ja_jp" -> "選択中の Plain モードに応じて String 入力を変換します. String モードは入力をそのまま返します. Number モードは有限の数値文字列を解析し, 無効な入力では0を出力します. Vector モードは Vector[x,y,z], Vector(x,y,z), [x,y,z], (x,y,z) を受け付けます. Vector は大文字小文字を区別せず, 解析失敗時はゼロベクトルを出力します. スペルピースを左クリックでモードを選択できます.";
            default -> "Converts the String input for the selected Plain mode. String mode returns the input as-is. Number mode parses finite numeric text and outputs 0 on invalid input. Vector mode accepts Vector[x,y,z], Vector(x,y,z), [x,y,z], or (x,y,z), case-insensitive for Vector, and outputs the zero vector when parsing fails. Left-click the spell piece to select the mode.";
        });
        add("psitweaks.spellpiece.operator_list_from_string_list", switch (locale) {
            case "ja_jp" -> "演算子: 文字列リストから変換";
            default -> "Operator: From String List";
        });
        add("psitweaks.spellpiece.operator_list_from_string_list.desc", switch (locale) {
            case "ja_jp" -> "選択中のモードに応じて String List をそのまま返すか、Number List または Vector List に変換します。";
            default -> "Returns the input String List as-is, or converts it into a Number List or Vector List for the selected mode.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_list_from_string_list", switch (locale) {
            case "ja_jp" -> "選択中の Plain モードに応じて String List を変換します. String モードは入力リストをそのまま返します. Number モードは各要素を有限の数値文字列として解析し, 無効な要素は0になります. Vector モードは Vector[x,y,z], Vector(x,y,z), [x,y,z], (x,y,z) に一致する要素のみ変換し, 無効な要素は無視します. Vector は大文字小文字を区別しません. スペルピースを左クリックでモードを選択できます.";
            default -> "Converts a String List for the selected Plain mode. String mode returns the input list as-is. Number mode parses each entry as finite numeric text, so invalid entries become 0. Vector mode converts only entries matching Vector[x,y,z], Vector(x,y,z), [x,y,z], or (x,y,z), case-insensitive for Vector, and skips invalid entries. Left-click the spell piece to select the mode.";
        });
        add("psitweaks.spellpiece.operator_number_list_to_vector", switch (locale) {
            case "ja_jp" -> "演算子: 数値リスト→ベクトル";
            default -> "Operator: Number List to Vector";
        });
        add("psitweaks.spellpiece.operator_number_list_to_vector.desc", switch (locale) {
            case "ja_jp" -> "要素数3の Number List を Vector に変換します。";
            default -> "Converts a Number List with exactly three elements into a Vector.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_number_list_to_vector", switch (locale) {
            case "ja_jp" -> "要素数3の Number List を Vector に変換します. リストの順序は X, Y, Z です. 要素数が3ではない場合は実行時エラーになります.";
            default -> "Converts a Number List with exactly three elements into a Vector. The list order is X, Y, Z. If the list does not contain exactly three elements, the spell raises a runtime error.";
        });
        add("psitweaks.spellpiece.operator_vector_to_number_list", switch (locale) {
            case "ja_jp" -> "演算子: ベクトル→数値リスト";
            default -> "Operator: Vector to Number List";
        });
        add("psitweaks.spellpiece.operator_vector_to_number_list.desc", switch (locale) {
            case "ja_jp" -> "Vector を要素数3の Number List に変換します。";
            default -> "Converts a Vector into a three-element Number List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_vector_to_number_list", switch (locale) {
            case "ja_jp" -> "Vector を X, Y, Z の順で3要素の Number List に変換します.";
            default -> "Converts a Vector into a Number List with three elements in X, Y, Z order.";
        });
        add("psitweaks.spellpiece.operator_to_string", switch (locale) {
            case "ja_jp" -> "演算子: 文字列へ変換";
            default -> "Operator: To String";
        });
        add("psitweaks.spellpiece.operator_to_string.desc", switch (locale) {
            case "ja_jp" -> "Any 入力をデバッグ表示相当の String に変換します。";
            default -> "Converts an Any input into a debug-display String.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_to_string", switch (locale) {
            case "ja_jp" -> "Any 入力を 作動式: デバッグ と同じ表示向けの文字列表現で String に変換します. Entity, Item, Block などの Contextual Value もレジストリIDではなくデバッグ表示相当の文字列になります. String はそのまま返します. List は各要素を同じ規則で変換し, コンマ区切りで結合します.";
            default -> "Converts an Any input into a String using the same display-oriented text form as Trick: Debug. Contextual values such as Entity, Item, and Block therefore use their debug display text rather than registry IDs. String values are returned as-is. List values convert each element by the same rules and join them with commas.";
        });
        add("psitweaks.spellpiece.operator_list_to_string_list", switch (locale) {
            case "ja_jp" -> "演算子: 文字列リストへ変換";
            default -> "Operator: To String List";
        });
        add("psitweaks.spellpiece.operator_list_to_string_list.desc", switch (locale) {
            case "ja_jp" -> "Any 入力をデバッグ表示相当の String List に変換します。";
            default -> "Converts an Any input into a debug-display String List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_list_to_string_list", switch (locale) {
            case "ja_jp" -> "Any 入力を 作動式: デバッグ と同じ表示向けの文字列表現で String List に変換します. Entity, Item, Block などの Contextual Value もレジストリIDではなくデバッグ表示相当の文字列になります. Block List を含む List は入力順を維持して各要素を同じ規則で変換します. List 以外は1要素の String List になります.";
            default -> "Converts an Any input into a String List using the same display-oriented text form as Trick: Debug. Contextual values such as Entity, Item, and Block therefore use their debug display text rather than registry IDs. List inputs, including Block List, convert each element by the same rules while preserving order. Non-list inputs become a one-element String List.";
        });
        add("psitweaks.spellpiece.operator_string_partial_match", switch (locale) {
            case "ja_jp" -> "演算子: 部分一致";
            default -> "Operator: Partial Match";
        });
        add("psitweaks.spellpiece.operator_string_partial_match.desc", switch (locale) {
            case "ja_jp" -> "文字列1が文字列2での検索にマッチする文字列を含むなら1、そうでなければ0を出力します。文字列2ではワイルドカードを使えます: * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します。";
            default -> "Outputs 1 if searching String 1 with String 2 finds matching text, otherwise outputs 0. String 2 accepts wildcards: * matches any text, ? matches one character, and [abc] matches one listed character.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_string_partial_match", switch (locale) {
            case "ja_jp" -> "文字列1を文字列2で検索してマッチする文字列を含むか判定します. * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. ワイルドカード文字はバックスラッシュでエスケープできます. 比較は大文字小文字を区別します.";
            default -> "Searches String 1 with String 2 and checks whether it contains matching text. * matches any text, ? matches one character, and [abc] matches one of the listed characters. Escape wildcard characters with a backslash. Matching is case-sensitive.";
        });
        add("psitweaks.spellpiece.operator_string_starts_with", switch (locale) {
            case "ja_jp" -> "演算子: 先頭一致";
            default -> "Operator: String Starts With";
        });
        add("psitweaks.spellpiece.operator_string_starts_with.desc", switch (locale) {
            case "ja_jp" -> "文字列1が文字列2で始まるなら1、そうでなければ0を出力します。";
            default -> "Outputs 1 if String 1 starts with String 2, otherwise outputs 0.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_string_starts_with", switch (locale) {
            case "ja_jp" -> "文字列1が文字列2で始まるなら1, そうでなければ0を出力します. 比較は大文字小文字を区別します.";
            default -> "Outputs 1 if String 1 starts with String 2, otherwise outputs 0. The comparison is case-sensitive.";
        });
        add("psitweaks.spellpiece.operator_string_ends_with", switch (locale) {
            case "ja_jp" -> "演算子: 末尾一致";
            default -> "Operator: String Ends With";
        });
        add("psitweaks.spellpiece.operator_string_ends_with.desc", switch (locale) {
            case "ja_jp" -> "文字列1が文字列2で終わるなら1、そうでなければ0を出力します。";
            default -> "Outputs 1 if String 1 ends with String 2, otherwise outputs 0.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_string_ends_with", switch (locale) {
            case "ja_jp" -> "文字列1が文字列2で終わるなら1, そうでなければ0を出力します. 比較は大文字小文字を区別します.";
            default -> "Outputs 1 if String 1 ends with String 2, otherwise outputs 0. The comparison is case-sensitive.";
        });
        add("psitweaks.spellpiece.operator_string_concat", switch (locale) {
            case "ja_jp" -> "演算子: 文字列結合";
            default -> "Operator: String Concat";
        });
        add("psitweaks.spellpiece.operator_string_concat.desc", switch (locale) {
            case "ja_jp" -> "文字列1 + 文字列2 + 文字列3";
            default -> "String 1 + String 2 + String 3";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_string_concat", switch (locale) {
            case "ja_jp" -> "文字列1, 文字列2, 任意の文字列3をこの順に結合します. ";
            default -> "Concatenates String 1, String 2, and optional String 3 in that order.";
        });
        add("psitweaks.spellpiece.operator_string_split", switch (locale) {
            case "ja_jp" -> "演算子: 文字列分割";
            default -> "Operator: String Split";
        });
        add("psitweaks.spellpiece.operator_string_split.desc", switch (locale) {
            case "ja_jp" -> "文字列1を文字列2のリテラル区切り文字で分割し、String List を出力します。";
            default -> "Splits String 1 with String 2 as a literal delimiter and outputs a String List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_string_split", switch (locale) {
            case "ja_jp" -> "文字列1を文字列2の区切り文字で分割し, String List を出力します. 文字列2が未接続の場合はカンマを使います. 空要素は保持されます.";
            default -> "Splits String 1 with String 2 as the delimiter and outputs a String List. If String 2 is not connected, a comma is used. Empty fields are preserved.";
        });
        add("psitweaks.spellpiece.operator_string_slice", switch (locale) {
            case "ja_jp" -> "演算子: 文字列スライス";
            default -> "Operator: String Slice";
        });
        add("psitweaks.spellpiece.operator_string_slice.desc", switch (locale) {
            case "ja_jp" -> "入力した文字列の文字を 数値A:数値B でスライスした新しい 文字列 を返します。";
            default -> "Slices the input String from Number A to Number B and returns a new String.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_string_slice", switch (locale) {
            case "ja_jp" -> "入力文字列の文字数を指定して範囲を切り出して新しい文字列として返します. AまたはBが未接続なら先頭または末尾を使います. 負数は末尾から数え, 範囲外は有効範囲に収め, 終了位置が開始位置以下なら空文字列を返します. 小数部分は0方向へ切り捨てます.";
            default -> "Returns a selected range of characters from the input String as a new String. If A or B is not connected, the beginning or end is used. Negative numbers count from the end, out-of-range values are clamped to the valid range, and an end position at or before the start returns an empty String. Decimal values are truncated toward zero.";
        });
        add("psitweaks.spellpiece.operator_string_length", switch (locale) {
            case "ja_jp" -> "演算子: 文字列長";
            default -> "Operator: String Length";
        });
        add("psitweaks.spellpiece.operator_string_length.desc", switch (locale) {
            case "ja_jp" -> "入力した文字列の文字数を Number として返します。";
            default -> "Returns the number of characters in the input String as a Number.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_string_length", switch (locale) {
            case "ja_jp" -> "入力した String の文字数を Number として返します. ";
            default -> "Returns the number of characters in the input String.";
        });
        add("psitweaks.spellpiece.operator_string_replace", switch (locale) {
            case "ja_jp" -> "演算子: 文字列置換";
            default -> "Operator: String Replace";
        });
        add("psitweaks.spellpiece.operator_string_replace.desc", switch (locale) {
            case "ja_jp" -> "入力した文字列1のうち、文字列2と一致する部分を、指定した文字列3へ置換する。";
            default -> "Replaces parts of String 1 matching String 2 with String 3.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_string_replace", switch (locale) {
            case "ja_jp" -> "文字列1のうち, 文字列2と大文字小文字を区別してリテラル一致するすべての部分を文字列3へ置換します. 文字列2は完全一致のみ判定します. 文字列2が空の場合は文字列1をそのまま返します. 結果は実行時のString文字数上限に従います.";
            default -> "Replaces every literal, case-sensitive occurrence of String 2 in String 1 with String 3. String 2 is evaluated only as an exact match. If String 2 is empty, String 1 is returned unchanged. The result is limited by the runtime String length limit.";
        });
        add("psitweaks.spellpiece.operator_string_trim", switch (locale) {
            case "ja_jp" -> "演算子: 文字列トリム";
            default -> "Operator: String Trim";
        });
        add("psitweaks.spellpiece.operator_string_trim.desc", switch (locale) {
            case "ja_jp" -> "入力した文字列の前後の空白を取り除きます。";
            default -> "Removes leading and trailing whitespace from the input String.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_string_trim", switch (locale) {
            case "ja_jp" -> "入力した文字列の先頭と末尾にある空白（スペース, タブ, 改行など）を削除します. 文字列の途中にある空白は残ります.";
            default -> "Removes whitespace characters (such as spaces, tabs, and line breaks) from the beginning and end of the input String. Whitespace inside the String is preserved.";
        });
        add("psitweaks.spellpiece.operator_string_list_join", switch (locale) {
            case "ja_jp" -> "演算子: 文字列リスト結合";
            default -> "Operator: String List Join";
        });
        add("psitweaks.spellpiece.operator_string_list_join.desc", switch (locale) {
            case "ja_jp" -> "入力リストを文字列2で結合し、String を出力します。";
            default -> "Joins the input String List with String 2 and outputs a String.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_string_list_join", switch (locale) {
            case "ja_jp" -> "入力リストを文字列2で結合し, String を出力します. 文字列2が未接続の場合はカンマを使います. 空の区切り文字は許可され, 要素を直接連結します.";
            default -> "Joins the input String List with String 2 and outputs a String. If String 2 is not connected, a comma is used. Empty delimiters are allowed and concatenate the elements directly.";
        });
        add("psitweaks.spellpiece.operator_player_name", switch (locale) {
            case "ja_jp" -> "演算子: プレイヤーネーム";
            default -> "Operator: Player Name";
        });
        add("psitweaks.spellpiece.operator_player_name.desc", switch (locale) {
            case "ja_jp" -> "Entityがプレイヤーならプレイヤー名を、そうでなければ空文字列を出力します。";
            default -> "Outputs the player name if the Entity is a player, otherwise outputs an empty string.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_player_name", switch (locale) {
            case "ja_jp" -> "入力 Entity がプレイヤーならプレイヤー名を出力します. プレイヤーでない Entity は空文字列になります.";
            default -> "Outputs the player's name when the input Entity is a player. Non-player entities output an empty String.";
        });
        add("psitweaks.spellpiece.operator_list_search", switch (locale) {
            case "ja_jp" -> "演算子: リスト検索";
            default -> "Operator: List Search";
        });
        add("psitweaks.spellpiece.operator_list_search.desc", switch (locale) {
            case "ja_jp" -> "リストの要素を文字列化したもののうち、入力した文字列で検索してマッチするもののみを残します。";
            default -> "Keeps only the input List elements whose string form matches the input search string.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_list_search", switch (locale) {
            case "ja_jp" -> "選択中の List モードを, 各要素の検索用文字列で絞り込みます. Entity と Item/Block などの Contextual Value はレジストリIDで比較します. それ以外の値は 演算子: 文字列へ変換 と同じ文字列で比較します. String入力ではワイルドカードを利用でき, * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. ワイルドカードなしでは完全一致です.";
            default -> "Filters the selected List mode by comparing the String input with each element's search text. Entity and contextual values such as Item and Block compare by registry ID. Other values use the same text form as Operator: To String. The String input accepts wildcards: * matches any text, ? matches one character, and [abc] matches one of the listed characters. Without wildcards, matching is exact.";
        });
        add("psitweaks.spellpiece.operator_list_search_exclude", switch (locale) {
            case "ja_jp" -> "演算子: リスト検索除外";
            default -> "Operator: List Search Exclude";
        });
        add("psitweaks.spellpiece.operator_list_search_exclude.desc", switch (locale) {
            case "ja_jp" -> "リストの要素を文字列化したもののうち、入力した文字列で検索してマッチするものを除外します。";
            default -> "Removes the input List elements whose string form matches the input search string.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_list_search_exclude", switch (locale) {
            case "ja_jp" -> "選択中の List モードから, 検索用文字列が入力ワイルドカードに一致する要素を除外します. Entity と Item/Block などの Contextual Value はレジストリIDで比較します. それ以外の値は 演算子: 文字列へ変換 と同じ文字列で比較します. * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. ワイルドカードなしでは完全一致です.";
            default -> "Filters the selected List mode by removing elements whose search text matches the input wildcard. Entity and contextual values such as Item and Block compare by registry ID. Other values use the same text form as Operator: To String. * matches any text, ? matches one character, and [abc] matches one of the listed characters. Without wildcards, matching is exact.";
        });
        add("psitweaks.spellpiece.operator_random_element", switch (locale) {
            case "ja_jp" -> "演算子: ランダム要素";
            default -> "Operator: Random Element";
        });
        add("psitweaks.spellpiece.operator_random_element.desc", switch (locale) {
            case "ja_jp" -> "入力した List からランダムに1要素を出力します。";
            default -> "Outputs one random element from the input List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_random_element", switch (locale) {
            case "ja_jp" -> "入力された List から要素を1つランダムに返します.";
            default -> "Returns one random element from the input List.";
        });
        add("psitweaks.spellpiece.operator_list_add", switch (locale) {
            case "ja_jp" -> "リストへ追加 (PT)";
            default -> "Operator: Add To List (PT)";
        });
        add("psitweaks.spellpiece.operator_list_add.desc", switch (locale) {
            case "ja_jp" -> "入力した List に最大3つの要素を追加します。";
            default -> "Adds up to three elements to the input List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_list_add", switch (locale) {
            case "ja_jp" -> "入力された List に最大3つの入力要素を追加します.";
            default -> "Adds up to three input elements to the input List.";
        });
        add("psitweaks.spellpiece.operator_list_remove", switch (locale) {
            case "ja_jp" -> "リストから削除 (PT)";
            default -> "Operator: Remove From List (PT)";
        });
        add("psitweaks.spellpiece.operator_list_remove.desc", switch (locale) {
            case "ja_jp" -> "入力した List から最大2つの要素を削除します。";
            default -> "Removes up to two elements from the input List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_list_remove", switch (locale) {
            case "ja_jp" -> "入力された List から最大2つの入力要素を削除します.";
            default -> "Removes up to two input elements from the input List.";
        });
        add("psitweaks.spellpiece.operator_list_remove_indices", switch (locale) {
            case "ja_jp" -> "演算子: インデックス要素削除";
            default -> "Operator: Remove Indexed Elements";
        });
        add("psitweaks.spellpiece.operator_list_remove_indices.desc", switch (locale) {
            case "ja_jp" -> "入力した List から数値Aと数値Bに対応するインデックスの要素を削除します。";
            default -> "Removes the elements at Number A and Number B from the input List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_list_remove_indices", switch (locale) {
            case "ja_jp" -> "選択中の List モードから数値Aと数値Bに対応するインデックスの要素を削除します. 負数は末尾から数え, -1 は末尾要素を削除します. どちらのインデックスも元の List を基準に解決し, 同じインデックスの場合は1要素だけ削除します. 範囲外はインデックス範囲外エラーになります.";
            default -> "Removes the elements at Number A and Number B from the selected List mode. Negative indexes count from the end, so -1 removes the last element. Both indexes are resolved against the original List, and duplicate indexes remove only one element. An out-of-range index raises an out-of-bounds spell error.";
        });
        add("psitweaks.spellpiece.operator_list_insert", switch (locale) {
            case "ja_jp" -> "演算子: リストに挿入";
            default -> "Operator: List Insert";
        });
        add("psitweaks.spellpiece.operator_list_insert.desc", switch (locale) {
            case "ja_jp" -> "入力した List の指定インデックスへ要素を挿入します。";
            default -> "Inserts the input element at the specified index in the input List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_list_insert", switch (locale) {
            case "ja_jp" -> "選択中の List モードの指定インデックス直前へ入力要素を挿入します. 0 は先頭へ, -1 は末尾要素の直前へ挿入します. List サイズより大きいインデックスは末尾へ, 負方向の範囲を超えたインデックスは先頭へ挿入します.";
            default -> "Inserts the input element before the specified index in the selected List mode. Index 0 inserts at the beginning and -1 inserts before the last element. Indexes above the List size insert at the end, while indexes below the negative range insert at the beginning.";
        });
        add("psitweaks.spellpiece.operator_list_slice", switch (locale) {
            case "ja_jp" -> "演算子: リストスライス";
            default -> "Operator: List Slice";
        });
        add("psitweaks.spellpiece.operator_list_slice.desc", switch (locale) {
            case "ja_jp" -> "入力した List の要素を 数値A:数値B でスライスした新しい List を返します。";
            default -> "Slices the input List from Number A to Number B and returns a new List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_list_slice", switch (locale) {
            case "ja_jp" -> "選択中の List モード に対しインデックスを指定して範囲を切り出して新しい List として返します. A を省略すると先頭から, B を省略すると末尾までが対象です. 負数は末尾から数え, 範囲外は有効範囲へ丸め, 終了位置が開始位置以前なら空の List を返します. 小数は0方向へ切り捨てます.";
            default -> "Returns a selected range of elements from the selected List mode as a new List. If A is omitted, the range starts at the beginning; if B is omitted, it continues to the end. Negative numbers count from the end, out-of-range values are clamped to the valid range, and an end position at or before the start returns an empty List. Decimal values are truncated toward zero.";
        });
        add("psitweaks.spellpiece.operator_list_size", switch (locale) {
            case "ja_jp" -> "演算子: リストサイズ (PT)";
            default -> "Operator: List Size (PT)";
        });
        add("psitweaks.spellpiece.operator_list_size.desc", switch (locale) {
            case "ja_jp" -> "入力した任意の List の要素数を出力します。";
            default -> "Outputs the number of elements in the input List.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_list_size", switch (locale) {
            case "ja_jp" -> "入力された任意の List 入力に含まれる要素数を出力します.";
            default -> "Outputs the number of elements in any input List.";
        });
        add("psitweaks.spellpiece.operator_list_exclusion", switch (locale) {
            case "ja_jp" -> "演算子: リスト除外 (PT)";
            default -> "Operator: List Exclusion (PT)";
        });
        add("psitweaks.spellpiece.operator_list_exclusion.desc", switch (locale) {
            case "ja_jp" -> "入力したリスト1からリスト2に含まれる要素を除外します。";
            default -> "Removes the elements in List 2 from the input List 1.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_list_exclusion", switch (locale) {
            case "ja_jp" -> "入力したList 1 の要素のうち、List 2 にも含まれる要素を除外して新しいリストとして返します.";
            default -> "Returns a new List by removing elements from List 1 when they are also present in List 2.";
        });
        add("psitweaks.spellpiece.operator_list_intersection", switch (locale) {
            case "ja_jp" -> "演算子: リスト共通部分 (PT)";
            default -> "Operator: List Intersection (PT)";
        });
        add("psitweaks.spellpiece.operator_list_intersection.desc", switch (locale) {
            case "ja_jp" -> "入力した2つの List に共通する要素を出力します。";
            default -> "Outputs the elements shared by the two input Lists.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_list_intersection", switch (locale) {
            case "ja_jp" -> "入力した2つのリストでどちらにも含まれる要素を新しいリストとして返します.";
            default -> "Returns a new List containing elements that are present in both input Lists.";
        });
        add("psitweaks.spellpiece.operator_list_concatenation", switch (locale) {
            case "ja_jp" -> "演算子: リスト結合 (PT)";
            default -> "Operator: List Concatenation (PT)";
        });
        add("psitweaks.spellpiece.operator_list_concatenation.desc", switch (locale) {
            case "ja_jp" -> "入力した2つの List を結合します。";
            default -> "Combines the two input Lists.";
        });
        add("psi.book.page.psitweaks_spellpiece.operator_list_concatenation", switch (locale) {
            case "ja_jp" -> "入力された2つのリストを結合します. Contextual Value List の場合、重複した要素は削除されます.";
            default -> "Combines the two input Lists. For Contextual Value Lists, duplicate elements are removed.";
        });
        add("psitweaks.spellpiece.selector_block", switch (locale) {
            case "ja_jp" -> "取得子: ブロック";
            default -> "Selector: Block";
        });
        add("psitweaks.spellpiece.selector_block.desc", switch (locale) {
            case "ja_jp" -> "指定座標にあるブロックを Block 型として返します。";
            default -> "Returns the block at the given position as a Block value.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_block", switch (locale) {
            case "ja_jp" -> "対象座標にあるブロックを Block 型として出力します. Block は保存座標, ディメンション, ブロック状態, レジストリID, ブロックタグを持つ読み取り用スナップショットです. Block 型を必要とするスペルピースにも Vector型 が自動変換されそのまま接続できますが, 作動式: デバッグ に Block としての情報を表示させたい場合など Block型 であることを明示的に示したい場合に用います.";
            default -> "Outputs the block at the target position as a Block value. Block is a read-only snapshot containing the saved position, dimension, block state, registry ID, and block tags. Spell pieces that require a Block can also accept a Vector through automatic conversion, so use this when you want to explicitly keep the value as a Block, such as when showing Block information with Trick: Debug.";
        });
        add("psitweaks.spellpiece.selector_block_list", switch (locale) {
            case "ja_jp" -> "取得子: ブロックリスト";
            default -> "Selector: Block List";
        });
        add("psitweaks.spellpiece.selector_block_list.desc", switch (locale) {
            case "ja_jp" -> "Vector List の座標にあるブロックを Block List として取得します。";
            default -> "Gets the blocks at the positions in a Vector List as a Block List.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_block_list", switch (locale) {
            case "ja_jp" -> "Vector List の座標にあるブロックを Block List として取得します. ブロックの種類でリストを検索したいなど, 座標をブロックとして扱いたい場合に使います.";
            default -> "Gets the blocks at the positions in a Vector List as a Block List. Use this when you want to handle positions as blocks, such as when searching a list by block type.";
        });
        add("psitweaks.spellpiece.selector_held_item", switch (locale) {
            case "ja_jp" -> "取得子: 手持ちアイテム";
            default -> "Selector: Main-Hand Item";
        });
        add("psitweaks.spellpiece.selector_held_item.desc", switch (locale) {
            case "ja_jp" -> "対象エンティティのメインハンドのアイテムを取得します。";
            default -> "Gets the target entity's main-hand item.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_held_item", switch (locale) {
            case "ja_jp" -> "対象 LivingEntity のメインハンドの ItemStack を Item 型として出力します. Item型は, アイテムの種類, 個数, 耐久値, 名前などを持つ読み取り用の値です. Psi の 取得子: 近くのアイテム で取得できるドロップアイテムはエンティティであり, PsiTweaksで追加されるItem型とは異なることに注意してください.";
            default -> "Outputs the target living entity's main-hand ItemStack as an Item value. Item is a read-only snapshot containing item data such as kind, count, durability, and name. Note that Psi's Selector: Nearby Items returns dropped item entities, which are different from the Item type added by PsiTweaks.";
        });
        add("psitweaks.spellpiece.selector_held_items", switch (locale) {
            case "ja_jp" -> "取得子: 所持アイテム";
            default -> "Selector: Carried Items";
        });
        add("psitweaks.spellpiece.selector_held_items.desc", switch (locale) {
            case "ja_jp" -> "対象 Entity の所持アイテムを Item List として取得します。";
            default -> "Outputs the target entity's carried items as an Item List.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_held_items", switch (locale) {
            case "ja_jp" -> "対象 Entity の所持アイテムを Item List として出力します. Player はインベントリをスロット単位で読み取り, その他の Entity は ItemHandler があればそれを使い, なければ手持ちと防具スロットを読み取ります. 空の場合は空の Item List を出力します.";
            default -> "Outputs carried items from the target Entity as an Item List. Player inventories are read slot by slot; other entities use their item handler when available, or their hands and armor slots as a fallback. Empty inventories output an empty Item List.";
        });
        add("psitweaks.spellpiece.selector_indexed_element", switch (locale) {
            case "ja_jp" -> "取得子: インデックス要素 (PT)";
            default -> "Selector: Indexed Element (PT)";
        });
        add("psitweaks.spellpiece.selector_indexed_element.desc", switch (locale) {
            case "ja_jp" -> "選択中の List モードから0始まりのインデックスにある要素を取得します。負のインデックスは末尾から数えます。";
            default -> "Outputs the element at a zero-based index from the selected List mode. Negative indexes count back from the end.";
        });
        add("psi.book.page.psitweaks_spellpiece.selector_indexed_element", switch (locale) {
            case "ja_jp" -> "入力された List から, 0始まりのインデックスにある要素を出力します. 負のインデックスは末尾から数え, -1 は末尾要素, -2 はその1つ前の要素を返します. 範囲外のインデックスは Psi 本体の Indexed Element と同じ範囲外エラーになります.";
            default -> "Outputs the element at a zero-based index from the input List. Negative indexes count back from the end: -1 returns the last element, and -2 returns the element before it. Out-of-range indexes produce the same out-of-bounds spell error as Psi's Indexed Element.";
        });
    }

    private void addEnglishTranslations() {
        add("item.psitweaks.moval_suit_helmet", "M.O.V.A.L.Suit Helmet");
        add("item.psitweaks.moval_suit_chestplate", "M.O.V.A.L.Suit ChestPlate");
        add("item.psitweaks.moval_suit_leggings", "M.O.V.A.L.Suit Leggings");
        add("item.psitweaks.moval_suit_boots", "M.O.V.A.L.Suit Boots");
        add("item.psitweaks.moval_suit_leggings_ivory", "M.O.V.A.L.Suit Leggings");
        add("item.psitweaks.flash_ring", "Flash Ring");
        add("item.psitweaks.flash_charm", "Flash Charm");
        add("item.psitweaks.psimetal_bow", "Psimetal Bow");
        add("item.psitweaks.curios_controller", "Curios Controller");
        add("item.psitweaks.enriched_psigem", "Enriched Psigem");
        add("item.psitweaks.enriched_ebony", "Enriched Ebony");
        add("item.psitweaks.enriched_ivory", "Enriched Ivory");
        add("item.psitweaks.enriched_echo", "Enriched Echo");
        add("item.psitweaks.enriched_hypostasis", "Enriched Hypostasis Gem");
        add("item.psitweaks.psionic_echo", "Psionic Echo");
        add("item.psitweaks.program_blank", "Blank Program");
        add("item.psitweaks.program_cocytus", "Program: Cocytus");
        add("item.psitweaks.program_time_accelerate", "Program: Time Accelerate");
        add("item.psitweaks.program_flight", "Program: Flight");
        add("item.psitweaks.program_phonon_maser", "Program: Phonon Maser");
        add("item.psitweaks.program_meteor_line", "Program: Meteor Line");
        add("item.psitweaks.program_supreme_infusion", "Program: Supreme Infusion");
        add("item.psitweaks.program_molecular_divider", "Program: Molecular Divider");
        add("item.psitweaks.program_radiation_injection", "Program: Radiation Injection");
        add("item.psitweaks.program_radiation_filter", "Program: Radiation Filter");
        add("item.psitweaks.program_cure_radiation", "Program: Cure Radiation");
        add("item.psitweaks.program_guillotine", "Program: Guillotine");
        add("item.psitweaks.program_active_air_mine", "Program: Active Air Mine");
        add("item.psitweaks.program_die_flex", "Program: Flexible Die");
        add("item.psitweaks.program_material_mutation", "Program: Material Mutation");
        add("item.psitweaks.alloy_psion", "Psionic Alloy");
        add("item.psitweaks.psionic_control_circuit", "Psionic Control Circuit");
        add("item.psitweaks.echo_control_circuit", "Echo Control Circuit");
        add("item.psitweaks.alloy_psionic_echo", "Echo Alloy");
        add("item.psitweaks.alloy_hypostasis", "Hypostasis Alloy");
        add("item.psitweaks.hypostasis_control_circuit", "Hypostasis Control Circuit");
        add("item.psitweaks.philosophers_stone", "Philosopher's Stone");
        add("item.psitweaks.heavy_psimetal_scrap", "Heavy Psimetal Scrap");
        add("item.psitweaks.echo_pellet", "HDΨE Pellet");
        add("item.psitweaks.pellet_neptunium", "Neptunium Pellet");
        add("item.psitweaks.pellet_americium", "Americium Pellet");
        add("item.psitweaks.hypostasis_gem", "Hypostasis Gem");
        add("item.psitweaks.jade", "Jade");
        add("item.psitweaks.magatama", "Magatama");
        add("item.psitweaks.echo_sheet", "HDΨE Sheet");
        add("item.psitweaks.magicians_brain", "Magician's Brain");
        add("item.psitweaks.psionic_factor", "Psionic Factor");
        add("item.psitweaks.psionic_factor_ivory", "Ivory Psionic Factor");
        add("item.psitweaks.psionic_factor_ebony", "Ebony Psionic Factor");
        add("item.psitweaks.chaotic_factor", "Chaotic Factor");
        add("item.psitweaks.chaotic_psimetal", "Chaotic Psimetal Ingot");
        add("item.psitweaks.flashmetal", "Flashmetal Ingot");
        add("item.psitweaks.unrefined_flashmetal", " Unrefined Flashmetal");
        add("item.psitweaks.heavy_psimetal", "Heavy Psimetal Ingot");
        add("item.psitweaks.raw_antinite", "Raw Antinite");
        add("item.psitweaks.shard_antinite", "Antinite Shard");
        add("item.psitweaks.crystal_antinite", "Antinite Crystal");
        add("item.psitweaks.clump_antinite", "Antinite Clump");
        add("item.psitweaks.dirty_dust_antinite", "Dirty Antinite Dust");
        add("item.psitweaks.antinite_dust", "Antinite Dust");
        add("item.psitweaks.antinite_ingot", "Antinite Ingot");
        add("item.psitweaks.psycheonic_metal_ingot", "Psycheonic Metal Ingot");
        add("item.psitweaks.psycheonic_metal_nugget", "Psycheonic Metal Nugget");
        add("item.psitweaks.cad_assembly_alloy_psion", "Psionic Alloy CAD Assembly");
        add("item.psitweaks.cad_assembly_chaotic_psimetal", "Chaotic Psimetal CAD Assembly");
        add("item.psitweaks.cad_assembly_flashmetal", "Flashmetal CAD Assembly");
        add("item.psitweaks.cad_assembly_heavy_psimetal_alpha", "Heavy Psimetal CAD Assembly α");
        add("item.psitweaks.cad_assembly_heavy_psimetal_beta", "Heavy Psimetal CAD Assembly β");
        add("item.psitweaks.cad_assembly_psycheonic_metal", "Psycheonic Metal CAD Assembly");
        add("item.psitweaks.incomplete_heavy_psimetal_assembly", "Incomplete Heavy Psimetal CAD Assembly");
        add("item.psitweaks.portable_cad_assembler", "Portable CAD Assembler");
        add("item.psitweaks.auto_caster_tick", "Auto Caster: tick");
        add("item.psitweaks.auto_caster_second", "Auto Caster: second");
        add("item.psitweaks.auto_caster_custom_tick", "Auto Caster: custom tick");
        add("item.psitweaks.interference_range_extender", "Interference Range Extender");
        add("item.psitweaks.third_eye_device", "Third‐Eye Device");
        add("item.psitweaks.sorcery_booster", "Sorcery Booster");
        add("item.psitweaks.spell_magazine", "Spell Magazine");
        add("item.psitweaks.inline_caster", "Inline Caster");
        add("item.psitweaks.secondary_caster", "Secondary Caster");
        add("item.psitweaks.parallel_caster", "Parallel Caster");
        add("item.psitweaks.advanced_spell_bullet", "Advanced Spell Bullet");
        add("item.psitweaks.resonant_spell_bullet", "Resonant Spell Bullet");
        add("item.psitweaks.sublimated_spell_bullet", "Sublimated Spell Bullet");
        add("item.psitweaks.awakened_spell_bullet", "Awakened Spell Bullet");
        add("item.psitweaks.transcendent_spell_bullet", "Transcendent Spell Bullet");
        add("death.attack.psitweaks.meteor_line", "%1$s was permeated by light");
        add("death.attack.psitweaks.meteor_line.player", "%1$s was permeated by light");
        add("death.attack.psitweaks.meteor_line.item", "%1$s was permeated by light");
        add("item.psitweaks.advanced_spell_bullet_projectile", "Advanced Projectile Spell Bullet");
        add("item.psitweaks.resonant_spell_bullet_projectile", "Resonant Projectile Spell Bullet");
        add("item.psitweaks.sublimated_spell_bullet_projectile", "Sublimated Projectile Spell Bullet");
        add("item.psitweaks.awakened_spell_bullet_projectile", "Awakened Projectile Spell Bullet");
        add("item.psitweaks.transcendent_spell_bullet_projectile", "Transcendent Projectile Spell Bullet");
        add("item.psitweaks.advanced_spell_bullet_loop", "Advanced Loopcast Spell Bullet");
        add("item.psitweaks.resonant_spell_bullet_loop", "Resonant Loopcast Spell Bullet");
        add("item.psitweaks.sublimated_spell_bullet_loop", "Sublimated Loopcast Spell Bullet");
        add("item.psitweaks.awakened_spell_bullet_loop", "Awakened Loopcast Spell Bullet");
        add("item.psitweaks.transcendent_spell_bullet_loop", "Transcendent Loopcast Spell Bullet");
        add("item.psitweaks.advanced_spell_bullet_charge", "Advanced Charge Spell Bullet");
        add("item.psitweaks.resonant_spell_bullet_charge", "Resonant Charge Spell Bullet");
        add("item.psitweaks.sublimated_spell_bullet_charge", "Sublimated Charge Spell Bullet");
        add("item.psitweaks.awakened_spell_bullet_charge", "Awakened Charge Spell Bullet");
        add("item.psitweaks.transcendent_spell_bullet_charge", "Transcendent Charge  Spell Bullet");
        add("item.psitweaks.advanced_spell_bullet_mine", "Advanced Mine Spell Bullet");
        add("item.psitweaks.resonant_spell_bullet_mine", "Resonant Mine Spell Bullet");
        add("item.psitweaks.sublimated_spell_bullet_mine", "Sublimated Mine Spell Bullet");
        add("item.psitweaks.awakened_spell_bullet_mine", "Awakened Mine Spell Bullet");
        add("item.psitweaks.transcendent_spell_bullet_mine", "Transcendent Mine Spell Bullet");
        add("item.psitweaks.advanced_spell_bullet_grenade", "Advanced Grenade Spell Bullet");
        add("item.psitweaks.resonant_spell_bullet_grenade", "Resonant Grenade Spell Bullet");
        add("item.psitweaks.sublimated_spell_bullet_grenade", "Sublimated Grenade Spell Bullet");
        add("item.psitweaks.awakened_spell_bullet_grenade", "Awakened Grenade Spell Bullet");
        add("item.psitweaks.transcendent_spell_bullet_grenade", "Transcendent Grenade Spell Bullet");
        add("item.psitweaks.advanced_spell_bullet_circle", "Advanced Circle Spell Bullet");
        add("item.psitweaks.resonant_spell_bullet_circle", "Resonant Circle Spell Bullet");
        add("item.psitweaks.sublimated_spell_bullet_circle", "Sublimated Circle Spell Bullet");
        add("item.psitweaks.awakened_spell_bullet_circle", "Awakened Circle Spell Bullet");
        add("item.psitweaks.transcendent_spell_bullet_circle", "Transcendent Circle Spell Bullet");
        add("block.psitweaks.cad_disassembler", "CAD Disassembler");
        add("block.psitweaks.program_researcher", "Program Research Table");
        add("block.psitweaks.ore_antinite", "Antinite Ore");
        add("block.psitweaks.antinite_block", "Antinite Block");
        add("block.psitweaks.chaotic_psimetal_block", "Chaotic Psimetal Block");
        add("block.psitweaks.flashmetal_block", "Flashmetal Block");
        add("block.psitweaks.heavy_psimetal_block", "Heavy Psimetal Block");
        add("block.psitweaks.plutonium_block", "Plutonium Block");
        add("block.psitweaks.polonium_block", "Polonium Block");
        add("block.psitweaks.raw_antinite_block", "Raw Antinite Block");
        add("block.psitweaks.spellmachinery_casing", "Spellmachinery Casing");
        add("block.psitweaks.sculk_eroder", "Sculk Eroder");
        add("block.psitweaks.material_mutator", "Material Mutator");
        add("block.psitweaks.psionic_generator", "Psi-Link Generator");
        add("block.psitweaks.transcendent_universal_cable", "Transcendent Universal Cable");
        add("block.psitweaks.transcendent_energy_cube", "Transcendent Energy Cube");
        add("container.psitweaks.program_researcher", "Program Research Table");
        add("jei.psitweaks.program_research", "Program Research");
        add("jei.psitweaks.material_mutation", "Material Mutation");
        add("jei.psitweaks.program_research.energy", "Energy: %s FE");
        add("jei.psitweaks.program_research.time", "Time: %s min %s sec");
        add("container.psitweaks.sculk_eroder", "Sculk Eroder");
        add("container.psitweaks.material_mutator", "Material Mutator");
        add("container.psitweaks.psionic_generator", "Psi-Link Generator");
        add("container.psitweaks.transcendent_energy_cube", "Transcendent Energy Cube");
        add("description.psitweaks.sculk_eroder", "Corrodes stone, dirt, and sand type blocks into Sculk");
        add("description.psitweaks.material_mutator", "Mutates items into different materials using Psionic Echo Gas");
        add("description.psitweaks.psionic_generator", "Consumes the owner's Psi and converts it into FE while linked");
        add("description.psitweaks.transcendent_universal_cable.transfer", "Transfer Capacity: %s/t");
        add("description.psitweaks.magicians_brain", "Left behind when a Magician meets a brutal end.");
        add("message.psitweaks.cad_disassembler.unsupported_tic_cad", "Failed to safely disassemble this TiCCAD.");
        add("message.psitweaks.global_traveler.linked", "Linked Global Traveler to inventory at (%s, %s, %s)");
        add("message.psitweaks.global_traveler.unlinked", "Unlinked Global Traveler destination");
        add("message.psitweaks.spell_unlock.cocytus", "Unlocked Trick: Cocytus");
        add("message.psitweaks.spell_unlock.cocytus.already", "Trick: Cocytus is already unlocked");
        add("message.psitweaks.spell_unlock.unlocked", "Unlocked %s");
        add("message.psitweaks.spell_unlock.already", "%s is already unlocked");
        add("message.psitweaks.spell_unlock.command.grant.single", "Granted %s unlock to %s");
        add("message.psitweaks.spell_unlock.command.grant.multi", "Granted %s unlock to %s/%s players");
        add("message.psitweaks.spell_unlock.command.revoke.single", "Revoked %s unlock from %s");
        add("message.psitweaks.spell_unlock.command.revoke.multi", "Revoked %s unlock from %s/%s players");
        add("message.psitweaks.spell_unlock.command.status.unlocked", "%s has %s unlocked");
        add("message.psitweaks.spell_unlock.command.status.locked", "%s has %s locked");
        add("message.psitweaks.spell_unlock.command.no_change", "No %s unlock status was changed");
        add("message.psitweaks.spell_unlock.command.all.no_change", "No all-spell unlock status was changed (%s player(s), %s spell(s))");
        add("message.psitweaks.spell_unlock.command.all.grant.single", "Granted all spell unlocks to %s (%s/%s changed)");
        add("message.psitweaks.spell_unlock.command.all.grant.multi", "Granted all spell unlocks: %s/%s changes, players changed %s/%s");
        add("message.psitweaks.spell_unlock.command.all.revoke.single", "Revoked all spell unlocks from %s (%s/%s changed)");
        add("message.psitweaks.spell_unlock.command.all.revoke.multi", "Revoked all spell unlocks: %s/%s changes, players changed %s/%s");
        add("message.psitweaks.spell_unlock.command.all.status", "%s has %s/%s spell unlocks");
        add("message.psitweaks.spell_unlock.command.cocytus.grant.single", "Granted Trick: Cocytus unlock to %s");
        add("message.psitweaks.spell_unlock.command.cocytus.grant.multi", "Granted Trick: Cocytus unlock to %s/%s players");
        add("message.psitweaks.spell_unlock.command.cocytus.revoke.single", "Revoked Trick: Cocytus unlock from %s");
        add("message.psitweaks.spell_unlock.command.cocytus.revoke.multi", "Revoked Trick: Cocytus unlock from %s/%s players");
        add("message.psitweaks.spell_unlock.command.cocytus.status.unlocked", "%s has Trick: Cocytus unlocked");
        add("message.psitweaks.spell_unlock.command.cocytus.status.locked", "%s has Trick: Cocytus locked");
        add("effect.psitweaks.parade", "Parade");
        add("effect.psitweaks.flight", "Flight");
        add("effect.psitweaks.barrier", "Barrier");
        add("effect.psitweaks.hardening", "Hardening");
        add("effect.psitweaks.radiation_filter", "Radiation Filter");
        add("attribute.psitweaks.spell_damage_factor", "Spell Damage Factor");
        add("attribute.psitweaks.max_psi_bonus", "Max Psi Bonus");
        add("attribute.psitweaks.psi_regen_bonus", "Psi Regeneration Bonus");
        add("module.psitweaks.psyon_supplying_unit", "Psyon Supplying Unit");
        add("description.psitweaks.psyon_supplying_unit", "Increases Psi regeneration.");
        add("module.psitweaks.psyon_capacity_unit", "Psyon Capacity Unit");
        add("description.psitweaks.psyon_capacity_unit", "Increases maximum Psi.");
        add("module.psitweaks.phenomenon_interference_enhancement_unit", "Phenomenon Interference Enhancement Unit");
        add("description.psitweaks.phenomenon_interference_enhancement_unit", "Increases spell damage.");
        add("psitweaks.event.second", "§bEvent§7: Second");
        add("psitweaks.event.custom_tick", "§bEvent§7: Custom Tick");
        add("screen.psitweaks.auto_caster_custom_tick", "Auto Caster Settings");
        add("screen.psitweaks.auto_caster_custom_tick.interval", "Cast Interval (ticks)");
        add("screen.psitweaks.auto_caster_custom_tick.range", "Range: %s - %s");
        add("screen.psitweaks.auto_caster_custom_tick.invalid", "Enter a value between %s and %s");
        add("gui.psitweaks.psionic_generator.toggle", "Toggle");
        add("gui.psitweaks.psionic_generator.owner", "Owner: %s");
        add("gui.psitweaks.psionic_generator.owner_state", "Owner State: %s");
        add("gui.psitweaks.psionic_generator.owner_online", "Online");
        add("gui.psitweaks.psionic_generator.owner_offline", "Offline");
        add("gui.psitweaks.psionic_generator.link_state", "Link: %s");
        add("gui.psitweaks.psionic_generator.link_on", "Enabled");
        add("gui.psitweaks.psionic_generator.link_off", "Disabled");
        add("gui.psitweaks.psionic_generator.summary_status", "%1$s / %2$s");
        add("gui.psitweaks.psionic_generator.summary_psi", "Psi: %1$s/%2$s");
        add("gui.psitweaks.psionic_generator.summary_consume", "Use: %s/t");
        add("gui.psitweaks.psionic_generator.summary_generate", "Gen: %s /t");
        add("gui.psitweaks.psionic_generator.summary_consume_full", "Consume: %s Psi/t");
        add("gui.psitweaks.psionic_generator.summary_generate_full", "Producing: %1$s / %2$s /t");
        add("gui.psitweaks.psionic_generator.psi", "Psi: %s / %s");
        add("gui.psitweaks.psionic_generator.consume", "Consume: %s Psi/t");
        add("gui.psitweaks.psionic_generator.generate", "Producing: %1$s / %2$s /t");
        add("tooltip.psitweaks.auto_caster_custom_tick.interval", "Cast interval: %s ticks");
        add("tooltip.psitweaks.spell_magazine.bullets", "Loaded spell bullets: %s/%s");
        add("gui.psitweaks.apply", "Apply");
        add("infuse_type.psitweaks.infuse_psigem", "Psigem");
        add("infuse_type.psitweaks.infuse_ebony", "Ebony");
        add("infuse_type.psitweaks.infuse_ivory", "Ivory");
        add("infuse_type.psitweaks.infuse_chaotic_factor", "Chaos");
        add("infuse_type.psitweaks.infuse_psionic_echo", "Psinonic Echo");
        add("infuse_type.psitweaks.infuse_hypostasis", "Hypostasis Gem");
        add("gas.psitweaks.gas_psionic_echo", "Psionic Echo Gas");
        add("gas.psitweaks.gas_peo_fuel", "ΨE-O Fuel");
        add("gas.psitweaks.gas_americium", "Americium");
        add("gas.psitweaks.gas_neptunium", "Neptunium");
        add("slurry.psitweaks.dirty_antinite", "Dirty Antinite Slurry");
        add("slurry.psitweaks.clean_antinite", "Clean Antinite Slurry");
        add("curios.identifier.magic_calculation_area", "Magic Calculation Area");
        add("psitweaks.spellpiece.trick_explode_no_destroy", "Trick: Antipersonnel Explode");
        add("psitweaks.spellpiece.trick_explode_no_destroy.desc", "Cause an explosion that does not destroy blocks");
        add("psitweaks.spellpiece.trick_barrier", "Trick: Barrier");
        add("psitweaks.spellpiece.trick_barrier.desc", "Reduce damage taken");
        add("psitweaks.spellpiece.trick_hardening", "Trick: Hardening");
        add("psitweaks.spellpiece.trick_hardening.desc", "When taking heavy damage, reduce it to a certain value");
        add("psitweaks.spellpiece.trick_parade", "Trick: Parade");
        add("psitweaks.spellpiece.trick_parade.desc", "Avoid damage with a certain probability");
        add("psitweaks.spellpiece.trick_interact_block", "Trick: Block Interact");
        add("psitweaks.spellpiece.trick_interact_block.desc", "Right-click with the off-hand item on the target block");
        add("psitweaks.spellpiece.trick_freeze_block", "Trick: Block Freeze");
        add("psitweaks.spellpiece.trick_freeze_block.desc", "Freeze the block at the target position into its next colder form");
        add("psitweaks.spellpiece.trick_melt_block", "Trick: Block Melt");
        add("psitweaks.spellpiece.trick_melt_block.desc", "Melt the block at the target position into its hotter form");
        add("psitweaks.spellpiece.trick_break_fortune", "Trick: Break Block (Fortune)");
        add("psitweaks.spellpiece.trick_break_fortune.desc", "Break a block with Fortune applied");
        add("psitweaks.spellpiece.trick_break_silk", "Trick: Break Block (Silk Touch)");
        add("psitweaks.spellpiece.trick_break_silk.desc", "Break a block with Silk Touch applied");
        add("psitweaks.spellpiece.trick_store_entity", "Trick: Store Entity");
        add("psitweaks.spellpiece.trick_store_entity.desc", "Store the entity's UUID string in the CAD memory");
        add("psitweaks.spellpiece.selector_stored_entity", "Selector: Stored Entity");
        add("psitweaks.spellpiece.selector_stored_entity.desc", "Retrieve entities from the UUID stored in the CAD memory");
        add("psitweaks.spellpiece.selector_nearby_spellgram", "Selector: Nearby SpellGram Object");
        add("psitweaks.spellpiece.selector_nearby_spellgram.desc", "Retrieve SpellGram objects around the specified position");
        add("psitweaks.spellparam.spellgram", "SpellGram");
        add("psitweaks.spellerror.not_spellgram_object", "ERROR: The input must be a SpellGramObject");
        add("psitweaks.spellpiece.trick_dispel", "Trick: Dispel");
        add("psitweaks.spellpiece.trick_dispel.desc", "Remove effects from target entity");
        add("psitweaks.spellpiece.trick_dispel_beneficial", "Trick: Dispel Beneficial");
        add("psitweaks.spellpiece.trick_dispel_beneficial.desc", "Remove beneficial effects from target entity");
        add("psitweaks.spellpiece.trick_dispel_non_beneficial", "Trick: Dispel Non Beneficial");
        add("psitweaks.spellpiece.trick_dispel_non_beneficial.desc", "Remove non beneficial effects from target entity");
        add("psitweaks.spellpiece.trick_cocytus", "Trick: Cocytus");
        add("psitweaks.spellpiece.trick_cocytus.desc", "Permanently freeze the target mob's mind");
        add("psitweaks.spellpiece.trick_time_accelerate", "Trick: Time Accelerate");
        add("psitweaks.spellpiece.trick_time_accelerate.desc", "Accelerates the time of the target block");
        add("psitweaks.spellpiece.trick_phonon_maser", "Trick: Phonon Maser");
        add("psitweaks.spellpiece.trick_phonon_maser.desc", "Vibrates ultrasonic waves to emit heat rays");
        add("psitweaks.spellpiece.trick_meteor_line", "Trick: Meteor Line");
        add("psitweaks.spellpiece.trick_meteor_line.desc", "Creates a line that permeates light and pierces living beings on its path");
        add("psitweaks.spellpiece.trick_supply_fe", "Trick: FE Charge");
        add("psitweaks.spellpiece.trick_supply_fe.desc", "Supplies FE to blocks. Direction can be specified.");
        add("psitweaks.spellpiece.trick_supreme_infusion", "Trick: Supreme Infusion");
        add("psitweaks.spellpiece.trick_supreme_infusion.desc", "Infuse Echo Shards into Psionic Echoes");
        add("psitweaks.spellpiece.trick_molecular_divider", "Trick: Molecular Divider");
        add("psitweaks.spellpiece.trick_molecular_divider.desc", "divide the living creatures within the area");
        add("psitweaks.spellpiece.trick_aqua_cutter", "Trick: Aqua Cutter");
        add("psitweaks.spellpiece.trick_aqua_cutter.desc", "Launches a water blade projectile that damages on hit");
        add("psitweaks.spellpiece.trick_blaze_ball", "Trick: Blaze Ball");
        add("psitweaks.spellpiece.trick_blaze_ball.desc", "Launches a fireball forward that deals fire damage on hit");
        add("psitweaks.spellpiece.trick_radiation_injection", "Trick: Radiation Injection");
        add("psitweaks.spellpiece.trick_radiation_injection.desc", "Applies radiation exposure to the target");
        add("psitweaks.spellpiece.trick_radiation_filter", "Trick: Radiation Filter");
        add("psitweaks.spellpiece.trick_radiation_filter.desc", "Applies a radiation filter effect to the target");
        add("psitweaks.spellpiece.trick_cure_radiation", "Trick: Cure Radiation");
        add("psitweaks.spellpiece.trick_cure_radiation.desc", "Removes radiation exposure from the target");
        add("psitweaks.spellpiece.trick_guillotine", "Trick: Guillotine");
        add("psitweaks.spellpiece.trick_guillotine.desc", "Deals a heavy slash to the target and drops a head on kill");
        add("psitweaks.spellpiece.trick_active_air_mine", "Trick: Active Air Mine");
        add("psitweaks.spellpiece.trick_active_air_mine.desc", "Deals magic damage to entities inside a spherical area around the target position");
        add("psitweaks.spellpiece.trick_flare_circle", "Trick: Fire Circle");
        add("psitweaks.spellpiece.trick_flare_circle.desc", "Places a flare Spell Gram Circle that repeatedly deals fire damage around it");
        add("psitweaks.spellpiece.trick_ice_circle", "Trick: Ice Circle");
        add("psitweaks.spellpiece.trick_ice_circle.desc", "Places an ice Spell Gram Circle that repeatedly deals freeze damage around it");
        add("psitweaks.spellpiece.trick_set_spellgram_follow_target", "Trick: Set SpellGram Follow Target");
        add("psitweaks.spellpiece.trick_set_spellgram_follow_target.desc", "Sets the follow target entity for a SpellGram object");
        add("psitweaks.spellpiece.trick_die_flex", "Trick: Flexible Die");
        add("psitweaks.spellpiece.trick_die_flex.desc", "Stops execution when given a number whose absolute value is less than 1, and refunds Psi cost for skipped pieces. When used in spells that cast every tick, client-side Psi display may temporarily desync.");
        add("psitweaks.spellpiece.trick_material_mutation", "Trick: Material Mutation");
        add("psitweaks.spellpiece.trick_material_mutation.desc", "Acts on a specific block, alters its material structure, and transmutes it into a different substance.");
        add("psitweaks.spellpiece.operator_tan", "Operator: Tangent");
        add("psitweaks.spellpiece.operator_tan.desc", "tan(A)");
        add("psitweaks.spellpiece.operator_atan", "Operator: Arc Tangent");
        add("psitweaks.spellpiece.operator_atan.desc", "atan(A)");
        add("psitweaks.spellpiece.operator_sinh", "Operator: Hyperbolic Sine");
        add("psitweaks.spellpiece.operator_sinh.desc", "sinh(A)");
        add("psitweaks.spellpiece.operator_cosh", "Operator: Hyperbolic Cosine");
        add("psitweaks.spellpiece.operator_cosh.desc", "cosh(A)");
        add("psitweaks.spellpiece.operator_tanh", "Operator: Hyperbolic Tangent");
        add("psitweaks.spellpiece.operator_tanh.desc", "tanh(A)");
        add("creativetabs.psitweaks", "Psi: Tweaks and Additions");
        add("psitweaks.spellpiece.trick_flight", "Trick: Flight");
        add("psitweaks.spellpiece.trick_flight.desc", "Enabling creative flight");
        add("material.psitweaks.flashmetal", "Flashmetal");
        add("material.psitweaks.psimetal", "Psimetal");
        add("material.psitweaks.ivory_psimetal", "Ivory Psimetal");
        add("material.psitweaks.ebony_psimetal", "Ebony Psimetal");
        add("material.psitweaks.heavy_psimetal", "Heavy Psimetal");
        add("material.psitweaks.chaotic_psimetal", "Chaotic Psimetal");
        add("material.psitweaks.antinite", "Antinite");
        add("material.psitweaks.psycheonic_metal", "Psycheonic Metal");
        add("item.psitweaks.molten_psimetal_bucket", "Molten Psimetal Bucket");
        add("item.psitweaks.molten_psigem_bucket", "Molten Psigem Bucket");
        add("item.psitweaks.molten_ivory_psimetal_bucket", "Molten Ivory Psimetal Bucket");
        add("item.psitweaks.molten_ebony_psimetal_bucket", "Molten Ebony Psimetal Bucket");
        add("item.psitweaks.molten_flashmetal_bucket", "Molten Flashmetal Bucket");
        add("item.psitweaks.molten_chaotic_psimetal_bucket", "Molten Chaotic Psimetal Bucket");
        add("item.psitweaks.molten_heavy_psimetal_bucket", "Molten Heavy Psimetal Bucket");
        add("item.psitweaks.molten_psionic_echo_bucket", "Molten Psionic Echo Bucket");
        add("item.psitweaks.molten_antinite_bucket", "Molten Antinite Bucket");
        add("item.psitweaks.molten_psycheonic_metal_bucket", "Molten Psycheonic Metal Bucket");
        add("block.psitweaks.molten_psimetal_fluid", "Molten Psimetal");
        add("block.psitweaks.molten_psigem_fluid", "Molten Psigem");
        add("block.psitweaks.molten_ivory_psimetal_fluid", "Molten Ivory Psimetal");
        add("block.psitweaks.molten_ebony_psimetal_fluid", "Molten Ebony Psimetal");
        add("block.psitweaks.molten_flashmetal_fluid", "Molten Flashmetal");
        add("block.psitweaks.molten_chaotic_psimetal_fluid", "Molten Chaotic Psimetal");
        add("block.psitweaks.molten_heavy_psimetal_fluid", "Molten Heavy Psimetal");
        add("block.psitweaks.molten_psionic_echo_fluid", "Molten Psionic Echo");
        add("block.psitweaks.molten_antinite_fluid", "Molten Antinite");
        add("block.psitweaks.molten_psycheonic_metal_fluid", "Molten Psycheonic Metal");
        add("fluid.psitweaks.molten_psimetal", "Molten Psimetal");
        add("fluid.psitweaks.molten_psigem", "Molten Psigem");
        add("fluid.psitweaks.molten_ivory_psimetal", "Molten Ivory Psimetal");
        add("fluid.psitweaks.molten_ebony_psimetal", "Molten Ebony Psimetal");
        add("fluid.psitweaks.molten_flashmetal", "Molten Flashmetal");
        add("fluid.psitweaks.molten_chaotic_psimetal", "Molten Chaotic Psimetal");
        add("fluid.psitweaks.molten_heavy_psimetal", "Molten Heavy Psimetal");
        add("fluid.psitweaks.molten_psionic_echo", "Molten Psionic Echo");
        add("fluid.psitweaks.molten_antinite", "Molten Antinite");
        add("fluid.psitweaks.molten_psycheonic_metal", "Molten Psycheonic Metal");
        add("fluid_type.psitweaks.molten_psimetal", "Molten Psimetal");
        add("fluid_type.psitweaks.molten_psigem", "Molten Psigem");
        add("fluid_type.psitweaks.molten_ivory_psimetal", "Molten Ivory Psimetal");
        add("fluid_type.psitweaks.molten_ebony_psimetal", "Molten Ebony Psimetal");
        add("fluid_type.psitweaks.molten_flashmetal", "Molten Flashmetal");
        add("fluid_type.psitweaks.molten_chaotic_psimetal", "Molten Chaotic Psimetal");
        add("fluid_type.psitweaks.molten_heavy_psimetal", "Molten Heavy Psimetal");
        add("fluid_type.psitweaks.molten_psionic_echo", "Molten Psionic Echo");
        add("fluid_type.psitweaks.molten_antinite", "Molten Antinite");
        add("fluid_type.psitweaks.molten_psycheonic_metal", "Molten Psycheonic Metal");
        add("modifier.psitweaks.psi_buffer", "Psi Buffer");
        add("modifier.psitweaks.psi_buffer.flavor", "Psyon Fortress");
        add("modifier.psitweaks.psi_buffer.description", "When worn as armor, consumes Psi to absorb incoming damage");
        add("modifier.psitweaks.load_computation", "Load Computation");
        add("modifier.psitweaks.load_computation.flavor", "Additive logic");
        add("modifier.psitweaks.load_computation.description", "When Psi is sufficient, consumes a fixed amount to add bonus magic damage on attack");
        add("modifier.psitweaks.casting_assist", "High-efficiency Computation");
        add("modifier.psitweaks.casting_assist.flavor", "Parallel processing");
        add("modifier.psitweaks.casting_assist.description", "Reduces spell Psi cost while this tool is held or worn as armor (does not stack)");
        add("modifier.psitweaks.erosion_computation", "Erosion Computation");
        add("modifier.psitweaks.erosion_computation.flavor", "Subtractive logic");
        add("modifier.psitweaks.erosion_computation.description", "On attack, applies debuffs and siphons a small amount of Psi from the target");
        add("modifier.psitweaks.psicological", "Psicological");
        add("modifier.psitweaks.psicological.flavor", "Psicological recovery");
        add("modifier.psitweaks.psicological.description", "While held or equipped, periodically consumes Psi to repair durability and can spend Psi instead of durability loss");
        add("modifier.psitweaks.global_traveler", "Global Traveler");
        add("modifier.psitweaks.global_traveler.flavor", "Items beyond borders");
        add("modifier.psitweaks.global_traveler.description", "Sneak-right-click an inventory block to link it; block and mob drops from this tool are routed there when possible");
        add("modifier.psitweaks.mind_crush", "Mind Crush");
        add("modifier.psitweaks.mind_crush.flavor", "Blue Metal of Death");
        add("modifier.psitweaks.mind_crush.description", "On attack, has a level×5% chance to freeze the target mob with NoAI. When worn as armor, triggers on the attacker instead");
        add("entity.minecraft.villager.psitweaks.spellcaster", "Magician");
        add("psi.book.category.psitweaks", "PsiTweaks");
        add("psi.book.category.psitweaks.desc", "$(thing)PsiTweaks$(0) expands Psi with stronger spell bullets, CAD parts, machinery, Mekanism integration, and late-game psionic materials.");
        add("psi.book.category.psitweaks_machines", "Machines");
        add("psi.book.category.psitweaks_machines.desc", "Machine blocks and infrastructure added by PsiTweaks.");
        add("psi.book.entry.psitweaks_overview", "What PsiTweaks Adds");
        add("psi.book.page.psitweaks_overview.0", "$(thing)PsiTweaks$(0) is an expansion for $(thing)Psi$(0). It adds many elements, including industrialized material production, new gear, and new spells.$(p)The mod is intended for setups where Psi remains powerful through the late game as both combat and utility, rather than ending at ordinary CADs and basic spell bullets.");
        add("psi.book.page.psitweaks_overview.2", "Major additions include higher-tier spell bullets, new CAD assemblies and casting support tools, new spells, processing machines, and generators.$(p)Most systems are built to bridge Psi with industrial environments and to strengthen Psi's late-game capabilities.");
        add("psi.book.entry.psitweaks_changes", "Changes by PsiTweaks");
        add("psi.book.page.psitweaks_changes.0", "Installing $(thing)Psi: Tweaks And Additions$(0) applies several adjustments to improve QOL.$(p)Psi is no longer reduced when you take damage, and the Psi regeneration cooldown after casting is removed.$(p)In the Spell Programmer screen, spell pieces can be searched in English regardless of the current language setting.");
        add("psi.book.entry.psitweaks_research", "Research");
        add("psi.book.page.psitweaks_research.0", "Some spell pieces added by $(thing)PsiTweaks$(0) must be unlocked through research before they can be used in spell programs.$(p)Research creates program items that correspond to those spell pieces. Right-clicking with a program unlocks its spell piece, and the program is not consumed.");
        add("psi.book.page.psitweaks_research.1", "Place the required ingredients in the $(l:psitweaks_machines/program_researcher)$(o)$(item)Program Research Table$(0)$(/l) and supply FE to craft a program.$(p)JEI lists each research recipe's required materials, energy cost, and processing time.");
        add("psi.book.entry.psitweaks_magician", "Magician");
        add("psi.book.page.psitweaks_magician.0", "$(thing)Magicians$(0) are a villager profession added by PsiTweaks. They represent villagers who work with Psi machinery and are connected to several caster-focused materials and upgrades.");
        add("psi.book.page.psitweaks_magician.1", "An unemployed villager can become a $(thing)Magician$(0) by claiming a $(l:basics/cad_assembler)$(o)$(item)CAD Assembler$(0)$(/l) as its job site.$(p)Place the assembler where a villager can reach it, just like other villager workstation blocks.");
        add("psi.book.page.psitweaks_magician.2", "A Magician is also the source of the $(l:components/psitweaks_magicians_brain)$(o)$(item)Magician's Brain$(0)$(/l). It drops when a Magician villager is killed by $(l:psitweaks_spell_pieces/trick_guillotine)$(o)Trick: Guillotine$(0)$(/l), and is used in caster-focused recipes.");
        add("psi.book.entry.psitweaks_mekanism_integration", "Mekanism Integration");
        add("psi.book.title.psitweaks_mekanism_integration.infusing", "Metallurgic Infusing");
        add("psi.book.title.psitweaks_mekanism_integration.mekasuit", "MekaSuit Modules");
        add("psi.book.page.psitweaks_mekanism_integration.0", "$(thing)PsiTweaks$(0) ties $(thing)Psi$(0) into $(thing)Mekanism$(0) progression. Its integration covers Metallurgic Infusing recipes for Psi materials, MekaSuit modules that improve caster stats, and machines that exchange Psi, FE, and psionic resources.$(p)Use JEI as the exact recipe reference; this entry explains what each group is for.");
        add("psi.book.page.psitweaks_mekanism_integration.1", "The $(item)Metallurgic Infuser$(0) can produce core Psi materials with PsiTweaks infuse types. Psigem infusion turns redstone into $(l:components/psidust)$(o)$(item)Psidust$(0)$(/l) and gold into $(item)Psimetal$(0).$(p)Ebony and Ivory infusion can make $(item)Ebony Substance$(0), $(item)Ivory Substance$(0), and their Psimetal variants. Later recipes extend this chain into $(l:components/psitweaks_chaotic_factor)psionic factors$(/l), $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)Chaotic Psimetal$(0)$(/l), $(l:components/psitweaks_psionic_echo)Psionic Echo materials$(/l), and $(l:components/psitweaks_alloy_hypostasis)Hypostasis-tier metal$(/l).");
        add("psi.book.page.psitweaks_mekanism_integration.2", "PsiTweaks adds three MekaSuit modules for spellcasters. The $(item)Psyon Supplying Unit$(0) and $(item)Psyon Capacity Unit$(0) install in the MekaSuit body armor and improve Psi regeneration and maximum Psi.$(p)The $(item)Phenomenon Interference Enhancement Unit$(0) installs in the MekaSuit helmet and increases spell damage.");
        add("psi.book.entry.psitweaks_spell_bullets", "Upgraded Spell Bullets");
        add("psi.book.page.psitweaks_spell_bullets.0", "$(thing)PsiTweaks$(0) adds upgraded tiers above the ordinary $(l:items/spell_bullet)$(o)$(item)Spell Bullet$(0)$(/l): Advanced, Resonant, Sublimated, Awakened, and Transcendent.$(p)Every Psi spell bullet variant has upgraded tiers available.");
        add("psi.book.page.psitweaks_spell_bullets.1", "Upgrading a spell bullet requires four bullets from the previous tier, making it very expensive.");
        add("psi.book.page.psitweaks_spell_bullets.2", "Higher tiers greatly improve cost efficiency, making powerful spells easier to cast.");
        add("psi.book.entry.psitweaks_machines", "Overview");
        add("psi.book.page.psitweaks_machines.0", "PsiTweaks adds machines that connect spellcraft with automation and industrial processing. Some machines are pure psionic tools, while others integrate with Mekanism-style energy and materials.");
        add("psi.book.page.psitweaks_machines.1", "These machines are the main entry points for research, mutation, Sculk processing, and Psi-to-energy conversion.");
        add("psi.book.page.psitweaks_machines.2", "The $(l:psitweaks_machines/psionic_generator)$(o)$(item)Psi-Link Generator$(0)$(/l) links to its owner. When enabled, loaded, and the owner is online, it consumes that player's Psi and produces energy for the machine's buffer.$(p)The rate is set in the GUI. More Psi per tick means more power, but it also drains the owner faster.");
        add("psi.book.page.psitweaks_machine.cad_disassembler", "A workbench for disassembling CADs and recovering their parts. Sneak-right-click it with a CAD to break the CAD into its installed components and loaded spell bullets.$(p)Use it when replacing CAD bodies or salvaging parts from old setups.");
        add("psi.book.page.psitweaks_machine.program_researcher", "A powered research table for producing PsiTweaks program items. Put the required ingredients in the input slots and supply FE; completed research outputs the program item.$(p)JEI shows each research recipe's energy cost and time.");
        add("psi.book.page.psitweaks_machine.sculk_eroder", "A machine that corrodes stone, dirt, sand, and related block items into Sculk outputs.$(p)Use it when you need Sculk materials without relying on natural spread.");
        add("psi.book.page.psitweaks_machine.material_mutator", "A Mekanism injecting-style machine that performs material mutation with $(item)Psionic Echo Gas$(0) and energy.$(p)It automates mutations that can also be produced by $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)Trick: Material Mutation$(0)$(/l), such as $(l:components/psitweaks_jade)Jade$(/l) and $(l:components/psitweaks_hypostasis_gem)Hypostasis Gems$(/l).");
        add("psi.book.page.psitweaks_machine.psionic_generator", "A generator that links to its owner and converts that player's Psi into energy while the owner is online.$(p)Use the GUI to enable the link and set the Psi consumed per tick. Higher consumption gives higher output and drains the owner faster.");
        add("psi.book.page.psitweaks_machine.spellmachinery_casing", "A casing block used to execute advanced magic through machinery.");
        add("psi.book.page.psitweaks_machine.transcendent_universal_cable", "This is simply an upgraded version of Mekanism's Universal Cable. It has 128 times the performance of the Ultimate Cable, making it suitable for fusion reactor-class power output.");
        add("psi.book.page.psitweaks_machine.transcendent_energy_cube", "This is simply an upgraded version of Mekanism's Energy Cube. It has 128 times the capacity and output performance of the Ultimate Energy Cube, making it suitable for buffering fission reactor-class power.");
        add("psi.book.entry.psitweaks_cad_and_gear", "CADs and Gear");
        add("psi.book.page.psitweaks_cad_and_gear.0", "Several PsiTweaks items make spellcasting easier to carry, automate, or specialize. Some are direct CAD tools, while others support bullet handling or improve a caster's combat role.");
        add("psi.book.page.psitweaks_cad_and_gear.1", "These recipes cover portable assembly, inline casting, spell storage, and caster support gear.");
        add("psi.book.page.psitweaks_cad_and_gear.2", "PsiTweaks also adds powerful CAD assemblies made from expensive ingots. When ordinary Psi equipment no longer keeps up, use them together with high-tier spell bullets and similar upgrades.");
        add("psi.book.page.psitweaks_cad_and_gear.3", "$(thing)PsiTweaks$(0) adds five CAD material lines: $(l:components/psitweaks_alloy_psion)Psionic Alloy$(/l), $(l:components/psitweaks_chaotic_psimetal)Chaotic Psimetal$(/l), $(l:components/psitweaks_flashmetal)Flashmetal$(/l), $(l:components/psitweaks_heavy_psimetal)Heavy Psimetal$(/l), and $(l:components/psitweaks_psycheonic_metal_ingot)Psycheonic Metal$(/l).$(p)Psionic Alloy CADs have excellent Efficiency but very small Potency. The other CAD assemblies improve in Efficiency and Potency as their materials move into later progression.");
        add("psi.book.entry.psitweaks_inline_casters", "Inline Casters");
        add("psi.book.entry.psitweaks_auto_casters", "Auto Casters");
        add("psi.book.entry.psitweaks_moval_suit", "M.O.V.A.L. Suit");
        add("psi.book.page.psitweaks_item.blank_program", "Used as the base item for program research. Put it into the $(l:psitweaks_machines/program_researcher)$(o)$(item)Program Research Table$(0)$(/l) with the required materials to create a written program.$(p)It can also be crafted with an existing written program item to duplicate that program.");
        add("psi.book.page.psitweaks_item.philosophers_stone", "A reusable catalyst that provides interconversion recipes for metals, diamonds, coal, ender pearls, and other resources.");
        add("psi.book.page.psitweaks_item.spell_magazine", "Stores up to twelve spell bullets and swaps them with the matching slots of your equipped CAD when used.$(p)Use it as a portable loadout carrier when you need to change many bullets at once.");
        add("psi.book.page.psitweaks_item.flash_ring", "A tool that can build and cast spells on the spot.$(p)Sneak-use it to open the programming screen.");
        add("psi.book.page.psitweaks_item.portable_cad_assembler", "A handheld CAD Assembler. Use it to open an assembler interface without placing the block, making it easy to adjust CAD parts or loaded spell bullets while away from base.");
        add("psi.book.page.psitweaks_item.sorcery_booster", "Equips in the Magic Calculation Area slot and increases spell damage by 30%.$(p)It is made from a $(l:components/psitweaks_magicians_brain)$(o)$(item)Magician's Brain$(0)$(/l), making it a combat-focused caster upgrade.");
        add("psi.book.page.psitweaks_item.flash_charm", "A Curios charm that continually removes Blindness and Darkness from the wearer.$(p)It also works while carried in inventory, making it useful in the Deep Dark and against vision-disrupting effects.");
        add("psi.book.page.psitweaks_item.interference_range_extender", "Equips in the Magic Calculation Area slot and extends spell targeting and raycast range to 64 blocks.$(p)It is the lower-tier range option used to craft the Third-Eye Device.");
        add("psi.book.page.psitweaks_item.third_eye_device", "Equips in the Magic Calculation Area slot and removes Psi's normal spell radius check for the caster.$(p)Raycasts are limited to 256 blocks, so use it carefully.");
        add("psi.book.page.psitweaks_item.inline_casters.0", "Inline, Secondary, and Parallel Casters are handheld spellcasting tools with their own bullet sockets. They still require the caster to have a CAD, but the selected bullet is stored in the caster item itself.");
        add("psi.book.page.psitweaks_item.inline_casters.1", "The Inline Caster has one slot, the Secondary Caster has five, and the Parallel Caster has eleven.$(p)Use them when you want to carry many spell bullets without constantly changing the CAD.");
        add("psi.book.page.psitweaks_item.auto_casters.0", "Auto Casters can be equipped in Curios slots and automatically cast spells.$(p)With the Curios Controller, you can change their spell bullets in the same way as psimetal exosuit armor.");
        add("psi.book.page.psitweaks_item.auto_casters.1", "The tick model fires every tick, the second model fires every 0.9 seconds, and the custom tick model lets you set an interval from 1 to 1200 ticks by using the item.");
        add("psi.book.page.psitweaks_item.curios_controller", "Controls the selected bullet slot of socketable items equipped in Magic Calculation Area slots.$(p)Use it while Auto Casters are equipped as the Curios-side counterpart to the exosuit controller.");
        add("psi.book.page.psitweaks_item.moval_suit.0", "The $(item)M.O.V.A.L. Suit$(0) is a high-grade Psi armor set crafted with $(l:components/psitweaks_heavy_psimetal)$(o)$(item)Heavy Psimetal$(0)$(/l) and $(item)Ebony Psimetal$(0). Each equipped piece supports spellcasters by increasing spell damage, Psi regeneration, and maximum Psi.$(p)Use it when you want armor that improves both survival and sustained spellcasting instead of only adding protection.");
        add("psi.book.page.psitweaks_item.moval_suit.1", "Like Psi's exosuit armor, M.O.V.A.L. Suit pieces can trigger spells from armor events. The helmet accepts exosuit sensors, the normal leggings cast on tick, the Ivory leggings cast on second, and the boots cast on jump.$(p)Each piece grants +10% spell damage, +5 Psi regeneration, and +500 maximum Psi.");
        add("psi.book.title.psitweaks_material.chaotic_psimetal", "Chaotic Psimetal Ingot");
        add("psi.book.title.psitweaks_material.psycheonic_metal_ingot", "Psycheonic Metal Ingot");
        add("psi.book.page.psitweaks_material.psionic_echo", "Made by infusing an $(item)Echo Shard$(0) with Psionic Echo infusion, or by using the $(l:psitweaks_spell_pieces/trick_supreme_infusion)$(o)Trick: Supreme Infusion$(0)$(/l).$(p)Used as the base catalyst for $(l:components/psitweaks_alloy_psionic_echo)echo-tier alloys$(/l), HDΨE materials, gas processing, and advanced psionic machinery.");
        add("psi.book.page.psitweaks_material.chaotic_factor", "$(item)Chaotic Factor$(0) starts with infusing an $(item)Ender Pearl$(0) with Psigem to make $(item)Psionic Factor$(0). Infuse that with Ivory or Ebony to make the aligned factor variants, then cross the opposite alignment to create $(item)Chaotic Factor$(0).$(p)Used as the infusion source for $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)Chaotic Psimetal$(0)$(/l), the first major PsiTweaks metal above ordinary Psimetal.");
        add("psi.book.page.psitweaks_material.chaotic_psimetal", "Made by infusing ordinary $(item)Psimetal$(0) with $(l:components/psitweaks_chaotic_factor)Chaotic Factor$(/l).$(p)Used in recipes for Chaotic Psimetal CAD assemblies and $(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)Unrefined Flashmetal$(0)$(/l).");
        add("psi.book.page.psitweaks_material.alloy_psion", "Made by infusing Mekanism's Atomic Alloy with Psigem.$(p)Used for $(l:components/psitweaks_psionic_control_circuit)$(o)$(item)Psionic Control Circuits$(0)$(/l), Psionic Alloy CAD assemblies, and the next alloy tier.");
        add("psi.book.page.psitweaks_material.alloy_psionic_echo", "Made by infusing $(l:components/psitweaks_alloy_psion)$(o)$(item)Psionic Alloy$(0)$(/l) with $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l).$(p)Used for $(l:components/psitweaks_echo_control_circuit)$(o)$(item)Echo Control Circuits$(0)$(/l) and as the base for $(l:components/psitweaks_alloy_hypostasis)Hypostasis Alloy$(/l).");
        add("psi.book.page.psitweaks_material.alloy_hypostasis", "Made by infusing $(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)Echo Alloy$(0)$(/l) with Hypostasis.$(p)Used for $(l:components/psitweaks_hypostasis_control_circuit)$(o)$(item)Hypostasis Control Circuits$(0)$(/l) and high-tier psionic machinery.");
        add("psi.book.page.psitweaks_material.psionic_control_circuit", "Crafted from $(l:components/psitweaks_alloy_psion)$(o)$(item)Psionic Alloy$(0)$(/l) and a Mekanism Ultimate Control Circuit.$(p)Used in PsiTweaks machines, upgraded spell bullets, and similar recipes.");
        add("psi.book.page.psitweaks_material.echo_control_circuit", "Crafted from $(l:components/psitweaks_psionic_control_circuit)$(o)$(item)Psionic Control Circuit$(0)$(/l), $(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)Echo Alloy$(0)$(/l), and $(l:components/psitweaks_echo_sheet)HDΨE Sheets$(/l).$(p)Used in recipes for advanced spell bullets and machinery.");
        add("psi.book.page.psitweaks_material.hypostasis_control_circuit", "Crafted from $(l:components/psitweaks_echo_control_circuit)$(o)$(item)Echo Control Circuit$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)$(o)$(item)Hypostasis Alloy$(0)$(/l), and $(l:components/psitweaks_echo_sheet)HDΨE Sheets$(/l).$(p)Used for the highest machinery.");
        add("psi.book.page.psitweaks_material.echo_pellet", "A compact HDΨE material made from $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l) processing.$(p)Used where recipes need condensed echo material rather than raw $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l). The ΨE-O Fuel produced as a byproduct can be used as fuel for the Gas Generator.");
        add("psi.book.page.psitweaks_material.pellet_neptunium", "Made by mutating a $(item)Polonium Block$(0) with $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)Trick: Material Mutation$(0)$(/l), or by processing it in the $(l:psitweaks_machines/material_mutator)$(o)Material Mutator$(0)$(/l).$(p)Used for the $(l:psitweaks_machines/transcendent_universal_cable)$(o)$(item)Transcendent Universal Cable$(0)$(/l) and the MekaSuit $(item)Phenomenon Interference Enhancement Unit$(0).");
        add("psi.book.page.psitweaks_material.pellet_americium", "Made by mutating a $(item)Plutonium Block$(0) with $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)Trick: Material Mutation$(0)$(/l), or by processing it in the $(l:psitweaks_machines/material_mutator)$(o)Material Mutator$(0)$(/l).$(p)Used for Psycheonic Metal CAD Assemblies and other late-game psionic recipes.");
        add("psi.book.page.psitweaks_material.echo_sheet", "Made by enriching HDΨE material into a sheet form.$(p)Used heavily in Echo and Hypostasis Control Circuit recipes.");
        add("psi.book.page.psitweaks_material.magicians_brain", "Dropped when a Magician villager is killed by $(l:psitweaks_spell_pieces/trick_guillotine)$(o)Trick: Guillotine$(0)$(/l).$(p)Used for the $(l:items/psitweaks_sorcery_booster)$(o)$(item)Sorcery Booster$(0)$(/l) and the Phenomenon Interference Enhancement Unit module.");
        add("psi.book.page.psitweaks_material.jade", "Made by applying material mutation to an $(item)Emerald Block$(0), either with the spell trick or the $(l:psitweaks_machines/material_mutator)Material Mutator$(/l).$(p)Used as the main material for crafting $(l:components/psitweaks_magatama)$(o)$(item)Magatama$(0)$(/l).");
        add("psi.book.page.psitweaks_material.hypostasis_gem", "Made by applying material mutation to an $(item)Antinite Block$(0), either with the spell trick or the $(l:psitweaks_machines/material_mutator)Material Mutator$(/l).$(p)It can be enriched and converted into Hypostasis infusion, and is also used for $(l:components/psitweaks_magatama)$(o)$(item)Magatama$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)Hypostasis Alloy$(/l), and the $(l:components/psitweaks_psycheonic_metal_ingot)Psycheonic Metal chain$(/l).");
        add("psi.book.page.psitweaks_material.magatama", "Crafted from $(l:components/psitweaks_jade)$(o)$(item)Jade$(0)$(/l) surrounding a $(l:components/psitweaks_hypostasis_gem)$(o)$(item)Hypostasis Gem$(0)$(/l).$(p)Used in $(l:psitweaks_machines/spellmachinery_casing)Spellmachinery Casings$(/l), making it a bridge between material mutation and advanced psionic machinery.");
        add("psi.book.page.psitweaks_material.unrefined_flashmetal", "Crafted from $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)Chaotic Psimetal$(0)$(/l) and Refined Glowstone.$(p)Enrich it to obtain usable $(l:components/psitweaks_flashmetal)$(o)$(item)Flashmetal$(0)$(/l).");
        add("psi.book.page.psitweaks_material.flashmetal", "Made by enriching $(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)Unrefined Flashmetal$(0)$(/l).$(p)Used for Flashmetal blocks, Flashmetal CAD assemblies, $(l:components/psitweaks_heavy_psimetal)Heavy Psimetal$(/l), and sublimated-tier spell bullets.");
        add("psi.book.page.psitweaks_material.heavy_psimetal_scrap", "Made by infusing Netherite Scrap with $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l).$(p)Used with $(l:components/psitweaks_flashmetal)$(o)$(item)Flashmetal$(0)$(/l) to craft $(l:components/psitweaks_heavy_psimetal)$(o)$(item)Heavy Psimetal$(0)$(/l).");
        add("psi.book.page.psitweaks_material.heavy_psimetal", "Crafted from $(l:components/psitweaks_heavy_psimetal_scrap)$(o)$(item)Heavy Psimetal Scrap$(0)$(/l) and $(l:components/psitweaks_flashmetal)$(o)$(item)Flashmetal$(0)$(/l).$(p)Used for stronger CAD assemblies, gear, and the $(l:components/psitweaks_psycheonic_metal_ingot)Psycheonic Metal chain$(/l).");
        add("psi.book.page.psitweaks_material.psycheonic_metal_ingot", "Infuse $(l:components/psitweaks_heavy_psimetal)$(o)$(item)Heavy Psimetal$(0)$(/l) with Hypostasis to make $(item)Psycheonic Metal Nuggets$(0), then craft nine nuggets into an ingot.$(p)Used for $(l:items/psitweaks_spell_bullets)transcendent spell bullets$(/l), Psycheonic Metal CAD assemblies, and the highest psionic material tier.");
        add("psi.book.page.psitweaks_material.antinite_ingot", "Smelted from Antinite ore or recovered from Antinite blocks. The dust, shard, clump, crystal, and dirty dust forms are intermediate products in Mekanism-style ore processing.$(p)Used as a material for $(l:components/psitweaks_hypostasis_gem)Hypostasis Gems$(/l) and the $(l:items/psitweaks_philosophers_stone)$(o)$(item)Philosopher's Stone$(0)$(/l).");
        add("psi.book.category.psitweaks_spell_pieces", "Additional Spell Pieces");
        add("psi.book.category.psitweaks_spell_pieces.desc", "Spell pieces added by PsiTweaks. Many are offensive, industrial, or utility-focused, and some require research depending on the config.");
        add("psi.book.page.psitweaks_spellpiece.trick_explode_no_destroy", "Creates an explosion that deals damage without destroying blocks. Be careful: dropped items and similar entities can still be erased.");
        add("psi.book.page.psitweaks_spellpiece.trick_barrier", "Applies a barrier effect that reduces incoming damage. It reduces damage taken by (level * 4).");
        add("psi.book.page.psitweaks_spellpiece.trick_hardening", "Applies a hardening effect that limits large incoming damage to a fixed value. Maximum damage taken is capped at (level - 2).");
        add("psi.book.page.psitweaks_spellpiece.trick_parade", "Applies an effect that evades attacks by chance. It evades attacks with a (62.5 + 7.5 * level)% chance.");
        add("psi.book.page.psitweaks_spellpiece.trick_flight", "Gives the target an effect that enables creative flight.");
        add("psi.book.page.psitweaks_spellpiece.trick_interact_block", "Acts on the target block as if right-clicked with the item in the caster's off hand.");
        add("psi.book.page.psitweaks_spellpiece.trick_freeze_block", "Freezes the target block one stage. Water becomes ice, ice becomes packed ice, packed ice becomes blue ice, lava becomes magma block, and magma block becomes obsidian.");
        add("psi.book.page.psitweaks_spellpiece.trick_melt_block", "Melts the target block one stage. Ice, packed ice, and blue ice become water; obsidian, stone-like blocks, and cobblestone-like blocks become magma block; and magma block becomes lava.");
        add("psi.book.page.psitweaks_spellpiece.trick_break_fortune", "Breaks the target block with Fortune.");
        add("psi.book.page.psitweaks_spellpiece.trick_break_silk", "Breaks the target block with Silk Touch.");
        add("psi.book.page.psitweaks_spellpiece.trick_store_entity", "Stores the target entity's UUID as a String value in CAD memory.");
        add("psi.book.page.psitweaks_spellpiece.selector_stored_entity", "Gets an entity from the UUID stored in CAD memory.");
        add("psi.book.page.psitweaks_spellpiece.selector_nearby_spellgram", "Gets SpellGram objects around the specified coordinates. It is mainly used by tricks that control placed SpellGram objects.");
        add("psi.book.page.psitweaks_spellpiece.trick_dispel", "Removes effects from the target entity. This is the general-purpose dispel that does not distinguish between beneficial and harmful effects.");
        add("psi.book.page.psitweaks_spellpiece.trick_dispel_beneficial", "Removes only beneficial effects from the target entity. It is suited for stripping enhancements from hostile targets.");
        add("psi.book.page.psitweaks_spellpiece.trick_dispel_non_beneficial", "Removes non-beneficial effects from the target entity. Use it when you want to cleanse harmful effects while leaving allied buffs intact.");
        add("psi.book.page.psitweaks_spellpiece.trick_cocytus", "Permanently freezes the mind of the target mob. Rather than merely dealing damage, this very powerful control trick prevents action.");
        add("psi.book.page.psitweaks_spellpiece.trick_supply_fe", "Supplies FE to the target block. When CAD Efficiency is 100, it supplies 20 FE per Psi.");
        add("psi.book.page.psitweaks_spellpiece.trick_time_accelerate", "Multiplies the target block's tick progression by (2 ^ power). The upper limit is 512x speed.");
        add("psi.book.page.psitweaks_spellpiece.trick_phonon_maser", "Fires a high-power heat ray using ultrasonic vibration. It is a powerful offensive trick.");
        add("psi.book.page.psitweaks_spellpiece.trick_meteor_line", "Creates a beam from the specified position in the direction of the Ray vector, dealing special and lethally massive damage to living beings along its path.");
        add("psi.book.page.psitweaks_spellpiece.trick_supreme_infusion", "Infusion-converts Echo Shards into Psionic Echo.");
        add("psi.book.page.psitweaks_spellpiece.trick_molecular_divider", "Cuts living beings along a plane defined by three points. It is a high-power area attack trick.");
        add("psi.book.page.psitweaks_spellpiece.trick_aqua_cutter", "Fires a water-blade projectile forward as an early-game offensive trick.");
        add("psi.book.page.psitweaks_spellpiece.trick_blaze_ball", "Fires a ball of flame forward as an early-game offensive trick.");
        add("psi.book.page.psitweaks_spellpiece.trick_active_air_mine", "Creates a spherical shockwave at the specified coordinates and damages living beings within range. This is an area attack detonated at a chosen location.");
        add("psi.book.page.psitweaks_spellpiece.trick_flare_circle", "Places a fire SpellGram Circle that continuously deals fire damage to living beings inside it. Once placed, the circle remains for 60 seconds.");
        add("psi.book.page.psitweaks_spellpiece.trick_ice_circle", "Places an ice SpellGram Circle that continuously deals freezing damage to living beings inside it. Like Fire Circle, it is suited for area control.");
        add("psi.book.page.psitweaks_spellpiece.trick_set_spellgram_follow_target", "Sets the follow target entity of a SpellGram object.");
        add("psi.book.page.psitweaks_spellpiece.trick_die_flex", "Behaves similarly to Psi's Trick: Die and refunds the Psi cost of spell pieces that were not executed. With high-frequency casting, the client-side Psi display may temporarily desync.");
        add("psi.book.page.psitweaks_spellpiece.trick_radiation_injection", "Applies Mekanism radiation exposure to the target.");
        add("psi.book.page.psitweaks_spellpiece.trick_radiation_filter", "Applies a radiation protection effect to the target, protecting them from radiation.");
        add("psi.book.page.psitweaks_spellpiece.trick_cure_radiation", "Removes the target's radiation exposure.");
        add("psi.book.page.psitweaks_spellpiece.trick_guillotine", "Deals powerful slash damage to the target and makes it drop a head when killed. This is a single-target offensive trick.");
        add("psi.book.page.psitweaks_spellpiece.trick_material_mutation", "Breaks specific blocks and transmutes them into other items. The Material Mutator can perform this process using power and Vaporized Psionic Echo.");
        add("psi.book.page.psitweaks_spellpiece.operator_tan", "Returns the tangent of the target number.");
        add("psi.book.page.psitweaks_spellpiece.operator_atan", "Returns the arc tangent of the target number.");
        add("psi.book.page.psitweaks_spellpiece.operator_sinh", "Returns the hyperbolic sine of the target number.");
        add("psi.book.page.psitweaks_spellpiece.operator_cosh", "Returns the hyperbolic cosine of the target number.");
        add("psi.book.page.psitweaks_spellpiece.operator_tanh", "Returns the hyperbolic tangent of the target number.");
    }

    private void addJapaneseTranslations() {
        add("item.psitweaks.moval_suit_helmet", "M.O.V.A.L. スーツヘルメット");
        add("item.psitweaks.moval_suit_chestplate", "M.O.V.A.L. スーツチェストプレート");
        add("item.psitweaks.moval_suit_leggings", "M.O.V.A.L. スーツレギンス");
        add("item.psitweaks.moval_suit_boots", "M.O.V.A.L. スーツブーツ");
        add("item.psitweaks.moval_suit_leggings_ivory", "M.O.V.A.L. スーツレギンス");
        add("item.psitweaks.flash_ring", "フラッシュリング");
        add("item.psitweaks.flash_charm", "フラッシュチャーム");
        add("item.psitweaks.psimetal_bow", "サイメタルの弓");
        add("item.psitweaks.curios_controller", "キュリオスコントローラ");
        add("item.psitweaks.enriched_psigem", "濃縮サイジェム");
        add("item.psitweaks.enriched_ebony", "濃縮エボニー");
        add("item.psitweaks.enriched_ivory", "濃縮アイボリー");
        add("item.psitweaks.enriched_echo", "濃縮エコー ");
        add("item.psitweaks.enriched_hypostasis", "濃縮ヒュポスタシスジェム");
        add("item.psitweaks.psionic_echo", "サイオニックエコー");
        add("item.psitweaks.program_blank", "空白のプログラム");
        add("item.psitweaks.program_cocytus", "プログラム: コキュートス");
        add("item.psitweaks.program_time_accelerate", "プログラム: 時間加速");
        add("item.psitweaks.program_flight", "プログラム: 飛行");
        add("item.psitweaks.program_phonon_maser", "プログラム: フォノンメーザー");
        add("item.psitweaks.program_meteor_line", "プログラム: 流星群");
        add("item.psitweaks.program_supreme_infusion", "プログラム: 超位注入");
        add("item.psitweaks.program_molecular_divider", "プログラム: 分子ディバイダー");
        add("item.psitweaks.program_radiation_injection", "プログラム: 放射線注入");
        add("item.psitweaks.program_radiation_filter", "プログラム: 放射線フィルタ");
        add("item.psitweaks.program_cure_radiation", "プログラム: 放射線除去");
        add("item.psitweaks.program_guillotine", "プログラム: ギロチン");
        add("item.psitweaks.program_active_air_mine", "プログラム: 能動空中機雷");
        add("item.psitweaks.program_die_flex", "プログラム: 柔軟停止");
        add("item.psitweaks.program_material_mutation", "プログラム: 物質変成");
        add("item.psitweaks.alloy_psion", "サイオニック合金");
        add("item.psitweaks.psionic_control_circuit", "サイオニック制御回路");
        add("item.psitweaks.echo_control_circuit", "感応制御回路");
        add("item.psitweaks.alloy_psionic_echo", "感応合金");
        add("item.psitweaks.alloy_hypostasis", "位格合金");
        add("item.psitweaks.hypostasis_control_circuit", "位格制御回路");
        add("item.psitweaks.philosophers_stone", "賢者の石");
        add("item.psitweaks.heavy_psimetal_scrap", "ヘビーサイメタルの欠片");
        add("item.psitweaks.echo_pellet", "HDΨEペレット");
        add("item.psitweaks.pellet_neptunium", "ネプツニウムペレット");
        add("item.psitweaks.pellet_americium", "アメリシウムペレット");
        add("item.psitweaks.hypostasis_gem", "ヒュポスタシスジェム");
        add("item.psitweaks.jade", "翡翠");
        add("item.psitweaks.magatama", "勾玉");
        add("item.psitweaks.echo_sheet", "HDΨEシート");
        add("item.psitweaks.magicians_brain", "魔法師の脳");
        add("item.psitweaks.psionic_factor", "サイオニック因子");
        add("item.psitweaks.psionic_factor_ivory", "偏陽サイオニック因子");
        add("item.psitweaks.psionic_factor_ebony", "偏陰サイオニック因子");
        add("item.psitweaks.chaotic_factor", "カオティック因子");
        add("item.psitweaks.chaotic_psimetal", "カオティックサイメタルインゴット");
        add("item.psitweaks.flashmetal", "フラッシュメタルインゴット");
        add("item.psitweaks.unrefined_flashmetal", "未精製フラッシュメタル");
        add("item.psitweaks.heavy_psimetal", "ヘビーサイメタルインゴット");
        add("item.psitweaks.raw_antinite", "アンティナイトの原石");
        add("item.psitweaks.shard_antinite", "アンティナイトの欠片");
        add("item.psitweaks.crystal_antinite", "アンティナイトの結晶");
        add("item.psitweaks.clump_antinite", "アンティナイトの凝塊");
        add("item.psitweaks.dirty_dust_antinite", "汚れたアンティナイトの粉");
        add("item.psitweaks.antinite_dust", "アンティナイトの粉");
        add("item.psitweaks.antinite_ingot", "アンティナイトインゴット");
        add("item.psitweaks.psycheonic_metal_ingot", "プシオニックメタルインゴット");
        add("item.psitweaks.psycheonic_metal_nugget", "プシオニックメタルナゲット");
        add("item.psitweaks.cad_assembly_alloy_psion", "サイオニック合金のCAD素体");
        add("item.psitweaks.cad_assembly_chaotic_psimetal", "カオティックサイメタルのCAD素体");
        add("item.psitweaks.cad_assembly_flashmetal", "フラッシュメタルのCAD素体");
        add("item.psitweaks.cad_assembly_heavy_psimetal_alpha", "ヘビーサイメタルのCAD素体 α型");
        add("item.psitweaks.cad_assembly_heavy_psimetal_beta", "ヘビーサイメタルのCAD素体 β型");
        add("item.psitweaks.cad_assembly_psycheonic_metal", "プシオニックメタルのCAD素体");
        add("item.psitweaks.incomplete_heavy_psimetal_assembly", "未完成ヘビーサイメタルCAD素体");
        add("item.psitweaks.portable_cad_assembler", "携帯型CAD組立機");
        add("item.psitweaks.auto_caster_tick", "術式自動詠唱デバイス: tick");
        add("item.psitweaks.auto_caster_second", "術式自動詠唱デバイス: セカンド");
        add("item.psitweaks.auto_caster_custom_tick", "術式自動詠唱デバイス: カスタムtick");
        add("item.psitweaks.interference_range_extender", "干渉距離延長デバイス");
        add("item.psitweaks.third_eye_device", "サードアイデバイス");
        add("item.psitweaks.sorcery_booster", "ソーサリーブースター");
        add("item.psitweaks.spell_magazine", "スペルマガジン");
        add("item.psitweaks.inline_caster", "インラインキャスター");
        add("item.psitweaks.secondary_caster", "セカンダリキャスター");
        add("item.psitweaks.parallel_caster", "パラレルキャスター");
        add("item.psitweaks.advanced_spell_bullet", "改良術式弾");
        add("item.psitweaks.resonant_spell_bullet", "共鳴術式弾");
        add("item.psitweaks.sublimated_spell_bullet", "昇華術式弾");
        add("item.psitweaks.awakened_spell_bullet", "覚醒術式弾");
        add("item.psitweaks.transcendent_spell_bullet", "超越術式弾");
        add("death.attack.psitweaks.meteor_line", "%1$sは光に透過された");
        add("death.attack.psitweaks.meteor_line.player", "%1$sは光に透過された");
        add("death.attack.psitweaks.meteor_line.item", "%1$sは光に透過された");
        add("item.psitweaks.advanced_spell_bullet_projectile", "発射型改良術式弾");
        add("item.psitweaks.resonant_spell_bullet_projectile", "発射型共鳴術式弾");
        add("item.psitweaks.sublimated_spell_bullet_projectile", "発射型昇華術式弾");
        add("item.psitweaks.awakened_spell_bullet_projectile", "発射型覚醒術式弾");
        add("item.psitweaks.transcendent_spell_bullet_projectile", "発射型超越術式弾");
        add("item.psitweaks.advanced_spell_bullet_loop", "ループ型改良術式弾");
        add("item.psitweaks.resonant_spell_bullet_loop", "ループ型共鳴術式弾");
        add("item.psitweaks.sublimated_spell_bullet_loop", "ループ型昇華術式弾");
        add("item.psitweaks.awakened_spell_bullet_loop", "ループ型覚醒術式弾");
        add("item.psitweaks.transcendent_spell_bullet_loop", "ループ型超越術式弾");
        add("item.psitweaks.advanced_spell_bullet_charge", "チャージ型改良術式弾");
        add("item.psitweaks.resonant_spell_bullet_charge", "チャージ型共鳴術式弾");
        add("item.psitweaks.sublimated_spell_bullet_charge", "チャージ型昇華術式弾");
        add("item.psitweaks.awakened_spell_bullet_charge", "チャージ型覚醒術式弾");
        add("item.psitweaks.transcendent_spell_bullet_charge", "チャージ型超越術式弾");
        add("item.psitweaks.advanced_spell_bullet_mine", "地雷型改良術式弾");
        add("item.psitweaks.resonant_spell_bullet_mine", "地雷型共鳴術式弾");
        add("item.psitweaks.sublimated_spell_bullet_mine", "地雷型昇華術式弾");
        add("item.psitweaks.awakened_spell_bullet_mine", "地雷型覚醒術式弾");
        add("item.psitweaks.transcendent_spell_bullet_mine", "地雷型超越術式弾");
        add("item.psitweaks.advanced_spell_bullet_grenade", "榴弾型改良術式弾");
        add("item.psitweaks.resonant_spell_bullet_grenade", "榴弾型共鳴術式弾");
        add("item.psitweaks.sublimated_spell_bullet_grenade", "榴弾型昇華術式弾");
        add("item.psitweaks.awakened_spell_bullet_grenade", "榴弾型覚醒術式弾");
        add("item.psitweaks.transcendent_spell_bullet_grenade", "榴弾型超越術式弾");
        add("item.psitweaks.advanced_spell_bullet_circle", "円形改良術式弾");
        add("item.psitweaks.resonant_spell_bullet_circle", "円形共鳴術式弾");
        add("item.psitweaks.sublimated_spell_bullet_circle", "円形昇華術式弾");
        add("item.psitweaks.awakened_spell_bullet_circle", "円形覚醒術式弾");
        add("item.psitweaks.transcendent_spell_bullet_circle", "円形超越術式弾");
        add("block.psitweaks.cad_disassembler", "CAD分解台");
        add("block.psitweaks.program_researcher", "プログラム研究台");
        add("block.psitweaks.ore_antinite", "アンティナイト鉱石");
        add("block.psitweaks.antinite_block", "アンティナイトブロック");
        add("block.psitweaks.chaotic_psimetal_block", "カオティックサイメタルブロック");
        add("block.psitweaks.flashmetal_block", "フラッシュメタルブロック");
        add("block.psitweaks.heavy_psimetal_block", "ヘビーサイメタルブロック");
        add("block.psitweaks.plutonium_block", "プルトニウムブロック");
        add("block.psitweaks.polonium_block", "ポロニウムブロック");
        add("block.psitweaks.raw_antinite_block", "アンティナイトの原石ブロック");
        add("block.psitweaks.spellmachinery_casing", "魔導機構ケーシング");
        add("block.psitweaks.sculk_eroder", "スカルク侵食室");
        add("block.psitweaks.material_mutator", "物質変成機");
        add("block.psitweaks.psionic_generator", "サイリンク発電機");
        add("block.psitweaks.transcendent_universal_cable", "超越ユニバーサルケーブル");
        add("block.psitweaks.transcendent_energy_cube", "超越エネルギーキューブ");
        add("container.psitweaks.program_researcher", "プログラム研究台");
        add("jei.psitweaks.program_research", "プログラム研究");
        add("jei.psitweaks.material_mutation", "物質変成");
        add("jei.psitweaks.program_research.energy", "消費電力: %s FE");
        add("jei.psitweaks.program_research.time", "研究時間: %s分 %s秒");
        add("message.psitweaks.cad_disassembler.unsupported_tic_cad", "このTiCCADは安全に分解できませんでした");
        add("message.psitweaks.global_traveler.linked", "Global Travelerを座標 (%s, %s, %s) のインベントリにリンクしました");
        add("message.psitweaks.global_traveler.unlinked", "Global Travelerのリンクを解除しました");
        add("message.psitweaks.spell_unlock.cocytus", "作動式: コキュートスを解禁しました");
        add("message.psitweaks.spell_unlock.cocytus.already", "作動式: コキュートスは既に解禁済みです");
        add("message.psitweaks.spell_unlock.unlocked", "%s を解禁しました");
        add("message.psitweaks.spell_unlock.already", "%s は既に解禁済みです");
        add("message.psitweaks.spell_unlock.command.grant.single", "%s の解禁を %s に付与しました");
        add("message.psitweaks.spell_unlock.command.grant.multi", "%s の解禁を %s/%s 人に付与しました");
        add("message.psitweaks.spell_unlock.command.revoke.single", "%s の解禁を %s から剥奪しました");
        add("message.psitweaks.spell_unlock.command.revoke.multi", "%s の解禁を %s/%s 人から剥奪しました");
        add("message.psitweaks.spell_unlock.command.status.unlocked", "%s は %s を解禁済みです");
        add("message.psitweaks.spell_unlock.command.status.locked", "%s は %s が未解禁です");
        add("message.psitweaks.spell_unlock.command.no_change", "%s の解禁状態は変更されませんでした");
        add("message.psitweaks.spell_unlock.command.all.no_change", "全術式の解禁状態は変更されませんでした（対象%s人・術式%s個）");
        add("message.psitweaks.spell_unlock.command.all.grant.single", "%s に全術式の解禁を付与しました（%s/%s 件変更）");
        add("message.psitweaks.spell_unlock.command.all.grant.multi", "全術式の解禁を付与しました（%s/%s 件変更、対象変更%s/%s人）");
        add("message.psitweaks.spell_unlock.command.all.revoke.single", "%s から全術式の解禁を剥奪しました（%s/%s 件変更）");
        add("message.psitweaks.spell_unlock.command.all.revoke.multi", "全術式の解禁を剥奪しました（%s/%s 件変更、対象変更%s/%s人）");
        add("message.psitweaks.spell_unlock.command.all.status", "%s は術式を %s/%s 解禁済みです");
        add("message.psitweaks.spell_unlock.command.cocytus.grant.single", "%s に作動式: コキュートスの解禁を付与しました");
        add("message.psitweaks.spell_unlock.command.cocytus.grant.multi", "%s/%s 人に作動式: コキュートスの解禁を付与しました");
        add("message.psitweaks.spell_unlock.command.cocytus.revoke.single", "%s の作動式: コキュートス解禁を剥奪しました");
        add("message.psitweaks.spell_unlock.command.cocytus.revoke.multi", "%s/%s 人の作動式: コキュートス解禁を剥奪しました");
        add("message.psitweaks.spell_unlock.command.cocytus.status.unlocked", "%s は作動式: コキュートスを解禁済みです");
        add("message.psitweaks.spell_unlock.command.cocytus.status.locked", "%s は作動式: コキュートスが未解禁です");
        add("container.psitweaks.sculk_eroder", "スカルク侵食室");
        add("container.psitweaks.material_mutator", "物質変成機");
        add("container.psitweaks.psionic_generator", "サイリンク発電機");
        add("container.psitweaks.transcendent_energy_cube", "超越エネルギーキューブ");
        add("description.psitweaks.sculk_eroder", "石・土・砂系のブロックをスカルクに加工します");
        add("description.psitweaks.material_mutator", "サイオニックエコーを用いて物質変成を行います。");
        add("description.psitweaks.psionic_generator", "所有者の Psi を消費し、リンク中のみ FE に変換します");
        add("description.psitweaks.transcendent_universal_cable.transfer", "転送容量: %s/t");
        add("description.psitweaks.magicians_brain", "魔法師が苛烈な最期を迎えたとき、その場に遺す。");
        add("effect.psitweaks.parade", "仮想行列");
        add("effect.psitweaks.flight", "飛行");
        add("effect.psitweaks.barrier", "障壁");
        add("effect.psitweaks.hardening", "硬化");
        add("effect.psitweaks.radiation_filter", "放射線フィルタ");
        add("attribute.psitweaks.spell_damage_factor", "術式ダメージ倍率");
        add("attribute.psitweaks.max_psi_bonus", "Psi保有量");
        add("attribute.psitweaks.psi_regen_bonus", "Psi回復速度");
        add("module.psitweaks.psyon_supplying_unit", "サイオン供給ユニット");
        add("description.psitweaks.psyon_supplying_unit", "Psi量回復速度を増加させます。");
        add("module.psitweaks.psyon_capacity_unit", "サイオン容量ユニット");
        add("description.psitweaks.psyon_capacity_unit", "Psi量保有上限を増加させます。");
        add("module.psitweaks.phenomenon_interference_enhancement_unit", "事象干渉力増大ユニット");
        add("description.psitweaks.phenomenon_interference_enhancement_unit", "ユーザーの術式の威力を増大させます。");
        add("psitweaks.event.second", "§bイベント§7: セカンド");
        add("psitweaks.event.custom_tick", "§bイベント§7: カスタムtick");
        add("screen.psitweaks.auto_caster_custom_tick", "オートキャスター設定");
        add("screen.psitweaks.auto_caster_custom_tick.interval", "詠唱間隔 (tick)");
        add("screen.psitweaks.auto_caster_custom_tick.range", "設定可能範囲: %s - %s");
        add("screen.psitweaks.auto_caster_custom_tick.invalid", "%s から %s の間で入力してください");
        add("gui.psitweaks.psionic_generator.toggle", "切替");
        add("gui.psitweaks.psionic_generator.owner", "所有者: %s");
        add("gui.psitweaks.psionic_generator.owner_state", "所有者状態: %s");
        add("gui.psitweaks.psionic_generator.owner_online", "オンライン");
        add("gui.psitweaks.psionic_generator.owner_offline", "オフライン");
        add("gui.psitweaks.psionic_generator.link_state", "リンク: %s");
        add("gui.psitweaks.psionic_generator.link_on", "有効");
        add("gui.psitweaks.psionic_generator.link_off", "無効");
        add("gui.psitweaks.psionic_generator.summary_status", "%1$s / %2$s");
        add("gui.psitweaks.psionic_generator.summary_psi", "Psi: %1$s/%2$s");
        add("gui.psitweaks.psionic_generator.summary_consume", "消費: %s/t");
        add("gui.psitweaks.psionic_generator.summary_generate", "発電: %s /t");
        add("gui.psitweaks.psionic_generator.summary_consume_full", "消費: %s Psi/t");
        add("gui.psitweaks.psionic_generator.summary_generate_full", "発電: %1$s / %2$s /t");
        add("gui.psitweaks.psionic_generator.psi", "Psi: %s / %s");
        add("gui.psitweaks.psionic_generator.consume", "消費: %s Psi/t");
        add("gui.psitweaks.psionic_generator.generate", "発電: %1$s / %2$s /t");
        add("tooltip.psitweaks.auto_caster_custom_tick.interval", "詠唱間隔: %s tick");
        add("tooltip.psitweaks.spell_magazine.bullets", "装填済み術式弾: %s/%s");
        add("gui.psitweaks.apply", "適用");
        add("infuse_type.psitweaks.infuse_psigem", "サイジェム");
        add("infuse_type.psitweaks.infuse_ebony", "エボニー");
        add("infuse_type.psitweaks.infuse_ivory", "アイボリー");
        add("infuse_type.psitweaks.infuse_chaotic_factor", "混沌");
        add("infuse_type.psitweaks.infuse_psionic_echo", "サイオニックエコー");
        add("infuse_type.psitweaks.infuse_hypostasis", "ヒュポスタシスジェム");
        add("gas.psitweaks.gas_psionic_echo", "気化サイオニックエコー");
        add("gas.psitweaks.gas_peo_fuel", "ΨE-O燃料");
        add("gas.psitweaks.gas_americium", "アメリシウム");
        add("gas.psitweaks.gas_neptunium", "ネプツニウム");
        add("slurry.psitweaks.dirty_antinite", "汚れたアンティナイトの懸濁液");
        add("slurry.psitweaks.clean_antinite", "純粋なアンティナイトの懸濁液");
        add("curios.identifier.magic_calculation_area", "魔法演算領域");
        add("psitweaks.spellpiece.trick_explode_no_destroy", "作動式: 対人爆発");
        add("psitweaks.spellpiece.trick_explode_no_destroy.desc", "ブロックを破壊しない爆発を起こす");
        add("psitweaks.spellpiece.trick_parade", "作動式: パレード");
        add("psitweaks.spellpiece.trick_parade.desc", "確率で被ダメージを回避する");
        add("psitweaks.spellpiece.trick_flight", "作動式: 飛行");
        add("psitweaks.spellpiece.trick_flight.desc", "クリエイティブ飛行を可能にする");
        add("psitweaks.spellpiece.trick_barrier", "作動式: 障壁");
        add("psitweaks.spellpiece.trick_barrier.desc", "受けるダメージを減少させる");
        add("psitweaks.spellpiece.trick_hardening", "作動式: 硬化");
        add("psitweaks.spellpiece.trick_hardening.desc", "大ダメージを受けた時、一定値まで減少させる");
        add("psitweaks.spellpiece.trick_interact_block", "作動式: ブロック作用");
        add("psitweaks.spellpiece.trick_interact_block.desc", "座標のブロックに、オフハンドのアイテムで右クリックの動作を行う");
        add("psitweaks.spellpiece.trick_freeze_block", "作動式: ブロック凍結");
        add("psitweaks.spellpiece.trick_freeze_block.desc", "指定座標のブロックを次の凍結段階に変化させる");
        add("psitweaks.spellpiece.trick_melt_block", "作動式: ブロック溶解");
        add("psitweaks.spellpiece.trick_melt_block.desc", "指定座標のブロックを溶解して高温の状態に変化させる");
        add("psitweaks.spellpiece.trick_break_fortune", "作動式: ブロック破壊(幸運)");
        add("psitweaks.spellpiece.trick_break_fortune.desc", "幸運付きでブロックを破壊する");
        add("psitweaks.spellpiece.trick_break_silk", "作動式: ブロック破壊(シルクタッチ)");
        add("psitweaks.spellpiece.trick_break_silk.desc", "シルクタッチ付きでブロックを破壊する");
        add("psitweaks.spellpiece.trick_store_entity", "作動式: エンティティ保存");
        add("psitweaks.spellpiece.trick_store_entity.desc", "エンティティのUUIDをStringとしてCADメモリに保存する");
        add("psitweaks.spellpiece.selector_stored_entity", "取得子: 保存されたエンティティ");
        add("psitweaks.spellpiece.selector_stored_entity.desc", "CADのメモリに保存されたUUIDからエンティティを取得する");
        add("psitweaks.spellpiece.selector_nearby_spellgram", "取得子: 近くの魔法式オブジェクト");
        add("psitweaks.spellpiece.selector_nearby_spellgram.desc", "指定座標の周囲にある魔法式オブジェクトを取得する");
        add("psitweaks.spellparam.spellgram", "魔法式");
        add("psitweaks.spellerror.not_spellgram_object", "エラー: 入力はSpellGramObjectである必要があります");
        add("psitweaks.spellpiece.trick_dispel", "作動式: 解呪");
        add("psitweaks.spellpiece.trick_dispel.desc", "対象からエフェクトを除去する");
        add("psitweaks.spellpiece.trick_dispel_beneficial", "作動式: 良性解呪");
        add("psitweaks.spellpiece.trick_dispel_beneficial.desc", "対象から有益なエフェクトを除去する");
        add("psitweaks.spellpiece.trick_dispel_non_beneficial", "作動式: 悪性解呪");
        add("psitweaks.spellpiece.trick_dispel_non_beneficial.desc", "対象から有益でないエフェクトを除去する");
        add("psitweaks.spellpiece.trick_cocytus", "作動式: コキュートス");
        add("psitweaks.spellpiece.trick_cocytus.desc", "対象のモブの精神を永久に凍結させる");
        add("psitweaks.spellpiece.trick_supply_fe", "作動式: FE供給");
        add("psitweaks.spellpiece.trick_supply_fe.desc", "ブロックにFEを供給します。方向の指定が可能です。");
        add("psitweaks.spellpiece.trick_time_accelerate", "作動式: 時間加速");
        add("psitweaks.spellpiece.trick_time_accelerate.desc", "対象のブロックの時を加速させます。");
        add("psitweaks.spellpiece.trick_phonon_maser", "作動式: フォノンメーザー");
        add("psitweaks.spellpiece.trick_phonon_maser.desc", "超音波を振動させ熱線を放出する");
        add("psitweaks.spellpiece.trick_meteor_line", "作動式: 流星群");
        add("psitweaks.spellpiece.trick_meteor_line.desc", "光を透過させるラインを作り出し経路上の生物を穿つ");
        add("psitweaks.spellpiece.trick_supreme_infusion", "作動式: 超位注入");
        add("psitweaks.spellpiece.trick_supreme_infusion.desc", "残響の欠片に注入してサイオニックエコーにします");
        add("psitweaks.spellpiece.trick_molecular_divider", "作動式: 分子ディバイダー");
        add("psitweaks.spellpiece.trick_molecular_divider.desc", "三点で作られた平面で生物を切断する");
        add("psitweaks.spellpiece.trick_aqua_cutter", "作動式: アクアカッター");
        add("psitweaks.spellpiece.trick_aqua_cutter.desc", "水刃の発射体を前方に放ち、命中した対象にダメージを与える");
        add("psitweaks.spellpiece.trick_blaze_ball", "作動式: ブレイズボール");
        add("psitweaks.spellpiece.trick_blaze_ball.desc", "前方へ火の玉を発射し、命中した対象へ炎属性ダメージを与える");
        add("psitweaks.spellpiece.trick_radiation_injection", "作動式: 放射線注入");
        add("psitweaks.spellpiece.trick_radiation_injection.desc", "対象を被ばくさせる");
        add("psitweaks.spellpiece.trick_radiation_filter", "作動式: 放射線フィルタ");
        add("psitweaks.spellpiece.trick_radiation_filter.desc", "対象に放射線防護効果を付与する");
        add("psitweaks.spellpiece.trick_cure_radiation", "作動式: 放射線除去");
        add("psitweaks.spellpiece.trick_cure_radiation.desc", "対象の被ばく量を除去する");
        add("psitweaks.spellpiece.trick_guillotine", "作動式: ギロチン");
        add("psitweaks.spellpiece.trick_guillotine.desc", "対象に強力な斬撃ダメージを与え、討伐時に頭をドロップさせる");
        add("psitweaks.spellpiece.trick_active_air_mine", "作動式: 能動空中機雷");
        add("psitweaks.spellpiece.trick_active_air_mine.desc", "指定座標を中心とした範囲内の生物にダメージを与える");
        add("psitweaks.spellpiece.trick_flare_circle", "作動式: ファイアサークル");
        add("psitweaks.spellpiece.trick_flare_circle.desc", "炎の魔法式オブジェクトを設置して内部の生物に継続的な炎ダメージを与える");
        add("psitweaks.spellpiece.trick_ice_circle", "作動式: アイスサークル");
        add("psitweaks.spellpiece.trick_ice_circle.desc", "氷の魔法式オブジェクトを設置して内部の生物に継続的な凍結ダメージを与える");
        add("psitweaks.spellpiece.trick_set_spellgram_follow_target", "作動式: 魔法式追従");
        add("psitweaks.spellpiece.trick_set_spellgram_follow_target.desc", "魔法式オブジェクトの追従対象エンティティを設定する");
        add("psitweaks.spellpiece.trick_die_flex", "作動式: 柔軟停止");
        add("psitweaks.spellpiece.trick_die_flex.desc", "絶対値が1未満の数値を受け取ると術式を停止し、未実行分のPsi消費を返却する。毎tick詠唱する術式に組み込むと、クライアント側のPsi量表示が同期ずれする場合があります");
        add("psitweaks.spellpiece.trick_material_mutation", "作動式: 物質変成");
        add("psitweaks.spellpiece.trick_material_mutation.desc", "特定のブロックに作用して物質構造を改変し異なる物質に変成させる");
        add("psitweaks.spellpiece.operator_tan", "演算子: タンジェント");
        add("psitweaks.spellpiece.operator_tan.desc", "tan(A)");
        add("psitweaks.spellpiece.operator_atan", "演算子: アークタンジェント");
        add("psitweaks.spellpiece.operator_atan.desc", "atan(A)");
        add("psitweaks.spellpiece.operator_sinh", "演算子: ハイパボリックサイン");
        add("psitweaks.spellpiece.operator_sinh.desc", "sinh(A)");
        add("psitweaks.spellpiece.operator_cosh", "演算子: ハイパボリックコサイン");
        add("psitweaks.spellpiece.operator_cosh.desc", "cosh(A)");
        add("psitweaks.spellpiece.operator_tanh", "演算子: ハイパボリックタンジェント");
        add("psitweaks.spellpiece.operator_tanh.desc", "tanh(A)");
        add("creativetabs.psitweaks", "Psi: Tweaks and Additions");
        add("psi.book.subtitle", "魔法学入門");
        add("psi.book.name", "サイオニカ魔法大全");
        add("psi.book.landing_text", "$(thing)Psi$(0) はあなたの創意工夫によって限界が決まる$(thing)魔法$(0)作成 mod です. 本書には, 熟練の魔法師になるために必要な知識がすべて収録されています.$(p)(本書は作成中です. 旧チュートリアル記事は「レガシーエントリー」でご覧いただけます.)");
        add("psi.book.category.basics", "基礎");
        add("psi.book.category.basics.desc", "$(thing)Psi$(0)を初めてご利用になる方は, こちらのすべての項目を必ずお読みになることをお勧めします. いずれも重要な情報が含まれています.");
        add("psi.book.entry.introduction", "導入");
        add("psi.book.page.introduction.0", "$(thing)Psi$(0)へようこそ！ 宇宙で最も偉大なスペルプログラムベースの魔法技術MODです！$(p)$(thing)Psi$(0)は（魔法科高校の劣等生シリーズにインスパイアされた）MODで, あなたの命令を実行する$(thing)魔法$(0)$(o)を創造・発動して意のままに操るmodです.$(p)魔法を操る魔法師への道へ踏み出すには, まず$(l:components/psidust)$(o)$(item)サイダスト$(0)$(/l)という素材が必要です――ただし, これを単純にクラフトできるものではありません.");
        add("psi.book.page.introduction.1", "代わりに, まず$(l:basics/cad_assembler)$(o)$(item)CAD組立機(0)$(/l)と$(l:components/assembly#iron)$(o)$(item)鉄のCAD素体$(0)$(/l)を作成してください. (p)$(l:basics/cad_assembler)$(o)$(item)CAD組立機$(0)$(/l)を配置し, 開いて$(l:components/assembly#iron)$(o)$ (item)鉄のCAD素体$(0)$(/l)を挿入し, 非常に簡素な$(thing)術式補助演算機(0)（略して$(thing)CAD$(0)）を構築します. (p)そこから地面に$(item)レッドストーンダスト$(0)を数個落とし, 新しく作成した$(thing)CAD$(0)をダストに向けて, $(k:use)を発動して$(l:components/psidust)$(o)$(item)サイダスト$(0)$(/l)を生成します.");
        add("psi.book.entry.cadAssembler", "CAD組立機");
        add("psi.book.page.cadAssembler.0", "$(item)CAD組立機(0)は$(thing)Psi$(0)の中核であり, 二つの重要な機能を果たします. $(p)第一に, 構成部品から$(thing)CAD$(0)を組み立てます. 次に, $(o)空でない$() $(item)術式弾$(0)を$(thing)CAD$(0)に装填します.（$(l:items/spell_bullet)$(o)$(thing)術式弾$(0)$(/l)を保持する他のアイテム, 例えば$(l:items/tools)$(o)$ (thing)サイメタルツール$(0)$(/l)など）に$(o)空でない$(0)$(item)術式弾$(0)$を装填する.");
        add("psi.book.page.cadAssembler.1", "CAD組立機のクラフト");
        add("psi.book.page.cadAssembler.2", "最も簡素な魔法使用可能CAD");
        add("psi.book.page.cadAssembler.3", "A $(thing)CAD$(0) is built from up to five components; the simplest $(thing)CAD$(0) only uses one component, an $(l:components/assembly)$(o)$(item)Assembly$(0)$(/l), though this is only useful for crafting $(l:components/psidust)$(o)$(item)Psidust$(0)$(/l).$(p)A $(thing)CAD$(0) capable of casting $(thing)Spells$(0) requires a $(l:components/core)$(o)$(item)Core$(0)$(/l) and a $(l:components/socket)$(o)$(item)Socket$(0)$(/l) as well.$(p)Adding a $(l:components/battery)$(o)$(item)Battery$(0)$(/l) slightly increases a user's maximum $(thing)Psi energy$(0), and adding a $(l:components/colorizer)$(o)$(item)Colorizer$(0)$(/l) changes the color of cast $(thing)Spells$(0), which is purely cosmetic.");
        add("psi.book.page.cadAssembler.4", "Once a $(thing)CAD$(0) is created, it can be placed in the leftmost panel of the $(item)CAD Assembler$(0).$(p)When placed there, the slots below open; $(item)Spell Bullets$(0) can be placed in these slots to be loaded into the $(thing)CAD$(0).$(p)Once the $(l:items/spell_bullet)$(o)$(thing)Bullets$(0)$(/l) are loaded, the $(thing)CAD$(0) is removed and held, and the $(thing)Psi master keybind$(0) ($(k:psimisc.keybind)) is held, the $(l:items/spell_bullet)$(o)$(thing)Bullets$(0)$(/l)' $(thing)Spells$(0) will be displayed on a radial menu, ready to be selected and cast.");
        add("psi.book.page.cadAssembler.5", "A CAD with one bullet loaded");
        add("psi.book.entry.spellProgrammer", "Spell Programmer");
        add("psi.book.page.spellProgrammer.0", "If the $(l:basics/cad_assembler)$(o)$(item)CAD Assembler$(0)$(/l) is the heart of $(thing)Psi$(0), then the $(item)Spell Programmer$(0) is the brains of the mod. It's where $(thing)Spells$(0) are written and compiled, and eventually copied into $(item)Spell Bullets$(0) to be cast.$(p)When placed and opened ($(k:use)), it displays a large 9x9 grid; see $(l:basics/tutorial_1)the tutorial entries$(/l) for more in-depth knowledge on using this grid.");
        add("psi.book.page.spellProgrammer.1", "Hour Of Code");
        add("psi.book.entry.vectorPrimer", "A Primer On Vectors");
        add("psi.book.page.vectorPrimer.0", "$(thing)Psi$(0) uses the concept of a Vector extensively. Therefore, if you haven't the foggiest idea what a vector is, I strongly recommend you watch the video below.$(p)The explanation in the next few pages is $(l)simplified$() for beginners. Don't take it as definitive.");
        add("psi.book.page.vectorPrimer.1", "In the world of $(thing)Psi$(0) (and indeed in a $(thing)Minecraft$(0) world), all vectors are three-dimensional. In essence, they're just lists of three coordinates: $(o)x$(), $(o)y$(), $(o)z$().$(p)The $(o)x$()-coordinate represents east when positive and west when negative, the $(o)y$()-direction up and down, and the $(o)z$()-direction south and north.$(p)If this doesn't make sense yet, open the debug screen (F3) and run around, paying attention to the row labeled \"XYZ:\".$(br)You'll understand.");
        add("psi.book.page.vectorPrimer.2", "That list of three numbers on the debug screen is, in fact, the first type of vector you'll meet: a $(l)position vector$().$(p)A position vector simply represents the location of a block, or an entity, or perhaps some empty space in the world. A single fixed location, represented by a list [$(o)x$(), $(o)y$(), $(o)z$()].$(p)However, not all vectors represent positions-- and it's important to note that $(l)any three numbers in a list constitute a vector$().");
        add("psi.book.page.vectorPrimer.3", "An interesting fact about vectors is that they're extremely easy to add.$(p)For example, say we have a grass block at some location, which we choose to represent as [$(o)x$(), $(o)y$(), $(o)z$()].$(p)If we wish to add another vector (e.g. [0, 1, 0]) to this one, all we would have to do is add corresponding numbers:$(br)our vector sum would be [$(o)x$()+0, $(o)y$()+1, $(o)z$()+0], or just [$(o)x$(), $(o)y$()+1, $(o)z$()].");
        add("psi.book.page.vectorPrimer.4", "Since the $(o)y$()-component of our new vector has increased by one, and positive-$(o)y$() means up, this new vector simply represents the block above our grass block.$(p)The vector [0, 1, 0] represents the $(o)difference$() in position between our original and new vector, and it's our second type of vector: an $(l)offset vector$().");
        add("psi.book.page.vectorPrimer.5", "Offset vectors are what most spellslingers spend the majority of their time manipulating, so a mastery over them is key.$(p)Usually, a mage starts with a single position vector, then adds, subtracts, or otherwise combines offset vectors with this position vector in order to target their desired point in the world.");
        add("psi.book.page.vectorPrimer.6", "It's important to note that the idea \"offset\" and \"position\" vectors is conceptual; the terms are unique to the terminology of this tablet.$(p)Again: all $(thing)Psi$(0) vectors are just lists of three numbers, and there's nothing stopping you from pretending the distinction doesn't exist.$(p)Indeed, in some contexts outside the scope of this book, the distinction doesn't even make $(o)sense$().");
        add("psi.book.page.vectorPrimer.7", "Offset vectors have $(l)magnitudes$().$(p)You can think of an offset vector's magnitude as its \"length,\" or the distance between a position in the world, and that same position when the offset vector is added to it.$(p)For example, our earlier offset vector [0, 1, 0] simply moved the position one block-length up, so it had a length, and therefore a magnitude, of 1.");
        add("psi.book.page.vectorPrimer.8", "Since a distance is always positive, so are vectors' magnitudes.$(p)Consider the vector [0, -3, 0] as an example: it represents down, three blocks-- yet the total distance moved is three blocks, and the \"down\" bit doesn't matter.$(p)Therefore, the magnitude of this vector is $(l)positive$() 3.");
        add("psi.book.page.vectorPrimer.9", "Almost all vectors also have $(l)directions$().$(p)An offset vector's direction is, well, the direction something would move if it followed the vector in a straight line.$(p)For example, the direction of [0, 1, 0] is simply straight up.$(p)The vector [1, 0, -1], on the other hand, represents one block east and one block north, so its direction is just straight ahead, due northeast.");
        add("psi.book.page.vectorPrimer.10", "(Most directions aren't so nice, usually looking like \"36.86 degrees north of west, 22.62 degrees below the horizon.\")$(p)Note that the only vector without a direction is [0, 0, 0] (the $(l)zero vector$()), since you have to be going somewhere $(o)else$() to have a direction.$(p)Note that the direction of a position vector is almost as meaningless as its magnitude-- most $(thing)Spells$(0) don't need to know \"where should I go to get away from bedrock at world spawn.\"");
        add("psi.book.page.vectorPrimer.11", "As a matter of fact, you can reconstruct any vector, given only its magnitude and direction, into a list of three numbers (which are known as the $(l)components$() of the vector).$(p)For example, the direction \"up\" and the magnitude 1 correspond to the vector [0, 1, 0].$(p)This isn't as surprising as it may seem: after all, if someone tells you which direction to go, and how far, you should know they want you to be.");
        add("psi.book.page.vectorPrimer.12", "There are several simple ways to manipulate position and offset vectors.$(p)First, we can add a position and an offset vector to get another position vector, as we did earlier with the grass block example.$(p)On the other hand, we can of course $(o)subtract$() two position vectors to get the offset vector representing the offset from one to the other:$(br)[$(o)x$(), $(o)y$()+1, $(o)z$()] − [$(o)x$(), $(o)y$(), $(o)z$()] = [0, 1, 0].");
        add("psi.book.page.vectorPrimer.13", "Perhaps more interestingly, we can add two offset vectors, to get a single offset representing their combination.$(p)Adding this offset to a position vector would be equivalent to first adding one of its components to that position vector, then adding the other.");
        add("psi.book.page.vectorPrimer.14", "And last, but most certainly not least, of the simple operations: we can $(l)scale$() a vector, by multiplying it by a number.$(p)Note that we're multiplying it by a number, and $(o)not$() another vector.$(p)If we wish to scale an vector [$(o)p$(), $(o)q$(), $(o)r$()] by a factor $(o)n$(), we simply multiply each of the vector's components by $(o)n$():$(br)$(o)n$()·[$(o)p$(), $(o)q$(), $(o)r$()] = [$(o)n$()·$(o)p$(), $(o)n$()·$(o)q$(), $(o)n$()·$(o)r$()].");
        add("psi.book.page.vectorPrimer.15", "This final operation relates nicely indeed to the concepts of magnitude and direction.$(p)When you scale a vector by a number $(o)n$(), you:$(li)multiply its magnitude by the absolute value of $(o)n$(), and$(li)don't change its direction if $(o)n$() is positive, but reverse its direction if $(o)n$() is negative.$(p)If $(o)n$()=0, then of course the resulting vector is the zero vector.");
        add("psi.book.page.vectorPrimer.16", "On the other hand, if we set $(o)n$()=-1, then we get a vector with the same magnitude (the absolute value of -1 is 1), but pointing the opposite way (since -1 is negative)!$(p)This vector is known as the $(l)negative$() of the original, and when the two are added we get the zero vector.$(p)This makes sense, since if we go in a direction, then go in the opposite direction for the same distance, our net movement is zero.");
        add("psi.book.page.vectorPrimer.17", "If, instead of multiplying, we $(o)divide$() a (nonzero) vector by its magnitude, we get a vector with magnitude 1 (since anything divided by itself is 1), but the same direction (since magnitudes are always positive).$(p)This is an important and well-known operation, known as $(l)normalizing$() a vector; the vector that results (and, in fact, any vector with magnitude 1) is called a $(l)unit vector$().");
        add("psi.book.page.vectorPrimer.18", "Unit vectors have a fixed magnitude, so they only represent direction.$(p)Many $(thing)Spell Pieces$(0) related to direction return unit vectors, like $(piece)Operator: Vector Axis Raycast$(0) and $(piece)Operator: Entity Look$(0).$(p)Indeed, there are $(thing)Operators$(0) to do $(o)most$() of the vector operations laid out in this article, usually with self-explanatory names.");
        add("psi.book.page.vectorPrimer.19", "The operations and their corresponding $(thing)Operators$(0) are as follows:$(li)Negating is $(piece)Operator: Vector Negate$(0);$(li)Normalizing is $(piece)Operator: Vector Normalize$(0);$(li)Scaling is $(piece)Operator: Vector Multiply$(0) and $(piece)Operator: Vector Divide$(0);$(li)Taking the magnitude is $(piece)Operator: Vector Magnitude$(0);$(li)Adding is $(piece)Operator: Vector Sum$(0);$(li)Subtracting is $(piece)Operator: Vector Subtract$(0).");
        add("psi.book.page.vectorPrimer.20", "Finally: vectors are still lists of three numbers. Don't lose sight of that.$(p)In a $(l:basics/spell_programmer)$(o)$(item)Spell Programmer$(0)$(/l), they can be constructed from up to three numbers with $(piece)Operator: Vector Construct$(0).$(p)Conversely, a vector can also be broken back down into numbers with $(piece)Operator: Extract X$(0), $(piece)Operator: Extract Y$(0), and $(piece)Operator: Extract Z$(0).");
        add("psi.book.page.vectorPrimer.21", "Congratulations on making it through this tutorial!$(p)Again, this is just an introduction to vectors-- I've not said anything about dot or cross products, or vector projections, for example.$(p)But this should be more than enough to put together some quite interesting $(thing)Spells$(0) already.$(p)That's all-- now go forth and spellsling!");
        add("psi.book.entry.tutorial1", "Tutorial (1): Writing A Spell");
        add("psi.book.category.psitweaks", "PsiTweaks");
        add("psi.book.category.psitweaks.desc", "$(thing)PsiTweaks$(0) は, 高位術式弾, CAD部品, 機械, Mekanism連携, そして終盤向けのサイオニック素材を追加して, $(thing)Psi$(0) の進行を拡張します.");
        add("psi.book.category.psitweaks_machines", "機械");
        add("psi.book.category.psitweaks_machines.desc", "PsiTweaks が追加する機械ブロックと基盤設備です.");
        add("psi.book.entry.psitweaks_overview", "PsiTweaksの追加要素");
        add("psi.book.page.psitweaks_overview.0", "$(thing)PsiTweaks$(0) は $(thing)Psi$(0) の拡張modです. 素材生成の工業化や, 新しい装備, 新しい術式など多数の要素を追加します.$(p)通常のCADや基本術式弾で終わらず, Psiを戦闘やユーティリティとして終盤まで強力に扱う構成を想定しています.");
        add("psi.book.page.psitweaks_overview.2", "主な追加要素は, 高位術式弾, 新しいCAD素体や詠唱補助具,新しい魔法, 加工機械や発電機です.$(p)多くのシステムは, Psiと工業環境の橋渡しや, Psiの終盤での能力強化などを目的として作られています.");
        add("psi.book.entry.psitweaks_changes", "PsiTweaksによる変更");
        add("psi.book.page.psitweaks_changes.0", "$(thing)Psi: Tweaks And Additions$(0) を導入することによって QOL を改善するためのいくつかの調整が加えられます.$(p)ダメージを受けた際に Psi量 が減少しなくなり, 詠唱時の Psi回復クールタイムも撤廃されます.$(p)スペルプログラム画面では, 現在の設定言語に関わらず英語でスペルピースを検索することができます.");
        add("psi.book.entry.psitweaks_research", "研究");
        add("psi.book.page.psitweaks_research.0", "$(thing)PsiTweaks$(0) が追加する一部のスペルピースは, 術式プログラムで使う前に研究によるアンロックが必要です.$(p)研究では, それらのスペルピースに対応するプログラムアイテムを作成します. プログラムを右クリックで使用することでスペルピースをアンロックできます(プログラムは消費しません).");
        add("psi.book.page.psitweaks_research.1", "$(l:psitweaks_machines/program_researcher)$(o)$(item)プログラム研究台$(0)$(/l) に必要素材を入れて FE を供給すると, プログラムをクラフトできます.$(p)各研究に必要な素材, 消費電力, 処理時間は JEI で確認できます.");
        add("psi.book.entry.psitweaks_magician", "魔法師");
        add("psi.book.page.psitweaks_magician.0", "$(thing)魔法師$(0) は PsiTweaks が追加する村人の職業です. Psi機械を扱う村人であり, 魔法師向けの素材や強化装備と関係します.");
        add("psi.book.page.psitweaks_magician.1", "無職の村人は, $(l:basics/cad_assembler)$(o)$(item)CAD組立機$(0)$(/l) を職業ブロックとして取得すると $(thing)魔法師$(0) になります.$(p)ほかの村人作業台と同じように, 村人が到達できる場所にCAD組立機を置いてください.");
        add("psi.book.page.psitweaks_magician.2", "魔法師は $(l:components/psitweaks_magicians_brain)$(o)$(item)魔法師の脳$(0)$(/l) の入手元でもあります. 魔法師の村人を $(l:psitweaks_spell_pieces/trick_guillotine)$(o)作動式: ギロチン$(0)$(/l) で倒したときにドロップし, 魔法師向けのレシピに使います.");
        add("psi.book.entry.psitweaks_mekanism_integration", "Mekanism連携");
        add("psi.book.title.psitweaks_mekanism_integration.infusing", "冶金吹き込み");
        add("psi.book.title.psitweaks_mekanism_integration.mekasuit", "MekaSuitモジュール");
        add("psi.book.page.psitweaks_mekanism_integration.0", "$(thing)PsiTweaks$(0) は $(thing)Psi$(0) を $(thing)Mekanism$(0) の進行へ接続します. 連携要素には, Psi素材を作る冶金吹き込みレシピ, 魔法師能力を伸ばすMekaSuitモジュール, Psi・FE・サイオニック資源を扱う機械があります.$(p)正確なレシピはJEIで確認してください. この項目では各要素の役割をまとめます.");
        add("psi.book.page.psitweaks_mekanism_integration.1", "$(item)冶金吹き込み機$(0) では, PsiTweaks の注入タイプを使って主要なPsi素材を作れます. サイジェム注入はレッドストーンを$(l:components/psidust)$(o)$(item)サイダスト$(0)$(/l)に, 金インゴットを$(item)サイメタル$(0)に変換します.$(p)エボニー注入とアイボリー注入では, $(item)エボニー基質$(0), $(item)アイボリー基質$(0), それぞれのサイメタル派生素材を作れます. さらに$(l:components/psitweaks_chaotic_factor)サイオニック因子$(/l), $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)カオティックサイメタル$(0)$(/l), $(l:components/psitweaks_psionic_echo)サイオニックエコー素材$(/l), $(l:components/psitweaks_alloy_hypostasis)位格合金$(/l)へ発展します.");
        add("psi.book.page.psitweaks_mekanism_integration.2", "PsiTweaks は魔法師向けのMekaSuitモジュールを3種類追加します. $(item)サイオン供給ユニット$(0) と $(item)サイオン容量ユニット$(0) はMekaSuit胴体に装着し, Psi回復速度と最大Psi保有量を強化します.$(p)$(item)事象干渉力増大ユニット$(0) はMekaSuitヘルメットに装着し, 術式ダメージを増加させます.");
        add("psi.book.entry.psitweaks_spell_bullets", "強化術式弾");
        add("psi.book.page.psitweaks_spell_bullets.0", "$(thing)PsiTweaks$(0) は通常の$(l:items/spell_bullet)$(o)$(item)術式弾$(0)$(/l)より上位の強化ティアとして, 改良, 共鳴, 昇華, 覚醒, 超越を追加します.$(p)Psiの術式弾のバリエーション全てにそれぞれ上位Tierが用意されています.");
        add("psi.book.page.psitweaks_spell_bullets.1", "術式弾の強化は前段階の術式弾を4つ必要とするため、非常に高コストです.");
        add("psi.book.page.psitweaks_spell_bullets.2", "上位のTierになるほどコスト効率が著しく改善し、強力な魔法を簡単に発動できるようになります.");
        add("psi.book.entry.psitweaks_machines", "概要");
        add("psi.book.page.psitweaks_machines.0", "PsiTweaks は, 術式工学と自動化・工業処理をつなぐ機械を追加します. 純粋なサイオニック装置もあれば, Mekanism形式のエネルギーや素材処理と連携する機械もあります.");
        add("psi.book.page.psitweaks_machines.1", "これらの機械は, 研究, 物質変成, スカルク処理, Psiからエネルギーへの変換の入口になります.");
        add("psi.book.page.psitweaks_machines.2", "$(l:psitweaks_machines/psionic_generator)$(o)$(item)サイリンク発電機$(0)$(/l) は所有者にリンクします. 有効化され, チャンクが読み込まれ, 所有者がオンラインのとき, そのプレイヤーのPsiを消費して内部バッファへエネルギーを生成します.$(p)発電レートはGUIで設定できます. tickあたりのPsi消費を増やすほど出力も増えますが, 所有者のPsiも速く減ります.");
        add("psi.book.page.psitweaks_machine.cad_disassembler", "CADを解体して部品を回収するための作業台です. CADを持ってスニーク右クリックすると, 装着されている構成部品と装填済みの術式弾を取り出してCADを分解します.$(p)CAD素体を交換するときや, 古い構成から部品を回収したいときに使います.");
        add("psi.book.page.psitweaks_machine.program_researcher", "PsiTweaksのプログラムアイテムを作成するための電力式研究台です. 必要素材を入力スロットに入れてFEを供給すると, 研究完了時にプログラムを出力します.$(p)各研究の消費電力と時間はJEIで確認できます.");
        add("psi.book.page.psitweaks_machine.sculk_eroder", "石, 土, 砂系などのブロックアイテムをスカルク系の出力へ侵食加工する機械です.$(p)自然なスカルク伝播に頼らず, スカルク素材を得たいときに使います.");
        add("psi.book.page.psitweaks_machine.material_mutator", "$(item)気化サイオニックエコー$(0) と電力を使い, 物質変成を実行するMekanism注入形式の機械です.$(p)$(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l)でも得られる$(l:components/psitweaks_jade)翡翠$(/l)や$(l:components/psitweaks_hypostasis_gem)ヒュポスタシスジェム$(/l)などの変成を自動化できます.");
        add("psi.book.page.psitweaks_machine.psionic_generator", "所有者にリンクし, 所有者がオンラインの間, そのプレイヤーのPsiをエネルギーへ変換する発電機です.$(p)GUIからリンクの有効化とtickあたりのPsi消費量を設定します. 消費量を増やすほど出力も増えますが, 所有者のPsiも速く減ります.");
        add("psi.book.page.psitweaks_machine.spellmachinery_casing", "高度な魔法を機械により実行するための筐体ブロックです.");
        add("psi.book.page.psitweaks_machine.transcendent_universal_cable", "これは単純に, MekanismのUniversal Cableの強化版です. 究極ケーブルの128倍の性能を持ち, 核融合炉級の電力出力を扱うのに向きます.");
        add("psi.book.page.psitweaks_machine.transcendent_energy_cube", "これは単純に, MekanismのEnergy Cubeの強化版です. 究極エネルギーキューブの128倍の容量と出力性能を持ち, 核分裂炉級の電力を蓄えるのに向きます.");
        add("psi.book.entry.psitweaks_cad_and_gear", "CADと装備");
        add("psi.book.page.psitweaks_cad_and_gear.0", "PsiTweaks には, 術式の携行, 自動化, 専門化を助けるアイテムが複数あります. CADに直接関わる道具もあれば, 術式弾の管理や魔法師の戦闘能力を支える装備もあります.");
        add("psi.book.page.psitweaks_cad_and_gear.1", "これらのレシピは, 携帯型組立, インライン詠唱, 術式保存, 魔法師支援装備の入口です.");
        add("psi.book.page.psitweaks_cad_and_gear.2", "PsiTweaks は, 高コストなインゴットを素材とした強力なCAD素体を追加します. 通常のPsi装備では物足りなくなったら, 高位術式弾などと組み合わせて使ってください.");
        add("psi.book.page.psitweaks_cad_and_gear.3", "$(thing)PsiTweaks$(0) はCAD素材として, $(l:components/psitweaks_alloy_psion)サイオニック合金$(/l), $(l:components/psitweaks_chaotic_psimetal)カオティックサイメタル$(/l), $(l:components/psitweaks_flashmetal)フラッシュメタル$(/l), $(l:components/psitweaks_heavy_psimetal)ヘビーサイメタル$(/l), $(l:components/psitweaks_psycheonic_metal_ingot)プシオニックメタル$(/l) の5系統を追加します.$(p)サイオニック合金CADは効率が非常に優れる一方で, 規模は非常に小さいCADです. 他のCAD素体は, 後半の素材ほど効率と規模が相応に強化されています.");
        add("psi.book.entry.psitweaks_inline_casters", "インラインキャスター");
        add("psi.book.entry.psitweaks_auto_casters", "術式自動詠唱デバイス");
        add("psi.book.entry.psitweaks_moval_suit", "M.O.V.A.L. スーツ");
        add("psi.book.page.psitweaks_item.blank_program", "プログラム研究の基礎素材です. $(l:psitweaks_machines/program_researcher)$(o)$(item)プログラム研究台$(0)$(/l) に必要素材と一緒に入れることで, 記入済みのプログラムを作成できます.$(p)既存の記入済みプログラムアイテムとクラフトすると, そのプログラムを複製することもできます.");
        add("psi.book.page.psitweaks_item.philosophers_stone", "再利用可能な触媒で, 金属, ダイヤモンド, 石炭やエンダーパールなどの相互変換レシピを提供します.");
        add("psi.book.page.psitweaks_item.spell_magazine", "最大12個の術式弾を保持し, 使用すると装備中のCADの対応スロットと入れ替えます.$(p)複数の術式弾をまとめて切り替えるための携帯用ロードアウトです.");
        add("psi.book.page.psitweaks_item.flash_ring", "その場で術式を組み立てて詠唱が可能なツールです.$(p)スニーク使用でプログラム画面を開くことができます.");
        add("psi.book.page.psitweaks_item.portable_cad_assembler", "手持ちで使えるCAD組立機です. ブロックを設置せずに組立機画面を開けるため, 拠点外でも簡単にCAD部品や装填済み術式弾を調整できます.");
        add("psi.book.page.psitweaks_item.sorcery_booster", "魔法演算領域スロットに装備し, 術式ダメージを30%増加させます.$(p)$(l:components/psitweaks_magicians_brain)$(o)$(item)魔法師の脳$(0)$(/l) を使う, 戦闘向けの魔法師強化装備です.");
        add("psi.book.page.psitweaks_item.flash_charm", "装備者の盲目と暗闇を継続的に解除するCuriosチャームです.$(p)インベントリ内に持っているだけでも機能するため, ディープダークや視界妨害効果への対策になります.");
        add("psi.book.page.psitweaks_item.interference_range_extender", "魔法演算領域スロットに装備し, 術式の対象判定とレイキャスト射程を64ブロックまで延長します.$(p)サードアイデバイスの素材にもなる下位の射程延長装備です.");
        add("psi.book.page.psitweaks_item.third_eye_device", "魔法演算領域スロットに装備し, 術者に対する通常の術式射程チェックを無効化します.$(p)レイキャスト射程は256ブロックまで延長されます.");
        add("psi.book.page.psitweaks_item.inline_casters.0", "インライン, セカンダリ, パラレルキャスターは, 自身に術式弾スロットを持つ手持ち詠唱具です. 詠唱には通常通りCADが必要ですが, 選択中の術式弾はキャスター側に保存されます.");
        add("psi.book.page.psitweaks_item.inline_casters.1", "インラインキャスターは1スロット, セカンダリキャスターは5スロット, パラレルキャスターは11スロットです.$(p)CADを頻繁に差し替えず, 多数の術式弾を持ち替えたいときに使います.");
        add("psi.book.page.psitweaks_item.auto_casters.0", "術式自動詠唱デバイスは, Curiosスロットに装備可能であり, 自動で術式を詠唱する装備です. $(p) キュリオスコントローラを用いることで、サイメタル外装と同じ要領で術式弾を変更できます.");
        add("psi.book.page.psitweaks_item.auto_casters.1", "tick型は毎tick, セカンド型は0.9秒ごとに詠唱します. カスタムtick型は, アイテム使用で1から1200tickの範囲から詠唱間隔を設定できます.");
        add("psi.book.page.psitweaks_item.curios_controller", "魔法演算領域スロットに装備したソケット対応アイテムの選択術式弾スロットを操作します.$(p)術式自動詠唱デバイスを装備しているときに使う, Curios版の外装コントローラです.");
        add("psi.book.page.psitweaks_item.moval_suit.0", "$(item)M.O.V.A.L. スーツ$(0) は, $(l:components/psitweaks_heavy_psimetal)$(o)$(item)ヘビーサイメタル$(0)$(/l) と $(item)エボニーサイメタル$(0) を使って製作する高性能なPsi系防具です. 各部位を装備するごとに, 術式ダメージ, Psi回復速度, 最大Psiを強化します.$(p)単なる防御力だけでなく, 継続的な詠唱能力も伸ばしたいときに使う装備です.");
        add("psi.book.page.psitweaks_item.moval_suit.1", "Psiの外装防具と同様に, M.O.V.A.L. スーツの各部位は防具イベントから術式を発動できます. ヘルメットは外装センサーに対応し, 通常レギンスはtick時, Ivory版レギンスはsecond時, ブーツはjump時に詠唱します.$(p)各部位は術式ダメージ+10%, Psi回復速度+5, 最大Psi+500を付与します.");
        add("psi.book.title.psitweaks_material.chaotic_psimetal", "カオティックサイメタル");
        add("psi.book.title.psitweaks_material.psycheonic_metal_ingot", "プシオニックメタル");
        add("psi.book.page.psitweaks_material.psionic_echo", "$(item)残響の欠片$(0) にサイオニックエコーを注入するか, $(l:psitweaks_spell_pieces/trick_supreme_infusion)$(o)作動式: 超位注入$(0)$(/l)で作成します.$(p)$(l:components/psitweaks_alloy_psionic_echo)感応系合金$(/l), HDΨE 素材, 気体加工, 上位のサイオニック機械の基礎触媒です.");
        add("psi.book.page.psitweaks_material.chaotic_factor", "$(item)カオティック因子$(0) は, $(item)エンダーパール$(0) にサイジェムを注入して $(item)サイオニック因子$(0) を作るところから始まります. そこへアイボリーまたはエボニーを注入して偏向因子を作り, 反対属性を重ねると $(item)カオティック因子$(0) になります.$(p)通常のサイメタルを上回る最初の PsiTweaks 金属, $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)カオティックサイメタル$(0)$(/l) の注入源です.");
        add("psi.book.page.psitweaks_material.chaotic_psimetal", "通常の $(item)サイメタル$(0) に$(l:components/psitweaks_chaotic_factor)カオティック因子$(/l)を注入して作ります.$(p)カオティックサイメタル CAD 素体や $(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)未精製フラッシュメタル$(0)$(/l) のレシピに使います.");
        add("psi.book.page.psitweaks_material.alloy_psion", "Mekanism の原子合金にサイジェムを注入して作ります.$(p)$(l:components/psitweaks_psionic_control_circuit)$(o)$(item)サイオニック制御回路$(0)$(/l), サイオニック合金 CAD 素体, 次 tier の合金素材に使います.");
        add("psi.book.page.psitweaks_material.alloy_psionic_echo", "$(l:components/psitweaks_alloy_psion)$(o)$(item)サイオニック合金$(0)$(/l) に$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)を注入して作ります.$(p)$(l:components/psitweaks_echo_control_circuit)$(o)$(item)感応制御回路$(0)$(/l) や$(l:components/psitweaks_alloy_hypostasis)位格合金$(/l)の素材に使います.");
        add("psi.book.page.psitweaks_material.alloy_hypostasis", "$(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)感応合金$(0)$(/l) にヒュポスタシスを注入して作ります.$(p)$(l:components/psitweaks_hypostasis_control_circuit)$(o)$(item)位格制御回路$(0)$(/l) や高 tier のサイオニック機械に使います.");
        add("psi.book.page.psitweaks_material.psionic_control_circuit", "$(l:components/psitweaks_alloy_psion)$(o)$(item)サイオニック合金$(0)$(/l) と Mekanism の究極制御回路からクラフトします.$(p)PsiTweaks の機械, 強化術式弾などのレシピに使います.");
        add("psi.book.page.psitweaks_material.echo_control_circuit", "$(l:components/psitweaks_psionic_control_circuit)$(o)$(item)サイオニック制御回路$(0)$(/l), $(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)感応合金$(0)$(/l), $(l:components/psitweaks_echo_sheet)HDΨE シート$(/l)からクラフトします.$(p) 上位の術式弾や機械のレシピに使います.");
        add("psi.book.page.psitweaks_material.hypostasis_control_circuit", "$(l:components/psitweaks_echo_control_circuit)$(o)$(item)感応制御回路$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)$(o)$(item)位格合金$(0)$(/l), $(l:components/psitweaks_echo_sheet)HDΨE シート$(/l)からクラフトします.$(p)最上位の機械に使います.");
        add("psi.book.page.psitweaks_material.echo_pellet", "$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)加工から得られる圧縮 HDΨE 素材です.$(p)生の$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)ではなく, 凝縮した感応素材を要求するレシピに使います. 副産物として得られるΨE-O燃料はガス発電機の燃料として使えます.");
        add("psi.book.page.psitweaks_material.pellet_neptunium", "$(item)ポロニウムブロック$(0) を $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l) で変成するか, $(l:psitweaks_machines/material_mutator)$(o)物質変成機$(0)$(/l) で加工して作ります.$(p)$(l:psitweaks_machines/transcendent_universal_cable)$(o)$(item)超越ユニバーサルケーブル$(0)$(/l) や MekaSuit の $(item)事象干渉力増大ユニット$(0) に使います.");
        add("psi.book.page.psitweaks_material.pellet_americium", "$(item)プルトニウムブロック$(0) を $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l) で変成するか, $(l:psitweaks_machines/material_mutator)$(o)物質変成機$(0)$(/l) で加工して作ります.$(p)プシオニックメタルCAD素体など, 終盤のサイオニックレシピに使います.");
        add("psi.book.page.psitweaks_material.echo_sheet", "HDΨE 素材をシート状に濃縮して作ります.$(p)感応制御回路と位格制御回路のレシピで多く使います.");
        add("psi.book.page.psitweaks_material.magicians_brain", "魔法師の村人を $(l:psitweaks_spell_pieces/trick_guillotine)$(o)作動式: ギロチン$(0)$(/l) で倒したときにドロップします.$(p)$(l:items/psitweaks_sorcery_booster)$(o)$(item)ソーサリーブースター$(0)$(/l) と事象干渉力増強ユニットの素材に使います.");
        add("psi.book.page.psitweaks_material.jade", "$(item)エメラルドブロック$(0) に物質変成を作用させて作ります. 術式でも$(l:psitweaks_machines/material_mutator)物質変成機$(/l)でも入手できます.$(p)$(l:components/psitweaks_magatama)$(o)$(item)勾玉$(0)$(/l) を作るための主素材です.");
        add("psi.book.page.psitweaks_material.hypostasis_gem", "$(item)アンティナイトブロック$(0) に物質変成を作用させて作ります. 術式でも$(l:psitweaks_machines/material_mutator)物質変成機$(/l)でも入手できます.$(p)濃縮してヒュポスタシス注入へ変換できるほか, $(l:components/psitweaks_magatama)$(o)$(item)勾玉$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)位格合金$(/l), $(l:components/psitweaks_psycheonic_metal_ingot)プシオニックメタル系列$(/l)に使います.");
        add("psi.book.page.psitweaks_material.magatama", "$(l:components/psitweaks_jade)$(o)$(item)翡翠$(0)$(/l) で $(l:components/psitweaks_hypostasis_gem)$(o)$(item)ヒュポスタシスジェム$(0)$(/l) を囲んでクラフトします.$(p)$(l:psitweaks_machines/spellmachinery_casing)スペル機械筐体$(/l)に使うため, 物質変成素材と上位サイオニック機械をつなぐ素材です.");
        add("psi.book.page.psitweaks_material.unrefined_flashmetal", "$(l:components/psitweaks_chaotic_psimetal)$(o)$(item)カオティックサイメタル$(0)$(/l) と精製グロウストーンからクラフトします.$(p)濃縮すると使用可能な $(l:components/psitweaks_flashmetal)$(o)$(item)フラッシュメタル$(0)$(/l) になります.");
        add("psi.book.page.psitweaks_material.flashmetal", "$(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)未精製フラッシュメタル$(0)$(/l) を濃縮して作ります.$(p)フラッシュメタルブロック, フラッシュメタル CAD 素体, $(l:components/psitweaks_heavy_psimetal)ヘビーサイメタル$(/l), 昇華 tier の術式弾に使います.");
        add("psi.book.page.psitweaks_material.heavy_psimetal_scrap", "ネザライトの欠片に$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)を注入して作ります.$(p)$(l:components/psitweaks_flashmetal)$(o)$(item)フラッシュメタル$(0)$(/l) と組み合わせて $(l:components/psitweaks_heavy_psimetal)$(o)$(item)ヘビーサイメタル$(0)$(/l) を作ります.");
        add("psi.book.page.psitweaks_material.heavy_psimetal", "$(l:components/psitweaks_heavy_psimetal_scrap)$(o)$(item)ヘビーサイメタルの欠片$(0)$(/l) と $(l:components/psitweaks_flashmetal)$(o)$(item)フラッシュメタル$(0)$(/l) からクラフトします.$(p)より強い CAD 素体, 装備, $(l:components/psitweaks_psycheonic_metal_ingot)プシオニックメタル系列$(/l)に使います.");
        add("psi.book.page.psitweaks_material.psycheonic_metal_ingot", "$(l:components/psitweaks_heavy_psimetal)$(o)$(item)ヘビーサイメタル$(0)$(/l) にヒュポスタシスを注入して $(item)プシオニックメタルナゲット$(0) を作り, 9個クラフトするとインゴットになります.$(p)$(l:items/psitweaks_spell_bullets)超越 tier の術式弾$(/l), プシオニックメタル CAD 素体, 最上位のサイオニック素材に使います.");
        add("psi.book.page.psitweaks_material.antinite_ingot", "アンティナイトの鉱石を精錬するか, アンティナイトブロックから戻して入手します. 粉, 欠片, 凝塊, 結晶, 汚れた粉は Mekanism 式鉱石処理の中間素材です.$(p)$(l:components/psitweaks_hypostasis_gem)ヒュポスタシスジェム$(/l)や$(l:items/psitweaks_philosophers_stone)賢者の石$(/l)の素材になります.");
        add("psi.book.category.psitweaks_spell_pieces", "追加術式");
        add("psi.book.category.psitweaks_spell_pieces.desc", "PsiTweaks が追加するスペルピースです. 攻撃, 工業処理, 補助用途のものが多く, 設定によっては研究解禁が必要になります.");
        add("psi.book.page.psitweaks_spellpiece.trick_explode_no_destroy", "ブロックを破壊しない爆発を起こしてダメージを与えます. ドロップアイテムなどは消滅するので注意してください.");
        add("psi.book.page.psitweaks_spellpiece.trick_barrier", "被ダメージを軽減する障壁効果を付与します.(レベル * 4)だけ被ダメージを減少させます.");
        add("psi.book.page.psitweaks_spellpiece.trick_hardening", "大きな被ダメージを一定値まで抑える硬化効果を付与します. 受ける最大ダメージを(レベル - 2)に抑えます.");
        add("psi.book.page.psitweaks_spellpiece.trick_parade", "確率で攻撃を回避する効果を付与します. (62.5 + 7.5 * レベル) % で攻撃を回避します.");
        add("psi.book.page.psitweaks_spellpiece.trick_flight", "対象にクリエイティブ飛行を可能にする効果を与えます. ");
        add("psi.book.page.psitweaks_spellpiece.trick_interact_block", "対象ブロックに対して, 術者のオフハンドのアイテムで右クリックしたように作用します.");
        add("psi.book.page.psitweaks_spellpiece.trick_freeze_block", "対象ブロックを1段階凍結させます. 水は氷, 氷は氷塊, 氷塊は青氷, 溶岩はマグマブロック, マグマブロックは黒曜石になります.");
        add("psi.book.page.psitweaks_spellpiece.trick_melt_block", "対象ブロックを1段階溶解させます. 氷, 氷塊, 青氷は水に, 黒曜石, 石系, 丸石系はマグマブロックに, マグマブロックは溶岩になります.");
        add("psi.book.page.psitweaks_spellpiece.trick_break_fortune", "対象ブロックを幸運付きで破壊します.");
        add("psi.book.page.psitweaks_spellpiece.trick_break_silk", "対象ブロックをシルクタッチ付きで破壊します.");
        add("psi.book.page.psitweaks_spellpiece.trick_store_entity", "対象エンティティのUUIDをString値としてCADメモリに保存します.");
        add("psi.book.page.psitweaks_spellpiece.selector_stored_entity", "CADメモリに保存されたUUIDからエンティティを取得します. ");
        add("psi.book.page.psitweaks_spellpiece.selector_nearby_spellgram", "指定座標の周囲にある魔法式オブジェクトを取得します. 設置済みの魔法式オブジェクトを制御する術式で主に使います.");
        add("psi.book.page.psitweaks_spellpiece.trick_dispel", "対象エンティティからエフェクトを除去します. 良性・悪性を区別しない汎用版の解呪です.");
        add("psi.book.page.psitweaks_spellpiece.trick_dispel_beneficial", "対象エンティティから有益なエフェクトだけを除去します. 敵対対象の強化を剥がす用途に向いています.");
        add("psi.book.page.psitweaks_spellpiece.trick_dispel_non_beneficial", "対象エンティティから有益でないエフェクトを除去します. 味方のバフを残したまま悪性効果を消したい時に使います.");
        add("psi.book.page.psitweaks_spellpiece.trick_cocytus", "対象モブの精神を永久に凍結させます. 単なるダメージではなく行動を封じる, 非常に強力な制御術式です.");
        add("psi.book.page.psitweaks_spellpiece.trick_supply_fe", "対象ブロックへFEを供給します. 供給量はCADの効率が100のとき、1psiあたり20FEです.");
        add("psi.book.page.psitweaks_spellpiece.trick_time_accelerate", "対象ブロックのtick進行を (2 ^ 威力) 倍にします.上限は512倍速まで.");
        add("psi.book.page.psitweaks_spellpiece.trick_phonon_maser", "超音波振動による高威力の熱線を放ちます. 攻撃用の強力な術式です.");
        add("psi.book.page.psitweaks_spellpiece.trick_meteor_line", "指定位置からRayベクトル方向へ光線を生み出し、経路上の生物に特殊かつ致死的な大ダメージを与えます。");
        add("psi.book.page.psitweaks_spellpiece.trick_supreme_infusion", "残響の欠片をサイオニックエコーへ注入変換します.");
        add("psi.book.page.psitweaks_spellpiece.trick_molecular_divider", "三点で定義した平面で生物を切断します. 高威力の範囲攻撃術式です.");
        add("psi.book.page.psitweaks_spellpiece.trick_aqua_cutter", "前方へ水刃の発射体を放つ序盤用の攻撃術式です.");
        add("psi.book.page.psitweaks_spellpiece.trick_blaze_ball", "前方へ火の弾を放つ序盤用の攻撃術式です.");
        add("psi.book.page.psitweaks_spellpiece.trick_active_air_mine", "指定座標に球状の衝撃波を作り, 範囲内の生物にダメージを与えます. 場所を指定して起爆する範囲攻撃です.");
        add("psi.book.page.psitweaks_spellpiece.trick_flare_circle", "炎の魔法式サークルを設置し, 内部の生物に継続的な炎ダメージを与えます. 一度設置したサークルは60秒残り続けます.");
        add("psi.book.page.psitweaks_spellpiece.trick_ice_circle", "氷の魔法式サークルを設置し, 内部の生物に継続的な凍結ダメージを与えます. ファイアサークルと同じく領域制圧に向いています.");
        add("psi.book.page.psitweaks_spellpiece.trick_set_spellgram_follow_target", "魔法式オブジェクトの追従対象エンティティを設定します.");
        add("psi.book.page.psitweaks_spellpiece.trick_die_flex", "Psi本体の停止と同様の動作を行い, 未実行分のスペルピースのPsiコストを返還します. 高頻度詠唱ではクライアント側のPsi表示が一時的にずれることがあります.");
        add("psi.book.page.psitweaks_spellpiece.trick_radiation_injection", "対象へMekanismの放射線被ばくを付与します. ");
        add("psi.book.page.psitweaks_spellpiece.trick_radiation_filter", "対象に放射線防護効果を付与し、放射線の影響から身を守ります.");
        add("psi.book.page.psitweaks_spellpiece.trick_cure_radiation", "対象の被ばく量を除去します.");
        add("psi.book.page.psitweaks_spellpiece.trick_guillotine", "対象に強力な斬撃ダメージを与え, 討伐時に頭をドロップさせます. 単体対象の攻撃術式です.");
        add("psi.book.page.psitweaks_spellpiece.trick_material_mutation", "特定のブロックを破壊して別のアイテムへ変成させます. 物質変成機はこの処理を電力と気化サイオニックエコーで実行できます.");
        add("psi.book.page.psitweaks_spellpiece.operator_tan", "対象数値のタンジェントを返します.");
        add("psi.book.page.psitweaks_spellpiece.operator_atan", "対象数値のアークタンジェントを返します.");
        add("psi.book.page.psitweaks_spellpiece.operator_sinh", "対象数値のハイパボリックサインを返します.");
        add("psi.book.page.psitweaks_spellpiece.operator_cosh", "対象数値のハイパボリックコサインを返します.");
        add("psi.book.page.psitweaks_spellpiece.operator_tanh", "対象数値のハイパボリックタンジェントを返します.");
        add("psi.desc", "これは第四の壁を破るジョークです、どうぞお楽しみください。");
        add("advancement.psi:iron_cad_pickup", "スイート・ボッド");
        add("advancement.psi:iron_cad_pickup.desc", "鉄のCAD素体を作成する");
        add("advancement.psi:gold_assembly_pickup", "金に値する");
        add("advancement.psi:gold_assembly_pickup.desc", "金のCAD素体を作成する");
        add("advancement.psi:psimetal_assembly_pickup", "ちょっとした改良");
        add("advancement.psi:psimetal_assembly_pickup.desc", "サイメタルのCAD素体を作成する");
        add("advancement.psi:ebony_assembly_pickup", "強力で重厚");
        add("advancement.psi:ebony_assembly_pickup.desc", "エボニーサイメタルのCAD素体を作成する");
        add("advancement.psi:ivory_assembly_pickup", "洗練されて魅力的");
        add("advancement.psi:ivory_assembly_pickup.desc", "アイボリーサイメタルのCAD素体を作成する");
        add("advancement.psi:psidust", "...クラフト不能？");
        add("advancement.psi:psidust.desc", "サイダストを初めて生成する");
        add("advancement.psi:psimetal_pickup", "畏敬の合金");
        add("advancement.psi:psimetal_pickup.desc", "初めて金に注入してサイメタルを入手する");
        add("advancement.psi:psigem_pickup", "思考の結晶");
        add("advancement.psi:psigem_pickup.desc", "初めてダイヤモンドに注入してサイジェムを入手する");
        add("advancement.psi:ebony_pickup", "高価値");
        add("advancement.psi:ebony_pickup.desc", "初めて石炭に注入してエボニー基質を入手する");
        add("advancement.psi:ivory_pickup", "異価値");
        add("advancement.psi:ivory_pickup.desc", "初めてクォーツに注入してアイボリー基質を入手する");
        add("advancement.psi:encyclopaedia_psionica", "タブレットに保存して");
        add("advancement.psi:encyclopaedia_psionica.desc", "サイオニカ魔法大全を入手して、Psiの全ての知識を得る");
        add("material.psitweaks.flashmetal", "フラッシュメタル");
        add("material.psitweaks.psimetal", "サイメタル");
        add("material.psitweaks.ivory_psimetal", "アイボリーサイメタル");
        add("material.psitweaks.ebony_psimetal", "エボニーサイメタル");
        add("material.psitweaks.heavy_psimetal", "ヘビーサイメタル");
        add("material.psitweaks.chaotic_psimetal", "カオティックサイメタル");
        add("material.psitweaks.antinite", "アンティナイト");
        add("material.psitweaks.psycheonic_metal", "プシオニックメタル");
        add("item.psitweaks.molten_psimetal_bucket", "溶融サイメタルバケツ");
        add("item.psitweaks.molten_psigem_bucket", "溶融サイジェムバケツ");
        add("item.psitweaks.molten_ivory_psimetal_bucket", "溶融アイボリーサイメタルバケツ");
        add("item.psitweaks.molten_ebony_psimetal_bucket", "溶融エボニーサイメタルバケツ");
        add("item.psitweaks.molten_flashmetal_bucket", "溶融フラッシュメタルバケツ");
        add("item.psitweaks.molten_chaotic_psimetal_bucket", "溶融カオティックサイメタルバケツ");
        add("item.psitweaks.molten_heavy_psimetal_bucket", "溶融ヘビーサイメタルバケツ");
        add("item.psitweaks.molten_psionic_echo_bucket", "溶融サイオニックエコーバケツ");
        add("item.psitweaks.molten_antinite_bucket", "溶融アンティナイトバケツ");
        add("item.psitweaks.molten_psycheonic_metal_bucket", "溶融プシオニックメタルバケツ");
        add("block.psitweaks.molten_psimetal_fluid", "溶融サイメタル");
        add("block.psitweaks.molten_psigem_fluid", "溶融サイジェム");
        add("block.psitweaks.molten_ivory_psimetal_fluid", "溶融アイボリーサイメタル");
        add("block.psitweaks.molten_ebony_psimetal_fluid", "溶融エボニーサイメタル");
        add("block.psitweaks.molten_flashmetal_fluid", "溶融フラッシュメタル");
        add("block.psitweaks.molten_chaotic_psimetal_fluid", "溶融カオティックサイメタル");
        add("block.psitweaks.molten_heavy_psimetal_fluid", "溶融ヘビーサイメタル");
        add("block.psitweaks.molten_psionic_echo_fluid", "溶融サイオニックエコー");
        add("block.psitweaks.molten_antinite_fluid", "溶融アンティナイト");
        add("block.psitweaks.molten_psycheonic_metal_fluid", "溶融プシオニックメタル");
        add("fluid.psitweaks.molten_psimetal", "溶融サイメタル");
        add("fluid.psitweaks.molten_psigem", "溶融サイジェム");
        add("fluid.psitweaks.molten_ivory_psimetal", "溶融アイボリーサイメタル");
        add("fluid.psitweaks.molten_ebony_psimetal", "溶融エボニーサイメタル");
        add("fluid.psitweaks.molten_flashmetal", "溶融フラッシュメタル");
        add("fluid.psitweaks.molten_chaotic_psimetal", "溶融カオティックサイメタル");
        add("fluid.psitweaks.molten_heavy_psimetal", "溶融ヘビーサイメタル");
        add("fluid.psitweaks.molten_psionic_echo", "溶融サイオニックエコー");
        add("fluid.psitweaks.molten_antinite", "溶融アンティナイト");
        add("fluid.psitweaks.molten_psycheonic_metal", "溶融プシオニックメタル");
        add("fluid_type.psitweaks.molten_psimetal", "溶融サイメタル");
        add("fluid_type.psitweaks.molten_psigem", "溶融サイジェム");
        add("fluid_type.psitweaks.molten_ivory_psimetal", "溶融アイボリーサイメタル");
        add("fluid_type.psitweaks.molten_ebony_psimetal", "溶融エボニーサイメタル");
        add("fluid_type.psitweaks.molten_flashmetal", "溶融フラッシュメタル");
        add("fluid_type.psitweaks.molten_chaotic_psimetal", "溶融カオティックサイメタル");
        add("fluid_type.psitweaks.molten_heavy_psimetal", "溶融ヘビーサイメタル");
        add("fluid_type.psitweaks.molten_psionic_echo", "溶融サイオニックエコー");
        add("fluid_type.psitweaks.molten_antinite", "溶融アンティナイト");
        add("fluid_type.psitweaks.molten_psycheonic_metal", "溶融プシオニックメタル");
        add("modifier.psitweaks.psi_buffer", "サイバッファ");
        add("modifier.psitweaks.psi_buffer.flavor", "想子要塞");
        add("modifier.psitweaks.psi_buffer.description", "Psiを消費して受けるダメージを肩代わりする");
        add("modifier.psitweaks.load_computation", "負荷演算");
        add("modifier.psitweaks.load_computation.flavor", "加算の理");
        add("modifier.psitweaks.load_computation.description", "Psi量に余裕があるとき、一定量消費して攻撃時に魔法ダメージを加算する");
        add("modifier.psitweaks.casting_assist", "効率化演算");
        add("modifier.psitweaks.casting_assist.flavor", "並列処理");
        add("modifier.psitweaks.casting_assist.description", "このツールを手持ちまたは防具として装備中は術式のPsi消費を軽減する（重複しない）");
        add("modifier.psitweaks.erosion_computation", "侵食演算");
        add("modifier.psitweaks.erosion_computation.flavor", "減算の理");
        add("modifier.psitweaks.erosion_computation.description", "攻撃時にデバフを付与し、対象から少量のPsiを吸収する");
        add("modifier.psitweaks.psicological", "サイコロジカル");
        add("modifier.psitweaks.psicological.flavor", "心理的回復過程");
        add("modifier.psitweaks.psicological.description", "手持ちまたは装備中、定期的にPsiを消費して耐久度を回復し、耐久消費をPsiで肩代わりする");
        add("modifier.psitweaks.global_traveler", "グローバルトラベラー");
        add("modifier.psitweaks.global_traveler.flavor", "世界を巡り舞い戻る");
        add("modifier.psitweaks.global_traveler.description", "インベントリを持つブロックをスニーク右クリックでリンクし、このツール由来のドロップアイテムを可能な限り転送する");
        add("modifier.psitweaks.mind_crush", "マインドクラッシュ");
        add("modifier.psitweaks.mind_crush.flavor", "略称: マイクラ");
        add("modifier.psitweaks.mind_crush.description", "攻撃時にレベル×5%の確率で対象のモブをNoAI状態にして行動不能にする。防具装備中は被弾時に攻撃者へ同様の効果を発動する");
        add("entity.minecraft.villager.psitweaks.spellcaster", "魔法師");
    }

    private void addBackportedMatrixTranslations() {
        addBackportedMatrixTranslation("item.psitweaks.program_mass_block_break", "Program: Mass Block Break", "プログラム: 大規模ブロック破壊");
        addBackportedMatrixTranslation("psitweaks.spellerror.mass_break_too_many_pending", "Too many pending mass-break blocks", "保留中の一括破壊ブロック数が上限を超えています");
        addBackportedMatrixTranslation("psitweaks.spellerror.matrix_incompatible_sizes", "Matrix dimensions are incompatible", "行列のサイズが整合しません");
        addBackportedMatrixTranslation("psitweaks.spellerror.matrix_invalid_dimension", "Matrix dimension must be between 1 and 4", "行列の次元は1〜4である必要があります");
        addBackportedMatrixTranslation("psitweaks.spellerror.matrix_invalid_transform", "Transform matrix must be 3x3 or 4x4", "変換行列は3×3または4×4である必要があります");
        addBackportedMatrixTranslation("psitweaks.spellerror.matrix_non_finite_result", "Matrix operation produced a non-finite result", "行列演算の結果が有限値ではありません");
        addBackportedMatrixTranslation("psitweaks.spellerror.matrix_not_square", "Matrix must be square", "行列は正方行列である必要があります");
        addBackportedMatrixTranslation("psitweaks.spellerror.matrix_out_of_bounds", "Matrix index is out of bounds", "行列のインデックスが範囲外です");
        addBackportedMatrixTranslation("psitweaks.spellerror.matrix_singular", "Matrix is singular and cannot be inverted", "行列が特異であり逆行列を求められません");
        addBackportedMatrixTranslation("psitweaks.spellerror.matrix_too_large", "Matrix or list exceeds the maximum size of 4", "行列またはリストが最大サイズ4を超えています");
        addBackportedMatrixTranslation("psitweaks.spellerror.matrix_zero_w", "Transformed w component is zero", "変換後のw成分が0です");
        addBackportedMatrixTranslation("psitweaks.spellerror.region_degenerate_edges", "The three edge vectors are degenerate", "3つの辺ベクトルが退化しています");
        addBackportedMatrixTranslation("psitweaks.spellerror.region_invalid_matrix", "Region operations require a 3x4 or 4x4 matrix", "領域演算には3×4または4×4の行列が必要です");
        addBackportedMatrixTranslation("psitweaks.spellerror.region_too_large", "The region contains too many blocks", "領域に含まれるブロックが多すぎます");
        addBackportedMatrixTranslation("psitweaks.spellparam.matrix", "Matrix", "行列");
        addBackportedMatrixTranslation("psitweaks.spellparam.matrix1", "Matrix 1", "行列1");
        addBackportedMatrixTranslation("psitweaks.spellparam.matrix2", "Matrix 2", "行列2");
        addBackportedMatrixTranslation("psitweaks.spellparam.matrix3", "Matrix 3", "行列3");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_add", "Operator: Matrix Add", "演算子: 行列加算");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_add.desc", "Adds two or three matrices of the same size.", "同じサイズの行列を2つまたは3つ加算します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_add", "Adds matrices of the same size. The optional third matrix is added after the second.", "同じサイズの行列を加算します. 任意の第3行列は第2行列の後に加算されます.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_subtract", "Operator: Matrix Subtract", "演算子: 行列減算");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_subtract.desc", "Subtracts two or three matrices of the same size.", "同じサイズの行列を2つまたは3つ減算します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_subtract", "Subtracts matrices of the same size. The optional third matrix is subtracted after the second.", "同じサイズの行列を減算します. 任意の第3行列は第2行列の後に減算されます.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_multiply", "Operator: Matrix Multiply", "演算子: 行列積");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_multiply.desc", "Multiplies matrices with compatible dimensions.", "次元が整合する行列を乗算します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_multiply", "Multiplies matrices from left to right. The columns of each matrix must match the rows of the next.", "左から右へ行列を乗算します. 各行列の列数が次の行列の行数と一致する必要があります.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_scalar_multiply", "Operator: Matrix Scalar Multiply", "演算子: 行列スカラー倍");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_scalar_multiply.desc", "Multiplies a matrix by a number.", "行列を数値でスカラー倍します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_scalar_multiply", "Multiplies every element of the matrix by the input number.", "行列のすべての要素を入力数値で乗算します.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_transpose", "Operator: Matrix Transpose", "演算子: 行列転置");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_transpose.desc", "Transposes a matrix.", "行列を転置します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_transpose", "Swaps rows and columns of the matrix.", "行列の行と列を入れ替えます.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_determinant", "Operator: Matrix Determinant", "演算子: 行列式");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_determinant.desc", "Computes the determinant of a square matrix.", "正方行列の行列式を求めます。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_determinant", "Computes the determinant. The matrix must be square.", "行列式を求めます. 行列は正方行列である必要があります.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_inverse", "Operator: Matrix Inverse", "演算子: 逆行列");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_inverse.desc", "Computes the inverse of a square matrix.", "正方行列の逆行列を求めます。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_inverse", "Computes the inverse matrix. The matrix must be square and have a non-zero determinant.", "逆行列を求めます. 行列は正方かつ行列式が0でない必要があります.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_extract_row", "Operator: Matrix Extract Row", "演算子: 行抽出");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_extract_row.desc", "Returns the specified row as a Number List.", "指定した行を Number List として返します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_extract_row", "Returns the row at the zero-based index as a Number List.", "0始まりのインデックスで指定した行を Number List として返します.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_extract_column", "Operator: Matrix Extract Column", "演算子: 列抽出");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_extract_column.desc", "Returns the specified column as a Number List.", "指定した列を Number List として返します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_extract_column", "Returns the column at the zero-based index as a Number List.", "0始まりのインデックスで指定した列を Number List として返します.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_replace_row", "Operator: Matrix Replace Row", "演算子: 行置換");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_replace_row.desc", "Replaces a row, expanding the matrix with zeros as needed.", "行を置換し、必要に応じてゼロで拡張します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_replace_row", "Replaces the specified row with a Number List or Vector. Missing columns are filled with zero, and specifying a row beyond the current size expands the matrix with zeros.", "指定行を Number List または Vector で置換します. 足りない列は0で埋められ、現在のサイズを超える行を指定するとゼロで拡張されます.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_replace_column", "Operator: Matrix Replace Column", "演算子: 列置換");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_replace_column.desc", "Replaces a column, expanding the matrix with zeros as needed.", "列を置換し、必要に応じてゼロで拡張します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_replace_column", "Replaces the specified column with a Number List or Vector. Missing rows are filled with zero, and specifying a column beyond the current size expands the matrix with zeros.", "指定列を Number List または Vector で置換します. 足りない行は0で埋められ、現在のサイズを超える列を指定するとゼロで拡張されます.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_replace_element", "Operator: Matrix Replace Element", "演算子: 行列要素置換");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_replace_element.desc", "Replaces the element at the zero-based [row, column] indices with the input number.", "0始まりの [行, 列] で指定した成分を入力数値で置換します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_replace_element", "Replaces the element at the zero-based [row, column] indices with the input number. The indices list must contain exactly two numbers.", "0始まりの [行, 列] で指定した成分を入力数値で置換します. 成分指定用のリストは必ず2つの数値を含む必要があります.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_delete_row", "Operator: Matrix Delete Row", "演算子: 行削除");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_delete_row.desc", "Deletes the specified row from the matrix and shifts the remaining rows up.", "指定した行を行列から削除し、残りの行を詰めます。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_delete_row", "Deletes the row at the zero-based index from the matrix. The matrix must have at least 2 rows so that the result is not empty.", "0始まりのインデックスで指定した行を行列から削除します. 結果が空にならないよう、元の行列は2行以上である必要があります.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_delete_column", "Operator: Matrix Delete Column", "演算子: 列削除");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_delete_column.desc", "Deletes the specified column from the matrix and shifts the remaining columns left.", "指定した列を行列から削除し、残りの列を詰めます。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_delete_column", "Deletes the column at the zero-based index from the matrix. The matrix must have at least 2 columns so that the result is not empty.", "0始まりのインデックスで指定した列を行列から削除します. 結果が空にならないよう、元の行列は2列以上である必要があります.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_element", "Operator: Matrix Element", "演算子: 行列要素");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_element.desc", "Returns the element at (row, column).", "(行, 列) の要素を返します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_element", "Returns the element at (row, column) using zero-based indexes.", "0始まりのインデックスで (行, 列) の要素を返します.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_row_count", "Operator: Matrix Row Count", "演算子: 行数");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_row_count.desc", "Returns the number of rows.", "行列の行数を返します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_row_count", "Returns the number of rows as a Number.", "行数を Number として返します.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_column_count", "Operator: Matrix Column Count", "演算子: 列数");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_column_count.desc", "Returns the number of columns.", "行列の列数を返します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_column_count", "Returns the number of columns as a Number.", "列数を Number として返します.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_multiply_vector", "Operator: Matrix Multiply Vector", "演算子: 行列×ベクトル");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_multiply_vector.desc", "Multiplies a matrix by a column vector or Number List.", "行列に列ベクトルまたは Number List を乗算します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_multiply_vector", "Treats the Vector or Number List as a column vector and multiplies it by the matrix on the left.", "Vector または Number List を列ベクトルとして扱い、左側の行列を掛けます.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_column_from_list", "Operator: Column Matrix From List", "演算子: 数値リスト→列行列");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_column_from_list.desc", "Converts a Number List or Vector into a column matrix.", "Number List または Vector を列行列に変換します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_column_from_list", "Converts a Number List or Vector into a column matrix. The input must contain 1 to 4 elements.", "Number List または Vector を列行列に変換します. 入力は1〜4要素である必要があります.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_flatten", "Operator: Matrix Flatten", "演算子: 行列平坦化");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_flatten.desc", "Flattens a matrix into a Number List in row-major order.", "行列を行優先の Number List に平坦化します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_flatten", "Flattens the matrix into a Number List in row-major order.", "行列を行優先の順序で Number List に平坦化します.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_identity", "Operator: Identity Matrix", "演算子: 単位行列");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_identity.desc", "Creates an identity matrix of the given size.", "指定次数の単位行列を作ります。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_identity", "Creates an identity matrix of the given size. The size must be 1 to 4.", "指定した次数の単位行列を作ります. 次数は1〜4である必要があります.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_zero", "Operator: Zero Matrix", "演算子: ゼロ行列");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_zero.desc", "Creates a zero matrix with the given dimensions.", "指定サイズのゼロ行列を作ります。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_zero", "Creates a zero matrix. If the second size is omitted, a square matrix is returned.", "ゼロ行列を作ります. 第2のサイズを省略すると正方行列を返します.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_diagonal", "Operator: Diagonal Matrix", "演算子: 対角行列");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_diagonal.desc", "Creates a diagonal matrix from a Number List or Vector.", "Number List または Vector から対角行列を作ります。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_diagonal", "Creates a diagonal matrix from a Number List or Vector. The input must contain 1 to 4 elements.", "Number List または Vector から対角行列を作ります. 入力は1〜4要素である必要があります.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_linear_part", "Operator: Matrix Linear Part", "演算子: 行列線形部分");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_linear_part.desc", "Extracts the top-left 3x3 submatrix.", "左上の 3×3 部分行列を取り出します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_linear_part", "Extracts the top-left 3x3 submatrix from the input matrix. If the input is 3x3 or larger, the first 3 rows and 3 columns are returned as a new 3x3 matrix.", "入力された行列の左上 3×3 部分行列を取り出します. 入力が 3×3 以上であれば、最初の3行3列を新しい 3×3 行列として返します.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_transform_vector", "Operator: Matrix Transform Vector", "演算子: 行列でベクトル変換");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_transform_vector.desc", "Transforms a vector with a 3x3 or 4x4 matrix.", "3×3 または 4×4 行列でベクトルを変換します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_transform_vector", "Transforms a vector with a 3x3 or 4x4 matrix. A 3x3 matrix multiplies [x,y,z]. A 4x4 matrix treats the vector as a homogeneous point [x,y,z,1] and divides the result by w.", "3×3 または 4×4 行列でベクトルを変換します. 3×3 行列では [x,y,z] の列ベクトルとして扱います. 4×4 行列では入力された Vector を列ベクトル [x,y,z,1] として行列との積を計算し、結果 [x',y',z',w'] の各成分を w' で除算した Vector を返します.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_cuboid_region", "Operator: Matrix Cuboid Region", "演算子: 直方体領域");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_matrix_cuboid_region.desc", "Builds a 4x4 matrix for an axis-aligned cuboid from a center position and size. Whole-number sizes act as block counts on each axis.", "中心位置とサイズから軸平行直方体を表す 4×4 行列を生成します。整数サイズは各軸でブロック数として扱われます。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_matrix_cuboid_region", "Builds a 4x4 matrix that defines an axis-aligned cuboid centered at the input position. Whole-number size components are treated as block counts: odd counts are centered on the input position, positive even counts include one extra block in the positive direction, and negative even counts include one extra block in the negative direction. Non-integer components keep continuous edge lengths. Pass the matrix to Operator: Region Vector List to turn the region into block coordinates before using Trick: Mass Block Break.", "入力位置を中心とする軸平行直方体を表す 4×4 行列を生成します. 整数のサイズ成分はブロック数として扱われます. 奇数は入力位置を中心に対称, 正の偶数は正方向へ1ブロック多く, 負の偶数は負方向へ1ブロック多く含みます. 非整数の成分は連続的な辺の長さとして扱われます. 作動式: 大規模ブロック破壊で使う前に、演算子: 領域ベクトルリスト化へ渡して領域をブロック座標に変換します.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_region_vector_list", "Operator: Region Vector List", "演算子: 領域ベクトルリスト化");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_region_vector_list.desc", "Returns block position vectors inside a matrix-defined region.", "行列で定義された領域内のブロック座標を Vector List として返します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_region_vector_list", "Returns all block position vectors inside a 3D parallelepiped defined by a 3x4 or 4x4 matrix. The first three columns are the edge vectors and the fourth column is the start point. Vectors are returned in fixed local u, v, and w order. The operator does not inspect the world, so air, unloaded chunks, and unbreakable blocks are not filtered. The returned Vector List can be passed to Trick: Mass Block Break. Regions above the shared internal limit raise an error.", "3×4 または 4×4 の行列で定義された平行六面体の内部にあるすべてのブロック座標を Vector List として返します. 最初の3列は辺ベクトル、第4列は開始点です. Vector は局所u, v, w軸の固定順序で返されます. ワールド状態は確認しないため、空気、未読み込みチャンク、破壊不能ブロックも除外されません. 返した Vector List は作動式: 大規模ブロック破壊へ渡せます. 領域が共通の内部上限を超える場合はエラーになります.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_inside_region", "Operator: Inside Region", "演算子: 領域内");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_inside_region.desc", "Keeps only the entities in the input Entity List whose bounding box centers are inside the matrix-defined parallelepiped.", "入力した Entity List のうち、当たり判定の中心が行列で定義された平行六面体の内部にあるエンティティのみを残します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_inside_region", "Filters an Entity List and returns only the entities whose bounding box centers are inside the parallelepiped defined by a 3x4 or 4x4 matrix. The first three columns are the edge vectors and the fourth column is the start point. Entities exactly on the boundary are counted as inside.", "3×4 または 4×4 の行列で定義された平行六面体の内部に当たり判定の中心があるエンティティのみを残した Entity List を返します. 最初の3列は辺ベクトル、第4列は開始点です. 境界面上にあるエンティティも内部とみなします.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_outside_region", "Operator: Outside Region", "演算子: 領域外");
        addBackportedMatrixTranslation("psitweaks.spellpiece.operator_outside_region.desc", "Keeps only the entities in the input Entity List whose bounding box centers are outside the matrix-defined parallelepiped.", "入力した Entity List のうち、当たり判定の中心が行列で定義された平行六面体の外部にあるエンティティのみを残します。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.operator_outside_region", "Filters an Entity List and returns only the entities whose bounding box centers are outside the parallelepiped defined by a 3x4 or 4x4 matrix. The first three columns are the edge vectors and the fourth column is the start point. Entities exactly on the boundary are counted as inside, so they are excluded from the result.", "3×4 または 4×4 の行列で定義された平行六面体の外部に当たり判定の中心があるエンティティのみを残した Entity List を返します. 最初の3列は辺ベクトル、第4列は開始点です. 境界面上にあるエンティティは内部とみなすため、結果から除外されます.");
        addBackportedMatrixTranslation("psitweaks.spellpiece.trick_mass_block_break", "Trick: Mass Block Break", "作動式: 大規模ブロック破壊");
        addBackportedMatrixTranslation("psitweaks.spellpiece.trick_mass_block_break.desc", "Breaks blocks at the coordinates in a Vector List. Drops are intentionally collected into the caster's inventory first; only overflow is dropped.", "Vector List の座標にあるブロックを破壊します。ドロップは意図的に先に術者のインベントリへ回収され、入りきらない分だけドロップします。");
        addBackportedMatrixTranslation("psi.book.page.psitweaks_spellpiece.trick_mass_block_break", "Breaks blocks at the coordinates in a Vector List. Vectors are treated as block coordinates, duplicate block positions are ignored after their first occurrence, and the remaining order is preserved. The maximum block count input must be a positive integer constant. Only positions within the configured maximum are range-checked and broken. Dropped items are merged by item and data components, then intentionally collected into the caster's inventory first. Only overflow is dropped at the caster's position, or at the focal point if the task is interrupted. The processed positions must fit within the shared internal limit.", "Vector List の座標にあるブロックを破壊します. Vector はブロック座標として扱われ、重複したブロック座標は最初の1回だけ使われ、残りの順序は維持されます. 破壊数の上限入力は正の整数定数である必要があります. 設定した最大数まで実際に処理される座標だけが範囲チェックと破壊の対象になります. ドロップアイテムはアイテムとデータコンポーネントごとにまとめられ、意図的に先に術者のインベントリへ回収されます. 入りきらない分だけ術者の位置へ、タスクが中断された場合は焦点位置へドロップします. 処理対象は共通の内部上限内に収まる必要があります.");
    }

    private void addBackportedMatrixTranslation(String key, String enUs, String jaJp) {
        add(key, switch (locale) {
            case "ja_jp" -> jaJp;
            default -> enUs;
        });
    }
}

