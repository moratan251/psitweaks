package com.moratan251.psitweaks.common.tile;

import com.moratan251.psitweaks.common.blocks.ZoneFeederBlock;
import com.moratan251.psitweaks.common.compat.MekanismGameplayCompat;
import com.moratan251.psitweaks.common.items.PsitweaksDataComponents;
import com.moratan251.psitweaks.common.registries.PsitweaksBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 領域給電機: ケーブルなどから FE を受け取ってためる。ためた FE は、このブロックを範囲に含む、置いた人の荷電領域が引いて配る
 * （{@code EntityChargedZone}）ほか、ケーブルなどから取り出せる。荷電領域からは受け取らない。
 * <ul>
 *   <li>1 tick に受け取れるのも取り出せるのも、それぞれ {@link #receiveRate()}（究極ユニバーサルケーブルの送電量）まで。</li>
 *   <li>ためられるのは、その {@link #BUFFER_TICKS} tick ぶん（荷電領域が引く間隔）。</li>
 *   <li>壊すと、ためた FE は落ちたアイテムに残り、置き直すと戻る。</li>
 * </ul>
 */
public class ZoneFeederBlockEntity extends BlockEntity {
    /**
     * Mekanism がないときの 1 tick あたりの受け取り（FE）。究極ユニバーサルケーブルの既定の 8,192,000 J/t を、Mekanism の既定の
     * 換算（2.5 J = 1 FE）で FE にした値。
     */
    public static final int DEFAULT_RECEIVE_RATE = 3_276_800;
    /** ためられる量: 受け取りの何 tick ぶんか。荷電領域が FE を配る間隔と同じにして、送電量を落とさない。 */
    public static final int BUFFER_TICKS = 5;
    /** 送っている見た目（光る・火花）を続ける長さ（tick）。荷電領域が引く間隔より長くする。 */
    private static final int ACTIVE_TICKS = 10;
    /** 火花を出す間隔（tick）。 */
    private static final int PARTICLE_INTERVAL = 10;
    /** 火花の数を最大にする、1 tick あたりの送った量（FE）。 */
    private static final double FULL_PARTICLE_FE_PER_TICK = 100_000.0;
    private static final String NBT_ENERGY = "Energy";
    private static final String NBT_OWNER = "Owner";

    private static int cachedRate = -1;

    private long energy;
    @Nullable
    private UUID owner;
    /** この tick に受け取った量と、その tick（1 tick の受け取りの上限を守るため）。サーバー側だけ。 */
    private long receivedAt = Long.MIN_VALUE;
    private long receivedThisTick;
    /** この tick に取り出された量と、その tick（1 tick の取り出しの上限を守るため）。サーバー側だけ。 */
    private long extractedAt = Long.MIN_VALUE;
    private long extractedThisTick;
    /** 最後に荷電領域が引いた時刻。サーバー側だけ。 */
    private long lastSentAt = Long.MIN_VALUE;
    /** 火花の間隔のあいだに送った量（FE）。サーバー側だけ。 */
    private long sentWindow;
    /** これまでに送った量の合計（FE、GameTest 用）。 */
    private long sentTotal;
    /** 保存とコンパレーターの更新が要るか。 */
    private boolean dirty;
    private final IEnergyStorage storage = new Storage();

    public ZoneFeederBlockEntity(BlockPos pos, BlockState state) {
        super(PsitweaksBlockEntityTypes.ZONE_FEEDER.get(), pos, state);
    }

    /**
     * 1 tick あたりに受け取れる量（FE）: 究極ユニバーサルケーブルの送電量。Mekanism があれば、その設定のケーブルの送電量を
     * Mekanism の換算で FE にした値（int に収まるよう、{@link #BUFFER_TICKS} 倍しても int を越えないところで止める）。
     */
    public static int receiveRate() {
        int rate = cachedRate;
        if (rate < 0) {
            long value = MekanismGameplayCompat.ultimateCableFePerTick(DEFAULT_RECEIVE_RATE);
            rate = (int) Math.max(1L, Math.min(value, Integer.MAX_VALUE / BUFFER_TICKS));
            cachedRate = rate;
        }
        return rate;
    }

    /** ためられる量（FE）。 */
    public static int capacity() {
        return receiveRate() * BUFFER_TICKS;
    }

    public IEnergyStorage energyStorage() {
        return storage;
    }

    /** ためている量（FE）。 */
    public long stored() {
        return energy;
    }

    @Nullable
    public UUID owner() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        markDirty();
    }

    /**
     * {@code caster}（領域の術者。自律詠唱機の詠唱主体は所有者の UUID を名乗る）の荷電領域が、この給電機から引いてよいか。
     * 置いた人が分からない給電機（コマンドで置いたなど）は、誰の荷電領域でも引ける。
     */
    public boolean allows(@Nullable Player caster) {
        return owner == null || (caster != null && owner.equals(caster.getUUID()));
    }

    /** 荷電領域が {@code amount} FE を引く（ためている量まで）。引いた量を返す。 */
    public long draw(long amount) {
        long taken = Math.max(0L, Math.min(amount, energy));
        if (taken <= 0L || level == null) {
            return 0L;
        }
        energy -= taken;
        sentWindow += taken;
        sentTotal += taken;
        lastSentAt = level.getGameTime();
        markDirty();
        return taken;
    }

    /** これまでに荷電領域へ送った量の合計（FE、GameTest 用）。 */
    public long sentTotal() {
        return sentTotal;
    }

    /** コンパレーターの信号（0〜15）: ためている割合。少しでもあれば 1 以上。 */
    public int comparatorSignal() {
        if (energy <= 0L) {
            return 0;
        }
        return 1 + (int) Math.floor(14.0 * energy / capacity());
    }

    private void markDirty() {
        dirty = true;
    }

    /** サーバー側で毎 tick 呼ぶ: 保存の印、送っている見た目（光る・火花・送り始めの音）。 */
    public void serverTick(ServerLevel level) {
        long now = level.getGameTime();
        if (dirty) {
            dirty = false;
            setChanged();
        }
        BlockState state = getBlockState();
        boolean sending = lastSentAt != Long.MIN_VALUE && now - lastSentAt <= ACTIVE_TICKS;
        if (state.hasProperty(ZoneFeederBlock.ACTIVE) && state.getValue(ZoneFeederBlock.ACTIVE) != sending) {
            level.setBlock(worldPosition, state.setValue(ZoneFeederBlock.ACTIVE, sending), 3);
            if (sending) {
                level.playSound(null, worldPosition, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.4F, 1.6F);
            }
        }
        if (now % PARTICLE_INTERVAL == 0) {
            if (sentWindow > 0L) {
                double perTick = (double) sentWindow / PARTICLE_INTERVAL;
                int count = 1 + (int) Math.round(5.0 * Math.min(1.0, perTick / FULL_PARTICLE_FE_PER_TICK));
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, worldPosition.getX() + 0.5, worldPosition.getY() + 1.05,
                        worldPosition.getZ() + 0.5, count, 0.2, 0.05, 0.2, 0.08);
            }
            sentWindow = 0L;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong(NBT_ENERGY, energy);
        if (owner != null) {
            tag.putUUID(NBT_OWNER, owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = Math.max(0L, tag.getLong(NBT_ENERGY));
        owner = tag.hasUUID(NBT_OWNER) ? tag.getUUID(NBT_OWNER) : null;
    }

    /** 壊したときのアイテムに、ためている FE を残す。空なら付けず、新品の給電機と重ねられるようにする。 */
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (energy > 0L) {
            builder.set(PsitweaksDataComponents.ZONE_FEEDER_ENERGY.get(), energy);
        }
    }

    /** 置き直したときに、アイテムに残した FE を戻す（ためられる量まで）。 */
    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        Long stored = input.get(PsitweaksDataComponents.ZONE_FEEDER_ENERGY.get());
        if (stored != null) {
            energy = Math.max(0L, Math.min(stored, capacity()));
            markDirty();
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(NBT_ENERGY);
    }

    /** FE の窓口（どの面も同じ）。受け取りと取り出しの上限は別々に数える。 */
    private final class Storage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            Level at = level;
            if (toReceive <= 0 || at == null || at.isClientSide) {
                return 0;
            }
            long now = at.getGameTime();
            long already = receivedAt == now ? receivedThisTick : 0L;
            long room = Math.min(receiveRate() - already, capacity() - energy);
            int accepted = (int) Math.max(0L, Math.min(toReceive, room));
            if (!simulate && accepted > 0) {
                energy += accepted;
                receivedAt = now;
                receivedThisTick = already + accepted;
                markDirty();
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            Level at = level;
            if (toExtract <= 0 || at == null || at.isClientSide) {
                return 0;
            }
            long now = at.getGameTime();
            long already = extractedAt == now ? extractedThisTick : 0L;
            long room = Math.min(receiveRate() - already, energy);
            int taken = (int) Math.max(0L, Math.min(toExtract, room));
            if (!simulate && taken > 0) {
                energy -= taken;
                extractedAt = now;
                extractedThisTick = already + taken;
                markDirty();
            }
            return taken;
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, energy);
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity();
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
