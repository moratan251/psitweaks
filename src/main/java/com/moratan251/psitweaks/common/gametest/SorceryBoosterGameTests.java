package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.items.curios.CuriosSlotChecker;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * ソーサリーブースター（{@code ItemSorceryBooster}）は魔法演算領域に 1 つしか装備できず、事象干渉力は何個付けても +0.3。
 * 装備の変化は Curios がエンティティの tick（{@link EntityTickEvent.Post}）で処理するので、その event を直接送って進める。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SorceryBoosterGameTests {
    private static final double BONUS = 0.3D;
    private static final double EPSILON = 1.0E-6D;

    private SorceryBoosterGameTests() {
    }

    @GameTest(template = "psi110_empty")
    public static void onlyOneSorceryBoosterCanBeEquipped(GameTestHelper helper) {
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "sorcery-booster-one"));
        player.setPos(helper.absoluteVec(new Vec3(1.5D, 1.0D, 1.5D)));
        IDynamicStackHandler slots = magicSlots(player);
        double base = interference(player);

        slots.setStackInSlot(2, new ItemStack(PsitweaksItems.INTERFERENCE_RANGE_EXTENDER.get()));
        helper.assertTrue(slots.isItemValid(0, booster()), "Another Magic Calculation Area item blocks the Sorcery Booster");
        slots.setStackInSlot(0, booster());
        curiosTick(player);
        assertInterference(helper, player, base + BONUS, "one booster");

        helper.assertFalse(slots.isItemValid(1, booster()), "A second Sorcery Booster can be equipped");
        helper.assertFalse(slots.isItemValid(3, booster()), "A second Sorcery Booster can be equipped in the last slot");
        helper.assertTrue(slots.isItemValid(0, booster()), "The equipped Sorcery Booster cannot be swapped in its own slot");

        slots.setStackInSlot(0, ItemStack.EMPTY);
        curiosTick(player);
        assertInterference(helper, player, base, "the booster removed");
        helper.assertTrue(slots.isItemValid(1, booster()), "The Sorcery Booster cannot be equipped after the old one is removed");
        helper.succeed();
    }

    /** 重複不可にする前のワールドで 2 つ付けたまま読み込まれた場合も +0.3 だけで、1 つ外しても残りの効果は消えない。 */
    @GameTest(template = "psi110_empty")
    public static void sorceryBoostersAlreadyWornTogetherDoNotStack(GameTestHelper helper) {
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "sorcery-booster-two"));
        player.setPos(helper.absoluteVec(new Vec3(1.5D, 1.0D, 1.5D)));
        IDynamicStackHandler slots = magicSlots(player);
        double base = interference(player);

        // setStackInSlot は canEquip を通らないので、古いワールドと同じく 2 つ並べられる。
        slots.setStackInSlot(0, booster());
        slots.setStackInSlot(1, booster());
        curiosTick(player);
        assertInterference(helper, player, base + BONUS, "two boosters");

        slots.setStackInSlot(0, ItemStack.EMPTY);
        curiosTick(player);
        assertInterference(helper, player, base + BONUS, "one of two boosters removed");

        slots.setStackInSlot(1, ItemStack.EMPTY);
        curiosTick(player);
        assertInterference(helper, player, base, "both boosters removed");
        helper.succeed();
    }

    private static IDynamicStackHandler magicSlots(FakePlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(curios -> curios.getStacksHandler(CuriosSlotChecker.MAGIC_CALCULATION_AREA))
                .orElseThrow()
                .getStacks();
    }

    private static ItemStack booster() {
        return new ItemStack(PsitweaksItems.SORCERY_BOOSTER.get());
    }

    private static void curiosTick(FakePlayer player) {
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(player));
    }

    private static double interference(FakePlayer player) {
        return player.getAttributeValue(PsitweaksAttributes.SPELL_DAMAGE_FACTOR);
    }

    private static void assertInterference(GameTestHelper helper, FakePlayer player, double expected, String state) {
        double actual = interference(player);
        helper.assertTrue(Math.abs(actual - expected) < EPSILON,
                "Phenomenon Interference with " + state + " is " + actual + ", expected " + expected);
    }
}
