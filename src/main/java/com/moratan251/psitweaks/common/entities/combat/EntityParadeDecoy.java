package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityParadeCore;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.ParadeSight;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * The image projected by Trick: Parade. It wears the caster's skin, armour, held items, name and health, and draws the
 * attacks of hostile mobs. Its time and integrity are held by an invisible Resident SpellGram inside it
 * ({@link EntityParadeCore}): nothing hurts the image, each attack only wears the core down by the same amount, and the
 * image goes when the core does (and the other way round). It is never saved.
 * <p>
 * It stays a fixed number of blocks in front of its owner (behind for a negative distance), measured along the way the
 * owner looks on the horizontal plane, at the height of the owner's feet; or along a fixed way (up and down included),
 * when the trick was given a Direction ({@link #isDirectionFixed}). It turns with the owner, floats and passes
 * through blocks, and nothing in the terrain (walls, lava, fire, water, falls) affects it. It copies the owner's gear,
 * crouching, arm swings, steps and the way they face, except that its head turns to whoever is after it (its last
 * attacker, else the nearest mob targeting it).
 */
public class EntityParadeDecoy extends PathfinderMob implements OwnableEntity {
    /** Entity event: a hit made the image waver; clients draw it see-through for a moment. */
    public static final byte EVENT_WAVER = 60;
    public static final int WAVER_TICKS = 8;
    /** Ticks after a hit during which further hits are ignored, as for players. */
    private static final int HIT_COOLDOWN = 10;
    /** How long the image keeps facing its last attacker. */
    private static final int ATTACKER_MEMORY_TICKS = 100;
    private static final double ATTACKER_FACE_RANGE = 24.0;
    /** Mobs targeting the image within this range make it turn its head to them. */
    private static final double WATCH_RANGE = 16.0;
    private static final int WATCH_SCAN_INTERVAL = 5;
    /** How far the head turns away from the body when it watches an enemy, like a player's. */
    private static final float MAX_HEAD_TURN = 75.0F;
    /** How often the image takes over mobs that went for its owner without asking (wardens, brain-driven mobs). */
    private static final int LURE_INTERVAL = 10;
    /** A crouching player's size and eye height, so the image's eyes (and its core) drop when it crouches. */
    private static final EntityDimensions CROUCHING_DIMENSIONS = EntityDimensions.scalable(0.6F, 1.5F).withEyeHeight(1.27F);

    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(EntityParadeDecoy.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(EntityParadeDecoy.class, EntityDataSerializers.INT);
    /** Blocks in front of the owner (negative: behind) where the image stands. */
    private static final EntityDataAccessor<Float> DISTANCE =
            SynchedEntityData.defineId(EntityParadeDecoy.class, EntityDataSerializers.FLOAT);
    /** The way (a unit vector) the image keeps to when it does not turn with the owner's view; zero when it does. */
    private static final EntityDataAccessor<Vector3f> FIXED_DIRECTION =
            SynchedEntityData.defineId(EntityParadeDecoy.class, EntityDataSerializers.VECTOR3);
    /** Whether the head is turned to an enemy; clients then keep the server's head rotation instead of the owner's. */
    private static final EntityDataAccessor<Boolean> WATCHING =
            SynchedEntityData.defineId(EntityParadeDecoy.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private WeakReference<Player> ownerRef;
    /** Server side: the Resident SpellGram that holds the image's time and integrity. */
    @Nullable
    private WeakReference<EntityParadeCore> coreRef;
    /** The chance that an attack on the owner misses while the image stands. */
    private double evasionChance;
    /** Client side: ticks left of the waver after a hit. */
    private int waver;
    @Nullable
    private WeakReference<LivingEntity> lastAttacker;
    private int lastAttackerTicks;
    @Nullable
    private WeakReference<Mob> watched;
    private boolean ownerWasSwinging;
    private int ownerSwingTime;

    public EntityParadeDecoy(EntityType<? extends EntityParadeDecoy> type, Level level) {
        super(type, level);
        setSilent(true);
        setCanPickUpLoot(false);
        setPersistenceRequired();
        setNoGravity(true);
        noPhysics = true;
    }

    public EntityParadeDecoy(Level level, Vec3 position) {
        this(PsitweaksEntities.PARADE_DECOY.get(), level);
        setPos(position.x, position.y, position.z);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    /**
     * Copies the owner's look and gear, puts the image {@code distance} blocks in front of the owner (behind if
     * negative), and sets the owner's evasion from that distance ({@link Parades#evasionChance}). How long it lasts and
     * how much it takes is up to its core ({@link #bindCore}). A {@code fixedDirection} (a unit vector) keeps the image
     * along it instead of the owner's view.
     */
    public void configure(Player owner, double distance, int color, @Nullable Vec3 fixedDirection) {
        entityData.set(OWNER, Optional.of(owner.getUUID()));
        entityData.set(COLOR, color & 0xFFFFFF);
        entityData.set(DISTANCE, (float) distance);
        entityData.set(FIXED_DIRECTION, fixedDirection != null ? fixedDirection.toVector3f() : new Vector3f());
        ownerRef = new WeakReference<>(owner);
        setCustomName(owner.getName());
        copyGear(owner);
        copyHealth(owner);
        setLeftHanded(owner.getMainArm() == HumanoidArm.LEFT);
        evasionChance = Parades.evasionChance(distance);
        follow(owner);
        copyRotation(owner);
        setOldPosAndRot();
        yBodyRotO = yBodyRot;
        yHeadRotO = yHeadRot;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, Optional.empty());
        builder.define(COLOR, CombatSpellHelper.DEFAULT_SPELL_COLOR);
        builder.define(DISTANCE, 0.0F);
        builder.define(FIXED_DIRECTION, new Vector3f());
        builder.define(WATCHING, false);
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return entityData.get(OWNER).orElse(null);
    }

    public boolean isOwnedBy(@Nullable Entity entity) {
        return entity != null && entity.getUUID().equals(getOwnerUUID());
    }

    /**
     * Whether {@code entity} is on the image's side: its owner (or the owner's autonomous caster, which carries the
     * owner's UUID) or their allies (team mates, pets). Their attacks and projectiles pass it by.
     */
    public boolean isFriendly(@Nullable Entity entity) {
        if (entity == null) {
            return false;
        }
        if (isOwnedBy(entity)) {
            return true;
        }
        Player owner = getOwnerPlayer();
        return owner != null && (owner.isAlliedTo(entity) || entity.isAlliedTo(owner));
    }

    /** The owner while they are in this level. */
    @Nullable
    public Player getOwnerPlayer() {
        Player cached = ownerRef != null ? ownerRef.get() : null;
        if (cached != null && !cached.isRemoved() && cached.level() == level()) {
            return cached;
        }
        UUID owner = getOwnerUUID();
        return owner != null ? level().getPlayerByUUID(owner) : null;
    }

    public int getColor() {
        return entityData.get(COLOR);
    }

    /** Blocks in front of the owner (negative: behind) where the image stands. */
    public double getDistance() {
        return entityData.get(DISTANCE);
    }

    /** Whether the image keeps to one side of its owner instead of turning with their view. */
    public boolean isDirectionFixed() {
        return entityData.get(FIXED_DIRECTION).lengthSquared() > 0.0F;
    }

    /**
     * Where the image stands from its owner: {@link #getDistance} blocks along the fixed way, else along the horizontal
     * direction of {@code ownerYaw}.
     */
    public Vec3 imageOffset(float ownerYaw) {
        Vector3f fixed = entityData.get(FIXED_DIRECTION);
        double distance = getDistance();
        return fixed.lengthSquared() > 0.0F
                ? new Vec3(fixed.x() * distance, fixed.y() * distance, fixed.z() * distance)
                : Parades.horizontalOffset(ownerYaw, distance);
    }

    public boolean isWatching() {
        return entityData.get(WATCHING);
    }

    /** Server side: ties the image to its core; the image goes when the core does, and the core when the image does. */
    public void bindCore(EntityParadeCore core) {
        coreRef = new WeakReference<>(core);
    }

    /**
     * Server side: sets the image's health and max health to its owner's, so health bars (Neat and the like) show the
     * owner's real health, never the core's integrity. Attacks on the image wear only the core and leave the health as
     * it is, the way attacks on the owner miss while the image stands.
     */
    private void copyHealth(Player owner) {
        AttributeInstance maxHealth = getAttribute(Attributes.MAX_HEALTH);
        double ownerMax = owner.getMaxHealth();
        if (maxHealth != null && maxHealth.getBaseValue() != ownerMax) {
            maxHealth.setBaseValue(ownerMax);
        }
        super.setHealth(owner.getHealth());
    }

    /** Server side: the core while it lasts. */
    @Nullable
    public EntityParadeCore getCore() {
        EntityParadeCore core = coreRef != null ? coreRef.get() : null;
        return core != null && !core.isEnded() ? core : null;
    }

    /** Server side: the chance that an attack on the owner misses while the image stands. */
    public double getEvasionChance() {
        return evasionChance;
    }

    /** Client side: 0..1, how strongly the image is wavering after a hit. */
    public float getWaver(float partialTick) {
        return waver <= 0 ? 0.0F : Math.min(1.0F, (waver - partialTick) / WAVER_TICKS);
    }

    /** Plays out its arm swings like a player's; plain mobs do not run the swing animation on their own. */
    @Override
    public void aiStep() {
        updateSwingTime();
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        Player owner = getOwnerPlayer();
        if (level().isClientSide) {
            if (waver > 0) {
                waver--;
            }
            // Follow the owner as this client knows them, so the image keeps up between the server's updates.
            if (owner != null) {
                follow(owner);
                if (isWatching()) {
                    yBodyRot = owner.yBodyRot;
                } else {
                    copyRotation(owner);
                }
            } else {
                yBodyRot = getYRot();
            }
            return;
        }
        if (owner == null || !owner.isAlive() || getCore() == null) {
            vanish(false);
            return;
        }
        follow(owner);
        mimic(owner);
        copyHealth(owner);
        // The core stands at the image's eyes and looks its way, whichever of the two ticks first.
        EntityParadeCore core = getCore();
        if (core != null) {
            core.followImage(this);
        }
        // Arrows and stingers that hit it do not stay stuck in it.
        if (getArrowCount() > 0) {
            setArrowCount(0);
        }
        if (getStingerCount() > 0) {
            setStingerCount(0);
        }
        if (tickCount % LURE_INTERVAL == 0 && level() instanceof ServerLevel level) {
            Parades.lureFrom(level, owner, this);
        }
    }

    /** Stands where the owner projects it, whatever is in the way. */
    private void follow(Player owner) {
        Vec3 at = owner.position().add(imageOffset(owner.getYRot()));
        setPos(at.x, at.y, at.z);
        setDeltaMovement(Vec3.ZERO);
        resetFallDistance();
    }

    /** Faces the way the owner faces. */
    private void copyRotation(Player owner) {
        setYRot(owner.getYRot());
        setXRot(owner.getXRot());
        yBodyRot = owner.yBodyRot;
        yHeadRot = owner.getYHeadRot();
    }

    /** Copies the owner's gear and gestures, and faces the owner's way, turning its head to whoever is after it. */
    private void mimic(Player owner) {
        copyGear(owner);
        // Goes by the owner's name, whatever a name tag says.
        if (!owner.getName().equals(getCustomName())) {
            setCustomName(owner.getName());
        }
        setShiftKeyDown(owner.isShiftKeyDown());
        setPose(owner.isCrouching() ? Pose.CROUCHING : Pose.STANDING);
        // A new swing: the owner starts swinging, or swings again before the last one is over (its swing time starts over).
        if (owner.swinging && (!ownerWasSwinging || owner.swingTime < ownerSwingTime)) {
            swing(owner.swingingArm != null ? owner.swingingArm : InteractionHand.MAIN_HAND, true);
        }
        ownerWasSwinging = owner.swinging;
        ownerSwingTime = owner.swingTime;

        LivingEntity enemy = findEnemy();
        entityData.set(WATCHING, enemy != null);
        if (enemy != null) {
            // The look control turns the head on the next tick; the body keeps the owner's facing.
            getLookControl().setLookAt(enemy, 30.0F, 30.0F);
            setYRot(owner.getYRot());
            yBodyRot = owner.yBodyRot;
            float turn = Mth.wrapDegrees(yHeadRot - yBodyRot);
            yHeadRot = yBodyRot + Mth.clamp(turn, -MAX_HEAD_TURN, MAX_HEAD_TURN);
        } else {
            copyRotation(owner);
        }
    }

    private void copyGear(Player owner) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HAND && slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            ItemStack worn = owner.getItemBySlot(slot);
            if (!ItemStack.matches(getItemBySlot(slot), worn)) {
                setItemSlot(slot, worn.copy());
                setDropChance(slot, 0.0F);
            }
        }
    }

    /** Whoever the image should face: its recent attacker, else the nearest mob targeting it. */
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

    /** Crouching shrinks it and lowers its eyes as it does a player's. */
    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return pose == Pose.CROUCHING ? CROUCHING_DIMENSIONS : super.getDefaultDimensions(pose);
    }

    /** Steps in time with the owner rather than with its own sweep as the owner turns. */
    @Override
    public void calculateEntityAnimation(boolean includeHeight) {
        Player owner = getOwnerPlayer();
        updateWalkAnimation(owner == null ? 0.0F : (float) Mth.length(owner.getX() - owner.xo, 0.0, owner.getZ() - owner.zo));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved()) {
            return false;
        }
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            // The void and /kill end it at once.
            vanish(false);
            return false;
        }
        // Gram Demolition and Psi Bullet wear the core down by their own amounts instead.
        if (source.is(PsitweaksDamageTypes.GRAM_DEMOLITION) || source.is(PsitweaksDamageTypes.PSI_BULLET)) {
            return false;
        }
        // Only attacks wear it down: the terrain (walls, lava, fire, water, falls) has no attacker, and friends do not count.
        Entity attacker = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
        if (attacker == null || isFriendly(attacker) || isInvulnerableTo(source) || invulnerableTime > 0) {
            return false;
        }
        invulnerableTime = HIT_COOLDOWN;
        if (attacker instanceof LivingEntity living) {
            lastAttacker = new WeakReference<>(living);
            lastAttackerTicks = ATTACKER_MEMORY_TICKS;
        }
        EntityParadeCore core = getCore();
        if (core != null) {
            // Whatever the damage, each attack takes the same integrity; the last one ends the core, which shatters the image.
            core.wearByAttack();
        }
        if (!isRemoved()) {
            level().broadcastEntityEvent(this, EVENT_WAVER);
            CombatEffects.sound((ServerLevel) level(), position(), SoundEvents.AMETHYST_BLOCK_HIT, 1.0F, 1.4F);
        }
        return true;
    }

    /**
     * Ends the image and its core: it shatters when it is worn out or dispelled, otherwise it simply fades.
     */
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
        EntityParadeCore core = getCore();
        if (core != null) {
            core.end(SpellGramObject.EndReason.FINISHED);
        }
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

    /** The owner's crosshair passes through their own image, so it never blocks their attacks or block breaking. */
    @Override
    public boolean isPickable() {
        if (level().isClientSide && ParadeSight.isLocalPlayer(getOwnerUUID())) {
            return false;
        }
        return super.isPickable();
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
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public void lavaHurt() {
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    /**
     * Its health is its owner's, whatever anyone else sets it to (another mod setting it directly, healing): this puts
     * it back to the owner's health, or to full while it has no owner yet.
     */
    @Override
    public void setHealth(float health) {
        Player owner = getOwnerPlayer();
        super.setHealth(owner != null ? owner.getHealth() : getMaxHealth());
    }

    /**
     * Goes by its owner's name (kept as its custom name) without counting as named, like a player: Neat writes the
     * names of named entities in italics.
     */
    @Override
    public boolean hasCustomName() {
        return false;
    }

    /** Shows its owner's name as the owner's own name shows (team colour and prefix, other mods' name formatting). */
    @Override
    public Component getDisplayName() {
        Player owner = getOwnerPlayer();
        return owner != null ? owner.getDisplayName() : super.getDisplayName();
    }

    /** Nothing gets past {@link #hurt} into the usual damage handling, even when called directly. */
    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
    }

    /** It does not die, even when another mod calls this directly: it only goes with its core (or the void and /kill). */
    @Override
    public void die(DamageSource source) {
    }

    /** Never starts dying, whatever its health data says. */
    @Override
    public boolean isDeadOrDying() {
        return false;
    }

    /** Nothing pushes or flings it (explosions, other mods' knockback, motion spells): it only ever stands where it is put. */
    @Override
    public void setDeltaMovement(Vec3 motion) {
        super.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    protected void markHurt() {
    }

    /** It leaves the world alone: pressure plates, tripwires and the like do not notice it. */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /** It never rides anything (boats, minecarts, mobs). */
    @Override
    protected boolean canRide(Entity vehicle) {
        return false;
    }

    /** No effect takes hold on an image: poison, wither, slowness, glowing and the rest, from any source. */
    @SuppressWarnings("deprecation")
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false;
    }

    /** It never catches fire (fire blocks, Fire Aspect, flaming arrows, lightning). */
    @Override
    public void setRemainingFireTicks(int ticks) {
        super.setRemainingFireTicks(0);
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    /** Powder snow does not freeze it. */
    @Override
    public boolean canFreeze() {
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
