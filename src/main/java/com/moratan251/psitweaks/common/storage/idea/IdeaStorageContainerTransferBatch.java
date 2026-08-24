package com.moratan251.psitweaks.common.storage.idea;

import java.util.function.BooleanSupplier;

/** Shift容器転送を同じサーバー処理内で安全に反復する。 */
public final class IdeaStorageContainerTransferBatch {
    public static final int MAX_SUCCESSFUL_TRANSFERS = 4_096;

    private IdeaStorageContainerTransferBatch() {
    }

    /** 最初の1回が成功済みの状態から、Shift時だけ次の転送を反復する。 */
    public static int continueAfterFirst(boolean bulk, BooleanSupplier transferNext) {
        int successfulTransfers = 1;
        if (!bulk) {
            return successfulTransfers;
        }
        while (successfulTransfers < MAX_SUCCESSFUL_TRANSFERS && transferNext.getAsBoolean()) {
            successfulTransfers++;
        }
        return successfulTransfers;
    }
}
