package com.moratan251.psitweaks.common.items.curios;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * 魔法演算領域スロットに 1 つだけ装備でき、事象干渉力を {@value #SPELL_DAMAGE_BONUS} 上げる。
 * <p>
 * 2 つ目は {@link #canEquip} で装備させない。重複不可にする前のワールドでは複数装備したまま読み込まれる
 * （Curios は読み込み中のほかのスロットを見せないので {@code canEquip} では外れない）ため、修飾子の ID を
 * スロットごとではなく共通の {@link #BONUS_ID} にして、何個付けていても効果は 1 つ分にする。
 */
public class ItemSorceryBooster extends Item implements ICurioItem {
    static final double SPELL_DAMAGE_BONUS = 0.3D;
    static final ResourceLocation BONUS_ID = Psitweaks.location("sorcery_booster");

    public ItemSorceryBooster(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CuriosSlotChecker.MAGIC_CALCULATION_AREA.equals(slotContext.identifier())
                && !isWornInAnotherSlot(slotContext, false);
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext,
                                                                                ResourceLocation id,
                                                                                ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
        if (CuriosSlotChecker.MAGIC_CALCULATION_AREA.equals(slotContext.identifier())) {
            modifiers.put(PsitweaksAttributes.SPELL_DAMAGE_FACTOR, bonus());
        }
        return modifiers;
    }

    /**
     * 修飾子の ID が共通なので、外した 1 つと一緒に効果が消える。ほかのスロットにまだ残っていれば付け直す
     * （重複不可にする前から複数装備していた場合）。
     */
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level().isClientSide() || !isWornInAnotherSlot(slotContext, true)) {
            return;
        }
        AttributeInstance instance = entity.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR);
        if (instance != null) {
            instance.addOrUpdateTransientModifier(bonus());
        }
    }

    private static AttributeModifier bonus() {
        return new AttributeModifier(BONUS_ID, SPELL_DAMAGE_BONUS, AttributeModifier.Operation.ADD_VALUE);
    }

    /**
     * {@code slotContext} 以外の魔法演算領域スロットにソーサリーブースターがあるか。{@code activeOnly} なら
     * Curios が効果を出しているスロット（有効なスロット）だけを見る。
     */
    private static boolean isWornInAnotherSlot(SlotContext slotContext, boolean activeOnly) {
        if (!CuriosSlotChecker.MAGIC_CALCULATION_AREA.equals(slotContext.identifier())) {
            return false;
        }
        return CuriosApi.getCuriosInventory(slotContext.entity())
                .flatMap(curios -> curios.getStacksHandler(CuriosSlotChecker.MAGIC_CALCULATION_AREA))
                .map(handler -> hasBoosterOutside(handler, slotContext.index(), activeOnly))
                .orElse(false);
    }

    private static boolean hasBoosterOutside(ICurioStacksHandler handler, int ownIndex, boolean activeOnly) {
        NonNullList<Boolean> activeStates = activeOnly ? handler.getActiveStates() : null;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (slot == ownIndex) {
                continue;
            }
            if (activeStates != null && !(activeStates.size() > slot && activeStates.get(slot))) {
                continue;
            }
            if (handler.getStacks().getStackInSlot(slot).getItem() instanceof ItemSorceryBooster) {
                return true;
            }
        }
        return false;
    }
}
