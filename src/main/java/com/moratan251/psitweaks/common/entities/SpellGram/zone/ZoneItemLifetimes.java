package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * 領域が加工している落ちているアイテム（Create の一括洗浄・燻製・製錬、AE2 のチャージ）は、加工している間だけ消えるまでの時間が
 * 進まない（2026-11-12 ユーザー）。NeoForge の {@link ItemEntity#lifespan} を、加工していた tick の分だけ延ばす。加工できない
 * 物になったら延ばさないので、そこから残りの時間がまた進む（領域を倉庫代わりにはできない）。
 * <p>
 * 加工は毎 tick ではなく、速さや間隔に応じてとびとびに呼ばれるので、前に延ばした tick（アイテムの永続データに覚える）から今までの
 * 分を、{@link #WINDOW} tick を上限に延ばす。同じ tick に何度呼ばれても（領域が重なっていても）一度だけ。
 */
public final class ZoneItemLifetimes {
    private static final String LAST_HELD = "psitweaks:zone_held_tick";
    /** 1 回で延ばす上限（tick）。加工の間隔（荷電領域の 5 tick、速さ 0.5 の Create の 2 tick）より長く取る。 */
    public static final int WINDOW = 8;

    private ZoneItemLifetimes() {
    }

    /** ゲーム時刻 {@code now} に、{@code item} を加工している（消えるまでの時間を止める）。 */
    public static void hold(ItemEntity item, long now) {
        CompoundTag data = item.getPersistentData();
        long from = Math.max(data.getLong(LAST_HELD), now - WINDOW);
        if (now <= from) {
            return;
        }
        // 年齢は short で保存されるので、NeoForge と同じく Short.MAX_VALUE - 1 で止める。
        item.lifespan = (int) Math.min(Short.MAX_VALUE - 1, (long) item.lifespan + (now - from));
        data.putLong(LAST_HELD, now);
    }
}
