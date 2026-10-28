package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.SpellGram.region.EntitySpellGramRegion;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramCapacity;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramPotencyLoad;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.base.ModCADComponents;

/** 「CAD の規模で数える版」の試作: 枠の大きさ（CAD の規模）、使用量、超えたときの解け方。 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellGramPotencyLoadGameTests {
    private static final String TEMPLATE = "connector_empty";

    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram")
    public static void regionsShareTheCadPotency(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "sgp_a_caster", new Vec3(0.5, 1.0, 0.5));
        List<EntitySpellGramRegion> placed = new ArrayList<>();
        try {
            helper.assertTrue(SpellGramPotencyLoad.capacityOf(caster) == -1, "A caster without a CAD had a load limit");

            var registries = helper.getLevel().registryAccess();
            ItemStack assembly = new ItemStack(ModCADComponents.cadAssemblyIron.get());
            caster.getInventory().setItem(0, ItemCAD.makeCADWithAssembly(registries, assembly, List.of(assembly,
                    new ItemStack(ModCADComponents.cadCoreBasic.get()), new ItemStack(ModCADComponents.cadSocketBasic.get()))));
            int capacity = SpellGramPotencyLoad.capacityOf(caster);
            helper.assertTrue(capacity > 0, "The CAD gave no load capacity: " + capacity);

            int half = capacity / 2;
            EntitySpellGramRegion first = place(helper, caster, half, placed);
            EntitySpellGramRegion second = place(helper, caster, capacity - half, placed);
            helper.assertTrue(!first.isRemoved() && !second.isRemoved(), "A region ended while the load still fit");
            helper.assertTrue(SpellGramPotencyLoad.loadOf(caster) == capacity,
                    "The load was " + SpellGramPotencyLoad.loadOf(caster) + " instead of " + capacity);

            EntitySpellGramRegion third = place(helper, caster, 1, placed);
            helper.assertTrue(first.isRemoved() && !second.isRemoved() && !third.isRemoved(),
                    "Going over the load did not end only the oldest region");
            helper.assertTrue(SpellGramPotencyLoad.loadOf(caster) == capacity - half + 1,
                    "The load did not follow the regions left: " + SpellGramPotencyLoad.loadOf(caster));
        } catch (SpellRuntimeException e) {
            throw new IllegalStateException("Reserving a region failed", e);
        } finally {
            placed.forEach(EntitySpellGramRegion::discard);
            GameTestPlayers.remove(helper, caster);
        }
        helper.assertTrue(SpellGramPotencyLoad.loadOf(caster) == 0, "The load stayed after the regions ended");
        helper.succeed();
    }

    private static EntitySpellGramRegion place(GameTestHelper helper, ServerPlayer caster, int potency,
                                               List<EntitySpellGramRegion> placed) throws SpellRuntimeException {
        int use = SpellGramCapacity.reserve(new SpellContext().setPlayer(caster), 0, potency, null);
        EntitySpellGramRegion region = new EntitySpellGramRegion(helper.getLevel());
        Vec3 at = helper.absoluteVec(new Vec3(1.5 + placed.size(), 1.0, 1.5));
        region.setPos(at.x, at.y + 0.01, at.z);
        region.configure(caster, AttackRules.Options.of(false), 2.0, 1.0F, 0xFFFFFF);
        region.setCapacityUse(use);
        helper.getLevel().addFreshEntity(region);
        placed.add(region);
        return region;
    }
}
