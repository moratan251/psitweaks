package com.moratan251.psitweaks.client.compat;

import com.moratan251.psitweaks.client.combat.ParadeHiddenPlayers;
import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import snownee.jade.api.Accessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IWailaClientRegistration;

/**
 * Trick: Parade の偽装を Jade の表示にも効かせる（Jade の照準の結果を差し替えるコールバック、mixin は使わない）。
 * 術者を隠されている人（{@link ParadeHiddenPlayers#isHiddenFrom}）から見て:
 * <ul>
 *   <li>隠れている術者本人に照準を合わせても、何も表示しない。</li>
 *   <li>虚像に照準を合わせると、虚像ではなく術者本人として表示する（名前・体力・防具・mod 名）。術者がこのクライアントに
 *   いなければ何も表示しない。</li>
 *   <li>虚像の胸にある核は、術者以外には透明（{@link com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject#isInvisibleTo}）
 *   なので Jade の照準に選ばれず、ここには来ない。Jade は小さい箱をふくらませて照準を引くため、核が虚像より手前で当たる。</li>
 * </ul>
 * 術者本人と同じチームの味方には、ふだんどおり表示する。
 */
public final class ParadeJadeDisguise {
    private ParadeJadeDisguise() {
    }

    public static void register(IWailaClientRegistration registration) {
        registration.addRayTraceCallback((hit, accessor, original) -> disguise(registration, hit, accessor));
    }

    @Nullable
    private static Accessor<?> disguise(IWailaClientRegistration registration, HitResult hit, @Nullable Accessor<?> accessor) {
        LocalPlayer viewer = Minecraft.getInstance().player;
        if (viewer == null || !(accessor instanceof EntityAccessor entityAccessor)) {
            return accessor;
        }
        Entity entity = entityAccessor.getEntity();
        if (ParadeHiddenPlayers.isHiddenFrom(entity, viewer)) {
            return null;
        }
        if (entity instanceof EntityParadeDecoy image) {
            Player owner = image.getOwnerPlayer();
            if (owner == null) {
                // 術者がこのクライアントにいないと、術者として見せられないので何も出さない。
                return null;
            }
            // 術者本人と味方にはそのまま、それ以外の人には術者本人として見せる。
            return ParadeHiddenPlayers.isHiddenFrom(owner, viewer)
                    ? registration.entityAccessor().from(entityAccessor).entity(owner).build()
                    : accessor;
        }
        return accessor;
    }
}
