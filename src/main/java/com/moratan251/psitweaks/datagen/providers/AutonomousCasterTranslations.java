package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonObject;
import com.moratan251.psitweaks.common.caster.VirtualCad;
import java.util.Locale;
import java.util.Map;
import vazkii.psi.api.cad.EnumCADStat;

/** 自律詠唱機の翻訳。ブロック名は {@link PsitweaksDatagenBlocks} 側で出力する。 */
final class AutonomousCasterTranslations {
    private static final String GUI = "gui.psitweaks.autonomous_caster.";
    private static final String LOG = "gui.psitweaks.autonomous_caster.log_window.";
    private static final String ERROR = "psitweaks.autonomous_caster.error.";
    private static final String CONFIG = "psitweaks.configuration.autonomous_caster";
    private static final String BOOK = "psi.book.page.psitweaks_machine.autonomous_caster";

    private final JsonObject root;
    private final boolean japanese;

    private AutonomousCasterTranslations(JsonObject root, String locale) {
        this.root = root;
        this.japanese = "ja_jp".equals(locale);
    }

    static void add(JsonObject root, String locale) {
        AutonomousCasterTranslations translations = new AutonomousCasterTranslations(root, locale);
        translations.addBlock();
        translations.addGui();
        translations.addLogWindow();
        translations.addUpgrades();
        translations.addErrors();
        translations.addConfig();
        translations.addBook();
    }

    private void put(String key, String enUs, String jaJp) {
        root.addProperty(key, japanese ? jaJp : enUs);
    }

    private void addBlock() {
        put("container.psitweaks.autonomous_caster", "Autonomous Caster", "自律詠唱機");
        put("tooltip.psitweaks.autonomous_caster.psi", "Stored Psi: %s", "内部Psi: %s");
        put("tooltip.psitweaks.autonomous_caster.settings", "Keeps its settings", "設定を保持");
        put("tooltip.psitweaks.autonomous_caster.memory", "Memory: %s stored values", "メモリ: 値%s個");
        put("description.psitweaks.autonomous_caster",
                "Casts the selected Spell Bullet on a redstone signal, using internal Psi restored from Psionic Echo Gas",
                "レッドストーン信号で選択中の術式弾を詠唱します。内部Psiは気化サイオニックエコーから回復します");
    }

