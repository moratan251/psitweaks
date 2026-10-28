package com.moratan251.psitweaks.common.entities.SpellGram.region;

import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 領域型の魔法式オブジェクト（試作）。円柱の領域を持ち、付いた性質（{@link SpellGramProperty}）を領域の中へ働かせ続ける。
 * 時間では消えない。術者が回収するか、壊されるか、術者がいなくなる（ログアウト・別のディメンション・チャンクの読み込み外）
 * まで続く。チャンクと一緒には保存しない。
 * <p>
 * 術者ごとの置ける数・枠は {@link SpellGramCapacity}、術者ごとの一覧は {@link SpellGramRegionIndex}。
 */
public class EntitySpellGramRegion extends SpellGramObject {
    public static final double MIN_RADIUS = 2.0D;
    public static final double MAX_RADIUS = 12.0D;
    /** 中心から下へ・上へ何ブロックを領域に含めるか。 */
    public static final int DEPTH = 1;
    public static final int HEIGHT = 3;
    public static final int PULSE_INTERVAL_TICKS = 10;
    /** ブロックへの作用は2回に1回（1秒ごと）。 */
    private static final int BLOCK_PULSE_EVERY = 2;
    private static final int CASTER_CHECK_INTERVAL_TICKS = 20;
    /** 強度 = 威力 × これ × 付けた術者の事象干渉力。 */
    public static final double INTEGRITY_PER_POWER = 10.0D;

    private static final String NBT_PROPERTIES = "Properties";
    private static final String NBT_CAPACITY_USE = "CapacityUse";
    private static final String NBT_CREATED_AT = "CreatedAt";
    private static final String NBT_SAFE_TO_PLAYERS = "SafeToPlayers";
    private static final String NBT_MAX_INTEGRITY = "MaxIntegrity";

