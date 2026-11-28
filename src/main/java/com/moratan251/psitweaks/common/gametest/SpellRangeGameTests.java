package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.items.curios.CuriosSlotChecker;
import com.moratan251.psitweaks.common.spells.SpellRange;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceMacroFaceAxialOffset;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 射程を延ばす装備（{@link SpellRange}）: 干渉距離延長デバイスで射程の判定とレイキャストが 64 ブロックに、サードアイデバイスで
 * 射程の判定がなくなりレイキャストが 256 ブロックになる。面基準マクロが術者の視線へ引く線も同じだけ延びる。自律詠唱機の
 * 干渉距離アップグレードは {@code AutonomousCasterChecks.rangeUpgradeExtendsSpellRange}。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellRangeGameTests {
    private SpellRangeGameTests() {
    }

    @GameTest(template = "psi110_empty")
    public static void rangeDevicesExtendTheRangeCheckAndRaycasts(GameTestHelper helper) {
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "spell-range-devices"));
        player.setPos(helper.absoluteVec(new Vec3(1.5D, 1.0D, 1.5D)));
        SpellContext context = new SpellContext().setPlayer(player);
        double base = SpellContext.MAX_DISTANCE;
        Vec3 origin = player.position();
        Vector3 at48 = Vector3.fromVec3d(origin.add(48.0D, 0.0D, 0.0D));
        Vector3 at70 = Vector3.fromVec3d(origin.add(0.0D, 0.0D, 70.0D));
        helper.assertTrue(!context.isInRadius(at48) && SpellRange.reach(player, base) == base,
                "A caster without devices reaches past Psi's range");

        var curios = CuriosApi.getCuriosInventory(player).orElseThrow();
        curios.setEquippedCurio(CuriosSlotChecker.MAGIC_CALCULATION_AREA, 0, new ItemStack(PsitweaksItems.INTERFERENCE_RANGE_EXTENDER.get()));
        helper.assertTrue(context.isInRadius(at48) && !context.isInRadius(at70),
                "The Interference Range Extender does not set the range to " + SpellRange.INTERFERENCE_RANGE_EXTENDER);
        helper.assertTrue(SpellRange.reach(player, base) == SpellRange.INTERFERENCE_RANGE_EXTENDER,
                "The Interference Range Extender does not lengthen raycasts: " + SpellRange.reach(player, base));

        curios.setEquippedCurio(CuriosSlotChecker.MAGIC_CALCULATION_AREA, 1, new ItemStack(PsitweaksItems.THIRD_EYE_DEVICE.get()));
        helper.assertTrue(context.isInRadius(at70) && SpellRange.reach(player, base) == SpellRange.THIRD_EYE_RAYCAST,
                "The Third-Eye Device does not remove the range check or lengthen raycasts to " + SpellRange.THIRD_EYE_RAYCAST);
        helper.succeed();
    }

    /** The macro looks straight up, so the test needs no barrier ceiling over its structure. */
    @GameTest(template = "connector_empty", skyAccess = true)
    public static void faceMacrosReachAsFarAsTheRangeDevices(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        FakePlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "face-macro-range"));
        Vec3 feet = helper.absoluteVec(new Vec3(2.5D, 1.0D, 2.5D));
        player.moveTo(feet.x, feet.y, feet.z, 0.0F, -90.0F);
        BlockPos eye = BlockPos.containing(player.getEyePosition());
        BlockPos at50 = eye.above(50);
        BlockPos at100 = eye.above(100);
        try {
            level.setBlockAndUpdate(at50, Blocks.STONE.defaultBlockState());
            helper.assertTrue(faceTarget(player) == null, "A caster without devices hits a block 50 blocks away: " + faceTarget(player));

            var curios = CuriosApi.getCuriosInventory(player).orElseThrow();
            curios.setEquippedCurio(CuriosSlotChecker.MAGIC_CALCULATION_AREA, 0, new ItemStack(PsitweaksItems.INTERFERENCE_RANGE_EXTENDER.get()));
            helper.assertTrue(at50.equals(faceTarget(player)), "The Interference Range Extender does not reach 50 blocks: " + faceTarget(player));

            level.setBlockAndUpdate(at50, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(at100, Blocks.STONE.defaultBlockState());
            helper.assertTrue(faceTarget(player) == null, "The Interference Range Extender reaches 100 blocks");

            curios.setEquippedCurio(CuriosSlotChecker.MAGIC_CALCULATION_AREA, 0, ItemStack.EMPTY);
            curios.setEquippedCurio(CuriosSlotChecker.MAGIC_CALCULATION_AREA, 1, new ItemStack(PsitweaksItems.THIRD_EYE_DEVICE.get()));
            helper.assertTrue(at100.equals(faceTarget(player)), "The Third-Eye Device does not reach 100 blocks: " + faceTarget(player));
            helper.succeed();
        } finally {
            level.setBlockAndUpdate(at50, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(at100, Blocks.AIR.defaultBlockState());
        }
    }

    /** The block that Macro: Face Axial Offset aims at with no inputs, or null when its raycast misses. */
    private static BlockPos faceTarget(FakePlayer player) {
        PieceMacroFaceAxialOffset macro = new PieceMacroFaceAxialOffset(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return null;
            }
        };
        try {
            Vector3 target = (Vector3) macro.execute(new SpellContext().setPlayer(player));
            return target.toBlockPos();
        } catch (SpellRuntimeException miss) {
            return null;
        }
    }
}