    private void addGui() {
        put(GUI + "psi", "Psi: %s / %s (+%s/t)", "Psi: %s / %s（+%s/t）");
        put(GUI + "bullet", "Bullet %s", "術式弾 %s");
        put(GUI + "bullet.tooltip", "Select the Spell Bullet to cast", "詠唱する術式弾を選びます");
        put(GUI + "item", "Slot %s", "スロット %s");
        put(GUI + "item.tooltip",
                "Select the hotbar slot treated as right of the CAD when casting",
                "詠唱時に CAD の右隣とみなすホットバースロットを選びます");
        put(GUI + "interval", "Interval: %st", "間隔: %st");
        put(GUI + "interval.tooltip",
                "Minimum ticks between casts (4 or more). Press Enter to apply",
                "詠唱から次の詠唱までの最小tick数です（4以上）。Enterで確定します");
        put(GUI + "yaw", "Yaw: %s°", "左右: %s°");
        put(GUI + "yaw.tooltip",
                "Horizontal look angle relative to the front face (-180 to 180). Press Enter to apply",
                "正面を0とした水平方向の視線角度です（-180〜180）。Enterで確定します");
        put(GUI + "pitch", "Pitch: %s°", "上下: %s°");
        put(GUI + "pitch.tooltip",
                "Vertical look angle (-90 is straight up, 90 is straight down). Press Enter to apply",
                "垂直方向の視線角度です（-90で真上、90で真下）。Enterで確定します");
        put(GUI + "loop", "Loop: %s", "ループ: %s");
        put(GUI + "loop.reset", "Reset the loop counter to 0", "ループカウンタを0に戻します");
        put(GUI + "memory", "Memory: %s/%s", "メモリ: %s/%s");
        put(GUI + "memory.clear", "Clear", "消去");
        put(GUI + "memory.clear.tooltip",
                "Clear all values in internal CAD memory (kept when broken)",
                "内部 CAD のメモリをすべて消去します（回収しても保持されます）");
        put(GUI + "memory.clear.confirm", "Clear the internal CAD's memory?", "内蔵CADのメモリを消去しますか？");
        put(GUI + "trigger.pulse", "Pulse", "パルス");
        put(GUI + "trigger.level", "Level", "レベル");
        put(GUI + "trigger.tooltip",
                "Pulse: cast once on signal / Level: cast every interval while powered",
                "パルス: 信号入力時に1回詠唱 / レベル: 信号中に間隔ごと詠唱");
        put(GUI + "cast_now", "Cast", "詠唱");
        put(GUI + "cast_now.tooltip",
                "Test-cast the selected bullet once (runs after cooldown)",
                "選択中の術式弾を1回テスト詠唱します（クールダウン終了後に実行）");
        put(GUI + "settings", "Config", "設定");
        put(GUI + "settings.tooltip",
                "Open the window to configure the bullet, interval, look angles, trigger, and more",
                "術式弾・間隔・視線・トリガーなどを設定するウィンドウを開きます");
        put(GUI + "settings_window.title", "Config", "設定");
        put(GUI + "log", "Log", "ログ");
        put(GUI + "log.tooltip", "Open the activity log", "作動ログを開きます");
        put(GUI + "sight", "Line of Sight Visuals: %s", "視線の可視化: %s");
        put(GUI + "signal_on", "ON", "ON");
        put(GUI + "signal_off", "OFF", "OFF");
        put(GUI + "status.idle", "Signal %s | Waiting", "信号 %s ｜ 待機中");
        put(GUI + "status.success", "Signal %s | Last cast succeeded", "信号 %s ｜ 前回は成功");
        put(GUI + "status.failure", "Signal %s | %s", "信号 %s ｜ %s");
        put(GUI + "status.failure_unknown", "Cast failed (see the log)", "詠唱に失敗しました（ログを参照）");
        put(GUI + "detail.signal", "Redstone signal: %s", "レッドストーン信号: %s");
        put(GUI + "detail.cooldown", "Cooldown: %st left", "クールダウン: 残り%st");
        put(GUI + "detail.outcome", "Last cast: %s", "前回の詠唱: %s");
        put(GUI + "outcome.none", "Not cast yet", "未実行");
        put(GUI + "outcome.success", "Succeeded", "成功");
        put(GUI + "outcome.failure", "Failed", "失敗");
        put(GUI + "detail.loop", "Loop counter: %s", "ループカウンタ: %s");
        put(GUI + "detail.recovery", "Psi recovery: %s/t", "Psi回復: %s/t");
        put(GUI + "component", "CAD component slot: %s", "CAD部品スロット: %s");
        put(GUI + "component.default.dye",
                "When empty, the default spell color is used",
                "空のときは既定の術式の色になります");
    }

    private void addLogWindow() {
        put(LOG + "title", "Activity Log", "作動ログ");
        put(LOG + "empty", "No log entries", "ログはありません");
        put(LOG + "count", "Entries: %s", "%s 件");
        put(LOG + "latest", "Latest", "最新へ");
        put(LOG + "latest.tooltip", "Scroll to the newest entry", "最新のログまでスクロールします");
        put(LOG + "clear", "Clear", "消去");
        put(LOG + "clear.tooltip", "Clear the activity log", "作動ログを消去します");
        put(LOG + "clear.confirm", "Clear the activity log?", "作動ログを消去しますか？");
    }

    private void addUpgrades() {
        String window = GUI + "upgrades.";
        put(window + "tab.tooltip", "Upgrades", "アップグレード");
        put(window + "title", "Upgrades", "アップグレード");
        put(window + "stat.potency", "Potency: %s", "規模: %s");
        put(window + "stat.efficiency", "Efficiency: %s", "効率: %s");
        put(window + "stat.complexity", "Complexity: %s", "複雑性: %s");
        put(window + "stat.projection", "Projection: %s", "作動量: %s");
        put(window + "stat.memory", "Memory: %s", "メモリ: %s");
        put(window + "stat.psi_capacity", "Max Psi: %s", "最大Psi: %s");
        put(window + "stat.conversion_speed", "Psi Conversion: %s/t", "Psi変換速度: %s/t");
        put(window + "stat.conversion_efficiency", "Echo per Psi: %s mB", "Psi変換効率: %s mB/Psi");
        put(window + "stat.interference", "Interference: %s%%", "事象干渉力: %s%%");
        put(window + "stat.range", "Range: %s blocks", "射程: %sブロック");
        put(window + "increase", "(+%s)", "(+%s)");
        put(window + "decrease", "(-%s)", "(-%s)");

        String tooltip = "tooltip.psitweaks.autonomous_caster_upgrade.";
        put(tooltip + "potency", "Potency +%s", "規模 +%s");
        put(tooltip + "efficiency", "Efficiency +%s", "効率 +%s");
        put(tooltip + "complexity", "Complexity +%s", "複雑性 +%s");
        put(tooltip + "projection", "Projection +%s", "作動量 +%s");
        put(tooltip + "memory", "Memory +%s", "メモリ +%s");
        put(tooltip + "psi_capacity", "Max Psi +%s", "最大Psi +%s");
        put(tooltip + "conversion_speed", "Psi conversion +%s/t", "Psi変換速度 +%s/t");
        put(tooltip + "conversion_efficiency", "Echo per Psi -%s mB", "Psi 1あたりのエコー消費 -%s mB");
        put(tooltip + "interference", "Phenomenon Interference +%s%%", "事象干渉力 +%s%%");
        put(tooltip + "range", "Spell range +%s blocks", "術式の射程 +%sブロック");
        put(tooltip + "usage",
                "Install in an Autonomous Caster's Upgrades tab (1 per kind)",
                "自律詠唱機のアップグレードタブに搭載（同種は1台につき1つまで）");
    }

