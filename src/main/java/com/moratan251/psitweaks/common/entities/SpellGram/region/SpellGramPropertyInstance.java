package com.moratan251.psitweaks.common.entities.SpellGram.region;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * 魔法式に付いた性質1つ分。威力と、付けた時点の倍率（全体倍率 × 個別倍率 × 付けた術者の事象干渉力）を持つ。
 * 倍率は付けた時点の値で固定し、後から術者の装備が変わっても変えない。
 */
public final class SpellGramPropertyInstance {
    private static final String NBT_PROPERTY = "Property";
    private static final String NBT_POWER = "Power";
    private static final String NBT_SCALE = "Scale";

    private final SpellGramProperty property;
    private double power;
    private double scale;

    public SpellGramPropertyInstance(SpellGramProperty property, double power, double scale) {
        this.property = property;
        this.power = Math.max(0.0D, power);
        this.scale = Math.max(0.0D, scale);
    }

    public SpellGramProperty property() {
        return property;
    }

    public double power() {
        return power;
    }

    public double scale() {
        return scale;
    }

    /** 同じ性質を重ねる。倍率は威力で重み付けした平均にする。 */
    public void merge(double addedPower, double addedScale) {
        double total = power + addedPower;
        if (total <= 0.0D) {
            return;
        }
        scale = (scale * power + addedScale * addedPower) / total;
        power = total;
    }

    /** 反応で威力を減らす。0 以下になったら取り除く合図として true を返す。 */
    public boolean weaken(double amount) {
        power = Math.max(0.0D, power - amount);
        return power <= 1.0E-6D;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(NBT_PROPERTY, property.id());
        tag.putDouble(NBT_POWER, power);
        tag.putDouble(NBT_SCALE, scale);
        return tag;
    }

    @Nullable
    public static SpellGramPropertyInstance load(CompoundTag tag) {
        SpellGramProperty property = SpellGramProperty.byId(tag.getString(NBT_PROPERTY));
        if (property == null) {
            return null;
        }
        return new SpellGramPropertyInstance(property, tag.getDouble(NBT_POWER), tag.getDouble(NBT_SCALE));
    }
}
