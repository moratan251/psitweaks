package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.items.curios.CuriosSlotChecker;
import com.moratan251.psitweaks.common.items.curios.ItemInterferenceRangeExtender;
import com.moratan251.psitweaks.common.items.curios.ItemThirdEyeDevice;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.spell.SpellContext;

/**
 * 術者ごとの術式の射程。Psi の射程（{@link SpellContext#MAX_DISTANCE}、32 ブロック）を、装備と自律詠唱機のアップグレードで延ばす。
 * <ul>
 * <li>干渉距離延長デバイス（魔法演算領域スロット）: 64 ブロック</li>
 * <li>自律詠唱機の干渉距離アップグレード: 32 + コンフィグの値</li>
 * <li>サードアイデバイス: 射程の判定をしない。レイキャストの長さは 256 ブロック</li>
 * </ul>
 * 複数当てはまるときは長いほう。射程の判定は {@code SpellContextRangeMixin}、レイキャストのピースの長さは
 * {@code SpellHelpersRangeLimitMixin} が使う。Psi のレイキャストを通さずに術者の視線へ線を引く処理（照準の生物、面の座標系）は
 * {@link #maxDistance} の長さにする（サードアイデバイスでも 256 ブロックまでは引かない）。
 */
public final class SpellRange {
    /** 干渉距離延長デバイスの射程。 */
    public static final double INTERFERENCE_RANGE_EXTENDER = 64.0D;
    /** サードアイデバイスのレイキャストの長さ。 */
    public static final double THIRD_EYE_RAYCAST = 256.0D;

    private SpellRange() {
    }

    /** サードアイデバイスで、射程の判定をしない術者か。 */
    public static boolean ignoresRange(@Nullable Player caster) {
        return caster != null && CuriosSlotChecker.hasItemInMagicSlot(caster, ItemThirdEyeDevice.class);
    }

    /** サードアイデバイスを除いた射程（ブロック）。延ばすものがなければ Psi の射程。 */
    public static double maxDistance(@Nullable Player caster) {
        double range = SpellContext.MAX_DISTANCE;
        if (caster == null) {
            return range;
        }
        if (caster instanceof AutonomousCasterPlayer autonomous) {
            range = Math.max(range, SpellContext.MAX_DISTANCE + autonomous.spellRangeBonus());
        }
        if (CuriosSlotChecker.hasItemInMagicSlot(caster, ItemInterferenceRangeExtender.class)) {
            range = Math.max(range, INTERFERENCE_RANGE_EXTENDER);
        }
        return range;
    }

    /**
     * レイキャストの長さの上限。{@code defaultLimit} はピースが Psi の射程として渡す長さで、射程が延びていればその分まで、
     * サードアイデバイスなら {@link #THIRD_EYE_RAYCAST} まで延ばす。
     */
    public static double reach(@Nullable Player caster, double defaultLimit) {
        if (ignoresRange(caster)) {
            return Math.max(defaultLimit, THIRD_EYE_RAYCAST);
        }
        return Math.max(defaultLimit, maxDistance(caster));
    }
}
