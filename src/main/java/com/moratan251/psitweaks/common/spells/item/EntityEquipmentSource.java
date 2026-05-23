package com.moratan251.psitweaks.common.spells.item;

import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;

public record EntityEquipmentSource(UUID entityUuid, EquipmentSlot slot) implements ItemSourceRef {
}
