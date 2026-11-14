package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramAim;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIntegrity;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * デバッグ用: 魔法式耐久値デバッガー（2026-11-14 ユーザー）。右クリックで、見ている魔法式オブジェクト（自分のものも含む）の
 * 耐久値を決めた量だけ減らす。狙い方は耐久値の表示と同じ核の箱（{@link SpellGramAim#aimedAt}、{@link SpellGramAim#RANGE}
 * ブロックまで）。減らし方は術式と同じ入口（{@link SpellGramIntegrity#reduce}、減らす側の事象干渉力は掛けないので量のとおり、
 * {@code SpellGramIntegrityEvent} も出る）。
 * <p>
 * 量: しゃがんで右クリックで {@link #PRESETS} を順に切り替える（アイテムに保存）。金床でアイテムの名前を正の数にすると、
 * その数を量として使う（任意の量）。
 */
public class ItemSpellGramIntegrityDebugger extends Item {
    /** しゃがんで右クリックで切り替える量。 */
    static final float[] PRESETS = {1.0F, 5.0F, 10.0F, 25.0F, 50.0F, 100.0F, 250.0F, 1000.0F};
    private static final String TAG_AMOUNT = "DebugIntegrityAmount";
    private static final float DEFAULT_AMOUNT = 10.0F;

    public ItemSpellGramIntegrityDebugger(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (player.isShiftKeyDown()) {
            float next = nextPreset(storedAmount(stack));
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putFloat(TAG_AMOUNT, next));
            player.displayClientMessage(Component.translatable("psitweaks.debug.spellgram_integrity.amount", format(amount(stack))), true);
            return InteractionResultHolder.success(stack);
        }
        SpellGramObject target = SpellGramAim.aimedAt(level, player, 1.0F);
        if (target == null) {
            player.displayClientMessage(Component.translatable("psitweaks.debug.spellgram_integrity.no_target")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        float amount = amount(stack);
        float removed = SpellGramIntegrity.reduce(target, amount, null);
        Component message = target.isEnded()
                ? Component.translatable("psitweaks.debug.spellgram_integrity.destroyed", target.getName(), format(removed))
                : Component.translatable("psitweaks.debug.spellgram_integrity.reduced", target.getName(), format(removed),
                        format(target.getIntegrity()));
        player.displayClientMessage(message, true);
        return InteractionResultHolder.success(stack);
    }

    /** 減らす量: 名前が正の数ならその数、なければ保存した量（なければ {@link #DEFAULT_AMOUNT}）。 */
    static float amount(ItemStack stack) {
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name != null) {
            try {
                float value = Float.parseFloat(name.getString().trim());
                if (Float.isFinite(value) && value > 0.0F) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // 数でない名前は量に使わない。
            }
        }
        return storedAmount(stack);
    }

    private static float storedAmount(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return DEFAULT_AMOUNT;
        }
        float value = data.copyTag().getFloat(TAG_AMOUNT);
        return value > 0.0F ? value : DEFAULT_AMOUNT;
    }

    /** {@code current} の次の量（最後なら最初に戻る）。 */
    static float nextPreset(float current) {
        for (float preset : PRESETS) {
            if (preset > current + 1.0E-4F) {
                return preset;
            }
        }
        return PRESETS[0];
    }

    private static String format(float value) {
        return Mth.equal(value, Math.round(value)) ? Integer.toString(Math.round(value)) : String.format(Locale.ROOT, "%.1f", value);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("psitweaks.debug.spellgram_integrity.tooltip.amount", format(amount(stack)))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("psitweaks.debug.spellgram_integrity.tooltip.use").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("psitweaks.debug.spellgram_integrity.tooltip.cycle").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("psitweaks.debug.spellgram_integrity.tooltip.rename").withStyle(ChatFormatting.GRAY));
    }
}
