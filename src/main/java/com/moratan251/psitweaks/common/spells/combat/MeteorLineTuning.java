package com.moratan251.psitweaks.common.spells.combat;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

/**
 * Trick: Meteor Line の調整値（試作）。秒数・本数・半径・見た目・強度・音はここだけで変える。
 * tick は夜を置いてからの経過。光条を降らせる数と時間はサーバー、見た目の値はクライアントが読む。
 */
public final class MeteorLineTuning {
    // ---- 半径 ----

    /** 半径を省略したとき。 */
    public static final double DEFAULT_RADIUS = 16.0;
    /** 入力の半径はこの範囲にそろえる（0 以下はピースが先に弾く）。星と光条の本数は最小で最少、最大で最多になる。 */
    public static final double MIN_RADIUS = 16.0;
    public static final double MAX_RADIUS = 32.0;

    // ---- 流れ（tick） ----

    /** 夜が広がり、星が灯る。 */
    public static final int SPREAD_TICKS = 20;
    /** 光条が降る。 */
    public static final int FALL_TICKS = 20;
    /** 光条が降り終わってから晴れ始めるまで、星空の夜だけが残る（2026-11-04 夜の時間を 0.5 秒延ばした分）。 */
    public static final int HOLD_TICKS = 10;
    /** 線が消え、星が散って夜が晴れる。 */
    public static final int CLEAR_TICKS = 20;
    public static final int FALL_START = SPREAD_TICKS;
    public static final int CLEAR_START = FALL_START + FALL_TICKS + HOLD_TICKS;
    public static final int TOTAL_TICKS = CLEAR_START + CLEAR_TICKS;

    // ---- 夜 ----

    /** 夜の色（半球と霧の色）。 */
    public static final int NIGHT_COLOR = 0x060914;
    /** 半球の暗さ（不透明度、0〜255）。表と裏の2枚が重なるので、外から見るとこれより暗い。 */
    public static final int DOME_ALPHA = 120;
    /** 半球の縁にかすかに見える光。 */
    public static final int DOME_RIM_COLOR = 0x5C7BC4;
    public static final float DOME_RIM_BRIGHTNESS = 0.3F;
    /** 夜の中にいるときの画面の暗さ。0 で暗くせず、1 で霧の色を夜の色にしきる。 */
    public static final float SCREEN_DARKNESS = 0.85F;
    /** 夜の中で見える地形の距離（霧の始まりと終わり、ブロック）。 */
    public static final float FOG_START = 2.0F;
    public static final float FOG_END = 32.0F;
    /** 中心より下でも、ここまでは夜の中とみなす（ブロック）。 */
    public static final double BELOW_CENTER = 1.5;

    // ---- CAD の色 ----

    /**
     * 光球（星）と光条には、術者の CAD の色をアクセントとして混ぜる。芯は白のまま、縁と光の広がりに混ざる。
     * 0 で混ぜず（下の固定の色だけ）、1 で縁が CAD の色になる。
     */
    public static final float STAR_ACCENT = 0.4F;
    public static final float BEAM_ACCENT = 0.55F;
    /** 星の周りに CAD の色だけでうっすら広がる光の大きさ（{@link #STAR_SIZE} に対する倍率）と明るさ。 */
    public static final float STAR_HALO_SCALE = 2.4F;
    public static final float STAR_HALO_BRIGHTNESS = 0.3F;

    // ---- 星 ----

    public static final int MIN_STARS = 48;
    public static final int MAX_STARS = 96;
    /** 青みの白。 */
    public static final int STAR_COLOR = 0xD8E6FF;
    /** 半径 {@link #MIN_RADIUS} の夜の星の光の大きさ（ブロック）。半径に比例して大きくし、地上からの見かけの大きさをそろえる。 */
    public static final float STAR_SIZE = 0.6F;
    /** 星が並ぶ、中心からの距離（半径に対する割合）と仰角の範囲（度）。 */
    public static final double STAR_DISTANCE = 0.9;
    public static final double STAR_MIN_ELEVATION = 30.0;
    public static final double STAR_MAX_ELEVATION = 85.0;
    /** 晴れるときに星が外へ散る距離（半径に対する割合）。 */
    public static final double STAR_SCATTER = 0.25;
    /**
     * 星はランダムに置くが、隣の星とはこれ以上離す（星が均等に並んだときの間隔に対する割合）。大きいほど均等に、
     * 小さいほど固まりやすくなる。置き場所の候補は1個につき {@link #STAR_PLACE_ATTEMPTS} 回まで選び直す。
     */
    public static final double STAR_SPACING = 0.6;
    public static final int STAR_PLACE_ATTEMPTS = 30;
    /** 星ごとの明るさと大きさの幅（倍率）。暗く小さい星が多く、明るく大きい星は少ない。 */
    public static final float STAR_MIN_BRIGHTNESS = 0.4F;
    public static final float STAR_MAX_BRIGHTNESS = 1.0F;
    public static final float STAR_MIN_SCALE = 0.55F;
    public static final float STAR_MAX_SCALE = 1.5F;

    // ---- 光条 ----

