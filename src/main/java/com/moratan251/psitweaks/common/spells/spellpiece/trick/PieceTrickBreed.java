package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.SpellWarning;
import com.moratan251.psitweaks.common.spells.param.ParamEntityOrEntityList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: 繁殖。
 * 対象(Entity または Entity List)の動物に、術者のインベントリにある繁殖用の餌を1つずつ与えて繁殖モードにする。
 * 子ども・繁殖モード中や待ち時間中・飼いならしていない(オオカミ・ネコ・ウマなど)・保護されている動物は飛ばす。
 * 餌を持っていない動物がいれば警告する。
 */
public class PieceTrickBreed extends PieceTrick {
    private static final int POTENCY = 20;
    private static final int COST = 40;

    private SpellParam<Object> target;

    public PieceTrickBreed(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntityOrEntityList(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        List<Entity> targets = ParamEntityOrEntityList.targets(context, getParamValue(context, target));
        if (!(context.caster instanceof ServerPlayer caster)) {
            return null;
        }
        Animal hungry = null;
        for (Entity entity : targets) {
            if (!(entity instanceof Animal animal) || animal.level().isClientSide || !isReadyToBreed(animal)
                    || !animal.level().mayInteract(caster, animal.blockPosition())
                    || PieceTrickItemTransferBase.isProtectedFromInteraction(caster, animal)) {
                continue;
            }
            int slot = findFood(caster.getInventory(), animal);
            if (slot < 0) {
                if (hungry == null) {
                    hungry = animal;
                }
                continue;
            }
            feed(caster, animal, slot);
        }
        if (hungry != null) {
            SpellWarning.warn(context, SpellWarning.NO_BREEDING_FOOD, hungry.getDisplayName());
        }
        return null;
    }

    /** Whether feeding the animal now would put it in love, as feeding it by hand would. */
    static boolean isReadyToBreed(Animal animal) {
        if (animal.getAge() != 0 || !animal.canFallInLove()) {
            return false;
        }
        // Untamed horses only grow tamer, and untamed wolves and cats never mate.
        if (animal instanceof AbstractHorse horse) {
            return horse.isTamed();
        }
        return !(animal instanceof TamableAnimal tamable) || tamable.isTame();
    }

    /** The inventory slot of the first food that breeds the animal, the hotbar first and the off hand last; -1 if none. */
    static int findFood(Inventory inventory, Animal animal) {
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            if (isBreedingFood(animal, inventory.items.get(slot))) {
                return slot;
            }
        }
        return isBreedingFood(animal, inventory.getItem(Inventory.SLOT_OFFHAND)) ? Inventory.SLOT_OFFHAND : -1;
    }

    static boolean isBreedingFood(Animal animal, @Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty() || !animal.isFood(stack)) {
            return false;
        }
        // Of the foods horses and llamas eat, only these put them in love; any food of a camel does.
        if (animal instanceof Llama) {
            return stack.is(Items.HAY_BLOCK);
        }
        if (animal instanceof AbstractHorse && !(animal instanceof Camel)) {
            return stack.is(Items.GOLDEN_CARROT) || stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE);
        }
        return true;
    }

    private static void feed(ServerPlayer caster, Animal animal, int slot) {
        Inventory inventory = caster.getInventory();
        ItemStack food = inventory.getItem(slot);
        if (animal instanceof AbstractHorse horse) {
            // Horses heal, eat and fall in love through their own feeding, which also uses up the food.
            horse.fedFood(caster, food);
            return;
        }
        if (food.getItem() instanceof MobBucketItem bucket) {
            // Like feeding an axolotl by hand: the creature is eaten and the water stays in the bucket.
            inventory.setItem(slot, ItemUtils.createFilledResult(food, caster, new ItemStack(bucket.content.getBucket())));
        } else {
            food.consume(1, caster);
        }
        animal.setInLove(caster);
    }
}
