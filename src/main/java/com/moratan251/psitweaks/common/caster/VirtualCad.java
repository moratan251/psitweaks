package com.moratan251.psitweaks.common.caster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.cad.CADComponentLookup;
import vazkii.psi.api.cad.CADStatEvent;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.base.ModCADComponents;
import vazkii.psi.common.item.base.ModDataComponents;
import vazkii.psi.common.item.data.CADDataValue;

/**
 * 自律詠唱機の内蔵仮想 CAD。
 * <p>
 * 性能値は部品に依らず {@link #FIXED_STATS} に固定する。CAD として組み立てるための素体・コア・弾倉は隠し部品で、
 * 機械のスロットに入れられるのは着色器だけ。バッテリーは持たず、詠唱コストは機械の内部 Psi だけから払う。
 * 時刻・ベクトル記憶・Psitweaks の記憶領域は組み替えても引き継ぐ。
 */
public final class VirtualCad {
    /** 内蔵 CAD の性能値。装弾数（術式弾は機械の8スロット）と容量（バッテリーなしで 0）は使わない。 */
    public static final Map<EnumCADStat, Integer> FIXED_STATS = fixedStats();
    /** 内蔵 CAD の目印。{@link #onCadStat} はこれが付いた CAD の性能値だけを置き換える。 */
    private static final String TAG_VIRTUAL_CAD = "psitweaks_autonomous_caster_cad";

    private VirtualCad() {
    }

    public static boolean acceptsDye(@Nullable HolderLookup.Provider registries, ItemStack stack) {
        return !stack.isEmpty() && !(stack.getItem() instanceof ICAD)
                && CADComponentLookup.isComponent(registries, stack, EnumCADComponent.DYE);
    }

    /**
     * 着色器スロットの内容から仮想 CAD を組み立て直す。
     *
     * @param dye      着色器スロットの中身。空や着色器以外なら既定の術式の色になる
     * @param previous 引き継ぎ元の仮想 CAD。初回は空
     */
    public static ItemStack rebuild(@Nullable HolderLookup.Provider registries, ItemStack dye, ItemStack previous) {
        ItemStack assembly = new ItemStack(ModCADComponents.cadAssemblyIron.get());
        List<ItemStack> components = new ArrayList<>(4);
        components.add(assembly);
        components.add(new ItemStack(ModCADComponents.cadCoreBasic.get()));
        components.add(new ItemStack(ModCADComponents.cadSocketBasic.get()));
        if (acceptsDye(registries, dye)) {
            components.add(dye.copyWithCount(1));
        }

        ItemStack cad = ItemCAD.makeCADWithAssembly(registries, assembly, components);
        if (!(cad.getItem() instanceof ICAD)) {
            cad = ItemCAD.makeCAD(registries, components);
        }
        carryRuntimeState(previous, cad);
        CustomData.update(DataComponents.CUSTOM_DATA, cad, tag -> tag.putBoolean(TAG_VIRTUAL_CAD, true));
        return cad;
    }

    /** 詠唱前の確認用。仮想 CAD として使えない stack を弾く。 */
    public static boolean isUsable(ItemStack cad) {
        return !cad.isEmpty() && cad.getItem() instanceof ICAD;
    }

    public static boolean isVirtualCad(ItemStack cad) {
        CustomData custom = cad.get(DataComponents.CUSTOM_DATA);
        return custom != null && custom.contains(TAG_VIRTUAL_CAD);
    }

    /** Psi の CADStatEvent handler。内蔵 CAD の性能値を固定値に置き換える。 */
    public static void onCadStat(CADStatEvent event) {
        Integer fixed = FIXED_STATS.get(event.getStat());
        if (fixed != null && isVirtualCad(event.getCad())) {
            event.setStatValue(fixed);
        }
    }

    private static Map<EnumCADStat, Integer> fixedStats() {
        Map<EnumCADStat, Integer> stats = new EnumMap<>(EnumCADStat.class);
        stats.put(EnumCADStat.EFFICIENCY, 175);
        stats.put(EnumCADStat.POTENCY, 1500);
        stats.put(EnumCADStat.COMPLEXITY, 46);
        stats.put(EnumCADStat.PROJECTION, 10);
        stats.put(EnumCADStat.BANDWIDTH, 9);
        stats.put(EnumCADStat.SAVED_VECTORS, 21);
        return Collections.unmodifiableMap(stats);
    }

    private static void carryRuntimeState(ItemStack previous, ItemStack cad) {
        CADDataValue data = previous.isEmpty() ? null : previous.get(ModDataComponents.CAD_DATA.get());
        cad.set(ModDataComponents.CAD_DATA.get(), (data == null ? CADDataValue.EMPTY : data).withBattery(0));
        CustomData custom = previous.isEmpty() ? null : previous.get(DataComponents.CUSTOM_DATA);
        if (custom != null && !custom.isEmpty()) {
            cad.set(DataComponents.CUSTOM_DATA, custom);
        }
    }
}
