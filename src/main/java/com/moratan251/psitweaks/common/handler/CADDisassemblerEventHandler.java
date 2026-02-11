package com.moratan251.psitweaks.common.handler;

import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.common.item.ItemCAD;

@Mod.EventBusSubscriber(modid = "psitweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CADDisassemblerEventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TIC_CAD_CLASS_NAME = "com.hutuneko.psi_ex.item.TiCCAD";
    private static final String TIC_CAD_DISASSEMBLE_FAILED_MESSAGE = "message.psitweaks.cad_disassembler.unsupported_tic_cad";
    private static final String TINKERS_TOOL_MATERIALS_TAG = "tic_materials";
    private static final String TINKERS_PART_MATERIAL_TAG = "Material";
    private static final ResourceLocation[] TIC_CAD_PART_IDS = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath("psi_ex", "tic_cad_core"),
            ResourceLocation.fromNamespaceAndPath("psi_ex", "tic_cad_assembly"),
            ResourceLocation.fromNamespaceAndPath("psi_ex", "tic_cad_battery"),
            ResourceLocation.fromNamespaceAndPath("psi_ex", "tic_cad_socket")
    };

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        ItemStack heldStack = event.getItemStack();


        // CAD分解ブロックかチェック
        if (!level.getBlockState(pos).is(PsitweaksBlocks.CAD_DISASSEMBLER.get())) {
            return;
        }



        // CADを持っているかチェック
        if (!(heldStack.getItem() instanceof ItemCAD)) {
            return;
        }

        // シフトを押しているかチェック
        if (!player.isShiftKeyDown()) {
            return;
        }

        // イベントをキャンセルして自分で処理
        event.setCanceled(true);
        event.setUseBlock(Event.Result.DENY);
        event.setUseItem(Event.Result.DENY);

        // クライアント側では処理しない
        if (level.isClientSide) {
            return;
        }

        // CADを分解
        if (!disassembleCAD(level, pos, heldStack)) {
            player.sendSystemMessage(Component.translatable(TIC_CAD_DISASSEMBLE_FAILED_MESSAGE));
            return;
        }

        level.playSound(
                null,                           // プレイヤー
                pos,                            // 位置
                SoundEvents.ITEM_BREAK,         // 破損音
                SoundSource.BLOCKS,             // サウンドカテゴリ
                1.0F,                           // 音量
                1.0F                            // ピッチ
        );

        // CADを消費
        heldStack.shrink(1);

    }

    private static boolean isTiCCAD(ItemStack cadStack) {
        Item item = cadStack.getItem();
        if (item.getClass().getName().equals(TIC_CAD_CLASS_NAME)) {
            return true;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        return itemId != null
                && "psi_ex".equals(itemId.getNamespace())
                && "tic_cad".equals(itemId.getPath());
    }

    private static boolean disassembleCAD(Level level, BlockPos pos, ItemStack cadStack) {
        boolean componentsDropped = isTiCCAD(cadStack)
                ? disassembleTiCCADComponents(level, pos, cadStack)
                : disassembleDefaultCADComponents(level, pos, cadStack);
        if (!componentsDropped) {
            return false;
        }

        dropSocketBullets(level, pos, cadStack);
        return true;
    }

    private static boolean disassembleDefaultCADComponents(Level level, BlockPos pos, ItemStack cadStack) {
        // コンポーネントをドロップ
        if (cadStack.getItem() instanceof ICAD cad) {
            for (EnumCADComponent component : EnumCADComponent.values()) {
                ItemStack componentStack = cad.getComponentInSlot(cadStack, component);
                if (!componentStack.isEmpty()) {
                    dropItem(level, pos, componentStack.copy());
                }
            }
            return true;
        }
        return false;
    }

    private static boolean disassembleTiCCADComponents(Level level, BlockPos pos, ItemStack cadStack) {
        CompoundTag cadTag = cadStack.getTag();
        if (cadTag == null || !cadTag.contains(TINKERS_TOOL_MATERIALS_TAG, Tag.TAG_LIST)) {
            LOGGER.warn("Failed to disassemble TiCCAD: missing '{}' tag", TINKERS_TOOL_MATERIALS_TAG);
            return false;
        }

        ListTag materialList = cadTag.getList(TINKERS_TOOL_MATERIALS_TAG, Tag.TAG_STRING);
        if (materialList.size() < TIC_CAD_PART_IDS.length) {
            LOGGER.warn("Failed to disassemble TiCCAD: expected {} materials, found {}",
                    TIC_CAD_PART_IDS.length, materialList.size());
            return false;
        }

        ItemStack[] partStacks = new ItemStack[TIC_CAD_PART_IDS.length];
        for (int i = 0; i < TIC_CAD_PART_IDS.length; i++) {
            ResourceLocation partId = TIC_CAD_PART_IDS[i];
            Item partItem = ForgeRegistries.ITEMS.getValue(partId);
            if (partItem == null || partItem == Items.AIR) {
                LOGGER.warn("Failed to disassemble TiCCAD: missing part item '{}'", partId);
                return false;
            }

            String materialId = materialList.getString(i);
            if (materialId == null || materialId.isBlank()) {
                LOGGER.warn("Failed to disassemble TiCCAD: empty material at index {}", i);
                return false;
            }

            ItemStack partStack = new ItemStack(partItem);
            partStack.getOrCreateTag().putString(TINKERS_PART_MATERIAL_TAG, materialId);
            partStacks[i] = partStack;
        }

        for (ItemStack partStack : partStacks) {
            dropItem(level, pos, partStack);
        }
        return true;
    }

    private static void dropSocketBullets(Level level, BlockPos pos, ItemStack cadStack) {
        // 術式弾をドロップ
        cadStack.getCapability(PsiAPI.SOCKETABLE_CAPABILITY).ifPresent(socketable -> {
            int lastSlot = socketable.getLastSlot();
            for (int i = 0; i <= lastSlot; i++) {
                if (socketable.isSocketSlotAvailable(i)) {
                    ItemStack bulletStack = socketable.getBulletInSocket(i);
                    if (!bulletStack.isEmpty()) {
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
