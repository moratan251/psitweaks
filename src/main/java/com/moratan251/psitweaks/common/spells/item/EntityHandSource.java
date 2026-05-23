package com.moratan251.psitweaks.common.spells.item;

import java.util.UUID;
import net.minecraft.world.InteractionHand;

public record EntityHandSource(UUID entityUuid, InteractionHand hand) implements ItemSourceRef {
}
