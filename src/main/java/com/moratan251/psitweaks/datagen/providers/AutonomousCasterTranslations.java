package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonObject;

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
                "Select the slot the spell treats as the held item. The 9 slots act as the caster's hotbar",
                "術式が手に持っているとみなすスロットを選びます。9個のスロットは詠唱主体のホットバーです");
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
        put(GUI + "loop.reset",
                "Reset the loop counter of the selected bullet to 0",
                "選択中の術式弾のループカウンタを0に戻します");
        put(GUI + "trigger.pulse", "Pulse", "パルス");
        put(GUI + "trigger.level", "Level", "レベル");
        put(GUI + "trigger.tooltip",
                "Pulse: casts once when the redstone signal turns on. Level: keeps casting at the interval while powered",
                "パルス: レッドストーン信号が入った瞬間に1回詠唱します。レベル: 信号が入っている間、詠唱間隔ごとに詠唱します");
        put(GUI + "cast_now", "Cast", "詠唱");
        put(GUI + "cast_now.tooltip",
                "Cast the selected bullet once for testing. Waits for the cooldown",
                "選択中の術式弾をテストとして1回詠唱します。クールダウン中は終わってから詠唱します");
        put(GUI + "log", "Log", "ログ");
        put(GUI + "log.tooltip", "Open the activity log", "作動ログを開きます");
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
        put(GUI + "detail.loop", "Loop counter of bullet %s: %s", "術式弾 %s のループカウンタ: %s");
        put(GUI + "detail.recovery", "Psi recovery: %s/t", "Psi回復: %s/t");
        put(GUI + "component", "CAD component slot: %s", "CAD部品スロット: %s");
        put(GUI + "component.default",
                "When empty, a basic component is used",
                "空のときは基本部品として扱います");
        put(GUI + "component.default.dye",
                "When empty, the default spell color is used",
                "空のときは既定の術式の色になります");
    }

    private void addLogWindow() {
        put(LOG + "title", "Activity Log", "作動ログ");
        put(LOG + "empty", "No log entries", "ログはありません");
        put(LOG + "count", "%s entries", "%s 件");
        put(LOG + "latest", "Latest", "最新へ");
        put(LOG + "latest.tooltip", "Scroll to the newest entry", "最新のログまでスクロールします");
        put(LOG + "clear", "Clear", "消去");
        put(LOG + "clear.tooltip", "Clear the activity log", "作動ログを消去します");
        put(LOG + "clear.confirm", "Clear the activity log?", "作動ログを消去しますか？");
    }

    private void addErrors() {
        put(ERROR + "no_spell", "Spell Bullet slot %s has no spell", "術式弾スロット%sに術式がありません");
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
                "Psi restored by one recovery operation. One operation runs per tick without Speed Upgrades.",
                "Psi回復1回で得られるPsiです。スピードアップグレードがなければ1tickに1回回復します。");
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
                        + "Put Spell Bullets in the top-left slots, pick one, and send a redstone signal. "
                        + "Internal Psi is restored from $(item)Psionic Echo Gas$(0).",
                "術式弾を自分のPsiで自律的に詠唱する機械です. 使用者のPsiは消費しません.$(p)"
                        + "左上のスロットに術式弾を入れて選択し, レッドストーン信号を送ると詠唱します. "
                        + "内部Psiは$(item)気化サイオニックエコー$(0)から回復します.");
        put(BOOK + ".1",
                "$(li)$(item)Pulse$(0): casts once when the redstone signal turns on."
                        + "$(li)$(item)Level$(0): casts at the cast interval while powered."
                        + "$(li)The $(item)Cast$(0) button in the GUI casts once for testing."
                        + "$(p)Signals during the cooldown are ignored. The interval cannot go below 4 ticks.",
                "$(li)$(item)パルス$(0): レッドストーン信号が入った瞬間に1回詠唱します."
                        + "$(li)$(item)レベル$(0): 信号が入っている間, 詠唱間隔ごとに詠唱します."
                        + "$(li)GUIの$(item)詠唱$(0)ボタンで1回だけテスト詠唱できます."
                        + "$(p)クールダウン中の信号は無視されます. 詠唱間隔は4tick未満にできません.");
        put(BOOK + ".2",
                "The caster stands at the front face and looks where $(item)Yaw$(0) and $(item)Pitch$(0) point.$(p)"
                        + "A $(item)Loopcast Spell Bullet$(0) runs once per trigger. "
                        + "Each bullet slot counts its successful casts, and $(item)Selector: Loopcast Index$(0) reads that count.",
                "詠唱主体は正面に立ち, $(item)Yaw$(0)と$(item)Pitch$(0)の方向を向いて詠唱します.$(p)"
                        + "$(item)ループ型術式弾$(0)はトリガー1回につき1回だけ実行されます. "
                        + "術式弾スロットごとに成功回数を数えており, $(item)取得子: ループ回数$(0)でその値を読めます.");
        put(BOOK + ".3",
                "The four slots on the right take CAD components and build the internal CAD. "
                        + "Empty slots count as basic parts, and the colorizer may be left out.$(p)"
                        + "The nine slots below are the caster's hotbar, and the selected one counts as the held item. "
                        + "Items that do not fit drop in front of the machine.",
                "右側の4スロットにCADの部品を入れると, 内部CADが組み上がります. 空のスロットは基本部品として扱われ, 着色器は無くても動きます.$(p)"
                        + "下段の9スロットは詠唱主体のホットバーで, 選択中のスロットが手に持っている扱いになります. "
                        + "入りきらないアイテムは機械の正面に落ちます.");
        put(BOOK + ".4",
                "By default, 250 mB of $(item)Psionic Echo Gas$(0) restores 25 Psi, up to 10,000 Psi. "
                        + "Feed the gas from any side, or use the slot left of the gauges.$(p)"
                        + "$(item)Speed Upgrades$(0) speed up recovery, and $(item)Chemical Upgrades$(0) reduce the gas used. "
                        + "A comparator reads the internal Psi.",
                "初期設定では, $(item)気化サイオニックエコー$(0)250mBでPsiが25回復し, 最大10,000まで貯まります. "
                        + "気体はどの面からでも搬入でき, ゲージ左のスロットに気体入りアイテムを入れても補充できます.$(p)"
                        + "$(item)スピードアップグレード$(0)で回復が速くなり, $(item)化学アップグレード$(0)で気体の消費が減ります. "
                        + "コンパレーターで内部Psiの残量を読み取れます.");
        put(BOOK + ".5",
                "Spells that use the Ideaspace Storage use the storage of the machine's owner.$(p)"
                        + "Debug output and spell errors go to the $(item)Activity Log$(0) in the GUI instead of your chat. "
                        + "Breaking the machine keeps its items, gas and upgrades, but resets its Psi, settings and log.",
                "イデアストレージを使う術式は, 機械の所有者のストレージを使います.$(p)"
                        + "デバッグ出力や術式のエラーはチャットではなく, GUIの$(item)作動ログ$(0)に記録されます. "
                        + "機械を回収するとアイテム・気体・アップグレードは残りますが, Psi・設定・ログは初期化されます.");
    }
}
