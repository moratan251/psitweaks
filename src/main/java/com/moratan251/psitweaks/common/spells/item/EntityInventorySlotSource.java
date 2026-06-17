package com.moratan251.psitweaks.common.spells.item;

import java.util.UUID;

public record EntityInventorySlotSource(UUID entityUuid, int entityId, int slot) implements ItemSourceRef {
}
