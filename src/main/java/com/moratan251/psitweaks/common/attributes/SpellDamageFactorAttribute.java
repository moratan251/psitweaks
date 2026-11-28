package com.moratan251.psitweaks.common.attributes;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import org.jetbrains.annotations.Nullable;

/**
 * 事象干渉力の属性。ツールチップの値を小数第 3 位まで出す（NeoForge の既定は第 2 位で、0.075 のような値が丸まる）。
 */
public class SpellDamageFactorAttribute extends RangedAttribute {
    private static final DecimalFormat FORMAT = Util.make(new DecimalFormat("#.###"),
            format -> format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));

    public SpellDamageFactorAttribute(String descriptionId, double defaultValue, double min, double max) {
        super(descriptionId, defaultValue, min, max);
    }

    @Override
    public MutableComponent toValueComponent(@Nullable AttributeModifier.Operation op, double value, TooltipFlag flag) {
        if (IAttributeExtension.isNullOrAddition(op)) {
            return Component.translatable("neoforge.value.flat", FORMAT.format(value));
        }
        return Component.translatable("neoforge.value.percent", FORMAT.format(value * 100));
    }
}
