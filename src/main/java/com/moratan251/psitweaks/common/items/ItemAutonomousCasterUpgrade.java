package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.common.caster.AutonomousCasterSettings;
import com.moratan251.psitweaks.common.caster.AutonomousCasterUpgrade;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

/** 自律詠唱機のアップグレード。機械のアップグレード窓のスロットに入れると、種類に応じた性能を1つ上げる。 */
public class ItemAutonomousCasterUpgrade extends Item {
    private static final String TOOLTIP = "tooltip.psitweaks.autonomous_caster_upgrade.";

    private final AutonomousCasterUpgrade upgrade;

    public ItemAutonomousCasterUpgrade(AutonomousCasterUpgrade upgrade, Properties properties) {
        super(properties);
        this.upgrade = upgrade;
    }

    public AutonomousCasterUpgrade upgrade() {
        return upgrade;
    }

    /** stack のアップグレードの種類。アップグレードでなければ null。 */
    @Nullable
    public static AutonomousCasterUpgrade typeOf(ItemStack stack) {
        return stack.getItem() instanceof ItemAutonomousCasterUpgrade item ? item.upgrade : null;
    }

    /** 上がる性能と上がり幅（「規模 +500」など）。上がり幅はコンフィグの値。 */
    public static MutableComponent effect(AutonomousCasterUpgrade upgrade) {
        return Component.translatable(TOOLTIP + upgrade.id(), amount(upgrade));
    }

    private static String amount(AutonomousCasterUpgrade upgrade) {
        return switch (upgrade) {
            case PSI_CAPACITY -> Integer.toString(AutonomousCasterSettings.upgradePsiCapacity());
            case CONVERSION_SPEED -> Integer.toString(AutonomousCasterSettings.upgradeConversionSpeed());
            case CONVERSION_EFFICIENCY -> decimal(AutonomousCasterSettings.upgradeConversionEfficiency());
            case INTERFERENCE -> decimal(AutonomousCasterSettings.upgradeInterference() * 100.0D);
            default -> Integer.toString(AutonomousCasterSettings.cadStatBonus(upgrade));
        };
    }

    /** 小数点以下2桁まで。整数なら小数点を付けない。 */
    private static String decimal(double value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        return text.replaceAll("\\.?0+$", "");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(effect(upgrade).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(TOOLTIP + "usage").withStyle(ChatFormatting.GRAY));
    }
}