    private void addErrors() {
        put(ERROR + "no_spell", "Spell Bullet slot %s has no spell", "術式弾スロット%sに術式がありません");
        put(ERROR + "loopcast_bullet", "Spell Bullet slot %s holds a Loopcast Spell Bullet, which the machine cannot cast",
                "術式弾スロット%sはループ型術式弾のため詠唱できません");
        put(ERROR + "no_psi", "The internal Psi is empty", "内部Psiが空です");
        put(ERROR + "not_enough_psi",
                "Not enough internal Psi (needs %s, has %s)",
                "内部Psiが足りません（必要 %s / 残り %s）");
        put(ERROR + "no_cad", "The internal CAD could not be assembled", "内部CADを組み立てられません");
        put(ERROR + "no_caster", "The caster is not ready", "詠唱主体の準備ができていません");
        put(ERROR + "cast_failed", "The spell was not cast", "術式を詠唱できませんでした");
        put("psitweaks.autonomous_caster.owner_only",
                "Only the owner and their team can cast or change settings",
                "詠唱と設定の変更は、持ち主とそのチームだけができます");
        put("psitweaks.autonomous_caster.owner_only_gui",
                "Only the owner and their team can open this Autonomous Caster",
                "この自律詠唱機の画面は、持ち主とそのチームだけが開けます");
        put(ERROR + "exception",
                "An error occurred while casting. See the server log",
                "詠唱中にエラーが発生しました。サーバーログを確認してください");
        put("psitweaks.spellerror.idea_storage_owner_unavailable",
                "Cannot tell whose Ideaspace Storage to use",
                "使用するイデアストレージの持ち主を特定できません");
    }

