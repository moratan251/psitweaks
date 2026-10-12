package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.caster.AutonomousCasterHost;
import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.caster.AutonomousCasterRegistry;
import com.moratan251.psitweaks.common.caster.CasterLog;
import com.moratan251.psitweaks.common.caster.VirtualCad;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageOwnerResolver;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.network.message.MessageSpellError;

/**
 * 自律詠唱機の GameTest。
 * 作動ログと専用 Caster は Mekanism なしで確認し、機械ブロックの確認は {@link AutonomousCasterChecks} へ委ねる。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AutonomousCasterGameTests {
    private static final String TEMPLATE = "connector_empty";

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

    // ---- 専用 Caster ----

    @GameTest(template = TEMPLATE)
    public static void dedicatedCasterCastsWithItsHostsPsi(GameTestHelper helper) {
        StubHost host = new StubHost();
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), host);
        try {
            ItemStack cad = VirtualCad.rebuild(helper.getLevel().registryAccess(),
                    Collections.nCopies(VirtualCad.SLOT_TYPES.size(), ItemStack.EMPTY), ItemStack.EMPTY);
            helper.assertTrue(VirtualCad.isUsable(cad), "The default virtual CAD is not usable");
            caster.getInventory().items.set(AutonomousCasterPlayer.CAD_SLOT, cad);
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

            helper.assertTrue(AutonomousCasterRegistry.find(helper.getLevel(), caster.getUUID()) == caster,
                    "The attached caster could not be found by UUID");
            caster.detach();
            int count = host.messages.size();
            caster.sendSystemMessage(Component.literal("after detach"));
            helper.assertTrue(host.messages.size() == count, "A detached caster still reported to its host");
            helper.assertTrue(AutonomousCasterRegistry.find(helper.getLevel(), caster.getUUID()) == null,
                    "A detached caster was still registered");
            Optional<ArrayList<Entity>> detached = cast(helper, caster, host, AutonomousCasterTestSpells.debugBullet(1), cad);
            helper.assertTrue(detached.isEmpty(), "A detached caster could still cast");
        } finally {
            caster.detach();
        }
        helper.succeed();
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
    public static void ideaStorageOwnerFollowsTheMachineOwner(GameTestHelper helper) {
        StubHost host = new StubHost();
        host.owner = UUID.randomUUID();
        AutonomousCasterPlayer caster = new AutonomousCasterPlayer(helper.getLevel(), UUID.randomUUID(), host);
        try {
            helper.assertTrue(IdeaStorageOwnerResolver.resolve(caster).equals(Optional.of(host.owner)),
                    "The dedicated caster did not use the machine owner's storage");
            host.owner = null;
            helper.assertTrue(IdeaStorageOwnerResolver.resolve(caster).isEmpty(), "An ownerless machine resolved an owner");
            boolean threw = false;
            try {
                IdeaStorageOwnerResolver.requireOwner(caster);
            } catch (SpellRuntimeException e) {
                threw = IdeaStorageOwnerResolver.ERROR_OWNER_UNAVAILABLE.equals(e.getMessage());
            }
            helper.assertTrue(threw, "Spells did not report an unavailable storage owner");
        } finally {
            caster.detach();
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
    public static void autonomousCasterMirrorsItsHotbar(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        AutonomousCasterChecks.mirrorsItsHotbar(helper);
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

    /** 機械の代わりに Caster の問い合わせへ答える。 */
    private static final class StubHost implements AutonomousCasterHost {
        private final List<Component> messages = new ArrayList<>();
        private int psi;
        private int insufficientCost = -1;
        private int insufficientAvailable = -1;
        private UUID owner;

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
        public void onInsufficientPsi(int cost, int available) {
            insufficientCost = cost;
            insufficientAvailable = available;
        }

        @Override
        public Optional<UUID> ideaStorageOwner() {
            return Optional.ofNullable(owner);
        }
    }
}
