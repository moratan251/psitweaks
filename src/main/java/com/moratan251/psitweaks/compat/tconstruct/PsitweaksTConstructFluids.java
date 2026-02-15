package com.moratan251.psitweaks.compat.tconstruct;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import slimeknights.mantle.registration.deferred.FluidDeferredRegister;
import slimeknights.mantle.registration.object.FlowingFluidObject;

import java.util.function.Consumer;

public class PsitweaksTConstructFluids {
    private static final FluidDeferredRegister FLUIDS = new FluidDeferredRegister(Psitweaks.MOD_ID);

    public static final FlowingFluidObject<ForgeFlowingFluid> MOLTEN_PSIMETAL = registerMoltenFluid("molten_psimetal", 0xFFB3BFF3, 1000, 10);
    public static final FlowingFluidObject<ForgeFlowingFluid> MOLTEN_PSIGEM = registerMoltenFluid("molten_psigem", 0xFF747ED3, 1000, 10);
    public static final FlowingFluidObject<ForgeFlowingFluid> MOLTEN_IVORY_PSIMETAL = registerMoltenFluid("molten_ivory_psimetal", 0xFFFFFFF0, 1000, 10);
    public static final FlowingFluidObject<ForgeFlowingFluid> MOLTEN_EBONY_PSIMETAL = registerMoltenFluid("molten_ebony_psimetal", 0xFF101010, 1000, 10);
    public static final FlowingFluidObject<ForgeFlowingFluid> MOLTEN_FLASHMETAL = registerMoltenFluid("molten_flashmetal", 0xFFFFFA95, 1100, 12);
    public static final FlowingFluidObject<ForgeFlowingFluid> MOLTEN_CHAOTIC_PSIMETAL = registerMoltenFluid("molten_chaotic_psimetal", 0xFF7F7F7F, 1000, 10);
    public static final FlowingFluidObject<ForgeFlowingFluid> MOLTEN_HEAVY_PSIMETAL = registerMoltenFluid("molten_heavy_psimetal", 0xFF325980, 1300, 13);
    public static final FlowingFluidObject<ForgeFlowingFluid> MOLTEN_PSIONIC_ECHO = registerMoltenFluid("molten_psionic_echo", 0xFF40006E, 1000, 10);

    private static FlowingFluidObject<ForgeFlowingFluid> registerMoltenFluid(String name, int tintColor, int temperature, int lightLevel) {
        String textureName = withoutMoltenPrefix(name);
        return FLUIDS.register(name)
                .type(() -> new MoltenFluidType(hot(name, temperature, lightLevel), tintColor, textureName))
                .commonTag()
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(30)
                .explosionResistance(100)
                .flowing();
    }

    private static String withoutMoltenPrefix(String name) {
        if (name.startsWith("molten_")) {
            return name.substring("molten_".length());
        }
        return name;
    }

    private static FluidType.Properties hot(String name, int temperature, int lightLevel) {
        return FluidType.Properties.create()
                .density(2000)
                .viscosity(10000)
                .temperature(temperature)
                .descriptionId("fluid." + Psitweaks.MOD_ID + "." + name)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                .motionScale(0.0023333333333333335D)
                .canSwim(false)
                .canDrown(false)
                .pathType(BlockPathTypes.LAVA)
                .adjacentPathType(null)
                .lightLevel(lightLevel);
    }

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }

    private static final class MoltenFluidType extends FluidType {
        private final ResourceLocation still;
        private final ResourceLocation flowing;
        private final int tintColor;

        private MoltenFluidType(Properties properties, int tintColor, String textureName) {
            super(properties);
            this.still = ResourceLocation.fromNamespaceAndPath("tconstruct", "fluid/molten/" + textureName + "/still");
            this.flowing = ResourceLocation.fromNamespaceAndPath("tconstruct", "fluid/molten/" + textureName + "/flowing");
            this.tintColor = tintColor;
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return still;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return flowing;
                }

                @Override
                public int getTintColor() {
                    return tintColor;
                }
            });
        }
    }
}
