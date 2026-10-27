package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.caster.AutonomousCasterHost;
import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.caster.AutonomousCasterRegistry;
import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.caster.CasterLog;
import com.moratan251.psitweaks.common.caster.VirtualCad;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.entities.EntityMeteorLineBeam;
import com.moratan251.psitweaks.common.entities.EntityPhononMaserBeam;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityFlareCircle;
import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.SpellWarning;
import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageAmount;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageLastTransfer;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.HarvestHelper;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakScheduler;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassHarvestScheduler;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickInteractBlock;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickPullItem;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageOwnerResolver;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.cad.CADComponentLookup;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.entity.EntitySpellCircle;
import vazkii.psi.common.entity.EntitySpellProjectile;
import vazkii.psi.common.entity.ModEntities;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.base.ModCADComponents;
import vazkii.psi.common.network.message.MessageSpellError;

/**
 * 自律詠唱機の GameTest。
 * 作動ログと専用 Caster は Mekanism なしで確認し、機械ブロックの確認は {@link AutonomousCasterChecks} へ委ねる。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AutonomousCasterGameTests {
    private static final String TEMPLATE = "connector_empty";
    /** 所有者役をワールドに入れるテストは、他のテストと同時に動かさない。 */
    private static final String OWNER_ONLINE_BATCH = "autonomous_caster_owner_online";

    // ---- 作動ログ ----

    @GameTest(template = TEMPLATE)
    public static void casterLogMergesRepeatsAndReplacesSpamless(GameTestHelper helper) {
        CasterLog log = new CasterLog(() -> 4, () -> 2048);
        log.add(CasterLog.Source.CASTER_MESSAGE, Component.literal("a"), 1);
        log.add(CasterLog.Source.CASTER_MESSAGE, Component.literal("a"), 2);
        helper.assertTrue(log.size() == 1 && log.entries().get(0).repeatCount() == 2
                        && log.entries().get(0).firstGameTime() == 1 && log.entries().get(0).lastGameTime() == 2,
                "Identical consecutive messages were not merged");
        log.add(CasterLog.Source.MACHINE_ERROR, Component.literal("a"), 3);
        helper.assertTrue(log.size() == 2, "A different source was merged into the previous entry");

        log.addSpamless(Component.literal("first"), 7, 4);
        log.add(CasterLog.Source.CASTER_MESSAGE, Component.literal("b"), 5);
        log.addSpamless(Component.literal("second"), 7, 6);
        List<CasterLog.Entry> entries = log.entries();
        long spamless = entries.stream().filter(CasterLog.Entry::isSpamless).count();
        CasterLog.Entry last = entries.get(entries.size() - 1);
        helper.assertTrue(spamless == 1 && last.isSpamless() && "second".equals(last.message().getString())
                && last.replacementKey() == 7, "Spamless output did not replace the entry with the same key");

        for (int i = 0; i < 6; i++) {
            log.add(CasterLog.Source.CASTER_MESSAGE, Component.literal("n" + i), 10 + i);
        }
        helper.assertTrue(log.size() == 4 && "n5".equals(log.entries().get(3).message().getString())
                && "n2".equals(log.entries().get(0).message().getString()), "The log did not drop its oldest entries");
        long previous = -1;
        for (CasterLog.Entry entry : log.entries()) {
            helper.assertTrue(entry.sequence() > previous, "Log entries are not ordered by sequence");
            previous = entry.sequence();
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void casterLogDeltasRebuildTheClientCopy(GameTestHelper helper) {
        CasterLog server = new CasterLog(() -> 5, () -> 2048);
        CasterLog client = CasterLog.clientCopy();
        long known = 0;

        server.add(CasterLog.Source.CASTER_MESSAGE, Component.literal("one"), 1);
        server.add(CasterLog.Source.MACHINE_ERROR, Component.literal("two").withStyle(Style.EMPTY.withBold(true)), 2);
        known = sync(helper, server, client, known);
        server.add(CasterLog.Source.MACHINE_ERROR, Component.literal("two").withStyle(Style.EMPTY.withBold(true)), 3);
        server.addSpamless(Component.literal("s1"), 2, 4);
        known = sync(helper, server, client, known);
        server.addSpamless(Component.literal("s2"), 2, 5);
        for (int i = 0; i < 4; i++) {
            server.add(CasterLog.Source.CASTER_MESSAGE, Component.literal("x" + i), 6 + i);
        }
        known = sync(helper, server, client, known);
        server.clear();
        server.add(CasterLog.Source.CASTER_MESSAGE, Component.literal("after clear"), 20);
        known = sync(helper, server, client, known);

        // 起点が合わない差分は拒否し、snapshot で立て直せる
        CasterLog stale = CasterLog.clientCopy();
        server.add(CasterLog.Source.CASTER_MESSAGE, Component.literal("late"), 21);
        CasterLog.Delta partial = roundTrip(helper, server.deltaSince(known));
        helper.assertTrue(!partial.snapshot() && !stale.apply(partial), "A client accepted a delta from the wrong base revision");
        helper.assertTrue(stale.apply(roundTrip(helper, server.deltaSince(0))), "A snapshot was rejected");
        assertSame(helper, server, stale);
        helper.succeed();
    }

    private static long sync(GameTestHelper helper, CasterLog server, CasterLog client, long known) {
        CasterLog.Delta delta = roundTrip(helper, server.deltaSince(known));
        helper.assertTrue(client.apply(delta), "The client rejected a delta from its own revision");
        assertSame(helper, server, client);
        return server.revision();
    }

    private static CasterLog.Delta roundTrip(GameTestHelper helper, CasterLog.Delta delta) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            CasterLog.Delta.STREAM_CODEC.encode(buf, delta);
            return CasterLog.Delta.STREAM_CODEC.decode(buf);
        } finally {
            buf.release();
        }
    }

    private static void assertSame(GameTestHelper helper, CasterLog expected, CasterLog actual) {
        List<CasterLog.Entry> left = expected.entries();
        List<CasterLog.Entry> right = actual.entries();
        helper.assertTrue(left.size() == right.size(), "Client log size " + right.size() + " != server " + left.size());
        for (int i = 0; i < left.size(); i++) {
            CasterLog.Entry a = left.get(i);
            CasterLog.Entry b = right.get(i);
            helper.assertTrue(a.sequence() == b.sequence() && a.source() == b.source() && a.repeatCount() == b.repeatCount()
                            && a.replacementKey() == b.replacementKey() && a.message().equals(b.message()),
                    "Client log entry " + i + " differs from the server");
        }
        helper.assertTrue(expected.revision() == actual.revision(), "Client revision did not follow the server");
    }

    @GameTest(template = TEMPLATE)
    public static void casterLogSurvivesSaveAndLoad(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        CasterLog log = new CasterLog(() -> 10, () -> 2048);
        log.add(CasterLog.Source.CASTER_MESSAGE, Component.translatable("psi.spellerror.position", "B", "3"), 5);
        log.add(CasterLog.Source.CASTER_MESSAGE, Component.translatable("psi.spellerror.position", "B", "3"), 6);
        log.addSpamless(Component.literal("spamless"), 4, 7);
        CompoundTag tag = new CompoundTag();
        log.save(tag, registries);

        CasterLog restored = new CasterLog(() -> 10, () -> 2048);
        long before = restored.revision();
        restored.load(tag, registries);
        helper.assertTrue(restored.revision() != before, "Loading did not change the revision, so viewers would keep stale copies");
        helper.assertTrue(restored.size() == 2 && restored.entries().get(0).repeatCount() == 2
                        && restored.entries().get(0).message().equals(log.entries().get(0).message())
                        && restored.entries().get(1).isSpamless() && restored.entries().get(1).replacementKey() == 4,
                "The log did not survive save and load");
        restored.add(CasterLog.Source.CASTER_MESSAGE, Component.literal("next"), 8);
        helper.assertTrue(restored.latest().orElseThrow().sequence() > log.latest().orElseThrow().sequence(),
                "Sequence numbers restarted after loading");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void casterLogLimitsAndSanitizesMessages(GameTestHelper helper) {
        Component longMessage = Component.literal("x".repeat(300));
        Component limited = CasterLog.limitLength(longMessage, 64);
        String text = limited.getString();
        helper.assertTrue(text.length() <= 65 && text.endsWith("…"), "A long message was not cut off: " + text.length());

        Component clickable = Component.literal("click")
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stop")));
        Component sanitized = CasterLog.sanitize(clickable);
        helper.assertTrue(sanitized.getStyle().getClickEvent() == null && "click".equals(sanitized.getString()),
                "A click event survived sanitizing");

        CasterLog log = new CasterLog(() -> 10, () -> 32);
        log.add(CasterLog.Source.CASTER_MESSAGE, clickable.copy().append("y".repeat(100)), 1);
        Component stored = log.entries().get(0).message();
        helper.assertTrue(stored.getString().length() <= 33 && !hasClickEvent(stored), "The log stored an unsafe or overlong message");
        helper.succeed();
    }

    private static boolean hasClickEvent(Component component) {
        if (component.getStyle().getClickEvent() != null) {
            return true;
        }
        for (Component sibling : component.getSiblings()) {
            if (hasClickEvent(sibling)) {
                return true;
            }
        }
        return false;
    }

    // ---- 内蔵 CAD ----

    @GameTest(template = TEMPLATE)
    public static void virtualCadHasFixedStatsAndOnlyTakesAColorizer(GameTestHelper helper) throws Exception {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        ItemStack dye = new ItemStack(ModCADComponents.cadColorizerRed.get());
        helper.assertTrue(VirtualCad.acceptsDye(registries, dye), "The dye slot refused a colorizer");
        helper.assertTrue(!VirtualCad.acceptsDye(registries, new ItemStack(ModCADComponents.cadAssemblyPsimetal.get())),
                "The dye slot accepted an assembly");

        ItemStack cad = VirtualCad.rebuild(registries, dye, ItemStack.EMPTY);
        ICAD item = (ICAD) cad.getItem();
        VirtualCad.FIXED_STATS.forEach((stat, value) -> helper.assertTrue(item.getStatValue(cad, stat) == value,
                "The internal CAD's " + stat + " is " + item.getStatValue(cad, stat) + " instead of " + value));
        helper.assertTrue(item.getStatValue(cad, EnumCADStat.OVERFLOW) == 0, "The internal CAD has a battery");
        helper.assertTrue(item.getSpellColor(cad) == CADComponentLookup.color(registries, dye),
                "The colorizer did not set the spell color");

        // メモリの最後の枠まで使え、着色器を外して組み直しても中身と固定性能が残る。
        int lastSlot = VirtualCad.FIXED_STATS.get(EnumCADStat.SAVED_VECTORS) - 1;
        item.setStoredVector(cad, lastSlot, new Vector3(1, 2, 3));
        ItemStack rebuilt = VirtualCad.rebuild(registries, ItemStack.EMPTY, cad);
        Vector3 kept = item.getStoredVector(rebuilt, lastSlot);
        helper.assertTrue(kept.x == 1 && kept.y == 2 && kept.z == 3, "The memory was lost when the CAD was rebuilt");
        helper.assertTrue(item.getStatValue(rebuilt, EnumCADStat.POTENCY) == VirtualCad.FIXED_STATS.get(EnumCADStat.POTENCY),
                "The rebuilt CAD lost its fixed stats");

        // プレイヤーの CAD の性能値は変えない。
        ItemStack assembly = new ItemStack(ModCADComponents.cadAssemblyIron.get());
        ItemStack playerCad = ItemCAD.makeCADWithAssembly(registries, assembly, List.of(assembly,
                new ItemStack(ModCADComponents.cadCoreBasic.get()), new ItemStack(ModCADComponents.cadSocketBasic.get())));
        helper.assertTrue(!VirtualCad.isVirtualCad(playerCad)
                        && ((ICAD) playerCad.getItem()).getStatValue(playerCad, EnumCADStat.POTENCY) == 100,
                "A player's CAD got the internal CAD's stats");
        helper.succeed();
    }

    // ---- 専用 Caster ----

    @GameTest(template = TEMPLATE)
    public static void dedicatedCasterCastsWithItsHostsPsi(GameTestHelper helper) {
        StubHost host = new StubHost();
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, host);
        try {
            ItemStack cad = VirtualCad.rebuild(helper.getLevel().registryAccess(), ItemStack.EMPTY, ItemStack.EMPTY);
            helper.assertTrue(VirtualCad.isUsable(cad), "The default virtual CAD is not usable");
            caster.getInventory().setItem(AutonomousCasterPlayer.CAD_SLOT, cad);
            Vec3 at = helper.absoluteVec(new Vec3(2.5, 1.0, 2.5));
            caster.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);

            host.psi = 1000;
            Optional<ArrayList<Entity>> debug = cast(helper, caster, host, AutonomousCasterTestSpells.debugBullet(42), cad);
            helper.assertTrue(debug.isPresent(), "The dedicated caster could not cast a debug spell");
            helper.assertTrue(host.messages.size() == 1 && host.messages.get(0).getString().contains("42"),
                    "Debug output did not reach the host: " + host.messages);

            host.psi = 10;
            Optional<ArrayList<Entity>> refused = cast(helper, caster, host, AutonomousCasterTestSpells.blinkBullet(1), cad);
            helper.assertTrue(refused.isEmpty() && host.insufficientCost > 10 && host.insufficientAvailable == 10,
                    "A spell costing more than the host's Psi was not refused");
            helper.assertTrue(PlayerDataHandler.get(caster).availablePsi == 10, "A refused spell still spent Psi");

            host.psi = 1000;
            host.insufficientCost = -1;
            Optional<ArrayList<Entity>> blink = cast(helper, caster, host, AutonomousCasterTestSpells.blinkBullet(1), cad);
            int left = PlayerDataHandler.get(caster).availablePsi;
            helper.assertTrue(blink.isPresent() && left < 1000 && left > 0 && host.insufficientCost < 0,
                    "An affordable spell did not spend the host's Psi (left " + left + ")");

            caster.capturePsiPayload(new MessageSpellError("psi.spellerror.position", 2, 5));
            Component position = host.messages.get(host.messages.size() - 1);
            helper.assertTrue(position.getContents() instanceof TranslatableContents contents
                            && "psi.spellerror.position".equals(contents.getKey())
                            && contents.getArgs().length == 2 && "B".equals(contents.getArgs()[0]),
                    "The spell error position was not converted to a grid column");

            helper.assertTrue(AutonomousCasterRegistry.find(helper.getLevel(), caster.casterKey()) == caster,
                    "The attached caster could not be found by its key");
            caster.detach();
            int count = host.messages.size();
            caster.sendSystemMessage(Component.literal("after detach"));
            helper.assertTrue(host.messages.size() == count, "A detached caster still reported to its host");
            helper.assertTrue(AutonomousCasterRegistry.find(helper.getLevel(), caster.casterKey()) == null,
                    "A detached caster was still registered");
            Optional<ArrayList<Entity>> detached = cast(helper, caster, host, AutonomousCasterTestSpells.debugBullet(1), cad);
            helper.assertTrue(detached.isEmpty(), "A detached caster could still cast");
        } finally {
            caster.detach();
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void dedicatedCasterLogsSpellWarningsWithTheirPosition(GameTestHelper helper) {
        StubHost host = new StubHost();
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, host);
        try {
            Spell spell = new Spell();
            var piece = new PieceSelectorIdeaStorageLastTransfer(spell);
            piece.x = 2;
            piece.y = 4;
            CompiledSpell compiled = new CompiledSpell(spell);
            SpellContext context = new SpellContext().setPlayer(caster).setCompiledSpell(compiled);
            context.currentAction = compiled.new Action(piece);
            SpellWarning.warn(context, SpellWarning.TARGET_FALLEN);

            helper.assertTrue(host.messages.size() == 2, "A warning should log its text and its position: " + host.messages);
            helper.assertTrue(host.messages.get(0).getContents() instanceof TranslatableContents warning
                            && SpellWarning.TARGET_FALLEN.equals(warning.getKey()),
                    "The warning text did not reach the log");
            Component position = host.messages.get(1);
            helper.assertTrue(position.getContents() instanceof TranslatableContents contents
                            && SpellWarning.POSITION.equals(contents.getKey())
                            && contents.getArgs().length == 2 && "C".equals(contents.getArgs()[0])
                            && Integer.valueOf(5).equals(contents.getArgs()[1])
                            && position.getStyle().getColor() != null
                            && position.getStyle().getColor().equals(TextColor.fromLegacyFormat(ChatFormatting.YELLOW)),
                    "The warning position was not logged as a yellow grid position: " + position);
        } finally {
            caster.detach();
        }
        helper.succeed();
    }

    /** 術者を UUID で引き直す術式エンティティ（フォノンメーザーの光線など）も、専用 Caster を術者として見つける。 */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void phononMaserFromTheDedicatedCasterKeepsFiring(GameTestHelper helper) {
        StubHost host = new StubHost();
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, host);
        Vec3 at = helper.absoluteVec(new Vec3(0.5, 1.0, 2.5));
        caster.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        Husk target = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(3.5, 1.0, 2.5));
        target.setNoAi(true);
        caster.lookAt(EntityAnchorArgument.Anchor.EYES, target.position().add(0.0, target.getBbHeight() / 2.0, 0.0));
        helper.assertTrue(CombatSpellHelper.findLiving(helper.getLevel(), caster.casterKey()) == caster,
                "The dedicated caster could not be found by its key");

        EntityPhononMaserBeam beam = new EntityPhononMaserBeam(helper.getLevel(), caster, 1, 40);
        helper.getLevel().addFreshEntity(beam);
        float health = target.getHealth();
        helper.runAfterDelay(5, () -> {
            try {
                helper.assertTrue(beam.isAlive(), "The beam vanished because it lost its caster");
                helper.assertTrue(!target.isAlive() || target.getHealth() < health, "The beam did not hurt the target");
                caster.detach();
                helper.assertTrue(CombatSpellHelper.findLiving(helper.getLevel(), caster.casterKey()) == null,
                        "A detached caster was still found by its key");
                helper.succeed();
            } finally {
                caster.detach();
                beam.discard();
            }
        });
    }

    @GameTest(template = TEMPLATE)
    public static void massBlockBreakFromTheDedicatedCasterCollectsDrops(GameTestHelper helper) throws SpellRuntimeException {
        StubHost host = new StubHost();
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, host);
        Vec3 at = helper.absoluteVec(new Vec3(0.5, 1.0, 0.5));
        caster.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        BlockPos relative = new BlockPos(2, 1, 2);
        BlockPos target = helper.absolutePos(relative);
        helper.setBlock(relative, Blocks.STONE);
        Vector3 focal = new Vector3(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        MassBlockBreakScheduler.schedule(caster, helper.getLevel(), focal, 1.0, List.of(target),
                new ItemStack(Items.DIAMOND_PICKAXE), state -> true);
        helper.runAfterDelay(10, () -> {
            try {
                helper.assertBlockPresent(Blocks.AIR, relative);
                helper.assertTrue(caster.getInventory().countItem(Items.COBBLESTONE) == 1,
                        "The broken block's drop did not reach the dedicated caster");
                helper.succeed();
            } finally {
                caster.detach();
            }
        });
    }

    @GameTest(template = TEMPLATE)
    public static void paradeDecoyFollowsTheDedicatedCasterUntilDetached(GameTestHelper helper) {
        StubHost host = new StubHost();
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, host);
        EntityParadeDecoy decoy = new EntityParadeDecoy(helper.getLevel(), helper.absoluteVec(new Vec3(2.5, 1.0, 2.5)));
        try {
            decoy.configure(caster, 3, 200, 0xFFFFFF);
            helper.assertTrue(decoy.getOwnerPlayer() == caster, "The decoy lost the dedicated caster");
            caster.detach();
            helper.assertTrue(decoy.getOwnerPlayer() == null, "The decoy kept following a detached caster");
            helper.succeed();
        } finally {
            caster.detach();
            decoy.discard();
        }
    }

    /**
     * 専用 Caster が出す分身は、機械の所有者の姿と名前で機械の手持ちを持つ。頭の内蔵 CAD は写さず、Trick: Break Block が
     * 手を内蔵 CAD に持ち替えている間も写さない。分身の持ち主は機械ごとの内部キーのまま（所有者本人を隠さない）。
     */
    @GameTest(template = TEMPLATE)
    public static void paradeDecoyOfTheDedicatedCasterLooksLikeTheMachineOwner(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        StubHost host = new StubHost();
        host.ownerName = "psitweaks_owner";
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), owner, host);
        AutonomousCasterPlayer ownerless = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, new StubHost());
        ItemStack cad = VirtualCad.rebuild(helper.getLevel().registryAccess(), ItemStack.EMPTY, ItemStack.EMPTY);
        caster.getInventory().setItem(AutonomousCasterPlayer.CAD_SLOT, cad);
        caster.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1.0, 2.5));
        caster.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        EntityParadeDecoy decoy = new EntityParadeDecoy(helper.getLevel(), at);
        EntityParadeDecoy anonymous = new EntityParadeDecoy(helper.getLevel(), at);
        boolean ticking = false;
        try {
            decoy.configure(caster, 3, 200, 0xFFFFFF);
            helper.assertTrue(decoy.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "The decoy wears the internal CAD");
            helper.assertTrue(decoy.getMainHandItem().is(Items.IRON_SWORD), "The decoy does not hold the machine's item");
            helper.assertTrue(decoy.getCustomName() != null && "psitweaks_owner".equals(decoy.getCustomName().getString()),
                    "The decoy is not named after the machine owner: " + decoy.getCustomName());
            helper.assertTrue(owner.equals(decoy.getLookUUID()), "The decoy does not wear the machine owner's skin");
            helper.assertTrue(caster.casterKey().equals(decoy.getOwnerUUID()), "The decoy is not kept by the machine's key");

            anonymous.configure(ownerless, 3, 200, 0xFFFFFF);
            helper.assertTrue(anonymous.getCustomName() == null && ownerless.casterKey().equals(anonymous.getLookUUID()),
                    "The decoy of an ownerless machine took a name or someone's skin");

            // 分身は毎 tick 持ち主の装備を写し直す。
            caster.setItemInHand(InteractionHand.MAIN_HAND, cad);
            helper.getLevel().addFreshEntity(decoy);
            ticking = true;
            helper.runAfterDelay(2, () -> {
                try {
                    helper.assertTrue(decoy.isAlive() && decoy.getMainHandItem().isEmpty()
                                    && decoy.getItemBySlot(EquipmentSlot.HEAD).isEmpty(),
                            "The decoy copied the internal CAD held while breaking a block");
                    helper.succeed();
                } finally {
                    caster.detach();
                    decoy.discard();
                }
            });
        } finally {
            ownerless.detach();
            anonymous.discard();
            if (!ticking) {
                caster.detach();
                decoy.discard();
            }
        }
    }

    /**
     * 機械から外れた専用 Caster は、手と防具の枠に物を受け取らない。機械自身を壊した Trick: Break Block が持ち替えを
     * 戻しても、機械のアイテムと一緒に落ちた物の写しが残らない。内蔵 CAD への持ち替えだけは受け取る。
     */
    @GameTest(template = TEMPLATE)
    public static void aDetachedCasterTakesNothingBackIntoItsHands(GameTestHelper helper) {
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, new StubHost());
        ItemStack cad = VirtualCad.rebuild(helper.getLevel().registryAccess(), ItemStack.EMPTY, ItemStack.EMPTY);
        try {
            caster.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
            helper.assertTrue(caster.getMainHandItem().is(Items.IRON_SWORD), "An attached caster could not hold an item");
            caster.detach();
            caster.setItemInHand(InteractionHand.MAIN_HAND, cad);
            helper.assertTrue(caster.getMainHandItem() == cad, "A detached caster could not take the internal CAD in hand");
            caster.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
            caster.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.BREAD));
            caster.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            helper.assertTrue(caster.getMainHandItem().isEmpty() && caster.getOffhandItem().isEmpty()
                            && caster.getItemBySlot(EquipmentSlot.CHEST).isEmpty(),
                    "A detached caster took items back into its hands");
            helper.succeed();
        } finally {
            caster.detach();
        }
    }

    /** 機械の外へ出た内蔵 CAD は、プレイヤーの持ち物に入ると消える。プレイヤーの CAD と専用 Caster の内蔵 CAD は残る。 */
    @GameTest(template = TEMPLATE)
    public static void anEscapedInternalCadVanishesFromAPlayersInventory(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "[psitweaks_test]"));
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, new StubHost());
        try {
            ItemStack escaped = VirtualCad.rebuild(registries, ItemStack.EMPTY, ItemStack.EMPTY);
            player.getInventory().setItem(0, escaped);
            escaped.inventoryTick(helper.getLevel(), player, 0, true);
            helper.assertTrue(escaped.isEmpty() && player.getInventory().getItem(0).isEmpty(),
                    "An internal CAD stayed in a player's inventory");

            ItemStack assembly = new ItemStack(ModCADComponents.cadAssemblyIron.get());
            ItemStack playerCad = ItemCAD.makeCADWithAssembly(registries, assembly, List.of(assembly,
                    new ItemStack(ModCADComponents.cadCoreBasic.get()), new ItemStack(ModCADComponents.cadSocketBasic.get())));
            playerCad.inventoryTick(helper.getLevel(), player, 1, false);
            helper.assertTrue(!playerCad.isEmpty(), "A player's own CAD vanished");

            ItemStack kept = VirtualCad.rebuild(registries, ItemStack.EMPTY, ItemStack.EMPTY);
            caster.getInventory().setItem(AutonomousCasterPlayer.CAD_SLOT, kept);
            kept.inventoryTick(helper.getLevel(), caster, AutonomousCasterPlayer.CAD_SLOT, false);
            helper.assertTrue(!kept.isEmpty(), "The dedicated caster lost its internal CAD");
            helper.succeed();
        } finally {
            caster.detach();
        }
    }

    @GameTest(template = TEMPLATE)
    public static void massHarvestDropsAtTheFocalPointOnceTheMachineIsGone(GameTestHelper helper) throws SpellRuntimeException {
        StubHost host = new StubHost();
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, host);
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1.0, 3.5));
        caster.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        BlockPos reached = new BlockPos(1, 1, 1);
        BlockPos notReached = new BlockPos(3, 1, 1);
        for (BlockPos pos : List.of(reached, notReached)) {
            helper.setBlock(pos.below(), Blocks.FARMLAND);
            helper.setBlock(pos, ((CropBlock) Blocks.WHEAT).getStateForAge(7));
        }
        HarvestHelper.Harvest harvest = HarvestHelper.begin(new SpellContext().setPlayer(caster));
        helper.assertTrue(harvest != null && harvest.harvest(helper.absolutePos(reached)), "The first crop was not harvested");
        BlockPos focalPoint = helper.absolutePos(new BlockPos(2, 1, 3));
        MassHarvestScheduler.schedule(harvest, List.of(helper.absolutePos(notReached)), focalPoint);
        // The machine is removed before the scheduler reaches the second crop.
        caster.detach();
        helper.succeedWhen(() -> {
            var dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(focalPoint).inflate(2));
            helper.assertTrue(dropped.stream().anyMatch(item -> item.getItem().is(Items.WHEAT)),
                    "The harvest so far did not drop at the focal point");
            helper.assertBlockState(notReached, state -> state.getValue(CropBlock.AGE) == 7,
                    () -> "A detached caster kept harvesting");
            helper.assertTrue(caster.getInventory().isEmpty(), "The harvest went to a detached caster");
            helper.killAllEntitiesOfClass(ItemEntity.class);
        });
    }

    @GameTest(template = TEMPLATE)
    public static void pullItemFromTheDedicatedCasterStopsWhenTheMachineSlotsAreFull(GameTestHelper helper) {
        StubHost host = new StubHost();
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, host);
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1.0, 2.5));
        caster.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CHEST);
        BlockPos chestPos = helper.absolutePos(new BlockPos(1, 1, 1));
        ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(Items.DIRT, 64));
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            caster.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
        }
        var pull = new PieceTrickPullItem(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(PsitweaksSpellParams.MAX) ? null : IdeaLogisticsGameTests.paramValue(param, chestPos, 0, null);
            }
        };
        SpellContext context = new SpellContext().setPlayer(caster);
        try {
            boolean threw = false;
            try {
                pull.execute(context);
            } catch (SpellRuntimeException e) {
                threw = true;
            }
            helper.assertTrue(threw && chest.getItem(0).getCount() == 64 && caster.getInventory().countItem(Items.DIRT) == 0,
                    "Pull Item moved items although the machine slots were full");
            // ホットバー以外の27枠も機械のスロットなので、そこが空けば引き出す。
            caster.getInventory().setItem(30, ItemStack.EMPTY);
            try {
                pull.execute(context);
            } catch (SpellRuntimeException e) {
                throw new IllegalStateException("Pull Item did not use the free machine slot", e);
            }
            helper.assertTrue(chest.getItem(0).isEmpty() && caster.getInventory().getItem(30).is(Items.DIRT),
                    "Pull Item did not move the stack into the free machine slot");
            helper.succeed();
        } finally {
            caster.detach();
        }
    }

    @GameTest(template = TEMPLATE)
    public static void interactBlockFromTheDedicatedCasterUsesTheSelectedSlot(GameTestHelper helper) {
        StubHost host = new StubHost();
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, host);
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1.0, 2.5));
        caster.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        BlockPos relative = new BlockPos(1, 1, 1);
        helper.setBlock(relative, Blocks.CAULDRON);
        BlockPos cauldron = helper.absolutePos(relative);
        caster.getInventory().selected = 0;
        caster.getInventory().setItem(0, new ItemStack(Items.WATER_BUCKET));
        var interact = new PieceTrickInteractBlock(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return new Vector3(cauldron.getX(), cauldron.getY(), cauldron.getZ());
            }
        };
        try {
            interact.execute(new SpellContext().setPlayer(caster));
            helper.assertBlockPresent(Blocks.WATER_CAULDRON, relative);
            helper.assertTrue(caster.getInventory().getItem(0).is(Items.BUCKET), "The water bucket in the selected slot was not used");
            helper.succeed();
        } catch (SpellRuntimeException e) {
            throw new IllegalStateException("Interact Block failed from the dedicated caster", e);
        } finally {
            caster.detach();
        }
    }

    @GameTest(template = TEMPLATE)
    public static void theDedicatedCasterUsesTheOwnersLanguage(GameTestHelper helper) {
        StubHost host = new StubHost();
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, host);
        try {
            String fallback = caster.getLanguage();
            host.language = "ja_jp";
            helper.assertTrue("ja_jp".equals(caster.getLanguage()), "The caster did not use the owner's language");
            host.language = null;
            helper.assertTrue(fallback.equals(caster.getLanguage()), "The caster lost its default language");
            host.language = "ja_jp";
            caster.detach();
            helper.assertTrue(fallback.equals(caster.getLanguage()), "A detached caster still used the machine's language");
            helper.succeed();
        } finally {
            caster.detach();
        }
    }

    // ---- 所有者の UUID ----

    /** 専用 Caster は所有者の UUID を名乗り、レジストリは機械ごとの内部キーで引く。同じ所有者の機械も別々に引ける。 */
    @GameTest(template = TEMPLATE)
    public static void theDedicatedCasterCarriesItsOwnersUuid(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        StubHost host = new StubHost();
        host.owner = owner;
        AutonomousCasterPlayer first = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), owner, host);
        AutonomousCasterPlayer second = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), owner, host);
        AutonomousCasterPlayer ownerless = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, new StubHost());
        try {
            helper.assertTrue(first.getUUID().equals(owner) && second.getUUID().equals(owner)
                            && first.getGameProfile().getId().equals(owner),
                    "The dedicated caster did not carry its owner's UUID");
            helper.assertTrue(AutonomousCasterPlayer.NAME.equals(first.getGameProfile().getName()),
                    "The dedicated caster lost its fake player name");
            helper.assertTrue(!first.casterKey().equals(owner) && !first.casterKey().equals(second.casterKey())
                            && CasterIds.idOf(first).equals(first.casterKey()),
                    "The machine key was not kept apart from the owner's UUID");
            helper.assertTrue(AutonomousCasterRegistry.find(helper.getLevel(), first.casterKey()) == first
                            && AutonomousCasterRegistry.find(helper.getLevel(), second.casterKey()) == second,
                    "Two machines of the same owner were not found separately");
            helper.assertTrue(AutonomousCasterRegistry.find(helper.getLevel(), owner) == null,
                    "The owner's UUID found a dedicated caster");
            helper.assertTrue(ownerless.getUUID().equals(ownerless.casterKey()), "An ownerless machine did not carry its own key");
            helper.succeed();
        } finally {
            first.detach();
            second.detach();
            ownerless.detach();
        }
    }

    /** 専用 Caster は所有者の UUID を名乗るので、戦闘魔法は所有者と所有者のペットを巻き込まない。 */
    @GameTest(template = TEMPLATE)
    public static void combatSpellsFromTheMachineSpareItsOwnerAndTheirPets(GameTestHelper helper) {
        FakePlayer owner = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "[psitweaks_owner]"));
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), owner.getUUID(), new StubHost());
        Wolf pet = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(1, 1, 1));
        pet.setTame(true, false);
        pet.setOwnerUUID(owner.getUUID());
        Wolf stray = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(3, 1, 1));
        Wolf angry = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(3, 1, 3));
        angry.setPersistentAngerTarget(owner.getUUID());
        try {
            helper.assertTrue(!CombatSpellHelper.canHarm(caster, pet, false), "A combat spell of the machine would hit its owner's pet");
            helper.assertTrue(!CombatSpellHelper.canHarm(caster, owner, false), "A combat spell of the machine would hit its owner");
            helper.assertTrue(CombatSpellHelper.canHarm(caster, stray, false), "The machine spared a wolf that nobody owns");
            helper.assertTrue(CombatSpellHelper.isHostileTarget(caster, angry, false),
                    "A wolf angry at the owner is not a target of the owner's machine");
            helper.succeed();
        } finally {
            caster.detach();
            helper.killAllEntitiesOfClass(Wolf.class);
        }
    }

    /** 所有者がオンラインでも、Psi の術式円と術式弾は保存・再読み込みの後まで専用 Caster を術者にし、本人を術者にしない。 */
    @GameTest(template = TEMPLATE, batch = OWNER_ONLINE_BATCH)
    public static void psiSpellEntitiesKeepTheDedicatedCasterWhileItsOwnerIsOnline(GameTestHelper helper) {
        FakePlayer owner = onlineOwner(helper, new Vec3(4.5, 1.0, 4.5));
        StubHost host = new StubHost();
        AutonomousCasterPlayer first = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), owner.getUUID(), host);
        AutonomousCasterPlayer second = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), owner.getUUID(), host);
        try {
            helper.assertTrue(helper.getLevel().getPlayerByUUID(owner.getUUID()) == owner, "The test owner is not online");
            EntitySpellCircle circle = new EntitySpellCircle(ModEntities.spellCircle.get(), helper.getLevel())
                    .setInfo(first, ItemStack.EMPTY, ItemStack.EMPTY);
            EntitySpellCircle otherCircle = new EntitySpellCircle(ModEntities.spellCircle.get(), helper.getLevel())
                    .setInfo(second, ItemStack.EMPTY, ItemStack.EMPTY);
            helper.assertTrue(circle.getCaster() == first && otherCircle.getCaster() == second,
                    "A spell circle cast by the machine found someone else: " + circle.getCaster());
            helper.assertTrue(reload(helper, circle) instanceof EntitySpellCircle loaded && loaded.getCaster() == first,
                    "A reloaded spell circle lost the machine");

            EntitySpellProjectile projectile = new EntitySpellProjectile(helper.getLevel(), first)
                    .setInfo(first, ItemStack.EMPTY, ItemStack.EMPTY);
            helper.assertTrue(projectile.getOwner() == first, "A spell projectile cast by the machine has another owner");
            Entity reloaded = reload(helper, projectile);
            helper.assertTrue(reloaded instanceof EntitySpellProjectile loaded && loaded.getOwner() == first,
                    "A reloaded spell projectile took someone else as its caster: "
                            + (reloaded instanceof Projectile loaded ? loaded.getOwner() : null));

            first.detach();
            helper.assertTrue(circle.getCaster() == null && ((Projectile) reloaded).getOwner() == null,
                    "The spell entities of a removed machine fell back to its owner");
            helper.succeed();
        } finally {
            first.detach();
            second.detach();
            removeOwner(helper, owner);
        }
    }

    /** 所有者がオンラインでも、術者を UUID で引き直す Psitweaks の術式エンティティは本人ではなく専用 Caster を見つける。 */
    @GameTest(template = TEMPLATE, batch = OWNER_ONLINE_BATCH, timeoutTicks = 100)
    public static void spellEntitiesFollowTheMachineNotItsOnlineOwner(GameTestHelper helper) {
        FakePlayer owner = onlineOwner(helper, new Vec3(4.5, 1.0, 4.5));
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), owner.getUUID(), new StubHost());
        Vec3 at = helper.absoluteVec(new Vec3(0.5, 1.0, 2.5));
        caster.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        Husk target = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(3.5, 1.0, 2.5));
        target.setNoAi(true);
        caster.lookAt(EntityAnchorArgument.Anchor.EYES, target.position().add(0.0, target.getBbHeight() / 2.0, 0.0));
        EntityFlareCircle circle = new EntityFlareCircle(helper.getLevel());
        EntityParadeDecoy decoy = new EntityParadeDecoy(helper.getLevel(), helper.absoluteVec(new Vec3(2.5, 1.0, 0.5)));
        EntityPhononMaserBeam beam = new EntityPhononMaserBeam(helper.getLevel(), caster, 1, 40);
        Runnable cleanup = () -> {
            caster.detach();
            beam.discard();
            decoy.discard();
            removeOwner(helper, owner);
        };
        try {
            UUID key = CasterIds.idOf(caster);
            helper.assertTrue(CombatSpellHelper.findLiving(helper.getLevel(), key) == caster
                            && CasterIds.find(helper.getLevel(), key) == caster,
                    "The machine key did not find the dedicated caster");
            helper.assertTrue(CombatSpellHelper.findLiving(helper.getLevel(), owner.getUUID()) == owner,
                    "The real owner could no longer be found by their UUID");

            circle.setCaster(caster);
            circle.setFollowTarget(caster, true);
            helper.assertTrue(circle.getCaster() == caster && circle.getFollowTargetEntity() == caster,
                    "A Flare Circle cast by the machine followed someone else");

            decoy.configure(caster, 3, 200, 0xFFFFFF);
            helper.assertTrue(!owner.getUUID().equals(decoy.getOwnerUUID()) && !decoy.isOwnedBy(owner)
                            && decoy.isOwnedBy(caster) && decoy.getOwnerPlayer() == caster,
                    "The Parade lookalike of the machine stands for its owner, who would be hidden");

            helper.getLevel().addFreshEntity(beam);
        } catch (RuntimeException e) {
            cleanup.run();
            throw e;
        }
        float health = target.getHealth();
        helper.runAfterDelay(5, () -> {
            try {
                // The start is synced as floats, which are a whole block apart this far out where the tests are placed.
                Vec3 start = beam.getStartPos();
                helper.assertTrue(beam.isAlive() && start.distanceTo(caster.getEyePosition()) < 1.5
                                && start.distanceTo(caster.getEyePosition()) < start.distanceTo(owner.getEyePosition()),
                        "The Phonon Maser beam of the machine followed its owner: " + start);
                helper.assertTrue(!target.isAlive() || target.getHealth() < health, "The beam did not hurt the target in front of the machine");
                helper.succeed();
            } finally {
                cleanup.run();
            }
        });
    }

    /**
     * 機械の分身は設置者の代わりに敵を引きつける。設置者を狙う敵も、狙う相手のいない敵対 mob も分身へ向かい、中立の mob は
     * 挑発しない。設置者自身の分身ではないので、設置者は分身に隠れた扱いにならない。
     */
    @GameTest(template = TEMPLATE, batch = OWNER_ONLINE_BATCH)
    public static void paradeDecoyOfTheMachineDrawsMobsInItsPlacersStead(GameTestHelper helper) {
        FakePlayer owner = onlineOwner(helper, new Vec3(4.5, 1.0, 4.5));
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), owner.getUUID(), new StubHost());
        Vec3 at = helper.absoluteVec(new Vec3(0.5, 1.0, 0.5));
        caster.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        Husk afterPlacer = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 1.0, 4.5));
        Husk idle = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(4.5, 1.0, 1.5));
        ZombifiedPiglin neutral = helper.spawnWithNoFreeWill(EntityType.ZOMBIFIED_PIGLIN, new Vec3(3.5, 1.0, 0.5));
        EntityParadeDecoy decoy = null;
        try {
            afterPlacer.setTarget(owner);
            helper.assertTrue(afterPlacer.getTarget() == owner, "The husk did not go for the placer to begin with");
            decoy = Parades.conjure(helper.getLevel(), caster, helper.absoluteVec(new Vec3(2.5, 1.0, 2.5)), 3, 200);
            helper.assertTrue(afterPlacer.getTarget() == decoy, "A mob after the placer did not turn to the machine's lookalike");
            helper.assertTrue(idle.getTarget() == decoy, "A hostile mob with no target did not notice the machine's lookalike");
            helper.assertTrue(neutral.getTarget() == null, "The machine's lookalike provoked a neutral mob");
            afterPlacer.setTarget(owner);
            helper.assertTrue(afterPlacer.getTarget() == decoy, "A mob that picks the placer again did not get the lookalike");
            helper.assertTrue(Parades.activeDecoy(owner) == null, "The placer counts as hiding behind the machine's lookalike");
            decoy.vanish(false);
            afterPlacer.setTarget(owner);
            helper.assertTrue(afterPlacer.getTarget() == owner, "Once the lookalike is gone the placer can be targeted");
            helper.succeed();
        } finally {
            caster.detach();
            if (decoy != null) {
                decoy.discard();
            }
            // kill() would drop loot that other tests' "nothing dropped" checks could see.
            afterPlacer.discard();
            idle.discard();
            neutral.discard();
            removeOwner(helper, owner);
        }
    }

    /** 所有者がオンラインでも、機械が一括破壊したブロックのドロップは本人ではなく機械に届く。 */
    @GameTest(template = TEMPLATE, batch = OWNER_ONLINE_BATCH)
    public static void massBlockBreakFromTheMachineIgnoresItsOnlineOwner(GameTestHelper helper) throws SpellRuntimeException {
        FakePlayer owner = onlineOwner(helper, new Vec3(4.5, 1.0, 4.5));
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), owner.getUUID(), new StubHost());
        Vec3 at = helper.absoluteVec(new Vec3(0.5, 1.0, 0.5));
        caster.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        BlockPos relative = new BlockPos(2, 1, 2);
        BlockPos target = helper.absolutePos(relative);
        helper.setBlock(relative, Blocks.STONE);
        Vector3 focal = new Vector3(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        try {
            MassBlockBreakScheduler.schedule(caster, helper.getLevel(), focal, 1.0, List.of(target),
                    new ItemStack(Items.DIAMOND_PICKAXE), state -> true);
        } catch (SpellRuntimeException | RuntimeException e) {
            caster.detach();
            removeOwner(helper, owner);
            throw e;
        }
        helper.runAfterDelay(10, () -> {
            try {
                helper.assertBlockPresent(Blocks.AIR, relative);
                helper.assertTrue(caster.getInventory().countItem(Items.COBBLESTONE) == 1
                                && owner.getInventory().countItem(Items.COBBLESTONE) == 0,
                        "The broken block's drop did not reach the machine");
                helper.succeed();
            } finally {
                caster.detach();
                removeOwner(helper, owner);
            }
        });
    }

    /**
     * 機械が撃った作動式: フォノンメーザーと作動式: 流星群の光線は、PvP が有効でも所有者と所有者のペットに当たらない。
     * 同じ線上の奥にいるハスクと野良のオオカミには当たる。
     */
    @GameTest(template = TEMPLATE, batch = OWNER_ONLINE_BATCH, timeoutTicks = 200)
    public static void beamsFromTheMachineSpareItsOwnerAndTheirPets(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        boolean pvp = level.getServer().isPvpAllowed();
        // 線は2本: z=1 に所有者と奥のハスク、z=3 にペットと奥の野良のオオカミ。光線はそれぞれの胴の高さを通す。
        // 光線の始点・終点は float で同期されるので、ずれないよう線の z は整数の座標にする（docs/pitfalls.md PIT-055）。
        ServerPlayer owner = survivalOwner(helper, new Vec3(2.5, 1.0, 1.0));
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(4.0, 1.0, 1.0));
        husk.setNoAi(true);
        Wolf pet = helper.spawnWithNoFreeWill(EntityType.WOLF, new Vec3(2.5, 1.0, 3.0));
        pet.setNoAi(true);
        pet.setTame(true, false);
        pet.setOwnerUUID(owner.getUUID());
        Wolf stray = helper.spawnWithNoFreeWill(EntityType.WOLF, new Vec3(4.0, 1.0, 3.0));
        // 落ちて光線の高さから外れないよう、その場に止める。
        stray.setNoAi(true);
        AutonomousCasterPlayer ownerLine = beamCaster(helper, owner.getUUID(), 1.0, owner.getBbHeight() / 2.0);
        AutonomousCasterPlayer petLine = beamCaster(helper, owner.getUUID(), 3.0, pet.getBbHeight() / 2.0);
        List<Entity> beams = new ArrayList<>();
        float ownerHealth = owner.getHealth();
        float petHealth = pet.getHealth();
        float huskHealth = husk.getHealth();
        float strayHealth = stray.getHealth();
        Runnable cleanup = () -> {
            beams.forEach(Entity::discard);
            ownerLine.detach();
            petLine.detach();
            removeOwner(helper, owner);
            level.getServer().setPvpAllowed(pvp);
            helper.killAllEntitiesOfClass(Wolf.class);
        };
        // 入った直後のプレイヤーの無敵（60 tick）が切れてから、PvP を有効にして撃つ。PvP は後始末で元に戻す。
        helper.runAfterDelay(61, guarded(cleanup, () -> {
            level.getServer().setPvpAllowed(true);
            helper.assertTrue(!owner.isCreative() && !owner.isSpectator() && owner.canHarmPlayer(ownerLine),
                    "The test owner could not be hurt by another player");
            for (AutonomousCasterPlayer caster : List.of(ownerLine, petLine)) {
                EntityPhononMaserBeam beam = new EntityPhononMaserBeam(level, caster, 1, 40);
                level.addFreshEntity(beam);
                beams.add(beam);
            }
        }));
        helper.runAfterDelay(66, guarded(cleanup, () -> {
            helper.assertTrue(husk.getHealth() < huskHealth && (!stray.isAlive() || stray.getHealth() < strayHealth),
                    "The Phonon Maser beams missed their lines");
            helper.assertTrue(owner.getHealth() == ownerHealth, "The Phonon Maser beam of the machine hit its owner");
            helper.assertTrue(pet.getHealth() == petHealth, "The Phonon Maser beam of the machine hit its owner's pet");
            beams.forEach(Entity::discard);
            beams.clear();
            for (AutonomousCasterPlayer caster : List.of(ownerLine, petLine)) {
                EntityMeteorLineBeam beam = new EntityMeteorLineBeam(level, caster, caster.getEyePosition(),
                        new Vec3(1.0, 0.0, 0.0), 4.0);
                level.addFreshEntity(beam);
                beams.add(beam);
            }
        }));
        helper.runAfterDelay(70, guarded(cleanup, () -> {
            helper.assertTrue(!husk.isAlive() && !stray.isAlive(), "The Meteor beams missed their lines");
            helper.assertTrue(owner.isAlive() && owner.getHealth() == ownerHealth, "The Meteor beam of the machine hit its owner");
            helper.assertTrue(pet.isAlive() && pet.getHealth() == petHealth, "The Meteor beam of the machine hit its owner's pet");
            cleanup.run();
            helper.succeed();
        }));
    }

    /** 失敗したら後始末をしてから、その失敗を GameTest へ伝える。 */
    private static Runnable guarded(Runnable cleanup, Runnable step) {
        return () -> {
            try {
                step.run();
            } catch (RuntimeException | Error e) {
                cleanup.run();
                throw e;
            }
        };
    }

    /**
     * 所有者役。ワールドのプレイヤー一覧に入れ、UUID での検索で本人が見つかる状態にする。
     * 他のテストの敵や選択子に拾われないよう、スペクテイターにして専用のバッチで動かす。
     */
    private static FakePlayer onlineOwner(GameTestHelper helper, Vec3 relative) {
        FakePlayer owner = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "[psitweaks_owner]"));
        Vec3 at = helper.absoluteVec(relative);
        owner.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        owner.setGameMode(GameType.SPECTATOR);
        helper.getLevel().addNewPlayer(owner);
        return owner;
    }

    private static void removeOwner(GameTestHelper helper, ServerPlayer owner) {
        GameTestPlayers.remove(helper, owner);
    }

    /** 光線を受ける所有者役。FakePlayer はダメージを受けないので、普通の ServerPlayer（サバイバル）をワールドに入れる。 */
    private static ServerPlayer survivalOwner(GameTestHelper helper, Vec3 relative) {
        return GameTestPlayers.inLevel(helper, "psitweaks_owner", relative);
    }

    /** 東（+x）を向き、目の高さが床から {@code height} の専用 Caster。光線を床と平行に撃たせる。 */
    private static AutonomousCasterPlayer beamCaster(GameTestHelper helper, UUID owner, double z, double height) {
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), owner, new StubHost());
        Vec3 at = helper.absoluteVec(new Vec3(0.5, 1.0 + height, z));
        caster.moveTo(at.x, at.y - caster.getEyeHeight(), at.z, -90.0F, 0.0F);
        caster.setYHeadRot(-90.0F);
        return caster;
    }

    /** 保存して、ワールドに入れない別のエンティティとして読み込み直す。 */
    private static Entity reload(GameTestHelper helper, Entity entity) {
        CompoundTag tag = new CompoundTag();
        helper.assertTrue(entity.save(tag), "The entity could not be saved");
        return EntityType.create(tag, helper.getLevel())
                .orElseThrow(() -> new GameTestAssertException("The entity could not be loaded"));
    }

    private static Optional<ArrayList<Entity>> cast(GameTestHelper helper, AutonomousCasterPlayer caster, StubHost host,
                                                    ItemStack bullet, ItemStack cad) {
        var data = PlayerDataHandler.get(caster);
        data.availablePsi = host.psi;
        data.overflowed = false;
        data.regenCooldown = 0;
        return ItemCAD.cast(helper.getLevel(), caster, data, bullet, cad, 0, 0, 0.0F, context -> {
            context.castFrom = InteractionHand.MAIN_HAND;
            context.targetSlot = 0;
        });
    }

    @GameTest(template = TEMPLATE)
    public static void ideaStorageOwnerFollowsTheMachineOwner(GameTestHelper helper) throws Exception {
        StubHost host = new StubHost();
        host.owner = UUID.randomUUID();
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), null, host);
        var ownerStorage = IdeaStorageService.get(helper.getLevel().getServer(), host.owner);
        long inserted = ownerStorage.insertEnergy(4_000L, false);
        var energy = new PieceSelectorIdeaStorageAmount(new Spell());
        energy.setModeOption(BlockResourceModes.ENERGY);
        try {
            helper.assertTrue(IdeaStorageOwnerResolver.resolve(caster).equals(Optional.of(host.owner)),
                    "The dedicated caster did not use the machine owner's storage");
            helper.assertTrue(energy.execute(new SpellContext().setPlayer(caster)).equals((double) inserted),
                    "A storage selector cast by the machine did not read the owner's storage");
            host.owner = null;
            helper.assertTrue(IdeaStorageOwnerResolver.resolve(caster).isEmpty(), "An ownerless machine resolved an owner");
            helper.assertTrue(energy.execute(new SpellContext().setPlayer(caster)).equals(0.0D),
                    "An ownerless machine read some storage");
            boolean threw = false;
            try {
                IdeaStorageOwnerResolver.requireOwner(caster);
            } catch (SpellRuntimeException e) {
                threw = IdeaStorageOwnerResolver.ERROR_OWNER_UNAVAILABLE.equals(e.getMessage());
            }
            helper.assertTrue(threw, "Spells did not report an unavailable storage owner");
        } finally {
            caster.detach();
            ownerStorage.extractEnergy(Long.MAX_VALUE, false);
        }
        helper.assertTrue(IdeaStorageOwnerResolver.resolve(caster).isEmpty(), "A detached caster resolved an owner");

        // 他 MOD の FakePlayer は、これまでどおり自分自身のストレージを使う
        var fake = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "[psitweaks_test]"));
        helper.assertTrue(IdeaStorageOwnerResolver.resolve(fake).equals(Optional.of(fake.getUUID())),
                "A generic fake player lost its own storage");
        helper.succeed();
    }

    // ---- 機械ブロック（Mekanism が必要） ----

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void autonomousCasterCastsOnRedstonePulse(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.castsOnRedstonePulse(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void autonomousCasterLevelModeRepeatsAtTheInterval(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.levelModeRepeatsAtTheInterval(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void autonomousCasterLogsWhyACastFailed(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.logsWhyACastFailed(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void autonomousCasterIgnoresSignalPresentWhenPlaced(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.ignoresSignalPresentWhenPlaced(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void autonomousCasterMirrorsItsInventory(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.mirrorsItsInventory(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void autonomousCasterRestoresPsiFromEcho(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.restoresPsiFromEcho(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void autonomousCasterKeepsContentsWhenBroken(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.keepsContentsWhenBroken(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void autonomousCasterBreaksItselfWithoutComingBackOrDroppingItsCad(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.breaksItselfWithoutComingBackOrDroppingItsCad(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void autonomousCasterMassBreakOfItselfKeepsTheHeldItem(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.massBreakOfItselfKeepsTheHeldItem(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void autonomousCasterReadsMachinesSavedWithEightBullets(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.readsMachinesSavedWithEightBullets(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void autonomousCasterSelectorGivesTheMachinePosition(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.selectorGivesTheMachinePosition(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void autonomousCasterIgnoresActionsFromOtherMenus(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.ignoresActionsFromOtherMenus(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void autonomousCasterLendsItsOwnersIdeaStorage(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.lendsItsOwnersIdeaStorage(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void autonomousCasterShowsWhereTheCasterLooks(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.showsWhereTheCasterLooks(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void autonomousCasterKeepsPsiReturnedAfterTheCast(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.keepsPsiReturnedAfterTheCast(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void autonomousCasterSpinChamberMovesTheBulletSlot(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.spinChamberMovesTheSelectedBulletSlot(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 160)
    public static void autonomousCasterRussianRouletteReselectsTheBulletSlot(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.russianRouletteReselectsTheBulletSlot(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void autonomousCasterSharesOneLoopCount(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.sharesOneLoopCountAndResetsItByHand(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 160)
    public static void autonomousCasterBreakLoopResetsTheLoopCount(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.breakLoopResetsTheLoopCount(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 60)
    public static void autonomousCasterRefusesLoopcastBullets(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.refusesLoopcastBullets(helper);
    }

    @GameTest(template = TEMPLATE)
    public static void autonomousCasterFeedsBulletsFromAnInput2Side(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.feedsBulletsFromAnInput2Side(helper);
    }

    /** 機械の代わりに Caster の問い合わせへ答える。 */
    private static final class StubHost implements AutonomousCasterHost {
        private final List<Component> messages = new ArrayList<>();
        private int psi;
        private int insufficientCost = -1;
        private int insufficientAvailable = -1;
        private UUID owner;
        private String language;
        private String ownerName;

        @Override
        public BlockPos machinePos() {
            return BlockPos.ZERO;
        }

        @Override
        public void onCasterMessage(Component message) {
            messages.add(message);
        }

        @Override
        public void onSpamlessMessage(Component message, int replacementKey) {
            messages.add(message);
        }

        @Override
        public int availablePsi() {
            return psi;
        }

        @Override
        public void onBreakLoop() {
        }

        @Override
        public void onInsufficientPsi(int cost, int available) {
            insufficientCost = cost;
            insufficientAvailable = available;
        }

        @Override
        public Optional<UUID> ideaStorageOwner() {
            return Optional.ofNullable(owner);
        }

        @Override
        public Optional<String> ownerLanguage() {
            return Optional.ofNullable(language);
        }

        @Override
        public Optional<String> ownerName() {
            return Optional.ofNullable(ownerName);
        }
    }
}