    /** 斉射の間隔（tick）。 */
    public static final int VOLLEY_INTERVAL = 2;
    /** 一回で降らせる本数。狙う相手がこれより多ければ、その分増える。 */
    public static final int MIN_BEAMS = 24;
    public static final int MAX_BASE_BEAMS = 48;
    /** 狙う分も含めた本数の上限。 */
    public static final int MAX_BEAMS = 64;
    /**
     * 1回の斉射で狙う相手の数の上限。大勢いても、狙う光条を斉射ごとに散らして 1 秒かけて降らせる
     * （{@link #MAX_BEAMS} を斉射の回数で割ったくらい）。
     */
    public static final int AIMED_PER_VOLLEY = 7;
    /** 当たりが残る時間（tick）。その間に線に触れた相手に、光条1本につき一度だけ当たる。 */
    public static final int BEAM_TICKS = 8;
    /** 流星の頭が星から終点まで走る時間（tick）。 */
    public static final int HEAD_TICKS = 3;
    /** 線が細くなって消える時間（{@link #BEAM_TICKS} の終わりの何 tick か）。 */
    public static final int BEAM_FADE_TICKS = 4;
    /** 白い芯と淡い水色の縁。 */
    public static final int BEAM_CORE_COLOR = 0xFFFFFF;
    public static final int BEAM_EDGE_COLOR = 0x9FE6FF;
    public static final float BEAM_CORE_WIDTH = 0.1F;
    public static final float BEAM_EDGE_WIDTH = 0.45F;
    /** 光条が地面や壁に入った点の印（穴の印）が残る時間（tick）と大きさ（ブロック）。 */
    public static final int MARK_TICKS = 60;
    public static final float MARK_SIZE = 0.3F;
    public static final int MAX_MARKS_PER_BEAM = 3;
    /** 当たり判定の箱を広げる幅。 */
    public static final double HIT_RADIUS = 0.15;
    /** 演出用の光条が落ちる地面の範囲（半径に対する割合）。 */
    public static final double GROUND_SPREAD = 0.85;
    /**
     * 光条1本が1回に与えるダメージ。全体倍率・個別倍率・事象干渉力は掛けない。当たる直前に相手の体力を 1 にしてから与えるので、
     * 体力の多い相手も1本で倒れる（ダメージそのものを打ち消す相手は体力 1 で残る）。
     */
    public static final float DAMAGE = 65_535.0F;

    // ---- 魔法式オブジェクト ----

    /**
     * 夜が 3.5 秒の間に時間で減らす耐久値の固有値（× 置いた時点の事象干渉力）。夜は終盤の魔法なので、置いたときの耐久値は
     * この {@code 1 / INTEGRITY_TIME_SHARE} 倍（5 倍）と高めにする。夜は耐久値が残っていても 3.5 秒で晴れる。
     * どちらも仮の値で、術式解体などを作るときに決め直す（2026-11-09 ユーザー）。
     */
    public static final float BASE_INTEGRITY = 100.0F;
    /** 存続時間の間に時間で減る耐久値の、置いたときの耐久値に対する割合（{@code SpellGramObject#getTimeShare}）。 */
    public static final float INTEGRITY_TIME_SHARE = 0.2F;

    // ---- 音（仮。本番の音は後で作る） ----

    /** 夜が広がる間の低い持続音。音量 1 を超える分は聞こえる距離（音量 × 16 ブロック）だけを広げ、夜の端まで届かせる。 */
    public static final SoundEvent SPREAD_SOUND = SoundEvents.BEACON_ACTIVATE;
    public static final float SPREAD_VOLUME = 3.0F;
    public static final float SPREAD_PITCH = 0.5F;
    /**
     * 光条が降るときの短い澄んだ高音。音程は MIN〜MAX の間で散らす。光条1本ごとに鳴らすとクライアントが同時に鳴らせる
     * 音の数（247）を使い切るので、斉射ごとに {@link #BEAM_SOUNDS_PER_VOLLEY} 回までにする。
     */
    public static final Holder<SoundEvent> BEAM_SOUND = SoundEvents.NOTE_BLOCK_CHIME;
    public static final float BEAM_MIN_PITCH = 1.4F;
    public static final float BEAM_MAX_PITCH = 2.0F;
    public static final float BEAM_VOLUME = 0.8F;
    public static final int BEAM_SOUNDS_PER_VOLLEY = 2;
    /** 夜が晴れ始める音。 */
    public static final SoundEvent CLEAR_SOUND = SoundEvents.BEACON_DEACTIVATE;
    public static final float CLEAR_VOLUME = 3.0F;
    public static final float CLEAR_PITCH = 0.7F;
    /** 強度が尽きて夜がつぶれる音。 */
    public static final SoundEvent COLLAPSE_SOUND = SoundEvents.AMETHYST_BLOCK_BREAK;

    private MeteorLineTuning() {
    }

    /** 半径を {@link #MIN_RADIUS}〜{@link #MAX_RADIUS} に収める（0 以下はピースが先に弾く）。 */
    public static double clampRadius(double radius) {
        return Mth.clamp(radius, MIN_RADIUS, MAX_RADIUS);
    }

    /** この半径の夜の星の光の大きさ。 */
    public static float starSize(double radius) {
        return (float) (STAR_SIZE * radius / MIN_RADIUS);
    }

    public static int stars(double radius) {
        return byRadius(radius, MIN_STARS, MAX_STARS);
    }

    /** 狙う相手がいなくても降らせる本数。 */
    public static int baseBeams(double radius) {
        return byRadius(radius, MIN_BEAMS, MAX_BASE_BEAMS);
    }

    /** 光条が降る間の斉射の回数。 */
    public static int volleys() {
        return Mth.positiveCeilDiv(FALL_TICKS, VOLLEY_INTERVAL);
    }

    private static int byRadius(double radius, int min, int max) {
        double t = Mth.clamp((radius - MIN_RADIUS) / (MAX_RADIUS - MIN_RADIUS), 0.0, 1.0);
        return (int) Math.round(Mth.lerp(t, min, max));
    }
}
