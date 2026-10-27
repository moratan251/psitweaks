package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.caster.AutonomousCasterRegistry;
import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.caster.VirtualCad;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The lookalike conjured by Trick: Parade. It wears the caster's skin, armour and held items, draws the attacks of
 * hostile mobs, and wavers away after a number of hits or when its time is up. Nothing hurts it: each attack only
 * uses up one of its hits. It is never saved.
 * <p>
 * It wanders around the spot it was conjured at, like a decoy that will not stand still, while acting like its owner:
 * it copies the owner's gear, crouching and arm swings, and looks where the owner looks, except that it turns to face
 * whoever is after it (its last attacker, else the nearest mob targeting it).
 * <p>
 * An autonomous caster conjures the lookalike of whoever placed the machine: their skin and name, with the machine's
 * held item, drawing mobs in the placer's stead. Its internal CAD (worn in the head slot, held for a moment while
 * breaking blocks) is never copied.
 */
public class EntityParadeDecoy extends PathfinderMob implements OwnableEntity {
    /** Entity event: a hit made the lookalike waver; clients draw it see-through for a moment. */
    public static final byte EVENT_WAVER = 60;
    public static final int WAVER_TICKS = 8;
    /** How far from where it was conjured the lookalike wanders. */
    public static final int WANDER_RADIUS = 5;
    /** Ticks after a hit during which further hits are ignored, as for players. */
    private static final int HIT_COOLDOWN = 10;
    /** How long the lookalike keeps facing its last attacker. */
    private static final int ATTACKER_MEMORY_TICKS = 100;
    private static final double ATTACKER_FACE_RANGE = 24.0;
    /** Mobs targeting the lookalike within this range make it turn to them. */
    private static final double WATCH_RANGE = 16.0;
    private static final int WATCH_SCAN_INTERVAL = 5;
    /** How often the lookalike takes over mobs that went for its owner without asking (wardens, brain-driven mobs). */
    private static final int LURE_INTERVAL = 10;

    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(EntityParadeDecoy.class, EntityDataSerializers.OPTIONAL_UUID);
    /** Whose skin the lookalike wears: the owner's UUID, which for an autonomous caster is the machine owner's. */
    private static final EntityDataAccessor<Optional<UUID>> LOOK =
            SynchedEntityData.defineId(EntityParadeDecoy.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(EntityParadeDecoy.class, EntityDataSerializers.INT);

    @Nullable
    private WeakReference<Player> ownerRef;
    private int hitsLeft = 1;
    private int lifetime = 20;
    /** Client side: ticks left of the waver after a hit. */
    private int waver;
    @Nullable
    private WeakReference<LivingEntity> lastAttacker;
    private int lastAttackerTicks;
    @Nullable
    private WeakReference<Mob> watched;
    private boolean ownerWasSwinging;

    public EntityParadeDecoy(EntityType<? extends EntityParadeDecoy> type, Level level) {
        super(type, level);
        setSilent(true);
        setCanPickUpLoot(false);
        setPersistenceRequired();
    }

    public EntityParadeDecoy(Level level, Vec3 position) {
        this(PsitweaksEntities.PARADE_DECOY.get(), level);
        setPos(position.x, position.y, position.z);
        restrictTo(BlockPos.containing(position), WANDER_RADIUS);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MoveTowardsRestrictionGoal(this, 1.0));
        // Short pauses between strolls, so it keeps moving like someone looking around.
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.9, 0.0F) {
            {
                setInterval(20);
            }
        });
    }

    /** Copies the owner's look and gear and sets how many hits and ticks the lookalike lasts. */
    public void configure(Player owner, int hits, int lifetimeTicks, int color) {
        // An autonomous caster has its machine owner's UUID; the clients would hide that real player behind this lookalike.
        entityData.set(OWNER, Optional.of(CasterIds.idOf(owner)));
        // ...but the lookalike still wears that owner's look.
        entityData.set(LOOK, Optional.of(owner.getUUID()));
        entityData.set(COLOR, color & 0xFFFFFF);
        ownerRef = new WeakReference<>(owner);
        setCustomName(lookalikeName(owner));
        copyGear(owner);
        setLeftHanded(owner.getMainArm() == HumanoidArm.LEFT);
        hitsLeft = Math.max(1, hits);
        lifetime = Math.max(1, lifetimeTicks);
        setYRot(owner.getYRot());
        setXRot(owner.getXRot());
        yBodyRot = owner.yBodyRot;
        yHeadRot = owner.getYHeadRot();
        yBodyRotO = yBodyRot;
        yHeadRotO = yHeadRot;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, Optional.empty());
        builder.define(LOOK, Optional.empty());
        builder.define(COLOR, CombatSpellHelper.DEFAULT_SPELL_COLOR);
    }

    /** The name the lookalike goes by: its owner's, or for an autonomous caster its machine owner's (none if unknown). */
    @Nullable
    private static Component lookalikeName(Player owner) {
        if (owner instanceof AutonomousCasterPlayer caster) {
            return caster.ownerName().<Component>map(Component::literal).orElse(null);
        }
        return owner.getName();
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return entityData.get(OWNER).orElse(null);
    }

    /** The UUID of the player whose skin the lookalike wears, which differs from the owner for an autonomous caster. */
    @Nullable
    public UUID getLookUUID() {
        return entityData.get(LOOK).or(() -> entityData.get(OWNER)).orElse(null);
    }

    public boolean isOwnedBy(Entity entity) {
        return CasterIds.matches(entity, getOwnerUUID());
    }

    /**
     * Whether an autonomous caster conjured this lookalike as the phantom of {@code player}, who placed the machine: it
     * wears their look but is kept by the machine. A player's own lookalike is not one.
     */
    public boolean standsInForPlacer(Player player) {
        return !(player instanceof AutonomousCasterPlayer) && player.getUUID().equals(getLookUUID()) && !isOwnedBy(player);
    }

    /** The owner while it is alive in this level; also finds owners that are not in the world (fake players). */
    @Nullable
    public Player getOwnerPlayer() {
        Player cached = ownerRef != null ? ownerRef.get() : null;
        // The autonomous caster's player is never added to the world, so it is never removed either; it is detached instead.
        boolean detached = cached instanceof AutonomousCasterPlayer caster && !caster.isAttached();
        if (cached != null && !detached && !cached.isRemoved() && cached.level() == level()) {
            return cached;
        }
        UUID owner = getOwnerUUID();
        if (owner == null) {
            return null;
        }
        // An autonomous caster is stored by its machine key, not by the owner's UUID it carries.
        Player caster = AutonomousCasterRegistry.find(level(), owner);
        return caster != null ? caster : level().getPlayerByUUID(owner);
    }

    public int getColor() {
        return entityData.get(COLOR);
    }

    public int getHitsLeft() {
        return hitsLeft;
    }

    /** Client side: 0..1, how strongly the lookalike is wavering after a hit. */
    public float getWaver(float partialTick) {
        return waver <= 0 ? 0.0F : Math.min(1.0F, (waver - partialTick) / WAVER_TICKS);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (waver > 0) {
                waver--;
            }
            return;
        }
        Player owner = getOwnerPlayer();
        if (owner == null || !owner.isAlive() || --lifetime <= 0) {
            vanish(false);
            return;
        }
        mimic(owner);
        if (tickCount % LURE_INTERVAL == 0 && level() instanceof ServerLevel level) {
            Parades.lureFrom(level, owner, this);
        }
    }

    /** Copies the owner's gear and gestures, and looks where the owner looks unless someone is after it. */
    private void mimic(Player owner) {
        copyGear(owner);
        setShiftKeyDown(owner.isShiftKeyDown());
        setPose(owner.isCrouching() ? Pose.CROUCHING : Pose.STANDING);
        if (owner.swinging && !ownerWasSwinging) {
            swing(owner.swingingArm != null ? owner.swingingArm : InteractionHand.MAIN_HAND, true);
        }
        ownerWasSwinging = owner.swinging;

        LivingEntity enemy = findEnemy();
        if (enemy != null) {
            getLookControl().setLookAt(enemy, 30.0F, 30.0F);
        } else {
            // Look the way the owner looks: at a point ahead along the owner's line of sight.
            Vec3 ahead = getEyePosition().add(owner.getLookAngle().scale(8.0));
            getLookControl().setLookAt(ahead.x, ahead.y, ahead.z, 30.0F, 30.0F);
        }
    }

    private void copyGear(Player owner) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HAND && slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            ItemStack worn = owner.getItemBySlot(slot);
            if (VirtualCad.isVirtualCad(worn)) {
                worn = ItemStack.EMPTY;
            }
            if (!ItemStack.matches(getItemBySlot(slot), worn)) {
                setItemSlot(slot, worn.copy());
                setDropChance(slot, 0.0F);
            }
        }
    }

    /** Whoever the lookalike should face: its recent attacker, else the nearest mob targeting it. */
    @Nullable
    LivingEntity findEnemy() {
        if (lastAttackerTicks > 0) {
            lastAttackerTicks--;
            LivingEntity attacker = lastAttacker != null ? lastAttacker.get() : null;
            if (attacker != null && attacker.isAlive() && attacker.level() == level()
                    && attacker.distanceToSqr(this) <= ATTACKER_FACE_RANGE * ATTACKER_FACE_RANGE) {
                return attacker;
            }
        }
        Mob current = watched != null ? watched.get() : null;
        if (current == null || !current.isAlive() || current.getTarget() != this || tickCount % WATCH_SCAN_INTERVAL == 0) {
            current = null;
            double best = WATCH_RANGE * WATCH_RANGE;
            for (Mob mob : level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(WATCH_RANGE),
                    mob -> mob.isAlive() && mob.getTarget() == this)) {
                double distance = mob.distanceToSqr(this);
                if (distance <= best) {
                    best = distance;
                    current = mob;
                }
            }
            watched = current != null ? new WeakReference<>(current) : null;
        }
        return current;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved()) {
            return false;
        }
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            // The void and /kill end it at once.
            discard();
            return false;
        }
        if (isInvulnerableTo(source) || invulnerableTime > 0) {
            return false;
        }
        invulnerableTime = HIT_COOLDOWN;
        if (source.getEntity() instanceof LivingEntity attacker && !isOwnedBy(attacker)) {
            lastAttacker = new WeakReference<>(attacker);
            lastAttackerTicks = ATTACKER_MEMORY_TICKS;
        }
        if (--hitsLeft <= 0) {
            vanish(true);
        } else {
            level().broadcastEntityEvent(this, EVENT_WAVER);
            CombatEffects.sound((ServerLevel) level(), position(), SoundEvents.AMETHYST_BLOCK_HIT, 1.0F, 1.4F);
        }
        return true;
    }

    /** Ends the lookalike: it shatters when its last hit is used up, otherwise it simply fades. */
    public void vanish(boolean shattered) {
        if (isRemoved()) {
            return;
        }
        if (level() instanceof ServerLevel level) {
            Vec3 center = getBoundingBox().getCenter();
            level.sendParticles(shattered ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.ENCHANT,
                    center.x, center.y, center.z, shattered ? 30 : 20, 0.3, 0.6, 0.3, shattered ? 0.3 : 0.5);
            CombatEffects.sound(level, center, shattered ? SoundEvents.AMETHYST_CLUSTER_BREAK : SoundEvents.ILLUSIONER_MIRROR_MOVE,
                    1.0F, shattered ? 1.2F : 1.0F);
        }
        discard();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_WAVER) {
            waver = WAVER_TICKS;
            hurtTime = 0;
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        // No leads, name tags or other mob handling: it is only an image.
        return InteractionResult.PASS;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
