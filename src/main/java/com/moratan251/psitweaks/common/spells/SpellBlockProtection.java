package com.moratan251.psitweaks.common.spells;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Asks whether a spell may change or use a block the way protection mods listen for it. Vanilla's
 * {@code Level.mayInteract} only sees spawn protection and the world border; protection mods such as FTB Chunks
 * listen to the break event and the right-click event instead.
 */
public final class SpellBlockProtection {
    private SpellBlockProtection() {
    }

    /** Whether the caster may change the block at {@code pos} (replace it, melt it, break it into something else). */
    public static boolean mayChange(Level level, Player caster, BlockPos pos) {
        if (!level.mayInteract(caster, pos) || !caster.mayBuild()) {
            return false;
        }
        return !NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), caster)).isCanceled();
    }

    /** Whether the caster may right-click the block, as vanilla asks before {@code BlockState.useItemOn}. */
    public static boolean mayUse(Level level, Player caster, InteractionHand hand, BlockHitResult hit) {
        if (!level.mayInteract(caster, hit.getBlockPos())) {
            return false;
        }
        PlayerInteractEvent.RightClickBlock event = NeoForge.EVENT_BUS.post(
                new PlayerInteractEvent.RightClickBlock(caster, hand, hit.getBlockPos(), hit));
        return !event.isCanceled() && event.getUseBlock() != TriState.FALSE;
    }
}