    private void addConfig() {
        put(CONFIG, "Autonomous Caster", "自律詠唱機");
        configValue("psi_capacity", "Psi Capacity", "内部Psi容量",
                "Internal Psi capacity of the Autonomous Caster.",
                "自律詠唱機の内部Psi容量です。");
        configValue("echo_per_operation", "Echo per Recovery", "回復1回あたりのエコー消費量",
                "Psionic Echo Gas (mB) consumed by one Psi recovery operation.",
                "Psi回復1回で消費する気化サイオニックエコーの量（mB）です。");
        configValue("psi_per_operation", "Psi per Recovery", "回復1回あたりのPsi",
                "Psi restored by one recovery operation. One operation runs per tick.",
                "Psi回復1回で得られるPsiです。1tickに1回回復します。");
        configValue("echo_tank_capacity", "Echo Tank Capacity", "エコータンク容量",
                "Psionic Echo Gas tank capacity (mB). Applies after the machine is reloaded.",
                "気化サイオニックエコータンクの容量（mB）です。機械の再読み込み後に反映されます。");
        configValue("min_cast_interval", "Minimum Cast Interval", "詠唱間隔の下限",
                "Shortest cast interval in ticks. Cannot be lower than 4.",
                "詠唱間隔の下限（tick）です。4未満にはできません。");
        configValue("default_cast_interval", "Default Cast Interval", "詠唱間隔の初期値",
                "Cast interval in ticks for newly placed Autonomous Casters.",
                "新しく設置した自律詠唱機の詠唱間隔（tick）です。");
        configValue("cast_volume", "Cast Volume", "詠唱音の音量",
                "Volume of the cast sound. 0 is silent.",
                "詠唱音の音量です。0で無音になります。");
        configValue("log_capacity", "Log Capacity", "ログ保持件数",
                "Number of entries kept in the activity log.",
                "作動ログに保持するエントリ数です。");
        configValue("max_log_message_length", "Max Log Entry Length", "ログ1件の最大文字数",
                "Maximum characters per activity log entry. Longer messages are cut off.",
                "作動ログ1件あたりの最大文字数です。超えた分は省略されます。");
        configValue("upgrade_potency", "Calculation Potency Upgrade", "演算規模アップグレード",
                "Potency the upgrade adds to the internal CAD.",
                "アップグレードで上がる内部CADの規模です。");
        configValue("upgrade_efficiency", "Calculation Efficiency Upgrade", "演算効率アップグレード",
                "Efficiency the upgrade adds to the internal CAD.",
                "アップグレードで上がる内部CADの効率です。");
        configValue("upgrade_complexity", "Overclock Upgrade", "オーバークロックアップグレード",
                "Complexity the upgrade adds to the internal CAD.",
                "アップグレードで上がる内部CADの複雑性です。");
        configValue("upgrade_projection", "Multi-Step Upgrade", "マルチステップアップグレード",
                "Projection the upgrade adds to the internal CAD.",
                "アップグレードで上がる内部CADの作動量です。");
        configValue("upgrade_memory", "Memory Upgrade", "メモリアップグレード",
                "Memory the upgrade adds to the internal CAD.",
                "アップグレードで上がる内部CADのメモリです。");
        configValue("upgrade_psi_capacity", "Psyon Capacity Upgrade", "サイオン容量アップグレード",
                "Internal Psi capacity the upgrade adds.",
                "アップグレードで増える内部Psi容量です。");
        configValue("upgrade_conversion_speed", "Psyon Conversion Speed Upgrade", "サイオン変換速度アップグレード",
                "Psi per tick the upgrade adds to the recovery.",
                "アップグレードで増える、1tickに回復するPsiです。");
        configValue("upgrade_conversion_efficiency", "Psyon Conversion Efficiency Upgrade", "サイオン変換効率アップグレード",
                "Psionic Echo Gas (mB) per Psi the upgrade saves. The cost never drops below a tenth of the base.",
                "アップグレードで減る、Psi 1あたりの気化サイオニックエコーの量（mB）です。基本値の1割より少なくはなりません。");
        configValue("upgrade_interference", "Phenomenon Interference Upgrade", "事象干渉力アップグレード",
                "Phenomenon Interference the upgrade adds to the machine's caster (0.3 is +30%%).",
                "アップグレードで詠唱主体の事象干渉力に足す値です（0.3で+30%%）。");
        configValue("upgrade_range", "Interference Range Upgrade", "干渉距離アップグレード",
                "Spell range (blocks) the upgrade adds to spells the machine casts. The base range is 32.",
                "アップグレードで延びる、機械が詠唱する術式の射程（ブロック）です。基本の射程は32です。");
    }

    private void configValue(String id, String enUs, String jaJp, String enUsTooltip, String jaJpTooltip) {
        put(CONFIG + "." + id, enUs, jaJp);
        put(CONFIG + "." + id + ".tooltip", enUsTooltip, jaJpTooltip);
    }

