package com.moratan251.psitweaks.common.storage.idea;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 術者から個人イデアストレージの所有者を解決する。
 * 自律詠唱機の専用 Caster は機械の所有者を返し、それ以外（通常プレイヤーや他 MOD の FakePlayer）はこれまでどおり術者本人とする。
 */
public final class IdeaStorageOwnerResolver {
    public static final String ERROR_OWNER_UNAVAILABLE = "psitweaks.spellerror.idea_storage_owner_unavailable";

    private IdeaStorageOwnerResolver() {
    }

    public static Optional<UUID> resolve(Player caster) {
        if (caster == null) {
            return Optional.empty();
        }
        if (caster instanceof IdeaStorageOwnerProvider provider) {
            return provider.ideaStorageOwner();
        }
        return Optional.of(caster.getUUID());
    }

    /** 術式から使うときの解決。所有者を決められない術者（持ち主のいない・撤去された機械）はスペルエラーにする。 */
    public static UUID requireOwner(Player caster) throws SpellRuntimeException {
        return resolve(caster).orElseThrow(() -> new SpellRuntimeException(ERROR_OWNER_UNAVAILABLE));
    }
}
