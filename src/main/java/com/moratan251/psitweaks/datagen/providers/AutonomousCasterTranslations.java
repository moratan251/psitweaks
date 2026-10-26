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
                "Select the slot the spell treats as the held item. The bottom row of 9 slots is the caster's hotbar",
                "術式が手に持っているとみなすスロットを選びます。最下段の9個のスロットが詠唱主体のホットバーです");
        put(GUI + "interval", "Interval: %st", "間隔: %st");
        put(GUI + "interval.tooltip",
                "Minimum ticks between casts (4 or more). Press Enter to apply",
                "詠唱から次の詠唱までの最小tick数です（4以上）。Enterで確定します");
        put(GUI + "yaw", "Yaw: %s°", "Yaw: %s°");
        put(GUI + "yaw.tooltip",
                "Horizontal look angle relative to the front face (-180 to 180). Press Enter to apply",
                "正面を0とした水平方向の視線角度です（-180〜180）。Enterで確定します");
        put(GUI + "pitch", "Pitch: %s°", "Pitch: %s°");
        put(GUI + "pitch.tooltip",
                "Vertical look angle (-90 is straight up, 90 is straight down). Press Enter to apply",
                "垂直方向の視線角度です（-90で真上、90で真下）。Enterで確定します");
        put(GUI + "loop", "Loop: %s", "ループ: %s");
        put(GUI + "loop.reset", "Reset the loop counter to 0", "ループカウンタを0に戻します");
        put(GUI + "trigger.pulse", "Pulse", "パルス");
        put(GUI + "trigger.level", "Level", "レベル");
        put(GUI + "trigger.tooltip",
                "Pulse: casts once when the redstone signal turns on. Level: keeps casting at the interval while powered",
                "パルス: レッドストーン信号が入った瞬間に1回詠唱します。レベル: 信号が入っている間、詠唱間隔ごとに詠唱します");
        put(GUI + "cast_now", "Cast", "詠唱");
        put(GUI + "cast_now.tooltip",
                "Cast the selected bullet once for testing. Waits for the cooldown",
                "選択中の術式弾をテストとして1回詠唱します。クールダウン中は終わってから詠唱します");
        put(GUI + "settings", "Config", "設定");
        put(GUI + "settings.tooltip",
                "Open the window for the selected bullet and held slot, the cast interval, the look angles, the trigger and the loop counter",
                "選択中の術式弾・手に持つスロット、詠唱間隔、視線の角度、トリガー、ループカウンタを設定するウィンドウを開きます");
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
    }

    private void configValue(String id, String enUs, String jaJp, String enUsTooltip, String jaJpTooltip) {
        put(CONFIG + "." + id, enUs, jaJp);
        put(CONFIG + "." + id + ".tooltip", enUsTooltip, jaJpTooltip);
    }

    private void addBook() {
        put(BOOK,
                "A machine that casts Spell Bullets by itself, using its own Psi instead of yours.$(p)"
                        + "Put Spell Bullets in the 12 slots at the top left, pick one in the $(item)Config$(0) window, and send a redstone signal. "
                        + "Internal Psi is restored from $(item)Psionic Echo Gas$(0).",
                "術式弾を自分のPsiで自律的に詠唱する機械です. 使用者のPsiは消費しません.$(p)"
                        + "左上の12スロットに術式弾を入れて$(item)設定$(0)ウィンドウで選択し, レッドストーン信号を送ると詠唱します. "
                        + "内部Psiは$(item)気化サイオニックエコー$(0)から回復します.");
        put(BOOK + ".1",
                "$(li)$(item)Pulse$(0): casts once when the redstone signal turns on."
                        + "$(li)$(item)Level$(0): casts at the cast interval while powered."
                        + "$(li)The $(item)Cast$(0) button in the $(item)Config$(0) window casts once for testing."
                        + "$(p)Signals during the cooldown are ignored. The interval cannot go below 4 ticks.",
                "$(li)$(item)パルス$(0): レッドストーン信号が入った瞬間に1回詠唱します."
                        + "$(li)$(item)レベル$(0): 信号が入っている間, 詠唱間隔ごとに詠唱します."
                        + "$(li)$(item)設定$(0)ウィンドウの$(item)詠唱$(0)ボタンで1回だけテスト詠唱できます."
                        + "$(p)クールダウン中の信号は無視されます. 詠唱間隔は4tick未満にできません.");
        put(BOOK + ".2",
                "The caster stands at the front face and looks where $(item)Yaw$(0) and $(item)Pitch$(0) point. "
                        + "The $(item)Line of Sight Visuals$(0) tab on the left of the GUI shows that line in the world.$(p)"
                        + "$(item)Loopcast Spell Bullets$(0) cannot be placed in the machine.",
                "詠唱主体は正面に立ち, $(item)Yaw$(0)と$(item)Pitch$(0)の方向を向いて詠唱します. "
                        + "GUI左の$(item)視線の可視化$(0)タブで, その視線をワールドに表示できます.$(p)"
                        + "$(item)ループ型術式弾$(0)は機械に入れられません.");
        put(BOOK + ".loop",
                "The machine has one loop count. Each successful cast adds 1, "
                        + "and $(item)Selector: Loopcast Index$(0) reads the count before the cast.$(p)"
                        + "It goes back to 0 when:"
                        + "$(li)another slot is selected in the GUI$(li)the selected bullet is changed"
                        + "$(li)the $(item)0$(0) button is pressed$(li)the machine casts $(item)Trick: Break Loop$(0)"
                        + "$(p)Slot changes made by spells keep the count.",
                "機械はループ回数を1つ持ち, 詠唱が成立するたびに1増えます. "
                        + "$(item)取得子: ループ回数$(0)は詠唱前の値を読みます.$(p)"
                        + "次のときは0に戻ります:"
                        + "$(li)GUIで別のスロットを選ぶ$(li)選択中の術式弾を替える"
                        + "$(li)GUIの$(item)0$(0)ボタンを押す$(li)機械が$(item)Trick: Break Loop$(0)を実行する"
                        + "$(p)術式によるスロットの切り替えでは引き継がれます.");
        put(BOOK + ".3",
                fixedStats("The internal CAD has fixed stats: Efficiency %d, Potency %d, Complexity %d, Projection %d, "
                        + "Bandwidth %d, Memory %d. A colorizer in the lower slot right of the gauges changes the spell color.$(p)")
                        + "The 36 slots below are the caster's inventory with its hotbar in the bottom row, "
                        + "and the selected hotbar slot counts as the held item. Items that do not fit drop in front of the machine.",
                fixedStats("内部CADの性能値は固定で, 効率%d・規模%d・複雑性%d・作動量%d・帯域幅%d・メモリ%dです. "
                        + "ゲージ右の下のスロットに着色器を入れると術式の色が変わります.$(p)")
                        + "下の36スロットは詠唱主体のインベントリで, 最下段がホットバーです. "
                        + "選択中のホットバーのスロットが手に持っている扱いになります. 入りきらないアイテムは機械の正面に落ちます.");
        put(BOOK + ".4",
                "By default, 250 mB of $(item)Psionic Echo Gas$(0) restores 25 Psi, up to 10,000 Psi. "
                        + "Feed the gas from any side, or use the upper slot right of the gauges. "
                        + "A side set to $(item)Input (2)$(0) in the side config feeds Spell Bullets into empty bullet slots.$(p)"
                        + "Upgrades cannot be installed. A comparator reads the internal Psi.",
                "初期設定では, $(item)気化サイオニックエコー$(0)250mBでPsiが25回復し, 最大10,000まで貯まります. "
                        + "気体はどの面からでも搬入でき, ゲージ右の上のスロットに気体入りアイテムを入れても補充できます. "
                        + "側面設定を$(item)搬入(2)$(0)にした面からは, 術式弾を空いている術式弾スロットに搬入できます.$(p)"
                        + "アップグレードは搭載できません. コンパレーターで内部Psiの残量を読み取れます.");
        put(BOOK + ".5",
                "Spells that use the Ideaspace Storage use the storage of the machine's owner.$(p)"
                        + "Debug output, spell errors and spell warnings go to the $(item)Activity Log$(0) in the GUI instead of your chat. "
                        + "Breaking the machine keeps its items, gas and Psi, but resets its settings, log and the internal CAD's memory.",
                "イデアストレージを使う術式は, 機械の所有者のストレージを使います.$(p)"
                        + "デバッグ出力や術式のエラー・警告はチャットではなく, GUIの$(item)作動ログ$(0)に記録されます. "
                        + "機械を回収するとアイテム・気体・Psiは残りますが, 設定・ログと内部CADのメモリは初期化されます.");
        put(BOOK + ".6",
                "$(item)Trick: Spin Chamber$(0) and $(item)Trick: Russian Roulette$(0) switch the machine's selected "
                        + "Spell Bullet slot, as they switch a CAD's selected bullet.$(p)"
                        + "Spells that need a screen, the caster's recorded movement or a CAD battery do nothing in the machine. "
                        + "They are listed on the next page.",
                "$(item)Trick: Spin Chamber$(0)と$(item)Trick: Russian Roulette$(0)は, CADの選択中の術式弾と同じように, "
                        + "機械の選択中の術式弾スロットを切り替えます.$(p)"
                        + "画面・術者の動きの記録・CADのバッテリーを使う術式は, 機械では効果がありません. 一覧は次のページにあります.");
        put(BOOK + ".7",
                "These do nothing when the machine casts them:"
                        + "$(li)Trick: Eidos Anchor$(li)Trick: Eidos Reversal$(li)Selector: Eidos Changelog"
                        + "$(li)Selector: Caster Battery"
                        + "$(li)Trick: Ender Chest$(li)Trick: Crafting Table$(li)Trick: View Ideaspace Storage",
                "次の術式は, 機械で詠唱しても効果がありません:"
                        + "$(li)作動式: エイドスアンカー$(li)作動式: エイドスリバーサル$(li)取得子: エイドス変更履歴"
                        + "$(li)Selector: Caster Battery"
                        + "$(li)作動式: エンダーチェスト$(li)作動式: 作業台$(li)作動式: イデアストレージ閲覧");
    }

    /** 内蔵 CAD の固定性能値を、効率・規模・複雑性・作動量・帯域幅・メモリの順に埋め込む。 */
    private static String fixedStats(String format) {
        Map<EnumCADStat, Integer> stats = VirtualCad.FIXED_STATS;
        return String.format(Locale.ROOT, format, stats.get(EnumCADStat.EFFICIENCY), stats.get(EnumCADStat.POTENCY),
                stats.get(EnumCADStat.COMPLEXITY), stats.get(EnumCADStat.PROJECTION), stats.get(EnumCADStat.BANDWIDTH),
                stats.get(EnumCADStat.SAVED_VECTORS));
    }
}
