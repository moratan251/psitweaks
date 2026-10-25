package com.moratan251.psitweaks.common.caster;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.cad.CADComponentLookup;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.base.ModCADComponents;
import vazkii.psi.common.item.base.ModDataComponents;
import vazkii.psi.common.item.data.CADDataValue;

/**
 * 自律詠唱機の内蔵仮想 CAD。
 * <p>
 * 機械の部品スロットに入れた CAD 部品から組み立てる。空のスロットは鉄のアセンブリ・基本コア・基本ソケット相当の
 * 隠し既定部品で補い、部品なしでも初期性能で動く。バッテリーは持たず、詠唱コストは機械の内部 Psi だけから払う。
 * 時刻・ベクトル記憶・Psitweaks の記憶領域は組み替えても引き継ぐ。
 */
public final class VirtualCad {
    /** 機械の部品スロットに対応する CAD 部品の種類（スロット順）。 */
    public static final List<EnumCADComponent> SLOT_TYPES = List.of(
            EnumCADComponent.ASSEMBLY,
            EnumCADComponent.CORE,
            EnumCADComponent.SOCKET,
            EnumCADComponent.DYE);

    private VirtualCad() {
    }

    public static boolean accepts(@Nullable HolderLookup.Provider registries, ItemStack stack, EnumCADComponent type) {
        return !stack.isEmpty() && !(stack.getItem() instanceof ICAD) && CADComponentLookup.isComponent(registries, stack, type);
    }

    /**
     * 部品スロットの内容から仮想 CAD を組み立て直す。
     *
     * @param installed {@link #SLOT_TYPES} と同じ順の部品。空や種類違いは既定部品として扱う
     * @param previous  引き継ぎ元の仮想 CAD。初回は空
     */
    public static ItemStack rebuild(@Nullable HolderLookup.Provider registries, List<ItemStack> installed, ItemStack previous) {
        List<ItemStack> components = new ArrayList<>(SLOT_TYPES.size());
        ItemStack assembly = ItemStack.EMPTY;
        for (int i = 0; i < SLOT_TYPES.size(); i++) {
            EnumCADComponent type = SLOT_TYPES.get(i);
            ItemStack stack = i < installed.size() ? installed.get(i) : ItemStack.EMPTY;
            ItemStack component = accepts(registries, stack, type) ? stack.copyWithCount(1) : defaultComponent(type);
            if (component.isEmpty()) {
                continue;
            }
            if (type == EnumCADComponent.ASSEMBLY) {
                assembly = component;
            }
            components.add(component);
        }

        ItemStack cad = ItemCAD.makeCADWithAssembly(registries, assembly, components);
        if (!(cad.getItem() instanceof ICAD)) {
            cad = ItemCAD.makeCAD(registries, components);
        }
        carryRuntimeState(previous, cad);
        return cad;
    }

    /** 詠唱前の確認用。仮想 CAD として使えない stack を弾く。 */
    public static boolean isUsable(ItemStack cad) {
        return !cad.isEmpty() && cad.getItem() instanceof ICAD;
    }

    private static ItemStack defaultComponent(EnumCADComponent type) {
        return switch (type) {
            case ASSEMBLY -> new ItemStack(ModCADComponents.cadAssemblyIron.get());
            case CORE -> new ItemStack(ModCADComponents.cadCoreBasic.get());
            case SOCKET -> new ItemStack(ModCADComponents.cadSocketBasic.get());
            default -> ItemStack.EMPTY;
        };
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
