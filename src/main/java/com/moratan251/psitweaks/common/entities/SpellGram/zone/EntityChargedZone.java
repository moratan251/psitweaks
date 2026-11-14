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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作動式: 荷電領域（仮、Charged Zone、2026-11-12 ユーザー）の範囲。
 * <ul>
 *   <li>充電能力（2026-11-14 ユーザー）: 領域は 1 tick あたり決まった量の FE を持つ（{@link #capacityPerTick}: 置いたときの
 *   コスト × {@link #FE_PER_PSI} ÷ 存続時間 × 機械を動かす速さ。威力はコストのつまみなので、威力とともに増える）。これを
 *   {@link #ENERGY_INTERVAL} tick ごとに、範囲の中の受け手で均等に分ける。受け手は FE を受け取れるもの（プレイヤーは持ち物
 *   ぜんぶ、ほかの生き物は手と防具、落ちているアイテム、FE を持つエンティティ）と、AE2 のチャージ中の落ちているアイテム。
 *   満タンで受け取れなかった分は、ほかの受け手に回す。ブロック（機械）には送らない（2026-11-12 ユーザー: 向きの決め方が
 *   分かりにくく、発電設備の代わりになりかねないため。ブロックへは作動式: FE供給 の役目）。</li>
 *   <li>Applied Energistics 2 があれば、落ちているアイテムに AE2 のチャージャーと同じ加工をする（ケルタスクォーツの水晶が
 *   チャージ済みケルタスクォーツの水晶になる、など）。1 個に {@link #FE_PER_AE2_CHARGE} FE を充電能力から使う（AE2 の
 *   チャージャーの 1600 AE を既定の換算にした値）。AE2 のレシピをそのまま使い、コードは AE2 に依存しない。</li>
 *   <li>同じ術者の乱流領域と重なったところは「雷雲」になる: ランダムな間隔で、連鎖電光に似た雷撃が中の生物を 1 体ずつ打つ
 *   （連鎖はしない）。雷撃の強さは乱流の威力（重なっていれば最も強いもの）、FE とチャージの速さも乱流の威力で上がる
 *   （威力 10 で 2 倍、ほかの組み合わせと同じ）。</li>
 * </ul>
 * 機械を動かす速さは、ほかの領域と同じく置いた時点の事象干渉力で決まる（干渉力 1 で半分、2 で基準、上限 2 倍）。雷雲の中の
 * 受け手は、分けた量の (1 + 乱流の上乗せ) 倍を受け取る（乱流の威力 10 で 2 倍）。
 */
public class EntityChargedZone extends SpellGramZone {
    /** 辺の色: 電気の黄色。 */
    public static final int OUTLINE_COLOR = 0xF4FF6A;
    /** 雷雲のときの辺の色: 雷の薄紫。 */
    public static final int STORM_OUTLINE_COLOR = 0xB9A8FF;
    /** 充電能力の比率: 置いたときのコスト 1 Psi あたりの FE（2026-11-15 ユーザー: サイリンク発電機の 100 FE の 2 倍）。 */
    public static final double FE_PER_PSI = 200.0;
    /** AE2 のチャージ 1 個に使う FE（2026-11-14 ユーザー: AE2 のチャージャーの 1600 AE を既定の換算 1 AE = 2 FE にした値）。 */
    public static final int FE_PER_AE2_CHARGE = 3200;
    /** 威力の既定値（置く前に設定しなかったとき）。 */
    public static final float DEFAULT_POWER = 5.0F;
    /** FE とチャージを進める間隔（tick）。 */
    public static final int ENERGY_INTERVAL = 5;
    /**
     * 受け手ごとに次の回へ繰り越す、渡しきれなかった分け前の上限（雷雲の上乗せの前の FE）。FE は整数でしか渡せず、Mekanism は
     * FE を J に換算して戻せる分しか受け取らない（奇数の FE は 1 少なくなる）ので、その端数（2 FE 未満）だけを繰り越す。
     */
    public static final double MAX_ENERGY_CARRY = 2.0;
    /** 乱流と重なっているかを確かめ直す間隔（tick）。 */
    public static final int STORM_INTERVAL = 10;
    /** 雷撃の間隔: 最短（tick）と、それに足すランダムな分の上限（仮、平均 1.75 秒）。 */
    public static final int STRIKE_MIN_TICKS = 20;
    public static final int STRIKE_RANDOM_TICKS = 30;
    /** 乱流の威力 1 あたりの、雷撃 1 回のダメージ（倍率を掛ける前、仮）。威力 10 で 10。 */
    public static final float STRIKE_DAMAGE_PER_POWER = 1.0F;
    private static final ResourceLocation AE2_CHARGER = ResourceLocation.fromNamespaceAndPath("ae2", "charger");
    private static final int MAX_SPARKS_PER_TICK = 3;

    /** 落ちているアイテムごとの、AE2 のチャージにためた FE（エンティティの番号ごと）。サーバー側だけ。 */
    private final Map<Integer, Double> chargeProgress = new HashMap<>();
    /** 受け手ごとの、前の回に渡しきれなかった端数（{@link #MAX_ENERGY_CARRY} まで）。毎回、今いる受け手だけで作り直す。サーバー側だけ。 */
    private Map<Long, Double> energyCarry = new HashMap<>();
    /** 威力（コストのつまみ、1〜10）。 */
    /** 1 体・1 か所あたりの強さに使う威力（{@link #MAX_EFFECT_POWER} まで）。 */
    private float power = DEFAULT_POWER;
    /** 入れた威力（10 より上もそのまま。コストと重なりの強さに使う）。 */
    private float rawPower = DEFAULT_POWER;
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

    /** 置く前に呼ぶ。 */
    public void setPower(float power) {
        this.rawPower = Math.max(0.0F, power);
        this.power = Math.min(rawPower, MAX_EFFECT_POWER);
    }

    public float getPower() {
        return power;
    }

    /** 同じ種類が重なったところでは、威力の一番高いもの（10 より上も比べる）だけが効く。 */
    @Override
    protected double stackStrength() {
        return rawPower;
    }

    /** 1 tick あたりの充電能力（FE）: 置いたときのコスト × {@link #FE_PER_PSI} ÷ 存続時間（tick）× 機械を動かす速さ。 */
    public double capacityPerTick() {
        return capacityPerTick(getPlacementCost(), getLifetimeTicks(), getMachineSpeed());
    }

    /**
     * 置いたときのコスト {@code placementCost}（時間の分を含む）、存続時間 {@code lifetimeTicks}、速さ {@code machineSpeed} の領域の
     * 1 tick あたりの充電能力（FE）。コストも存続時間も「時間」に比例するので、1 tick あたりの量は時間によらない。
     */
    public static double capacityPerTick(float placementCost, int lifetimeTicks, float machineSpeed) {
        return lifetimeTicks <= 0 ? 0.0 : placementCost * FE_PER_PSI / lifetimeTicks * machineSpeed;
    }

    /** 雷撃 1 回の基本のダメージ（倍率を掛ける前）。 */
    public static float strikeDamage(float turbulencePower) {
        return STRIKE_DAMAGE_PER_POWER * Math.max(0.0F, turbulencePower);
    }

    /** 1 体ずつの効き目はない（FE は {@link #supplyEnergy} でまとめて分ける）ので、ほぼ呼ばない。 */
    @Override
    protected int effectInterval() {
        return Integer.MAX_VALUE;
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

    /** FE は {@link #supplyEnergy} でまとめて分けるので、1 体ずつの効き目はない。 */
    @Override
    protected void affect(Entity target) {
    }

    /**
     * 充電能力を受け取る相手 1 つ: FE の窓口か、AE2 のチャージ中の落ちているアイテム。{@code boost} は雷雲の上乗せ。{@code key} は
     * 端数の繰り越しに使う、エンティティの番号と窓口の場所（{@link #receiverKey}）。
     */
    private record Receiver(IEnergyStorage storage, ItemEntity charging, double boost, long key) {
    }

    /** エンティティ {@code entityId} の窓口 {@code slot}（0: エンティティ自身、1: 落ちているアイテム、2 以降: 持ち物の枠）。 */
    private static long receiverKey(int entityId, int slot) {
        return ((long) entityId << 16) | (slot & 0xFFFF);
    }

    /**
     * {@link #ENERGY_INTERVAL} tick ぶんの充電能力を、範囲の中の受け手で均等に分ける。満タンで受け取れなかった分は、受け取りきった
     * 受け手に分け直す（1 回だけ）。FE の端数で渡しきれなかった分は、受け手ごとに次の回へ繰り越す（{@link #MAX_ENERGY_CARRY}）。
     */
    private void supplyEnergy(ServerLevel level) {
        List<Receiver> receivers = new ArrayList<>();
        for (Entity target : entitiesInside()) {
            double boost = 1.0 + stormBoost(stormPowerAt(BlockPos.containing(target.getBoundingBox().getCenter())));
            int id = target.getId();
            addReceiver(receivers, target.getCapability(Capabilities.EnergyStorage.ENTITY, null), boost, receiverKey(id, 0));
            if (target instanceof ItemEntity item) {
                addReceiver(receivers, item.getItem().getCapability(Capabilities.EnergyStorage.ITEM), boost, receiverKey(id, 1));
                if (!ae2ChargeResult(level, item.getItem()).isEmpty()) {
                    receivers.add(new Receiver(null, item, boost, receiverKey(id, 1)));
                } else {
                    chargeProgress.remove(item.getId());
                }
            } else if (target instanceof Player player) {
                Inventory inventory = player.getInventory();
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    addReceiver(receivers, itemStorage(inventory.getItem(i)), boost, receiverKey(id, 2 + i));
                }
            } else if (target instanceof LivingEntity living) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    addReceiver(receivers, itemStorage(living.getItemBySlot(slot)), boost, receiverKey(id, 2 + slot.ordinal()));
                }
            }
        }
        Map<Long, Double> carry = new HashMap<>();
        if (receivers.isEmpty()) {
            energyCarry = carry;
            return;
        }
        double share = capacityPerTick() * ENERGY_INTERVAL / receivers.size();
        double leftover = 0.0;
        List<Receiver> filled = new ArrayList<>();
        for (Receiver receiver : receivers) {
            double owed = share + energyCarry.getOrDefault(receiver.key(), 0.0);
            double rest = owed - give(level, receiver, owed);
            if (rest <= MAX_ENERGY_CARRY) {
                // 受け取りきった。残りは FE の端数なので次の回へ繰り越す。
                filled.add(receiver);
                if (rest > 1.0E-9) {
                    carry.put(receiver.key(), rest);
                }
            } else {
                // 満タンで受け取れなかった。今回の分け前のうち渡せなかった分を分け直しに回し、前の繰り越しは捨てる。
                leftover += Math.min(share, rest);
            }
        }
        if (leftover > 1.0E-6 && !filled.isEmpty()) {
            double extra = leftover / filled.size();
            for (Receiver receiver : filled) {
                double owed = extra + carry.getOrDefault(receiver.key(), 0.0);
                double rest = owed - give(level, receiver, owed);
                if (rest > 1.0E-9) {
                    carry.put(receiver.key(), Math.min(MAX_ENERGY_CARRY, rest));
                } else {
                    carry.remove(receiver.key());
                }
            }
        }
        energyCarry = carry;
    }

    private static IEnergyStorage itemStorage(ItemStack stack) {
        return stack.isEmpty() ? null : stack.getCapability(Capabilities.EnergyStorage.ITEM);
    }

    /** FE を受け取れる窓口だけ足す（満タンのものは数えない）。 */
    private static void addReceiver(List<Receiver> receivers, IEnergyStorage storage, double boost, long key) {
        if (storage != null && storage.canReceive() && storage.receiveEnergy(Integer.MAX_VALUE, true) > 0) {
            receivers.add(new Receiver(storage, null, boost, key));
        }
    }

    /** 受け手に {@code share}（雷雲の上乗せの前）を渡し、使った分（上乗せの前）を返す。 */
    private double give(ServerLevel level, Receiver receiver, double share) {
        double amount = share * receiver.boost();
        if (receiver.charging() != null) {
            if (receiver.charging().isAlive()) {
                chargeAe2(level, receiver.charging(), amount);
            }
            return share;
        }
        // 渡せるのは整数の FE だけ。渡した分（上乗せの前）を返し、端数は呼ぶ側が繰り越す。
        int offered = (int) Math.floor(amount);
        if (offered <= 0) {
            return 0.0;
        }
        int accepted = receiver.storage().receiveEnergy(offered, false);
        return accepted / receiver.boost();
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
        if (tickCount % ENERGY_INTERVAL == 0) {
            supplyEnergy(level);
        }
        if (tickCount % (ENERGY_INTERVAL * 20) == 0) {
            chargeProgress.keySet().removeIf(id -> !(level.getEntity(id) instanceof ItemEntity item) || !item.isAlive());
        }
    }

    /**
     * 落ちているアイテムに AE2 のチャージャーと同じ加工を進める。FE を {@code energy} ためて、{@link #FE_PER_AE2_CHARGE} ごとに 1 個
     * ずつ加工した結果に変え、同じ場所に落とす。
     */
    private void chargeAe2(ServerLevel level, ItemEntity item, double energy) {
        ItemStack stack = item.getItem();
        ItemStack result = ae2ChargeResult(level, stack);
        if (result.isEmpty()) {
            chargeProgress.remove(item.getId());
            return;
        }
        // 加工している間は消えるまでの時間を止める（2026-11-12 ユーザー）。
        ZoneItemLifetimes.hold(item, level.getGameTime());
        double progress = chargeProgress.getOrDefault(item.getId(), 0.0) + energy;
        int done = (int) Math.min(stack.getCount(), Math.floor(progress / FE_PER_AE2_CHARGE));
        progress -= done * (double) FE_PER_AE2_CHARGE;
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
                && stormAt(BlockPos.containing(target.getBoundingBox().getCenter())) != null
                && isStrongestAt(BlockPos.containing(target.getBoundingBox().getCenter())));
        LivingEntity target = candidates.isEmpty() ? null : candidates.get(random.nextInt(candidates.size()));
        Vec3 end;
        if (target != null) {
            end = target.getBoundingBox().getCenter();
        } else {
            end = null;
            for (int i = 0; i < 6 && end == null; i++) {
                Vec3 point = new Vec3(bounds.minX + random.nextDouble() * bounds.getXsize(), bounds.minY + 0.05,
                        bounds.minZ + random.nextDouble() * bounds.getZsize());
                if (stormAt(BlockPos.containing(point)) != null && isStrongestAt(BlockPos.containing(point))) {
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
        energyCarry = new HashMap<>();
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
