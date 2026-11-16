package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.registries.PsitweaksVillagers;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGuillotine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/** 魔法師の頭: ほかのモブの頭と同じく置け、壊すと戻り、ギロチンで魔法師から取れる。旧 ID (魔法師の脳) は頭として読み込む。 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MagiciansHeadGameTests {
    private static final String TEMPLATE = "connector_empty";

    @GameTest(template = TEMPLATE)
    public static void magiciansHeadPlacesAndDropsLikeAMobHead(GameTestHelper helper) {
        BlockPos floor = new BlockPos(1, 1, 1);
        BlockPos wall = new BlockPos(3, 1, 3);
        helper.setBlock(floor, PsitweaksBlocks.MAGICIANS_HEAD.get().defaultBlockState().setValue(SkullBlock.ROTATION, 4));
        helper.setBlock(wall, PsitweaksBlocks.MAGICIANS_WALL_HEAD.get().defaultBlockState().setValue(WallSkullBlock.FACING, Direction.EAST));
        for (BlockPos pos : new BlockPos[]{floor, wall}) {
            helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(pos)) instanceof SkullBlockEntity,
                    "The Magician's Head at " + pos + " has no skull block entity");
        }

        helper.getLevel().destroyBlock(helper.absolutePos(floor), true);
        helper.getLevel().destroyBlock(helper.absolutePos(wall), true);
        helper.assertItemEntityCountIs(PsitweaksItems.MAGICIANS_HEAD.get(), floor, 1.0, 1);
        helper.assertItemEntityCountIs(PsitweaksItems.MAGICIANS_HEAD.get(), wall, 1.0, 1);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void oldMagiciansBrainLoadsAsTheHead(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.get(Psitweaks.location("magicians_brain")) == PsitweaksItems.MAGICIANS_HEAD.get(),
                "psitweaks:magicians_brain does not point at the Magician's Head");
        CompoundTag saved = new CompoundTag();
        saved.putString("id", "psitweaks:magicians_brain");
        saved.putInt("count", 3);
        ItemStack loaded = ItemStack.parseOptional(helper.getLevel().registryAccess(), saved);
        helper.assertTrue(loaded.is(PsitweaksItems.MAGICIANS_HEAD.get()) && loaded.getCount() == 3,
                "A saved Magician's Brain loaded as " + loaded);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void guillotineDropsTheMagiciansHead(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "magician_head_caster", new Vec3(0.5, 1.0, 0.5));
        // 頭が落ちる場所はこのテスト内の座標のまま持つ (helper.relativePos は回転なしでも 180° 回した座標を返すので使わない)。
        BlockPos at = new BlockPos(2, 1, 2);
        Villager magician = helper.spawn(EntityType.VILLAGER, at);
        magician.setVillagerData(magician.getVillagerData().setProfession(PsitweaksVillagers.SPELLCASTER.get()));
        magician.setVillagerXp(1);
        magician.setHealth(1.0F);
        var guillotine = new PieceTrickGuillotine(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return magician;
            }
        };
        try {
            guillotine.execute(new SpellContext().setPlayer((Player) caster));
        } catch (SpellRuntimeException e) {
            GameTestPlayers.remove(helper, caster);
            throw new IllegalStateException("Guillotine failed on the Magician", e);
        }
        GameTestPlayers.remove(helper, caster);
        helper.assertTrue(!magician.isAlive(), "The Magician survived the Guillotine");
        helper.assertItemEntityCountIs(PsitweaksItems.MAGICIANS_HEAD.get(), at, 2.0, 1);
        helper.succeed();
    }
}
