package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.storage.idea.*;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.*;
import mekanism.api.chemical.infuse.*;
import mekanism.api.chemical.pigment.*;
import mekanism.api.chemical.slurry.*;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.registries.IForgeRegistry;
import java.util.function.BiFunction;
import org.jetbrains.annotations.Nullable;

/** Forge 1.20.1 Chemical registries remain separate. Encoded keys are namespace:kind/path. */
public final class IdeaStorageMekanismIntegration {
    private IdeaStorageMekanismIntegration() {}

    public static ResourceLocation key(String kind, ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), kind + "/" + id.getPath());
    }

    @Nullable
    public static Chemical<?> chemical(ResourceLocation key) {
        int slash = key.getPath().indexOf('/');
        if (slash < 0) return null;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath().substring(slash + 1));
        Chemical<?> chemical = switch (key.getPath().substring(0, slash)) {
            case "gas" -> MekanismAPI.gasRegistry().getValue(id);
            case "infuse" -> MekanismAPI.infuseTypeRegistry().getValue(id);
            case "pigment" -> MekanismAPI.pigmentRegistry().getValue(id);
            case "slurry" -> MekanismAPI.slurryRegistry().getValue(id);
            default -> null;
        };
        return chemical == null || chemical.isEmptyType() ? null : chemical;
    }

    @Nullable
    public static IdeaStorageChemicalTransfer plan(PlayerIdeaStorage storage, ItemStack container,
            @Nullable ResourceLocation target, IdeaStorageTransferDirection direction) {
        IdeaStorageChemicalTransfer result = planKind(storage, container, target, direction, "gas",
                Capabilities.GAS_HANDLER, MekanismAPI.gasRegistry(), GasStack::new);
        if (result == null) result = planKind(storage, container, target, direction, "infuse",
                Capabilities.INFUSION_HANDLER, MekanismAPI.infuseTypeRegistry(), InfusionStack::new);
        if (result == null) result = planKind(storage, container, target, direction, "pigment",
                Capabilities.PIGMENT_HANDLER, MekanismAPI.pigmentRegistry(), PigmentStack::new);
        if (result == null) result = planKind(storage, container, target, direction, "slurry",
                Capabilities.SLURRY_HANDLER, MekanismAPI.slurryRegistry(), SlurryStack::new);
        return result;
    }

    @Nullable
    private static <C extends Chemical<C>, S extends ChemicalStack<C>, H extends IChemicalHandler<C, S>>
            IdeaStorageChemicalTransfer planKind(PlayerIdeaStorage storage, ItemStack container,
                    @Nullable ResourceLocation target, IdeaStorageTransferDirection direction, String kind,
                    Capability<H> capability, IForgeRegistry<C> registry, BiFunction<C, Long, S> stackFactory) {
        ItemStack working = container.copyWithCount(1);
        H handler = working.getCapability(capability).orElse(null);
        if (handler == null) return null;
        if (direction.allowsIntoContainer() && target != null && target.getPath().startsWith(kind + "/")) {
            C chemical = registry.getValue(ResourceLocation.fromNamespaceAndPath(target.getNamespace(), target.getPath().substring(kind.length() + 1)));
            long available = storage.simulateExtractChemical(target, Long.MAX_VALUE);
            if (chemical != null && !chemical.isEmptyType() && available > 0) {
                S request = stackFactory.apply(chemical, available);
                long accepted = available - handler.insertChemical(request, Action.SIMULATE).getAmount();
                if (accepted > 0) {
                    long moved = accepted - handler.insertChemical(stackFactory.apply(chemical, accepted), Action.EXECUTE).getAmount();
                    if (moved > 0) return new IdeaStorageChemicalTransfer(working, target, moved, false);
                }
            }
        }
        if (!direction.allowsIntoStorage()) return null;
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            S contained = handler.getChemicalInTank(tank);
            if (contained.isEmpty()) continue;
            ResourceLocation id = key(kind, contained.getTypeRegistryName());
            if (target != null && !target.equals(id)) continue;
            long accepted = storage.simulateInsertChemical(id, contained.getAmount());
            if (accepted <= 0) continue;
            S extracted = handler.extractChemical(tank, accepted, Action.EXECUTE);
            if (!extracted.isEmpty()) return new IdeaStorageChemicalTransfer(working, id, extracted.getAmount(), true);
        }
        return null;
    }
}
