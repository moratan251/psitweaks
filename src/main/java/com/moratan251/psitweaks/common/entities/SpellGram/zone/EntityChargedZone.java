package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作動式: 荷電領域（仮、Charged Zone、2026-11-12 ユーザー）の範囲。
 * <ul>
 *   <li>範囲の中のエンティティに、少しずつ FE を送る（{@link #FE_PER_TICK} × 機械を動かす速さ）: プレイヤーは持ち物ぜんぶ、
 *   ほかの生き物は手と防具、落ちているアイテム、FE を持つエンティティ。ブロック（機械）には送らない（2026-11-12 ユーザー:
 *   向きの決め方が分かりにくく、発電設備の代わりになりかねないため。ブロックへは作動式: FE供給 の役目）。</li>
 *   <li>Applied Energistics 2 があれば、落ちているアイテムに AE2 のチャージャーと同じ加工をする（ケルタスクォーツの水晶が
 *   チャージ済みケルタスクォーツの水晶になる、など）。AE2 のレシピをそのまま使い、コードは AE2 に依存しない。</li>
 *   <li>同じ術者の乱流領域と重なったところは「雷雲」になる: ランダムな間隔で、連鎖電光に似た雷撃が中の生物を 1 体ずつ打つ
 *   （連鎖はしない）。雷撃の強さは乱流の威力（重なっていれば最も強いもの）、FE とチャージの速さも乱流の威力で上がる
 *   （威力 10 で 2 倍、ほかの組み合わせと同じ）。</li>
 * </ul>
 * 機械を動かす速さは、ほかの領域と同じく置いた時点の事象干渉力で決まる（干渉力 1 で半分、2 で基準、上限 2 倍）。
 */
public class EntityChargedZone extends SpellGramZone {
    /** 辺の色: 電気の黄色。 */
    public static final int OUTLINE_COLOR = 0xF4FF6A;
    /** 雷雲のときの辺の色: 雷の薄紫。 */
    public static final int STORM_OUTLINE_COLOR = 0xB9A8FF;
    /** 速さ 1（干渉力 2）で 1 tick に送る FE（持ち物 1 つごと、仮）。 */
    public static final int FE_PER_TICK = 20;
    /** 速さ 1 で、AE2 のチャージ 1 個にかかる tick（仮）。 */
    public static final int CHARGE_TICKS = 40;
    /** FE とチャージを進める間隔（tick）。 */
    public static final int ENERGY_INTERVAL = 5;
    /** 乱流と重なっているかを確かめ直す間隔（tick）。 */
    public static final int STORM_INTERVAL = 10;
    /** 雷撃の間隔: 最短（tick）と、それに足すランダムな分の上限（仮、平均 1.75 秒）。 */
    public static final int STRIKE_MIN_TICKS = 20;
    public static final int STRIKE_RANDOM_TICKS = 30;
    /** 乱流の威力 1 あたりの、雷撃 1 回のダメージ（倍率を掛ける前、仮）。威力 10 で 10。 */
    public static final float STRIKE_DAMAGE_PER_POWER = 1.0F;
    private static final ResourceLocation AE2_CHARGER = ResourceLocation.fromNamespaceAndPath("ae2", "charger");
    private static final int MAX_SPARKS_PER_TICK = 3;

    /** 落ちているアイテムごとの、AE2 のチャージの進み（エンティティの番号ごと）。サーバー側だけ。 */
    private final Map<Integer, Float> chargeProgress = new HashMap<>();
    /** 次の雷撃のゲーム時刻（まだ決めていなければ -1）。サーバー側だけ。 */
    private long nextStrike = -1;

    public EntityChargedZone(EntityType<? extends EntityChargedZone> type, Level level) {
        super(type, level);
    }

    public EntityChargedZone(Level level) {
        this(PsitweaksEntities.CHARGED_ZONE.get(), level);
    }

    @Override
    public int getOutlineColor() {
        return isStorm() ? STORM_OUTLINE_COLOR : OUTLINE_COLOR;
    }

    /**
     * 速さ {@code machineSpeed}・乱流の威力 {@code stormPower}（なければ 0）のところで、{@code ticks} tick ぶんに送る FE。
     */
    public static int energyFor(float machineSpeed, double stormPower, int ticks) {
        return (int) Math.round(FE_PER_TICK * machineSpeed * ticks * (1.0 + stormBoost(stormPower)));
    }

    /** 雷撃 1 回の基本のダメージ（倍率を掛ける前）。 */
    public static float strikeDamage(float turbulencePower) {
        return STRIKE_DAMAGE_PER_POWER * Math.max(0.0F, turbulencePower);
    }

    @Override
    protected int effectInterval() {
        return ENERGY_INTERVAL;
    }

    /** 生き物、落ちているアイテム、FE を持つエンティティ。 */
    @Override
    protected boolean affects(Entity entity) {
        if (!entity.isAlive()) {
            return false;
        }
        return entity instanceof LivingEntity || entity instanceof ItemEntity
                || entity.getCapability(Capabilities.EnergyStorage.ENTITY, null) != null;
    }

    @Override
    protected void affect(Entity target) {
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        double stormPower = stormPowerAt(BlockPos.containing(target.getBoundingBox().getCenter()));
        int energy = energyFor(getMachineSpeed(), stormPower, ENERGY_INTERVAL);
        IEnergyStorage own = target.getCapability(Capabilities.EnergyStorage.ENTITY, null);
        if (own != null) {
            give(own, energy);
        }
        if (target instanceof ItemEntity item) {
            chargeItem(item.getItem(), energy);
            chargeAe2(level, item, (float) (getMachineSpeed() * ENERGY_INTERVAL * (1.0 + stormBoost(stormPower)) / CHARGE_TICKS));
        } else if (target instanceof Player player) {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                chargeItem(inventory.getItem(i), energy);
            }
        } else if (target instanceof LivingEntity living) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                chargeItem(living.getItemBySlot(slot), energy);
            }
        }
    }

    private static void chargeItem(ItemStack stack, int energy) {
        if (stack.isEmpty()) {
            return;
        }
        IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (storage != null) {
            give(storage, energy);
        }
    }

    private static int give(IEnergyStorage storage, int energy) {
        return energy > 0 && storage.canReceive() ? storage.receiveEnergy(energy, false) : 0;
    }

    @Override
    protected void onSpellGramTickServer() {
        super.onSpellGramTickServer();
        if (isEnded() || getArea().isEmpty() || !(level() instanceof ServerLevel level)) {
            return;
        }
        if (tickCount % STORM_INTERVAL == 0) {
            refreshStorm();
        }
        if (isStorm()) {
            tickThundercloud(level);
        } else {
            nextStrike = -1;
        }
        if (tickCount % (ENERGY_INTERVAL * 20) == 0) {
            chargeProgress.keySet().removeIf(id -> !(level.getEntity(id) instanceof ItemEntity item) || !item.isAlive());
        }
    }

    /**
     * 落ちているアイテムに AE2 のチャージャーと同じ加工を進める。たまった分だけ 1 個ずつ加工した結果に変え、同じ場所に落とす。
     */
    private void chargeAe2(ServerLevel level, ItemEntity item, float progressAdd) {
        ItemStack stack = item.getItem();
        ItemStack result = ae2ChargeResult(level, stack);
        if (result.isEmpty()) {
            chargeProgress.remove(item.getId());
            return;
        }
        // 加工している間は消えるまでの時間を止める（2026-11-12 ユーザー）。
        ZoneItemLifetimes.hold(item, level.getGameTime());
        float progress = chargeProgress.getOrDefault(item.getId(), 0.0F) + progressAdd;
        int done = (int) Math.min(stack.getCount(), Math.floor(progress));
        progress -= done;
        if (done > 0) {
            ItemStack rest = stack.copyWithCount(stack.getCount() - done);
            ItemStack charged = result.copyWithCount(result.getCount() * done);
            ItemEntity out = new ItemEntity(level, item.getX(), item.getY(), item.getZ(), charged);
            out.setDeltaMovement(0.0, 0.1, 0.0);
            level.addFreshEntity(out);
            if (rest.isEmpty()) {
                item.discard();
            } else {
                item.setItem(rest);
            }
            Vec3 at = item.position();
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y + 0.2, at.z, 6, 0.15, 0.15, 0.15, 0.05);
            CombatEffects.sound(level, at, SoundEvents.RESPAWN_ANCHOR_CHARGE, 0.25F, 1.8F);
        }
        if (item.isAlive()) {
            chargeProgress.put(item.getId(), progress);
        } else {
            chargeProgress.remove(item.getId());
        }
    }

    /**
     * {@code stack} に AE2 のチャージャーのレシピ（{@code ae2:charger}）があれば、その結果。なければ空。AE2 がなければいつも空。
     * レシピは Minecraft の共通の形（材料の 1 つめ・結果）で読むので、AE2 のクラスは使わない。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ItemStack ae2ChargeResult(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        RecipeType<?> type = BuiltInRegistries.RECIPE_TYPE.get(AE2_CHARGER);
        if (type == null) {
            return ItemStack.EMPTY;
        }
        for (Object entry : level.getRecipeManager().getAllRecipesFor((RecipeType) type)) {
            Recipe<?> recipe = ((RecipeHolder<?>) entry).value();
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            if (!ingredients.isEmpty() && ingredients.get(0).test(stack)) {
                ItemStack result = recipe.getResultItem(level.registryAccess());
                if (!result.isEmpty()) {
                    return result.copy();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 雷雲: ランダムな間隔で、雷雲の下（同じ術者の乱流と重なったところ）にいる生物を 1 体選んで雷撃で打つ。打てる相手が
     * いなければ、雷雲の下のどこかへ落ちる（演出だけ）。
     */
    private void tickThundercloud(ServerLevel level) {
        long now = level.getGameTime();
        if (nextStrike < 0) {
            nextStrike = now + STRIKE_MIN_TICKS + random.nextInt(STRIKE_RANDOM_TICKS + 1);
            return;
        }
        if (now < nextStrike) {
            return;
        }
        nextStrike = now + STRIKE_MIN_TICKS + random.nextInt(STRIKE_RANDOM_TICKS + 1);
        LivingEntity caster = getCaster();
        AABB bounds = getArea().bounds();
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, bounds, target -> target.isAlive()
                && AttackRules.canHarm(caster, target, AttackRules.Options.DEFAULT)
                && stormAt(BlockPos.containing(target.getBoundingBox().getCenter())) != null);
        LivingEntity target = candidates.isEmpty() ? null : candidates.get(random.nextInt(candidates.size()));
        Vec3 end;
        if (target != null) {
            end = target.getBoundingBox().getCenter();
        } else {
            end = null;
            for (int i = 0; i < 6 && end == null; i++) {
                Vec3 point = new Vec3(bounds.minX + random.nextDouble() * bounds.getXsize(), bounds.minY + 0.05,
                        bounds.minZ + random.nextDouble() * bounds.getZsize());
                if (stormAt(BlockPos.containing(point)) != null) {
                    end = point;
                }
            }
            if (end == null) {
                return;
            }
        }
        float power = stormPowerAt(BlockPos.containing(end));
        Vec3 start = new Vec3(end.x + (random.nextDouble() - 0.5) * 1.5, Math.max(bounds.maxY - 0.25, end.y + 2.0),
                end.z + (random.nextDouble() - 0.5) * 1.5);
        CombatEffects.lightningArcs(level, List.of(start, end), List.of(-1, 0), getColor(), random.nextLong());
        CombatEffects.sound(level, end, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.7F, 1.4F + random.nextFloat() * 0.3F);
        CombatEffects.sound(level, start, SoundEvents.TRIDENT_THUNDER, 0.35F, 1.7F);
        if (target != null && power > 0.0F) {
            DamageSource source = AttackRules.source(level, PsitweaksDamageTypes.THUNDERCLOUD, this, caster);
            float damage = AttackRules.scale(strikeDamage(power), PsitweaksConfig.COMMON.thundercloudDamageMultiplier.get(), caster);
            target.invulnerableTime = 0;
            AttackRules.harm(caster, target, AttackRules.Options.DEFAULT, source, damage);
        }
    }

    @Override
    protected void onEnd(EndReason reason) {
        super.onEnd(reason);
        chargeProgress.clear();
    }

    /** 中に散る小さな火花。雷雲のところは多め。 */
    @Override
    protected void spawnClientParticles() {
        AABB box = getOutline();
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        int sparks = (int) Math.min(MAX_SPARKS_PER_TICK, Math.max(1, volume / 250.0));
        for (int i = 0; i < sparks; i++) {
            Vec3 at = ZoneAttacks.randomPoint(box, random);
            if (getArea().contains(BlockPos.containing(at)) && random.nextInt(3) == 0) {
                level().addParticle(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 0.0, 0.0, 0.0);
            }
        }
        if (isStorm()) {
            for (AABB storm : stormBoxes()) {
                int count = ZoneParticles.count(storm, 60.0, 1, 4);
                for (int i = 0; i < count; i++) {
                    Vec3 at = ZoneAttacks.randomPoint(storm, random);
                    level().addParticle(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z,
                            (random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.2);
                }
            }
        }
    }
}