    private static final EntityDataAccessor<Integer> DATA_PROPERTIES =
            SynchedEntityData.defineId(EntitySpellGramRegion.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(EntitySpellGramRegion.class, EntityDataSerializers.INT);

    public enum EndReason {
        /** 術者が回収した。 */
        RECALLED,
        /** 強度が尽きた（他の術式・攻撃で壊された）。 */
        DESTROYED,
        /** 術者がいなくなった。 */
        CASTER_GONE,
        /** 置ける数・枠を超えたので、新しいものに場所を譲った。 */
        REPLACED
    }

    private final Map<SpellGramProperty, SpellGramPropertyInstance> properties = new EnumMap<>(SpellGramProperty.class);
    private int capacityUse;
    private long createdAt;
    private boolean safeToPlayers;
    private float maxIntegrity;
    private int pulseCounter;
    private int blockPulseCounter;
    private boolean ended;

    public EntitySpellGramRegion(EntityType<? extends EntitySpellGramRegion> type, Level level) {
        super(type, level);
    }

    public EntitySpellGramRegion(Level level) {
        this(PsitweaksEntities.SPELLGRAM_REGION.get(), level);
    }

    @Override
    protected void defineSpellGramSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_PROPERTIES, 0);
        builder.define(DATA_COLOR, 0xFFFFFF);
    }

    /** 置く前に呼ぶ。強度は性質を付けるときに足す（素の魔法式は {@code baseIntegrity} だけ）。 */
    public void configure(LivingEntity caster, AttackRules.Options options, double radius, float baseIntegrity, int color) {
        setCaster(caster);
        this.safeToPlayers = options.safeToPlayers();
        setDamageRadius((float) clampRadius(radius));
        setAoEDamage(0.0F);
        setMotionDamping(0.0F);
        this.maxIntegrity = Math.max(1.0F, baseIntegrity);
        setIntegrity(this.maxIntegrity);
        this.createdAt = level().getGameTime();
        setColor(color);
    }

    public static double clampRadius(double radius) {
        return Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
    }

    /** 術者の事象干渉力（置いた・付けた時点の値を使う）。 */
    public static double interferenceOf(@Nullable LivingEntity caster) {
        return caster != null ? caster.getAttributeValue(PsitweaksAttributes.SPELL_DAMAGE_FACTOR) : 1.0D;
    }

    // ---- 性質 ----

    /**
     * 性質を足す。炎と氷は打ち消し合い、打ち消した分は湿潤（水蒸気）になる。
     *
     * @param scale    全体倍率 × 個別倍率 × 術者の事象干渉力（{@link AttackRules#multiplier}）
     * @param integrity 増やす強度
     */
    public void addProperty(SpellGramProperty property, double power, double scale, float integrity) {
        double remaining = Math.max(0.0D, power);
        SpellGramProperty opposite = property.opposite();
        SpellGramPropertyInstance against = opposite != null ? properties.get(opposite) : null;
        if (against != null && remaining > 0.0D) {
            double reacted = Math.min(against.power(), remaining);
            if (against.weaken(reacted)) {
                properties.remove(opposite);
            }
            remaining -= reacted;
            mergeProperty(SpellGramProperty.WET, reacted, scale);
            onSteam();
        }
        if (remaining > 0.0D) {
            mergeProperty(property, remaining, scale);
        }

        maxIntegrity += Math.max(0.0F, integrity);
        setIntegrity(getIntegrity() + Math.max(0.0F, integrity));
        syncProperties();
    }

    private void mergeProperty(SpellGramProperty property, double power, double scale) {
        if (power <= 0.0D) {
            return;
        }
        SpellGramPropertyInstance existing = properties.get(property);
        if (existing == null) {
            properties.put(property, new SpellGramPropertyInstance(property, power, scale));
        } else {
            existing.merge(power, scale);
        }
    }

    private void onSteam() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 0.8F);
            double spread = getDamageRadius() * 0.4D;
            serverLevel.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5D, getZ(), 24, spread, 0.3D, spread, 0.02D);
        }
    }

    @Nullable
    public SpellGramPropertyInstance getProperty(SpellGramProperty property) {
        return properties.get(property);
    }

    public double getPropertyPower(SpellGramProperty property) {
        SpellGramPropertyInstance instance = properties.get(property);
        return instance != null ? instance.power() : 0.0D;
    }

    public boolean hasProperty(SpellGramProperty property) {
        return (getPropertyMask() & property.bit()) != 0;
    }

    /** 付いている性質（クライアントでも読める）。 */
    public int getPropertyMask() {
        return this.entityData.get(DATA_PROPERTIES);
    }

    private void syncProperties() {
        int mask = 0;
        for (SpellGramProperty property : properties.keySet()) {
            mask |= property.bit();
        }
        this.entityData.set(DATA_PROPERTIES, mask);
    }

    public int getColor() {
        return this.entityData.get(DATA_COLOR);
    }

    public void setColor(int color) {
        this.entityData.set(DATA_COLOR, color & 0xFFFFFF);
    }

    // ---- 枠 ----

    /** この魔法式が使っている枠（{@link SpellGramCapacity} が決める。数え方は版によって違う）。 */
    public int getCapacityUse() {
        return capacityUse;
    }

    public void setCapacityUse(int capacityUse) {
        this.capacityUse = Math.max(0, capacityUse);
    }

    public void addCapacityUse(int amount) {
        if (amount == 0) {
            return;
        }
        setCapacityUse(capacityUse + amount);
        SpellGramCapacity.onChanged(level(), getCasterUuid());
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public float getMaxIntegrity() {
        return maxIntegrity;
    }

    public boolean isOwnedBy(@Nullable Entity entity) {
        return CasterIds.matches(entity, getCasterUuid());
    }

    // ---- 領域 ----

    public AABB regionBox() {
        double radius = getDamageRadius();
        return new AABB(getX() - radius, getY() - DEPTH, getZ() - radius, getX() + radius, getY() + HEIGHT, getZ() + radius);
    }

    public boolean contains(Vec3 point) {
        double radius = getDamageRadius();
        double dx = point.x - getX();
        double dz = point.z - getZ();
        return dx * dx + dz * dz <= radius * radius && point.y >= getY() - DEPTH && point.y <= getY() + HEIGHT;
    }

    public boolean contains(Entity entity) {
        double radius = getDamageRadius();
        double dx = entity.getX() - getX();
        double dz = entity.getZ() - getZ();
        AABB box = entity.getBoundingBox();
        return dx * dx + dz * dz <= radius * radius && box.maxY >= getY() - DEPTH && box.minY <= getY() + HEIGHT;
    }

    public List<LivingEntity> livingInside() {
        return level().getEntitiesOfClass(LivingEntity.class, regionBox(), this::contains);
    }

    // ---- 動き ----

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && !isRemoved()) {
            spawnClientParticles();
        }
    }

    @Override
    protected void onSpellGramTickServer() {
        if (tickCount % CASTER_CHECK_INTERVAL_TICKS == 0 && getCaster() == null) {
            end(EndReason.CASTER_GONE);
            return;
        }

        pulseCounter++;
        if (pulseCounter < PULSE_INTERVAL_TICKS || properties.isEmpty()) {
            return;
        }
        pulseCounter = 0;
        blockPulseCounter = (blockPulseCounter + 1) % BLOCK_PULSE_EVERY;

        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity caster = getCaster();
        List<LivingEntity> inside = livingInside();
        for (SpellGramPropertyInstance instance : new ArrayList<>(properties.values())) {
            SpellGramPropertyEffects.pulse(this, serverLevel, caster, inside, instance, blockPulseCounter == 0);
        }
    }

    @Override
    protected void tickLifetime() {
        // 時間では消えない（術者が魔力を与え続けている間は続く）。
    }

    @Override
    public AttackRules.Options attackOptions() {
        return AttackRules.Options.of(safeToPlayers);
    }

    /** 魔法式を解く。枠は {@link #onRemovedFromLevel} で戻る。 */
    public void end(EndReason reason) {
        if (ended || isRemoved() || level().isClientSide) {
            return;
        }
        ended = true;
        playEndEffects();
        if (reason == EndReason.REPLACED && getCaster() instanceof Player player) {
            player.displayClientMessage(Component.translatable("psitweaks.spellgram_region.replaced"), true);
        }
        discard();
    }

    @Override
    protected void onSpellGramDestroyed(@Nullable DamageSource source) {
        if (!ended) {
            ended = true;
            playEndEffects();
        }
    }

    private void playEndEffects() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.7F, 1.4F);
            double spread = getDamageRadius() * 0.3D;
            serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.2D, getZ(), 12, spread, 0.1D, spread, 0.02D);
        }
    }

    /** 中を歩く生き物に押されて動かないようにする（術式での移動は {@link #moveBySpell}）。 */
    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!level().isClientSide) {
            SpellGramRegionIndex.add(this);
            SpellGramCapacity.onChanged(level(), getCasterUuid());
        }
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (!level().isClientSide) {
            SpellGramRegionIndex.remove(this);
            SpellGramCapacity.onChanged(level(), getCasterUuid());
        }
    }

    /** 術者が近くにいる間しか続かないので、チャンクと一緒には保存しない。 */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    // ---- 見た目 ----

    private void spawnClientParticles() {
        float radius = getDamageRadius();
        if (radius <= 0.0F || tickCount % 2 != 0) {
            return;
        }
        int mask = getPropertyMask();
        List<SpellGramProperty> shown = new ArrayList<>();
        for (SpellGramProperty property : SpellGramProperty.values()) {
            if ((mask & property.bit()) != 0) {
                shown.add(property);
            }
        }

        int ringCount = 4 + (int) radius;
        double baseAngle = (tickCount * 0.05D) % (Math.PI * 2.0D);
        for (int i = 0; i < ringCount; i++) {
            double angle = baseAngle + (Math.PI * 2.0D * i) / ringCount;
            double x = getX() + Math.cos(angle) * radius;
            double z = getZ() + Math.sin(angle) * radius;
            if (shown.isEmpty()) {
                level().addParticle(dust(getColor()), x, getY() + 0.1D, z, 0.0D, 0.0D, 0.0D);
            } else {
                SpellGramProperty property = shown.get(i % shown.size());
                level().addParticle(dust(property.color()), x, getY() + 0.1D, z, 0.0D, 0.0D, 0.0D);
            }
        }

        for (SpellGramProperty property : shown) {
            for (int i = 0; i < 2; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double distance = Math.sqrt(random.nextDouble()) * radius;
                double x = getX() + Math.cos(angle) * distance;
                double z = getZ() + Math.sin(angle) * distance;
                double y = getY() + random.nextDouble() * HEIGHT * 0.5D;
                level().addParticle(property.particle(), x, y, z, 0.0D, 0.01D, 0.0D);
            }
        }
    }

    private static DustParticleOptions dust(int color) {
        return new DustParticleOptions(new Vector3f(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F), 1.2F);
    }

    // ---- 保存・同期 ----

    @Override
    protected void readSpellGramData(CompoundTag tag) {
        properties.clear();
        ListTag list = tag.getList(NBT_PROPERTIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            SpellGramPropertyInstance instance = SpellGramPropertyInstance.load(list.getCompound(i));
            if (instance != null) {
                properties.put(instance.property(), instance);
            }
        }
        capacityUse = Math.max(0, tag.getInt(NBT_CAPACITY_USE));
        createdAt = tag.getLong(NBT_CREATED_AT);
        safeToPlayers = tag.getBoolean(NBT_SAFE_TO_PLAYERS);
        maxIntegrity = Math.max(1.0F, tag.getFloat(NBT_MAX_INTEGRITY));
        syncProperties();
    }

    @Override
    protected void addSpellGramData(CompoundTag tag) {
        ListTag list = new ListTag();
        for (SpellGramPropertyInstance instance : properties.values()) {
            list.add(instance.save());
        }
        tag.put(NBT_PROPERTIES, list);
        tag.putInt(NBT_CAPACITY_USE, capacityUse);
        tag.putLong(NBT_CREATED_AT, createdAt);
        tag.putBoolean(NBT_SAFE_TO_PLAYERS, safeToPlayers);
        tag.putFloat(NBT_MAX_INTEGRITY, maxIntegrity);
    }

    @Override
    protected void writeSpellGramSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(getPropertyMask());
        buffer.writeInt(getColor());
    }

    @Override
    protected void readSpellGramSpawnData(RegistryFriendlyByteBuf buffer) {
        this.entityData.set(DATA_PROPERTIES, buffer.readInt());
        setColor(buffer.readInt());
    }
}
