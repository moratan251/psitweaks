package com.moratan251.psitweaks.common.entities.SpellGram;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import java.lang.ref.WeakReference;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * 作動式: 仮装行列の核。虚像（{@link EntityParadeDecoy}）の胸の高さに重なる見えない常駐魔法式で、仮装行列の残り時間と耐久値を
 * 持つ。核が消えると虚像も消え、虚像が消えると核も消える。
 * <ul>
 *   <li>虚像が攻撃されるたびに、ダメージの量によらず耐久値が {@link #ATTACK_WEAR} 減る（{@link #wearByAttack}）。
 *   作動式: 術式解体・サイ・ブリットは虚像ではなく核を、それぞれの量で削る。</li>
 *   <li>作動式: 魔法式延長・魔法式回収、取得子: 自分の常駐魔法式の対象になり、置ける数の上限にも数える。</li>
 *   <li>他人の領域干渉では止まらない。</li>
 *   <li>照準を合わせたときの名前と耐久値は術者にだけ見せる（{@link #isShownTo}）。他人に見えると虚像だと分かるため。
 *   術者以外には透明として扱い（{@link SpellGramObject#isInvisibleTo}）、Jade などの照準の表示にも出さない。</li>
 * </ul>
 */
public class EntityParadeCore extends SpellGramObject {
    /** 虚像が 1 回攻撃されたときに減る耐久値。 */
    public static final float ATTACK_WEAR = 2.0F;
    /** 虚像の足元から核までの高さ（胸のあたり）。術式解体とサイ・ブリットは虚像の体を狙えば核に届く。 */
    public static final double CHEST_HEIGHT = 1.0;

    private static final EntityDataAccessor<Integer> DATA_IMAGE_ID =
            SynchedEntityData.defineId(EntityParadeCore.class, EntityDataSerializers.INT);
    private static final int NO_IMAGE = -1;

    @Nullable
    private WeakReference<EntityParadeDecoy> imageRef;

    public EntityParadeCore(EntityType<? extends EntityParadeCore> type, Level level) {
        super(type, level);
    }

    public EntityParadeCore(Level level) {
        this(PsitweaksEntities.PARADE_CORE.get(), level);
    }

    @Override
    public boolean isResident() {
        return true;
    }

    @Override
    protected void defineSpellGramSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_IMAGE_ID, NO_IMAGE);
    }

    /** 置く前に呼ぶ。核を重ねる虚像。 */
    public void bind(EntityParadeDecoy image) {
        imageRef = new WeakReference<>(image);
        entityData.set(DATA_IMAGE_ID, image.getId());
        followImage(image);
    }

    /** 核を重ねている虚像。消えていれば null。 */
    @Nullable
    public EntityParadeDecoy getImage() {
        EntityParadeDecoy cached = imageRef != null ? imageRef.get() : null;
        if (cached != null && !cached.isRemoved()) {
            return cached;
        }
        int id = entityData.get(DATA_IMAGE_ID);
        return id != NO_IMAGE && level().getEntity(id) instanceof EntityParadeDecoy image && !image.isRemoved() ? image : null;
    }

    /** 虚像が攻撃されたとき: ダメージの量によらず耐久値を {@link #ATTACK_WEAR} 減らす。尽きれば核も虚像も消える。 */
    public void wearByAttack() {
        reduceIntegrity(ATTACK_WEAR);
    }

    /**
     * 術者。保存した ID で引けないとき（ワールドに入っていない他 Mod の FakePlayer など）は、虚像が覚えている持ち主を使う。
     * 虚像の持ち主はいつも核の術者と同じ。
     */
    @Nullable
    @Override
    public LivingEntity getCaster() {
        LivingEntity caster = super.getCaster();
        if (caster != null) {
            return caster;
        }
        EntityParadeDecoy image = getImage();
        return image != null ? image.getOwnerPlayer() : null;
    }

    @Override
    protected void tickMotion() {
        EntityParadeDecoy image = getImage();
        if (image != null) {
            followImage(image);
        }
    }

    private void followImage(EntityParadeDecoy image) {
        setPos(image.getX(), image.getY() + CHEST_HEIGHT, image.getZ());
    }

    @Override
    protected void onSpellGramTickServer() {
        if (getImage() == null) {
            // 虚像が先に消えた（術者が倒れたなど）。
            end(EndReason.FINISHED);
        }
    }

    /** 耐久値が尽きた・解体されたときは虚像が砕け、それ以外（時間切れ・回収・術者がいない）は薄れて消える。 */
    @Override
    protected void onEnd(EndReason reason) {
        EntityParadeDecoy image = getImage();
        if (image != null) {
            image.vanish(reason == EndReason.DESTROYED || reason == EndReason.DISPELLED);
        }
    }

    /**
     * 残りの時間。仮装行列は時間と耐久値が一つ（耐久値は時間とともに減り切る）なので、今の耐久値が持つ時間を返す。攻撃で
     * 削られた分も、そのぶん短くなる（耐久値は削られた分だけ早く尽きる）。
     */
    @Override
    public int getRemainingTicks() {
        float max = getMaxIntegrity();
        float share = getTimeShare();
        int byTime = super.getRemainingTicks();
        if (max <= 0.0F || share <= 0.0F) {
            return byTime;
        }
        return Math.min(byTime, (int) Math.ceil(getIntegrity() * getLifetimeTicks() / (max * share)));
    }

    @Override
    public boolean isShownTo(Entity viewer) {
        EntityParadeDecoy image = getImage();
        return image != null && image.isOwnedBy(viewer);
    }
}
