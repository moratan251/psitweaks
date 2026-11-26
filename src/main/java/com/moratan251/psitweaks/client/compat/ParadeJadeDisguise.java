package com.moratan251.psitweaks.client.compat;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.combat.ParadeHiddenPlayers;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityParadeCore;
import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import snownee.jade.addon.core.DistanceProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

/**
 * Trick: Parade の偽装を Jade の表示にも効かせる（Jade の照準の結果を差し替えるコールバックと、座標の行を直す表示の部品、
 * Psitweaks の mixin は使わない）。
 * 術者を隠されている人（{@link ParadeHiddenPlayers#isHiddenFrom}）から見て:
 * <ul>
 *   <li>隠れている術者本人に照準を合わせても、何も表示しない。</li>
 *   <li>虚像に照準を合わせると、虚像ではなく術者本人として表示する（名前・体力・防具・mod 名）。術者がこのクライアントに
 *   いなければ何も表示しない。サーバーのデータは、照準を替えた直後に前の相手の分が出ないよう確かめ直させる。</li>
 *   <li>座標の行（Jade の設定で出すもの）は、術者の本当の位置ではなく虚像の位置にする（{@link Coordinates}）。</li>
 *   <li>虚像の胸にある核は、術者以外には透明（{@link com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject#isInvisibleTo}）
 *   なので Jade の照準に選ばれず、ここには来ない。Jade は小さい箱をふくらませて照準を引くため、核が虚像より手前で当たる。</li>
 * </ul>
 * 術者本人から見ると、自分の虚像と核は素通りして、その奥にあるもの（バニラの照準が当たったブロックかエンティティ）を表示する。
 * バニラの照準は、術者には虚像を選ばない（{@link EntityParadeDecoy#isPickable}）が、Jade は部位のないエンティティの
 * {@code isPickable} を見ないため。術者本人と同じチームの味方には、ふだんどおり表示する。
 */
public final class ParadeJadeDisguise {
    private ParadeJadeDisguise() {
    }

    public static void register(IWailaClientRegistration registration) {
        registration.addRayTraceCallback((hit, accessor, original) -> disguise(registration, accessor));
        registration.registerEntityComponent(Coordinates.INSTANCE, Player.class);
        registration.markAsClientFeature(Coordinates.UID);
    }

    @Nullable
    private static Accessor<?> disguise(IWailaClientRegistration registration, @Nullable Accessor<?> accessor) {
        LocalPlayer viewer = Minecraft.getInstance().player;
        if (viewer == null || !(accessor instanceof EntityAccessor entityAccessor)) {
            return accessor;
        }
        if (isOwnImage(entityAccessor.getEntity(), viewer)) {
            accessor = behindOwnImage(registration, viewer);
            if (!(accessor instanceof EntityAccessor behind)) {
                return accessor;
            }
            entityAccessor = behind;
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
                    ? registration.entityAccessor().from(entityAccessor).entity(owner).requireVerification().build()
                    : entityAccessor;
        }
        return entityAccessor;
    }

    /** Whether {@code entity} is the viewer's own image or its core. */
    private static boolean isOwnImage(Entity entity, Player viewer) {
        if (entity instanceof EntityParadeDecoy image) {
            return image.isOwnedBy(viewer);
        }
        return entity instanceof EntityParadeCore core && core.isShownTo(viewer);
    }

    /**
     * What lies behind the viewer's own image: what the vanilla crosshair hit, which passes through the image. Nothing if
     * it hit nothing, or something Jade hides.
     */
    @Nullable
    private static Accessor<?> behindOwnImage(IWailaClientRegistration registration, Player viewer) {
        HitResult vanilla = Minecraft.getInstance().hitResult;
        if (vanilla instanceof BlockHitResult block && block.getType() == HitResult.Type.BLOCK) {
            Level level = viewer.level();
            BlockPos pos = block.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (registration.shouldHide(state)) {
                return null;
            }
            return registration.blockAccessor().blockState(state).blockEntity(() -> level.getBlockEntity(pos))
                    .hit(block).requireVerification().build();
        }
        if (vanilla instanceof EntityHitResult entityHit && !registration.shouldHide(entityHit.getEntity())) {
            return registration.entityAccessor().hit(entityHit).entity(entityHit.getEntity()).requireVerification().build();
        }
        return null;
    }

    /**
     * 虚像を術者として見せた表示の、座標の行を虚像の位置に直す。Jade の距離と座標の部品（{@link DistanceProvider}）は表示の
     * 相手の位置を使うので、術者の本当の位置が出てしまう。その部品のすぐあとに動き、行を作り直す。
     */
    static final class Coordinates implements IEntityComponentProvider {
        static final ResourceLocation UID = Psitweaks.location("parade_coordinates");
        static final Coordinates INSTANCE = new Coordinates();
        /** Jade の距離と座標の部品（-4600）のすぐあと。 */
        private static final int PRIORITY = -4599;

        private Coordinates() {
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (!(accessor.getHitResult().getEntity() instanceof EntityParadeDecoy image) || accessor.getEntity() == image
                    || !config.get(JadeIds.CORE_COORDINATES) || tooltip.get(JadeIds.CORE_DISTANCE).isEmpty()) {
                return;
            }
            tooltip.remove(JadeIds.CORE_DISTANCE);
            try {
                DistanceProvider.getEntity().append(tooltip, accessor, image.blockPosition(), config);
            } catch (LinkageError e) {
                // Jade の中身が変わって作り直せないときは、術者の位置を出すよりはと、行を消したままにする。
            }
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public boolean isRequired() {
            return true;
        }

        @Override
        public int getDefaultPriority() {
            return PRIORITY;
        }
    }
}
