package com.moratan251.psitweaks.common.attributes;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PsitweaksAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Psitweaks.MOD_ID);

    public static final RegistryObject<Attribute> SPELL_DAMAGE_FACTOR =
            ATTRIBUTES.register("spell_damage_factor",
                    () -> new RangedAttribute("attribute.psitweaks.spell_damage_factor", 1.0, 0.0, 100.0)
                            .setSyncable(true));

    public static final RegistryObject<Attribute> MAX_PSI_BONUS =
            ATTRIBUTES.register("max_psi_bonus",
                    () -> new RangedAttribute("attribute.psitweaks.max_psi_bonus", 0.0, -100000.0, 100000.0)
                            .setSyncable(true));

    public static final RegistryObject<Attribute> PSI_REGEN_BONUS =
            ATTRIBUTES.register("psi_regen_bonus",
                    () -> new RangedAttribute("attribute.psitweaks.psi_regen_bonus", 0.0, -1000.0, 1000.0)
                            .setSyncable(true));

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }
}
