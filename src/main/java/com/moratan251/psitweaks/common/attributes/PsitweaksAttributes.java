package com.moratan251.psitweaks.common.attributes;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PsitweaksAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, Psitweaks.MOD_ID);

    public static final DeferredHolder<Attribute, Attribute> SPELL_DAMAGE_FACTOR =
            ATTRIBUTES.register("spell_damage_factor",
                    () -> new RangedAttribute("attribute.psitweaks.spell_damage_factor", 1.0, 0.0, 100.0)
                            .setSyncable(true));

    private PsitweaksAttributes() {
    }

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }
}
