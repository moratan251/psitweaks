package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.CarriedEnergyStorage;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityChargedZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityTurbulenceZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import com.moratan251.psitweaks.common.items.ItemZoneReceiver;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.items.curios.CuriosSlotChecker;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.combat.CombatStatuses;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickChargedZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickTurbulenceZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * 作動式: 荷電領域。範囲の中の機械・領域受電器を持つプレイヤーの持ち物・落ちているアイテムに FE を送り、AE2 のチャージ加工をし、
 * 乱流領域と重なると雷雲になって中の生物を雷撃で打つ。Mekanism・AE2 を使うテストは、入っていないときは中身を飛ばして通す。
 * <p>
 * プレイヤーをワールドに入れるので、他のテストと同時に動かないようバッチを分ける。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ChargedZoneGameTests {
    private static final String TEMPLATE = "connector_empty";
    /** 領域受電器を入れる持ち物の枠（ホットバーの外）。 */
    private static final int RECEIVER_SLOT = 9;
    /** FE アイテムを入れる持ち物の枠（ホットバーの外）。 */
    private static final int BACKPACK_SLOT = 20;

    private ChargedZoneGameTests() {
    }

    /**
     * 充電能力: 置いたときのコスト × {@link EntityChargedZone#FE_PER_PSI} ÷ 存続時間 × 速さ。コストも存続時間も時間に比例するので、
     * 1 tick あたりの量は時間によらない。サイズ 16・威力 10（コスト 1483）の速さ 1 で約 494 FE/t。
     * 雷撃は乱流の威力 1 あたり 1。
     */
    @GameTest(template = TEMPLATE)
    public static void chargedZoneCapacityFollowsTheCost(GameTestHelper helper) {
        int cost = PieceTrickChargedZone.costFor(256, 10.0);
        helper.assertTrue(cost == 1483, "Size 16, Power 10 must cost 1483 for 30 s, got " + cost);
        double perTick = EntityChargedZone.capacityPerTick(cost, 600, 1.0F);
        helper.assertTrue(Math.abs(perTick - 1483 * EntityChargedZone.FE_PER_PSI / 600.0) < 1.0E-6,
                "1483 Psi must give " + 1483 * EntityChargedZone.FE_PER_PSI / 600.0 + " FE/t, got " + perTick);
        helper.assertTrue(Math.abs(EntityChargedZone.capacityPerTick(cost * 4, 2400, 1.0F) - perTick) < 1.0E-6,
                "A 120 s zone must charge as fast per tick as a 30 s one");
        helper.assertTrue(Math.abs(EntityChargedZone.capacityPerTick(cost, 600, 0.5F) - perTick / 2.0) < 1.0E-6,
                "Speed 0.5 must halve the capacity");
        helper.assertTrue(EntityChargedZone.strikeDamage(10.0F) == 5.0F && EntityChargedZone.strikeDamage(0.0F) == 0.0F,
                "A strike must deal 0.5 per attack power of the turbulence");
        helper.assertTrue(EntityChargedZone.strikeDamage(40.0F, 1) == 20.0F && EntityChargedZone.strikeDamage(40.0F, 2) == 16.25F
                        && EntityChargedZone.strikeDamage(40.0F, 3) == 12.5F,
                "A strike must deal 20 / 16.25 / 12.5 to each of 1 / 2 / 3 beings at attack power 40");
        helper.assertTrue(EntityChargedZone.strikeDamage(40.0F, 1, true) == 25.0F && EntityChargedZone.strikeDamage(40.0F, 3, true) == 15.625F
                        && EntityChargedZone.strikeDamage(40.0F, 1, false) == 20.0F,
                "A strike must deal 1.25 times the damage to a wet being");
        helper.succeed();
    }

    /**
     * 充電能力は受け手で分け合う: 落ちているエネルギータブレット（Mekanism）が 1 つでも 4 つでも、同じ時間に
     * 入る FE の合計は同じ（充電能力 × 時間）。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_shared", timeoutTicks = 120)
    public static void chargedZoneSharesItsCapacity(GameTestHelper helper) {
        Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
        if (tablet == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_shared_caster", new Vec3(4.5, 1.0, 4.5));
        List<ItemEntity> one = List.of(dropTablet(helper, tablet, new Vec3(0.5, 1.2, 0.5)));
        List<ItemEntity> four = List.of(dropTablet(helper, tablet, new Vec3(0.5, 1.2, 4.5)), dropTablet(helper, tablet, new Vec3(1.5, 1.2, 4.5)),
                dropTablet(helper, tablet, new Vec3(0.5, 1.2, 3.5)), dropTablet(helper, tablet, new Vec3(1.5, 1.2, 3.5)));
        Runnable cleanup = () -> {
            one.forEach(Entity::discard);
            four.forEach(Entity::discard);
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        EntityChargedZone[] zones = new EntityChargedZone[2];
        try {
            // 1 つのところと 4 つのところに、同じ大きさ・威力の領域を別々に置く。
            zones[0] = placeCharged(caster, helper.absolutePos(new BlockPos(0, 1, 0)), 1, 5.0);
            zones[1] = placeCharged(caster, helper.absolutePos(new BlockPos(1, 1, 4)), 3, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(41, () -> {
            int singleTotal = one.stream().mapToInt(item -> stored(item.getItem())).sum();
            int sharedTotal = four.stream().mapToInt(item -> stored(item.getItem())).sum();
            double singleCapacity = zones[0].capacityPerTick();
            double sharedCapacity = zones[1].capacityPerTick();
            float sharedUsed = zones[1].getResourceUsed();
            cleanup.run();
            // 8 回（40 tick）ぶん。FE の端数は受け手ごとに 2 FE 未満まで次の回へ繰り越すので、足りないのは受け手 1 つにつき 2 FE まで。
            helper.assertTrue(singleTotal <= singleCapacity * 40 + 1 && singleTotal >= singleCapacity * 40 - 8,
                    "One tablet must take the whole capacity (" + singleCapacity * 40 + " FE in 40 ticks), got " + singleTotal);
            helper.assertTrue(sharedTotal <= sharedCapacity * 40 + 1 && sharedTotal >= sharedCapacity * 40 - 32,
                    "Four tablets must share the capacity (" + sharedCapacity * 40 + " FE in 40 ticks), got " + sharedTotal);
            // 照準の表示: 使っている量は、受け手がみな受け取りきるので充電能力のほとんど。
            helper.assertTrue(sharedUsed > sharedCapacity * 0.8 && sharedUsed <= sharedCapacity * 1.0001,
                    "The synced use must be close to the capacity (" + sharedCapacity + " FE/t), got " + sharedUsed);
            int each = four.stream().mapToInt(item -> stored(item.getItem())).min().orElse(0);
            helper.assertTrue(each > 0, "Every tablet must get a share");
            helper.succeed();
        });
    }

    /** 重なった自分の荷電領域は、それぞれが自分の充電能力を配る（足し算）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_stack", timeoutTicks = 120)
    public static void overlappingChargedZonesAddTheirCapacity(GameTestHelper helper) {
        Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
        if (tablet == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_stack_caster", new Vec3(4.5, 1.0, 4.5));
        ItemEntity dropped = dropTablet(helper, tablet, new Vec3(0.5, 1.2, 0.5));
        Runnable cleanup = () -> {
            dropped.discard();
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        EntityChargedZone[] zones = new EntityChargedZone[2];
        try {
            zones[0] = placeCharged(caster, helper.absolutePos(new BlockPos(0, 1, 0)), 1, 5.0);
            zones[1] = placeCharged(caster, helper.absolutePos(new BlockPos(0, 1, 0)), 1, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(41, () -> {
            int total = stored(dropped.getItem());
            double capacity = zones[0].capacityPerTick() + zones[1].capacityPerTick();
            cleanup.run();
            helper.assertTrue(total <= capacity * 40 + 1 && total >= capacity * 40 - 16,
                    "Two overlapping Charged Zones must give both capacities (" + capacity * 40 + " FE in 40 ticks), got " + total);
            helper.succeed();
        });
    }

    /** 入の領域受電器を持ち物に入れたプレイヤーを {@code relative} に立たせる。 */
    private static ServerPlayer carrier(GameTestHelper helper, String name, Vec3 relative) {
        ServerPlayer player = GameTestPlayers.inLevel(helper, name, relative);
        player.getInventory().setItem(RECEIVER_SLOT, new ItemStack(PsitweaksItems.ZONE_RECEIVER.get()));
        return player;
    }

    private static ItemEntity dropTablet(GameTestHelper helper, Item tablet, Vec3 relative) {
        Vec3 at = helper.absoluteVec(relative);
        ItemEntity dropped = new ItemEntity(helper.getLevel(), at.x, at.y, at.z, new ItemStack(tablet));
        dropped.setNoGravity(true);
        dropped.setDeltaMovement(Vec3.ZERO);
        dropped.setPickUpDelay(32767);
        helper.getLevel().addFreshEntity(dropped);
        return dropped;
    }

    /**
     * 領域受電器を持つプレイヤーの手のエネルギータブレットと落ちているエネルギータブレット（Mekanism）と、ブロック（AE2 のチャージャー）に
     * FE が入る（ブロックにも送る）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_energy", timeoutTicks = 80)
    public static void chargedZoneFillsCarriedItemsAndBlocks(GameTestHelper helper) {
        Block chargerBlock = ModList.get().isLoaded("ae2") ? block("ae2:charger") : null;
        Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
        if (chargerBlock == null && tablet == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos charger = new BlockPos(0, 1, 0);
        if (chargerBlock != null) {
            helper.setBlock(charger, chargerBlock);
        }
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_energy_caster", new Vec3(4.5, 1.0, 4.5));
        ServerPlayer holder = carrier(helper, "charged_energy_holder", new Vec3(2.5, 1.0, 0.5));
        Vec3 droppedAt = helper.absoluteVec(new Vec3(0.5, 1.2, 2.5));
        ItemEntity dropped = new ItemEntity(level, droppedAt.x, droppedAt.y, droppedAt.z,
                tablet != null ? new ItemStack(tablet) : new ItemStack(Items.STONE));
        if (tablet != null) {
            holder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(tablet));
            dropped.setNoGravity(true);
            dropped.setDeltaMovement(Vec3.ZERO);
            dropped.setPickUpDelay(32767);
            level.addFreshEntity(dropped);
        }
        Runnable cleanup = () -> {
            GameTestPlayers.remove(helper, holder);
            dropped.discard();
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            placeCharged(caster, helper.absolutePos(new BlockPos(2, 1, 2)), 5, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(30, () -> {
            int inCharger = chargerBlock != null ? stored(level, helper.absolutePos(charger)) : 0;
            int inHand = tablet != null ? stored(holder.getMainHandItem()) : 1;
            int onGround = tablet == null ? 1 : dropped.isAlive() ? stored(dropped.getItem()) : -1;
            cleanup.run();
            // ブロックにも送る。
            helper.assertTrue(chargerBlock == null || inCharger > 0, "Blocks inside the zone must get FE, the Charger has " + inCharger);
            helper.assertTrue(inHand > 0, "The Energy Tablet held inside the zone must get FE, has " + inHand);
            helper.assertTrue(onGround > 0, "The dropped Energy Tablet inside the zone must get FE, has " + onGround);
            helper.succeed();
        });
    }

    /** 領域受電器のないプレイヤーの持ち物には給電しない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_no_receiver", timeoutTicks = 80)
    public static void playersWithoutAReceiverGetNoEnergy(GameTestHelper helper) {
        checkCarriedTablet(helper, "charged_no_receiver", null, false);
    }

    /** 入の領域受電器を持つプレイヤーの持ち物には、術者でなくても給電する。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_receiver_on", timeoutTicks = 80)
    public static void playersWithAnEnabledReceiverGetEnergy(GameTestHelper helper) {
        checkCarriedTablet(helper, "charged_receiver_on", true, true);
    }

    /** 切の領域受電器では給電しない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_receiver_off", timeoutTicks = 80)
    public static void playersWithADisabledReceiverGetNoEnergy(GameTestHelper helper) {
        checkCarriedTablet(helper, "charged_receiver_off", false, false);
    }

    /**
     * 術者でないプレイヤーの手にエネルギータブレットを持たせて荷電領域に入れ、FE が入るかを確かめる。{@code receiver} は領域受電器の状態
     * （null なら持たせない）。領域が働いていることは、落ちているエネルギータブレットで確かめる。
     */
    private static void checkCarriedTablet(GameTestHelper helper, String name, @Nullable Boolean receiver, boolean expectEnergy) {
        Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
        if (tablet == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, name + "_caster", new Vec3(4.5, 1.0, 4.5));
        ServerPlayer player = GameTestPlayers.inLevel(helper, name + "_player", new Vec3(1.5, 1.0, 1.5));
        if (receiver != null) {
            ItemStack stack = new ItemStack(PsitweaksItems.ZONE_RECEIVER.get());
            ItemZoneReceiver.setEnabled(stack, receiver);
            player.getInventory().setItem(RECEIVER_SLOT, stack);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(tablet));
        ItemEntity dropped = dropTablet(helper, tablet, new Vec3(3.5, 1.2, 0.5));
        Runnable cleanup = () -> {
            dropped.discard();
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, player);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            placeCharged(caster, helper.absolutePos(new BlockPos(2, 1, 2)), 5, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(30, () -> {
            int carried = stored(player.getMainHandItem());
            int onGround = stored(dropped.getItem());
            cleanup.run();
            helper.assertTrue(onGround > 0, "The dropped Energy Tablet must get FE, has " + onGround);
            helper.assertTrue(expectEnergy ? carried > 0 : carried == 0, "With receiver state " + receiver
                    + " the carried Energy Tablet must " + (expectEnergy ? "get FE" : "get no FE") + ", has " + carried);
            helper.succeed();
        });
    }

    /** プレイヤー以外の生き物の手と防具には給電しない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_mob_gear", timeoutTicks = 80)
    public static void mobEquipmentGetsNoEnergy(GameTestHelper helper) {
        Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
        if (tablet == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_mob_gear_caster", new Vec3(4.5, 1.0, 4.5));
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 1.0, 1.5));
        husk.setNoAi(true);
        husk.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(tablet));
        husk.setItemSlot(EquipmentSlot.CHEST, new ItemStack(tablet));
        ItemEntity dropped = dropTablet(helper, tablet, new Vec3(3.5, 1.2, 0.5));
        Runnable cleanup = () -> {
            husk.discard();
            dropped.discard();
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            placeCharged(caster, helper.absolutePos(new BlockPos(2, 1, 2)), 5, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(30, () -> {
            int inHand = stored(husk.getItemBySlot(EquipmentSlot.MAINHAND));
            int worn = stored(husk.getItemBySlot(EquipmentSlot.CHEST));
            int onGround = stored(dropped.getItem());
            cleanup.run();
            helper.assertTrue(onGround > 0, "The dropped Energy Tablet must get FE, has " + onGround);
            helper.assertTrue(inHand == 0 && worn == 0, "A mob's equipment must get no FE, hand " + inHand + ", armor " + worn);
            helper.succeed();
        });
    }

    /**
     * FE アイテムを 3 つ持つプレイヤーは受け手 1 つに数える: ブロック（Mekanism のエネルギーキューブ）1 つと分けると、ブロックが
     * 渡した FE のおよそ半分を受け取る。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_one_share", timeoutTicks = 100)
    public static void aPlayerCountsAsOneReceiver(GameTestHelper helper) {
        Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
        Block cube = ModList.get().isLoaded("mekanism") ? block("mekanism:basic_energy_cube") : null;
        if (tablet == null || cube == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos cubePos = new BlockPos(0, 1, 0);
        helper.setBlock(cubePos, cube);
        // Mekanism の機械の面の設定は、アイテムの既定の値を読むまで空で、どの面からも FE を受け取らない。
        level.getBlockEntity(helper.absolutePos(cubePos)).applyComponentsFromItemStack(new ItemStack(cube));
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_one_share_caster", new Vec3(4.5, 1.0, 4.5));
        ServerPlayer player = carrier(helper, "charged_one_share_player", new Vec3(2.5, 1.0, 2.5));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(tablet));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(tablet));
        player.getInventory().setItem(BACKPACK_SLOT, new ItemStack(tablet));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, player);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            placeCharged(caster, helper.absolutePos(new BlockPos(2, 1, 2)), 5, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(41, () -> {
            int inCube = stored(level, helper.absolutePos(cubePos));
            int carried = stored(player.getMainHandItem()) + stored(player.getOffhandItem())
                    + stored(player.getInventory().getItem(BACKPACK_SLOT));
            cleanup.run();
            int total = inCube + carried;
            helper.assertTrue(total > 0, "The cube and the player must get FE");
            helper.assertTrue(inCube >= total * 0.4 && inCube <= total * 0.6,
                    "The player must take one share: the cube has " + inCube + " FE, the player " + carried + " FE");
            helper.succeed();
        });
    }

    /** 足りないときは防具が先に満たされ、メインハンドと残りの持ち物には回らない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_armor_first", timeoutTicks = 80)
    public static void armorFillsFirstWhenEnergyIsShort(GameTestHelper helper) {
        Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
        if (tablet == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_armor_first_caster", new Vec3(4.5, 1.0, 4.5));
        ServerPlayer player = carrier(helper, "charged_armor_first_player", new Vec3(1.5, 1.0, 1.5));
        player.getInventory().armor.set(0, new ItemStack(tablet));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(tablet));
        player.getInventory().setItem(BACKPACK_SLOT, new ItemStack(tablet));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, player);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            placeCharged(caster, helper.absolutePos(new BlockPos(2, 1, 2)), 5, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(30, () -> {
            int armor = stored(player.getInventory().armor.get(0));
            int hand = stored(player.getMainHandItem());
            int backpack = stored(player.getInventory().getItem(BACKPACK_SLOT));
            cleanup.run();
            helper.assertTrue(armor > 0, "The armor slot must fill first, has " + armor);
            helper.assertTrue(hand == 0 && backpack == 0,
                    "Nothing may reach the other items while the armor takes it all, hand " + hand + ", backpack " + backpack);
            helper.succeed();
        });
    }

    /**
     * 持ち物をまとめた窓口: 防具 → メインハンド → オフハンド → 残りの持ち物の順に詰め、満タンのアイテムは飛ばし、引き出せない。受電器は
     * 持ち物のどの区画にあっても効く。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_carried_storage", timeoutTicks = 40)
    public static void carriedStorageFillsInOrder(GameTestHelper helper) {
        Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
        if (tablet == null) {
            helper.succeed();
            return;
        }
        ServerPlayer player = GameTestPlayers.inLevel(helper, "charged_carried_storage", new Vec3(1.5, 1.0, 1.5));
        try {
            Inventory inventory = player.getInventory();
            ItemStack receiver = new ItemStack(PsitweaksItems.ZONE_RECEIVER.get());
            helper.assertTrue(!ItemZoneReceiver.isCarriedBy(player), "A player with no receiver must not count as carrying one");
            for (int slot : new int[]{0, RECEIVER_SLOT, Inventory.INVENTORY_SIZE, Inventory.SLOT_OFFHAND}) {
                inventory.setItem(slot, receiver.copy());
                helper.assertTrue(ItemZoneReceiver.isCarriedBy(player), "A receiver in slot " + slot + " must count");
                inventory.setItem(slot, ItemStack.EMPTY);
            }
            inventory.setItem(RECEIVER_SLOT, receiver.copy());
            helper.assertTrue(CarriedEnergyStorage.of(player) == null, "A player with no FE item must not be a receiver");

            List<ItemStack> ordered = List.of(new ItemStack(tablet), new ItemStack(tablet), new ItemStack(tablet), new ItemStack(tablet));
            inventory.armor.set(0, ordered.get(0));
            inventory.setItem(inventory.selected, ordered.get(1));
            inventory.offhand.set(0, ordered.get(2));
            inventory.setItem(BACKPACK_SLOT, ordered.get(3));
            for (int filling = 0; filling < ordered.size(); filling++) {
                CarriedEnergyStorage storage = CarriedEnergyStorage.of(player);
                helper.assertTrue(storage != null && storage.canReceive(), "Item " + filling + " must still take FE");
                helper.assertTrue(!storage.canExtract() && storage.extractEnergy(100, false) == 0, "FE must not come back out");
                int[] before = ordered.stream().mapToInt(ChargedZoneGameTests::stored).toArray();
                helper.assertTrue(storage.receiveEnergy(100, false) > 0, "Item " + filling + " must take the FE");
                for (int i = 0; i < ordered.size(); i++) {
                    int gained = stored(ordered.get(i)) - before[i];
                    helper.assertTrue(i == filling ? gained > 0 : gained == 0,
                            "With items before " + filling + " full, item " + i + " gained " + gained);
                }
                IEnergyStorage filled = ordered.get(filling).getCapability(Capabilities.EnergyStorage.ITEM);
                while (filled.receiveEnergy(Integer.MAX_VALUE, false) > 0) {
                    // 1 回に受け取れる量に上限があるので、満タンになるまで繰り返す。
                }
            }
            helper.assertTrue(CarriedEnergyStorage.of(player) == null, "A player whose FE items are all full must not be a receiver");
            helper.succeed();
        } finally {
            GameTestPlayers.remove(helper, player);
        }
    }

    /** Curios のスロットに付けた受電器も効き、Curios のスロットの FE アイテムにも持ち物のあとで給電する。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_curios_receiver", timeoutTicks = 40)
    public static void zoneReceiverWorksFromCurios(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.inLevel(helper, "charged_curios_receiver", new Vec3(1.5, 1.0, 1.5));
        try {
            ItemStack receiver = new ItemStack(PsitweaksItems.ZONE_RECEIVER.get());
            helper.assertTrue(receiver.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "curio"))),
                    "The Zone Receiver must be in curios:curio so tag-validated Curios slots accept it");
            var curios = CuriosApi.getCuriosInventory(player).orElseThrow();
            curios.setEquippedCurio(CuriosSlotChecker.MAGIC_CALCULATION_AREA, 3, receiver);
            helper.assertTrue(ItemZoneReceiver.isCarriedBy(player), "A receiver in a Curios slot must count");
            ItemStack equipped = CuriosSlotChecker.getItemFromSelectedMagicSlot(player, 3);
            ItemZoneReceiver.setEnabled(equipped, false);
            helper.assertTrue(!ItemZoneReceiver.isCarriedBy(player), "A switched-off receiver in a Curios slot must not count");
            ItemZoneReceiver.setEnabled(equipped, true);

            Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
            if (tablet != null) {
                helper.assertTrue(CarriedEnergyStorage.of(player) == null, "A player with no FE item must not be a receiver");
                ItemStack curioTablet = new ItemStack(tablet);
                curios.setEquippedCurio(CuriosSlotChecker.MAGIC_CALCULATION_AREA, 0, curioTablet);
                ItemStack equippedTablet = CuriosSlotChecker.getItemFromSelectedMagicSlot(player, 0);
                CarriedEnergyStorage storage = CarriedEnergyStorage.of(player);
                helper.assertTrue(storage != null && storage.receiveEnergy(100, false) > 0 && stored(equippedTablet) > 0,
                        "An FE item in a Curios slot must take FE");

                ItemStack backpackTablet = new ItemStack(tablet);
                player.getInventory().setItem(BACKPACK_SLOT, backpackTablet);
                int curioBefore = stored(equippedTablet);
                storage = CarriedEnergyStorage.of(player);
                helper.assertTrue(storage != null && storage.receiveEnergy(100, false) > 0, "The inventory tablet must take FE");
                helper.assertTrue(stored(backpackTablet) > 0 && stored(equippedTablet) == curioBefore,
                        "The inventory must fill before the Curios slots");
            }
            helper.succeed();
        } finally {
            GameTestPlayers.remove(helper, player);
        }
    }

    /** 右クリックで入と切が切り替わり、光と保存もそれに従う。作ったばかりの受電器は入。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_receiver_toggle", timeoutTicks = 40)
    public static void zoneReceiverTogglesOnUse(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = GameTestPlayers.inLevel(helper, "charged_receiver_toggle", new Vec3(1.5, 1.0, 1.5));
        try {
            ItemStack stack = new ItemStack(PsitweaksItems.ZONE_RECEIVER.get());
            helper.assertTrue(stack.getMaxStackSize() == 1, "The Zone Receiver must not stack");
            helper.assertTrue(ItemZoneReceiver.isEnabled(stack) && stack.hasFoil(), "A new Zone Receiver must be on and glow");
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            stack.use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(!ItemZoneReceiver.isEnabled(stack) && !stack.hasFoil(), "Using it must switch it off and stop the glow");
            ItemStack loaded = ItemStack.parseOptional(level.registryAccess(), (CompoundTag) stack.save(level.registryAccess()));
            helper.assertTrue(!ItemZoneReceiver.isEnabled(loaded), "The off state must survive saving");
            stack.use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(ItemZoneReceiver.isEnabled(stack) && stack.hasFoil(), "Using it again must switch it back on");
            helper.succeed();
        } finally {
            GameTestPlayers.remove(helper, player);
        }
    }

    /** AE2 があれば、落ちているケルタスクォーツの水晶がチャージ済みケルタスクォーツの水晶になる。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_ae2", timeoutTicks = 140)
    public static void chargedZoneChargesCertusQuartz(GameTestHelper helper) {
        Item certus = item("ae2:certus_quartz_crystal");
        Item charged = item("ae2:charged_certus_quartz_crystal");
        if (!ModList.get().isLoaded("ae2") || certus == null || charged == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        helper.assertTrue(EntityChargedZone.ae2ChargeResult(level, new ItemStack(certus)).is(charged),
                "The AE2 Charger recipe must turn Certus Quartz into Charged Certus Quartz");
        helper.assertTrue(EntityChargedZone.ae2ChargeResult(level, new ItemStack(Items.STONE)).isEmpty(),
                "Stone has no Charger recipe");
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_ae2_caster", new Vec3(4.5, 1.0, 4.5));
        Vec3 at = helper.absoluteVec(new Vec3(1.5, 1.2, 1.5));
        ItemEntity crystal = new ItemEntity(level, at.x, at.y, at.z, new ItemStack(certus));
        crystal.setDeltaMovement(Vec3.ZERO);
        crystal.setPickUpDelay(32767);
        level.addFreshEntity(crystal);
        AABB box = new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(6.0);
        Runnable cleanup = () -> {
            level.getEntitiesOfClass(ItemEntity.class, box).forEach(Entity::discard);
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            // 事象干渉力 4（速さ 2）、サイズ 9・威力 10（コスト 503）: 充電能力 約 335 FE/t、1 個 3200 FE で約 10 tick。
            caster.getAttribute(com.moratan251.psitweaks.common.attributes.PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            placeCharged(caster, helper.absolutePos(new BlockPos(2, 1, 2)), 9, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(100, () -> {
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box, ItemEntity::isAlive);
            boolean hasCharged = items.stream().anyMatch(item -> item.getItem().is(charged));
            boolean hasCertus = items.stream().anyMatch(item -> item.getItem().is(certus));
            cleanup.run();
            helper.assertTrue(hasCharged && !hasCertus, "The dropped Certus Quartz must be charged, charged " + hasCharged
                    + ", certus left " + hasCertus);
            helper.succeed();
        });
    }

    /** 乱流領域と重ねると雷雲になり、中の生物を雷撃が打つ（術者は打たない）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_thundercloud", timeoutTicks = 120)
    public static void thundercloudStrikesBeingsInside(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_storm_caster", new Vec3(4.5, 1.0, 4.5));
        Husk target = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 1.0, 1.5)), 200.0F);
        target.setNoAi(true);
        target.setHealth(200.0F);
        Runnable cleanup = () -> {
            target.discard();
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        EntityChargedZone[] zone = new EntityChargedZone[1];
        try {
            BlockPos center = helper.absolutePos(new BlockPos(2, 1, 2));
            placeCharged(caster, center, 5, 5.0);
            placeTurbulence(caster, center, 5, 10.0);
            zone[0] = SpellGramIndex.all(level, EntityChargedZone.class).stream()
                    .filter(found -> caster.getUUID().equals(found.getCasterUuid())).findFirst().orElseThrow();
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        // 乱流と重なったと気づくまで最大 10 tick、最初の雷撃までさらに最大 35 tick。
        helper.runAfterDelay(90, () -> {
            boolean storm = zone[0].isStorm();
            float health = target.getHealth();
            cleanup.run();
            helper.assertTrue(storm, "A Charged Zone overlapping the caster's Turbulence Zone must become a Thundercloud");
            helper.assertTrue(health < 200.0F, "A strike of the Thundercloud must hit the husk inside, health " + health);
            helper.succeed();
        });
    }

    /** 雷雲の雷撃は、濡れた相手に 1.25 倍のダメージを与える。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_thundercloud_wet", timeoutTicks = 120)
    public static void thundercloudStrikesWetBeingsHarder(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_storm_wet_caster", new Vec3(4.5, 1.0, 4.5));
        Husk target = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 1.0, 1.5)), 200.0F);
        target.setNoAi(true);
        target.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        target.setHealth(200.0F);
        CombatStatuses.soak(target, 1000);
        Runnable cleanup = () -> {
            target.discard();
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        EntityTurbulenceZone[] turbulence = new EntityTurbulenceZone[1];
        EntityChargedZone[] charged = new EntityChargedZone[1];
        try {
            BlockPos center = helper.absolutePos(new BlockPos(2, 1, 2));
            placeCharged(caster, center, 5, 5.0);
            placeTurbulence(caster, center, 5, 10.0);
            charged[0] = SpellGramIndex.all(level, EntityChargedZone.class).stream()
                    .filter(found -> caster.getUUID().equals(found.getCasterUuid())).findFirst().orElseThrow();
            turbulence[0] = SpellGramIndex.all(level, EntityTurbulenceZone.class).stream()
                    .filter(found -> caster.getUUID().equals(found.getCasterUuid())).findFirst().orElseThrow();
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        float attack = EntityTurbulenceZone.limitedAttackPower(turbulence[0].attackPower(),
                com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone.enteredPowerOf(charged[0].getPower()),
                EntityTurbulenceZone.CHARGED_ATTACK_POWER_PER_POWER);
        float[] firstLoss = {0.0F};
        helper.onEachTick(() -> {
            if (firstLoss[0] == 0.0F) {
                firstLoss[0] = 200.0F - target.getHealth();
            }
        });
        helper.runAfterDelay(90, () -> {
            cleanup.run();
            float expected = EntityChargedZone.strikeDamage(attack, 1, true);
            helper.assertTrue(Math.abs(firstLoss[0] - expected) < 1.0E-3F,
                    "A strike must deal " + expected + " to the wet husk, dealt " + firstLoss[0]);
            helper.succeed();
        });
    }

    /** 雷雲の 1 回の雷撃が打つのは、中に 5 体いても 3 体まで。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_thundercloud_targets", timeoutTicks = 120)
    public static void thundercloudStrikesAtMostThreeBeings(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_storm_targets_caster", new Vec3(4.5, 1.0, 4.5));
        List<Husk> husks = new ArrayList<>();
        for (Vec3 at : List.of(new Vec3(1.5, 1.0, 1.5), new Vec3(2.5, 1.0, 1.5), new Vec3(3.5, 1.0, 1.5),
                new Vec3(1.5, 1.0, 3.5), new Vec3(2.5, 1.0, 3.5))) {
            Husk husk = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, at), 200.0F);
            husk.setNoAi(true);
            husk.setHealth(200.0F);
            husks.add(husk);
        }
        Runnable cleanup = () -> {
            husks.forEach(Entity::discard);
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            BlockPos center = helper.absolutePos(new BlockPos(2, 1, 2));
            placeCharged(caster, center, 5, 5.0);
            placeTurbulence(caster, center, 5, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        int[] firstStruck = {0};
        helper.onEachTick(() -> {
            if (firstStruck[0] == 0) {
                firstStruck[0] = (int) husks.stream().filter(husk -> husk.getHealth() < 200.0F).count();
            }
        });
        helper.runAfterDelay(90, () -> {
            cleanup.run();
            helper.assertTrue(firstStruck[0] == EntityChargedZone.MAX_STRIKE_TARGETS,
                    "The first strike must hit 3 of the 5 husks, hit " + firstStruck[0]);
            helper.succeed();
        });
    }

    private static int stored(ServerLevel level, BlockPos pos) {
        int best = 0;
        IEnergyStorage any = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        if (any != null) {
            best = any.getEnergyStored();
        }
        for (Direction side : Direction.values()) {
            IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side);
            if (storage != null) {
                best = Math.max(best, storage.getEnergyStored());
            }
        }
        return best;
    }

    private static int stored(ItemStack stack) {
        IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return storage == null ? -1 : storage.getEnergyStored();
    }

    @Nullable
    private static Block block(String id) {
        ResourceLocation key = ResourceLocation.parse(id);
        return BuiltInRegistries.BLOCK.containsKey(key) ? BuiltInRegistries.BLOCK.get(key) : null;
    }

    @Nullable
    private static Item item(String id) {
        ResourceLocation key = ResourceLocation.parse(id);
        return BuiltInRegistries.ITEM.containsKey(key) ? BuiltInRegistries.ITEM.get(key) : null;
    }

    private static MatrixValue cube(BlockPos center, int size) {
        double low = -(size / 2) - 0.5;
        return new MatrixValue(4, 4, new double[]{
                size, 0, 0, center.getX() + low,
                0, size, 0, center.getY() + low,
                0, 0, size, center.getZ() + low,
                0, 0, 0, 1});
    }

    /** {@code center} のブロックを中心にした一辺 {@code size}（奇数）、威力 {@code power} の荷電領域を置き、それを返す。 */
    private static EntityChargedZone placeCharged(ServerPlayer caster, BlockPos center, int size, double power)
            throws SpellRuntimeException {
        MatrixValue matrix = cube(center, size);
        var piece = new PieceTrickChargedZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                    return matrix;
                }
                if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                    return (double) size;
                }
                return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? power : null;
            }
        };
        List<EntityChargedZone> before = SpellGramIndex.all(caster.level(), EntityChargedZone.class);
        execute(piece, caster);
        return SpellGramIndex.all(caster.level(), EntityChargedZone.class).stream().filter(zone -> !before.contains(zone))
                .findFirst().orElseThrow();
    }

    /** {@code center} のブロックを中心にした一辺 {@code size}（奇数）、威力 {@code power} の乱流領域を置く。 */
    private static void placeTurbulence(ServerPlayer caster, BlockPos center, int size, double power) throws SpellRuntimeException {
        MatrixValue matrix = cube(center, size);
        var piece = new PieceTrickTurbulenceZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                    return matrix;
                }
                if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                    return (double) size;
                }
                return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? power : null;
            }
        };
        execute(piece, caster);
    }

    private static void execute(PieceTrickZoneBase<? extends SpellGramZone> piece, ServerPlayer caster) throws SpellRuntimeException {
        piece.execute(new SpellContext().setPlayer(caster));
    }
}
