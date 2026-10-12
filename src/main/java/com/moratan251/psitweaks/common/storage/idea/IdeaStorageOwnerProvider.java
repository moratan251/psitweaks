package com.moratan251.psitweaks.common.storage.idea;

import java.util.Optional;
import java.util.UUID;

/**
 * 術者 UUID とは別に、サーバー側で検証したイデアストレージ所有者を返す Caster 用 interface。
 * 自律詠唱機の専用 Caster が実装し、詠唱のたびに機械の現在の所有者を引き直す。
 */
public interface IdeaStorageOwnerProvider {
    Optional<UUID> ideaStorageOwner();
}
