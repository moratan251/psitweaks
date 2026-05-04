package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.cad.CadDisassemblyResult;
import com.moratan251.psitweaks.common.cad.CadDisassemblyService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Psitweaks.MOD_ID)
public final class CADDisassemblerEventHandler {
    private CADDisassemblerEventHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        ItemStack heldStack = event.getItemStack();

        if (!level.getBlockState(pos).is(PsitweaksBlocks.CAD_DISASSEMBLER.get())) {
            return;
        }
        if (!CadDisassemblyService.canDisassemble(heldStack)) {
            return;
        }
        if (!player.isShiftKeyDown()) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);

        if (level.isClientSide) {
            return;
        }

        CadDisassemblyResult result = CadDisassemblyService.disassemble(heldStack);
        if (!result.successful()) {
            if (result.failureMessageKey() != null) {
                player.sendSystemMessage(Component.translatable(result.failureMessageKey()));
            }
            return;
        }

        for (ItemStack drop : result.drops()) {
            dropItem(level, pos, drop);
        }
        level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        heldStack.shrink(1);
    }

    private static void dropItem(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack);
        itemEntity.setPickUpDelay(10);
        itemEntity.setDeltaMovement(
                (level.random.nextDouble() - 0.5) * 0.1,
                0.2,
                (level.random.nextDouble() - 0.5) * 0.1
        );

        level.addFreshEntity(itemEntity);
    }
}
