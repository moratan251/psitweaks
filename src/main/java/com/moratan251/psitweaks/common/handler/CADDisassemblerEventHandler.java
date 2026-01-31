package com.moratan251.psitweaks.common.handler;

import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.common.item.ItemCAD;

@Mod.EventBusSubscriber(modid = "psitweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CADDisassemblerEventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        ItemStack heldStack = event.getItemStack();

        LOGGER.info("[CADDisassembler Event] RightClickBlock triggered");

        // CAD分解ブロックかチェック
        if (!level.getBlockState(pos).is(PsitweaksBlocks.CAD_DISASSEMBLER.get())) {
            return;
        }

        LOGGER.info("[CADDisassembler Event] It's the disassembler block!");

        // CADを持っているかチェック
        if (!(heldStack.getItem() instanceof ItemCAD)) {
            LOGGER.info("[CADDisassembler Event] Not holding CAD");
            return;
        }

        // シフトを押しているかチェック
        if (!player.isShiftKeyDown()) {
            LOGGER.info("[CADDisassembler Event] Not shifting");
            return;
        }

        LOGGER.info("[CADDisassembler Event] All conditions met!");

        // イベントをキャンセルして自分で処理
        event.setCanceled(true);
        event.setUseBlock(Event.Result.DENY);
        event.setUseItem(Event.Result.DENY);

        // クライアント側では処理しない
        if (level.isClientSide) {
            return;
        }

        LOGGER.info("[CADDisassembler Event] Disassembling CAD...");

        // CADを分解
        disassembleCAD(level, pos, heldStack);

        level.playSound(
                null,                           // プレイヤー（nullでサーバー側から全員に送信）
                pos,                            // 位置
                SoundEvents.ITEM_BREAK,         // 破損音
                SoundSource.BLOCKS,             // サウンドカテゴリ
                1.0F,                           // 音量
                1.0F                            // ピッチ
        );

        // CADを消費
        heldStack.shrink(1);

        LOGGER.info("[CADDisassembler Event] Done!");
    }

    private static void disassembleCAD(Level level, BlockPos pos, ItemStack cadStack) {
        // コンポーネントをドロップ
        if (cadStack.getItem() instanceof ICAD cad) {
            for (EnumCADComponent component : EnumCADComponent.values()) {
                ItemStack componentStack = cad.getComponentInSlot(cadStack, component);
                if (!componentStack.isEmpty()) {
                    LOGGER.info("[CADDisassembler Event] Dropping component: {}", component.name());
                    dropItem(level, pos, componentStack.copy());
                }
            }
        }

        // 術式弾をドロップ
        cadStack.getCapability(PsiAPI.SOCKETABLE_CAPABILITY).ifPresent(socketable -> {
            int lastSlot = socketable.getLastSlot();
            for (int i = 0; i <= lastSlot; i++) {
                if (socketable.isSocketSlotAvailable(i)) {
                    ItemStack bulletStack = socketable.getBulletInSocket(i);
                    if (!bulletStack.isEmpty()) {
                        LOGGER.info("[CADDisassembler Event] Dropping bullet from slot {}", i);
                        dropItem(level, pos, bulletStack.copy());
                    }
                }
            }
        });
    }

    private static void dropItem(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) return;

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