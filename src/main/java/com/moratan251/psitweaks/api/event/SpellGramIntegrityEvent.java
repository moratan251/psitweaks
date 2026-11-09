package com.moratan251.psitweaks.api.event;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * 魔法式オブジェクトの耐久値が術式で減らされる直前（サーバー側、{@code NeoForge.EVENT_BUS}）。
 * 減る量（減らす側の事象干渉力を掛けた後）を変えたり、取り消して減らさないようにしたりできる。
 * 情報強化のような守りや、ほかの mod の魔法から減り方を変えるための受け口。
 */
public class SpellGramIntegrityEvent extends Event implements ICancellableEvent {
    private final SpellGramObject object;
    @Nullable
    private final LivingEntity reducer;
    private float amount;

    public SpellGramIntegrityEvent(SpellGramObject object, @Nullable LivingEntity reducer, float amount) {
        this.object = object;
        this.reducer = reducer;
        this.amount = amount;
    }

    /** 減らされる魔法式オブジェクト。 */
    public SpellGramObject getObject() {
        return object;
    }

    /** 減らす側の術者。術者のいない減り方（ほかの mod など）は null。 */
    @Nullable
    public LivingEntity getReducer() {
        return reducer;
    }

    /** 減る量。 */
    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = Math.max(0.0F, amount);
    }
}
