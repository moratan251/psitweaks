package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.SpellGram.region.EntitySpellGramRegion;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramCapacity;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramPsiLock;
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
import vazkii.psi.api.cad.RegenPsiEvent;
import vazkii.psi.common.core.handler.PlayerData;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/** 「Psi を確保する版」の試作: 確保の量、上限を超えたときの解け方、回復の上限と速さ。 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellGramPsiLockGameTests {
    private static final String TEMPLATE = "connector_empty";

    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram")
    public static void lockedPsiCapsAndSlowsRegeneration(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "sgl_a_caster", new Vec3(0.5, 1.0, 0.5));
        List<EntitySpellGramRegion> placed = new ArrayList<>();
        try {
            PlayerData data = PlayerDataHandler.get(caster);
            int total = data.getTotalPsi();
            int limit = SpellGramPsiLock.maxLock(caster);
            int cost = total / 5;

            EntitySpellGramRegion first = place(helper, caster, cost, placed);
            helper.assertTrue(first.getCapacityUse() == cost, "Locked " + first.getCapacityUse() + " instead of " + cost);
            helper.assertTrue(SpellGramPsiLock.lockOf(caster) == cost, "The caster's lock was " + SpellGramPsiLock.lockOf(caster));

            data.availablePsi = total - cost / 2;
            RegenPsiEvent over = new RegenPsiEvent(caster, data, ItemStack.EMPTY);
            SpellGramPsiLock.onRegenPsi(over);
            helper.assertTrue(data.availablePsi == total - cost, "Psi above the cap was not lowered: " + data.availablePsi);
            helper.assertTrue(over.getPlayerRegen() == 0, "Psi regenerated into the locked share");

            data.availablePsi = total / 2;
            RegenPsiEvent regen = new RegenPsiEvent(caster, data, ItemStack.EMPTY);
            int base = regen.getRegenRate();
            SpellGramPsiLock.onRegenPsi(regen);
            int expected = base - (int) Math.round(base * (double) cost / total);
            helper.assertTrue(regen.getPlayerRegen() == expected,
                    "Regeneration was " + regen.getPlayerRegen() + " instead of " + expected);

            EntitySpellGramRegion big = place(helper, caster, limit - cost / 2, placed);
            helper.assertTrue(first.isRemoved(), "The oldest region stayed past the lock limit");
            helper.assertTrue(SpellGramPsiLock.lockOf(caster) == big.getCapacityUse(),
                    "The lock did not follow the regions left: " + SpellGramPsiLock.lockOf(caster));
        } catch (SpellRuntimeException e) {
            throw new IllegalStateException("Reserving a region failed", e);
        } finally {
            placed.forEach(EntitySpellGramRegion::discard);
            GameTestPlayers.remove(helper, caster);
        }
        helper.assertTrue(SpellGramPsiLock.lockOf(caster) == 0, "The lock stayed after the regions ended");
        helper.succeed();
    }

    private static EntitySpellGramRegion place(GameTestHelper helper, ServerPlayer caster, int cost,
                                               List<EntitySpellGramRegion> placed) throws SpellRuntimeException {
        int use = SpellGramCapacity.reserve(new SpellContext().setPlayer(caster), cost, 0, null);
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
