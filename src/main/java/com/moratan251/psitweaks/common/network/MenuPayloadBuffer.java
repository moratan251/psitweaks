package com.moratan251.psitweaks.common.network;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

/** Owned by one open menu; incomplete data is discarded with the menu. */
public final class MenuPayloadBuffer {
    private UUID transfer;
    private int kind, sequence;
    private ByteArrayOutputStream bytes;

    public byte[] accept(MessageMenuFragment part) {
        if (part.sequence() == 0) {
            transfer = part.transfer(); kind = part.kind(); sequence = 0;
            bytes = new ByteArrayOutputStream();
        }
        if (bytes == null || !part.transfer().equals(transfer) || kind != part.kind()
                || part.sequence() != sequence++ || bytes.size() + part.data().length > MessageMenuFragment.MAX_TOTAL) {
            bytes = null;
            return null;
        }
        bytes.writeBytes(part.data());
        if (!part.last()) return null;
        byte[] result = bytes.toByteArray();
        bytes = null;
        return result;
    }
}
