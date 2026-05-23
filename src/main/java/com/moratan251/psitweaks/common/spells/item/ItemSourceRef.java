package com.moratan251.psitweaks.common.spells.item;

public sealed interface ItemSourceRef permits
        ItemEntitySource,
        EntityHandSource,
        EntityEquipmentSource,
        BlockInventorySlotSource {
}
