package com.moratan251.psitweaks.common.spells.item;

import java.util.UUID;

public record ItemEntitySource(UUID entityUuid, int entityId) implements ItemSourceRef {
}
