package com.moratan251.psitweaks.common.items.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import vazkii.psi.api.exosuit.IPsiEventArmor;
import vazkii.psi.api.exosuit.PsiArmorEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Curiosのmagic_calculation_areaスロットをチェックして
 * 指定されたクラスのアイテムがあるかを確認するユーティリティクラス
 */

public class CuriosSlotChecker {

    public static boolean hasItemInMagicSlot(Player player, Class<?> itemClass) {
        LazyOptional<ICuriosItemHandler> maybeCuriosInventory = CuriosApi.getCuriosInventory(player);

        return maybeCuriosInventory.map(curiosInventory -> {
            // magic_calculation_areaスロットの取得
            Optional<ICurioStacksHandler> maybeSlotInventory = curiosInventory.getStacksHandler("magic_calculation_area");

            return maybeSlotInventory.map(slotInventory -> {
                // スロット内の全アイテムをチェック
                for (int i = 0; i < slotInventory.getSlots(); i++) {
                    ItemStack stack = slotInventory.getStacks().getStackInSlot(i);

                    if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        }).orElse(false);
    }

    public static ItemStack getItemFromMagicSlot(Player player, Class<?> itemClass) {
        LazyOptional<ICuriosItemHandler> maybeCuriosInventory = CuriosApi.getCuriosInventory(player);

        return maybeCuriosInventory.map(curiosInventory -> {
            Optional<ICurioStacksHandler> maybeSlotInventory = curiosInventory.getStacksHandler("magic_calculation_area");

            return maybeSlotInventory.map(slotInventory -> {
                for (int i = 0; i < slotInventory.getSlots(); i++) {
                    ItemStack stack = slotInventory.getStacks().getStackInSlot(i);

                    if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                        return stack;
                    }
                }
                return ItemStack.EMPTY;
            }).orElse(ItemStack.EMPTY);
        }).orElse(ItemStack.EMPTY);
    }

    public static ItemStack getItemFromSelectedMagicSlot(Player player, int slot) {
        LazyOptional<ICuriosItemHandler> maybeCuriosInventory = CuriosApi.getCuriosInventory(player);

        return maybeCuriosInventory.map(curiosInventory -> {
            Optional<ICurioStacksHandler> maybeSlotInventory = curiosInventory.getStacksHandler("magic_calculation_area");

            return maybeSlotInventory.map(slotInventory -> {

                return slotInventory.getStacks().getStackInSlot(slot);

            }).orElse(ItemStack.EMPTY);
        }).orElse(ItemStack.EMPTY);
    }





    public static List<ItemStack> getAllItemsFromMagicSlot(Player player, Class<?> itemClass) {
        List<ItemStack> items = new ArrayList<>();
        LazyOptional<ICuriosItemHandler> maybeCuriosInventory = CuriosApi.getCuriosInventory(player);

        maybeCuriosInventory.ifPresent(curiosInventory -> {
            curiosInventory.getStacksHandler("magic_calculation_area").ifPresent(slotInventory -> {
                for (int i = 0; i < slotInventory.getSlots(); i++) {
                    ItemStack stack = slotInventory.getStacks().getStackInSlot(i);

                    if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                        items.add(stack);
                    }
                }
            });
        });

        return items;
    }

    public static void performActionIfItemExists(Player player, Class<?> itemClass, Runnable action) {
        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
            curiosInventory.getStacksHandler("magic_calculation_area").ifPresent(slotInventory -> {
                for (int i = 0; i < slotInventory.getSlots(); i++) {
                    ItemStack stack = slotInventory.getStacks().getStackInSlot(i);

                    if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                        action.run();
                        return; // 最初の一つが見つかったら実行して終了
                    }
                }
            });
        });
    }



}