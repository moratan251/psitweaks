package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.storage.idea.IdeaStorageChemicalTransfer;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageTransferDirection;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.Nullable;

public final class MekanismCompat {
    private static final String MEKANISM_MOD_ID = "mekanism";
    private static final String GENERATORS_MOD_ID = "mekanismgenerators";

    private MekanismCompat() {
    }

    public static boolean isIdeaStorageChemicalContainer(net.minecraft.world.item.ItemStack stack) {
        return isMekanismLoaded() && MekanismIntegration.isIdeaStorageChemicalContainer(stack);
    }

    public static boolean isMekanismLoaded() {
        return ModList.get().isLoaded(MEKANISM_MOD_ID);
    }

    /**
     * 作動式: 放射線注入を登録するか。偽のあいだは、ピース・ピースの絵・プログラム・クリエイティブタブ・研究のアンロック・設定と、
     * 生成する研究レシピ・アンロック・アイテムのモデルと名前・言語・ピースのグループを作らず、解説書の項目も隠し、保存された魔法式からは
     * このピースを外す（{@link OptionalSpellPieceCompat}）。戻すときは真にして runData する。
     */
    public static final boolean RADIATION_INJECTION_ENABLED = false;
    /** 解説書（Patchouli）で 作動式: 放射線注入 の項目を出すかのフラグ。 */
    public static final String RADIATION_INJECTION_BOOK_FLAG = "psitweaks_radiation_injection";

    /** 作動式: 放射線注入を登録するか（{@link #RADIATION_INJECTION_ENABLED} で、Mekanism があるとき）。 */
    public static boolean isRadiationInjectionEnabled() {
        return RADIATION_INJECTION_ENABLED && isMekanismLoaded();
    }

    public static boolean isGeneratorsLoaded() {
        return isMekanismLoaded() && ModList.get().isLoaded(GENERATORS_MOD_ID);
    }

    public static void register(IEventBus modEventBus) {
        if (isMekanismLoaded()) {
            MekanismIntegration.register(modEventBus);
        }
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        if (isMekanismLoaded()) {
            MekanismIntegration.commonSetup(event);
        }
    }

    public static void registerClient(IEventBus modEventBus) {
        if (isMekanismLoaded()) {
            MekanismIntegration.registerClient(modEventBus);
        }
    }

    public static void addCreativeTabContents(CreativeModeTab.Output output) {
        if (isMekanismLoaded()) {
            MekanismIntegration.addCreativeTabContents(output);
        }
    }

    @Nullable
    public static IdeaStorageChemicalTransfer planIdeaStorageChemicalTransfer(
            PlayerIdeaStorage storage, ItemStack container, @Nullable ResourceLocation targetChemicalId,
            IdeaStorageTransferDirection direction) {
        return isMekanismLoaded()
                ? MekanismIntegration.planIdeaStorageChemicalTransfer(
                        storage, container, targetChemicalId, direction)
                : null;
    }
}