    private void addBook() {
        put(BOOK,
                "A machine that automatically casts Spell Bullets using its own internal Psi."
                        + "$(li)Place Spell Bullets at top left (Loopcast unsupported)"
                        + "$(li)Feed $(item)Psionic Echo Gas$(0) to build internal Psi"
                        + "$(li)Apply a redstone signal",
                "術式弾を自動で詠唱する機械です。Psi は使用者ではなく機械の内部 Psi を使います。"
                        + "$(li)左上に術式弾を入れる（ループ型は不可）"
                        + "$(li)$(item)気化サイオニックエコー$(0)で内部 Psi を貯める"
                        + "$(li)レッドストーン信号を送る");
        put(BOOK + ".1",
                "Use $(item)Config$(0) to choose the bullet, trigger ($(item)Pulse$(0): once on signal / $(item)Level$(0): repeat at interval), "
                        + "and look angles ($(item)Yaw$(0)/$(item)Pitch$(0)). Signals during cooldown are ignored; $(item)Cast$(0) runs a single test cast.$(p)"
                        + "Casts from the front along the set direction (previewable via the left tab; position via $(item)Selector: Autonomous Caster Position$(0)). "
                        + "Its loop counter increments per cast and resets on manual bullet change, swap, the $(item)0$(0) button, or $(item)Trick: Break Loop$(0).",
                "$(item)設定$(0)ウィンドウで術式弾・トリガー（$(item)パルス$(0): 信号入力時に1回 / $(item)レベル$(0): 信号中に間隔ごと）・"
                        + "視線（$(item)左右$(0)・$(item)上下$(0)）を選びます。クールダウン中は信号を無視し、$(item)詠唱$(0)ボタンで単発テストできます。$(p)"
                        + "正面から指定方向へ詠唱し、左タブで視線を可視化できます（機械座標は$(item)取得子: 詠唱機位置$(0)）。"
                        + "ループ回数は詠唱成功ごとに増え、設定での弾切替・入替・0ボタン・$(item)Trick: Break Loop$(0)で0に戻ります。");
        put(BOOK + ".2",
                baseStats("Base internal CAD stats: Efficiency %d, Potency %d, Complexity %d, Projection %d, Bandwidth %d, Memory %d. ")
                        + "Install up to 3 upgrades (1 per kind) in the right $(item)Upgrades$(0) tab, and place a Colorizer in the lower slot right of the gauges.$(p)"
                        + "The 36 lower slots act as the caster's inventory (bottom row is the hotbar); "
                        + "the slot chosen in $(item)Config$(0) counts as right of the CAD, and overflow drops in front.",
                baseStats("内部 CAD の基本性能は、効率 %d・規模 %d・複雑性 %d・作動量 %d・帯域幅 %d・メモリ %d です。")
                        + "右の$(item)アップグレード$(0)タブに専用アップグレードを3つまで搭載できます（同種は1つまで）。着色器はゲージ右下のスロットに入れます。$(p)"
                        + "下段36スロットは術者のインベントリ（最下段がホットバー）で、$(item)設定$(0)で選んだスロットを CAD の右隣として扱います。"
                        + "入りきらないアイテムは機械の前に落ちます。");
        put(BOOK + ".3",
                "By default, converts 10 mB of $(item)Psionic Echo Gas$(0) per Psi (up to 25 Psi/t, max 10,000 Psi). "
                        + "Feed gas from any side or the upper slot right of the gauges, supply bullets via a side set to $(item)Input (2)$(0), "
                        + "and read internal Psi with a Comparator.$(p)"
                        + "Debug output, errors, and warnings go to the $(item)Activity Log$(0). "
                        + "Breaking the machine keeps everything except the log and loop counter (memory can be cleared in $(item)Config$(0)).",
                "初期設定では$(item)気化サイオニックエコー$(0) 10 mB で 1 Psi を回復します（最大 25 Psi/t、上限 10,000 Psi）。"
                        + "気体はどの面やゲージ右上のスロットからも搬入でき、$(item)搬入(2)$(0)設定面から術式弾を補充できます。内部 Psi はコンパレーターで読めます。$(p)"
                        + "デバッグ出力・エラー・警告は GUI の$(item)作動ログ$(0)に記録されます。"
                        + "回収してもログとループ回数以外はすべて保持されます（メモリは$(item)設定$(0)で消去可）。");
        put(BOOK + ".4",
                "Ideaspace Storage spells access the owner's storage. "
                        + "$(item)Trick: Spin Chamber$(0) and $(item)Trick: Russian Roulette$(0) switch the selected bullet slot without resetting the loop counter.$(p)"
                        + "$(item)Trick: Psi Bullet$(0) and $(item)Trick: Gram Demolition$(0) spare SpellGram objects belonging to the machine, "
                        + "its owner, and the owner's other casters. Spells that open GUIs or use the Eidos Changelog (e.g., $(item)Ender Chest$(0), $(item)Eidos Anchor$(0)) have no effect.",
                "イデアストレージ系術式は機械の所有者のストレージを使用します。"
                        + "$(item)Trick: Spin Chamber$(0)・$(item)Trick: Russian Roulette$(0)は選択中の術式弾スロットを切り替え、ループ回数も維持します。$(p)"
                        + "$(item)作動式: サイ・ブリット$(0)・$(item)術式解体$(0)は自身や所有者側の魔法式オブジェクトを削りません。"
                        + "GUI を開く術式（$(item)エンダーチェスト$(0)等）やエイドス履歴系（$(item)エイドスアンカー$(0)等）は効果がありません。");
    }

    /** アップグレードなしの内蔵 CAD の性能値を、効率・規模・複雑性・作動量・帯域幅・メモリの順に埋め込む。 */
    private static String baseStats(String format) {
        Map<EnumCADStat, Integer> stats = VirtualCad.BASE_STATS;
        return String.format(Locale.ROOT, format, stats.get(EnumCADStat.EFFICIENCY), stats.get(EnumCADStat.POTENCY),
                stats.get(EnumCADStat.COMPLEXITY), stats.get(EnumCADStat.PROJECTION), stats.get(EnumCADStat.BANDWIDTH),
                stats.get(EnumCADStat.SAVED_VECTORS));
    }
}
