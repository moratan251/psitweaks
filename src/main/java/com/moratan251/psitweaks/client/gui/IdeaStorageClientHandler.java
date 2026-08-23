package com.moratan251.psitweaks.client.gui;

import com.mojang.blaze3d.platform.Window;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * イデアストレージ同期 payload のクライアント側処理。
 * common 側の payload ハンドラから委譲され、dedicated server ではロードされない。
 * スナップショットは開いている Screen インスタンス内にのみ保持し、static キャッシュを持たない。
 */
public final class IdeaStorageClientHandler {
    /** リサイズ時の Menu 再生成をまたいで検索文字列を維持するための一時保持(String のみ、Level/Player 参照は持たない)。 */
    private static String lastFilter = "";
    /** ソートモード。クライアント側のみの表示状態で、画面の開き直しをまたいで維持する。 */
    private static IdeaStorageSortMode sortMode = IdeaStorageSortMode.PORT;
    /** 行数変更での開き直し時にカーソル位置を復元するための退避(scaled 座標 + 退避時刻)。 */
    private static double stashedMouseX;
    private static double stashedMouseY;
    private static long stashedMouseTime;
    /** カーソル位置退避の有効期限(ms)。古い退避は別操作の可能性があるため復元しない。 */
    private static final long MOUSE_STASH_TTL_MS = 2_000L;

    private IdeaStorageClientHandler() {
    }

    public static void handleSync(MessageIdeaStorageSync message) {
        if (Minecraft.getInstance().screen instanceof IdeaStorageScreen screen) {
            screen.applySnapshot(message.entries(), message.loadFailed());
        }
    }

    public static String lastFilter() {
        return lastFilter;
    }

    public static void stashFilter(String filter) {
        lastFilter = filter != null ? filter : "";
    }

    public static IdeaStorageSortMode sortMode() {
        return sortMode;
    }

    /** ソートモードを1つ巡回し、新しいモードを返す。 */
    public static IdeaStorageSortMode cycleSortMode() {
        sortMode = sortMode.next();
        return sortMode;
    }

    public static void stashMousePosition(double scaledX, double scaledY) {
        stashedMouseX = scaledX;
        stashedMouseY = scaledY;
        stashedMouseTime = Util.getMillis();
    }

    /** 新鮮なカーソル位置退避があれば復元して消費する。リサイズで開き直された Screen の init から呼ぶ。 */
    public static void restoreMousePositionIfStashed() {
        if (stashedMouseTime <= 0L || Util.getMillis() - stashedMouseTime >= MOUSE_STASH_TTL_MS) {
            return;
        }
        stashedMouseTime = 0L;
        Window window = Minecraft.getInstance().getWindow();
        double guiScale = window.getGuiScale();
        GLFW.glfwSetCursorPos(window.getWindow(), stashedMouseX * guiScale, stashedMouseY * guiScale);
    }
}
