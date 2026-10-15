package com.moratan251.psitweaks.client.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.client.book.gui.GuiBook;

import java.util.function.UnaryOperator;

/**
 * Psi の crafting_multi テンプレート用の見出し。
 * Psi の見出しは1つ目のレシピの完成品名で固定されるため、完成品スロットと同じ周期
 * （20 tick ごと）で、表示中のレシピの完成品名に切り替える。
 * ページ側で heading を指定した場合は、その見出しをそのまま表示する。
 */
public class PatchouliCyclingRecipeHeading implements ICustomComponent {
    // テンプレート JSON から Patchouli が設定する
    private IVariable text;
    private IVariable outputs;

    private transient Component heading = Component.empty();
    private transient Component[] names = new Component[0];
    private transient int x;
    private transient int y;

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        x = componentX == -1 ? GuiBook.PAGE_WIDTH / 2 : componentX;
        y = componentY;
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        heading = lookup.apply(text).as(Component.class);
        if (heading == null) {
            heading = Component.empty();
        }
        ItemStack[] stacks = lookup.apply(outputs).as(ItemStack[].class);
        // Psi が既定の見出しにする1つ目の完成品名と一致するときだけ切り替える
        if (stacks != null && stacks.length > 1
                && heading.getString().equals(stacks[0].getHoverName().getString())) {
            names = new Component[stacks.length];
            for (int i = 0; i < stacks.length; i++) {
                names[i] = stacks[i].getHoverName();
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
        Component current = names.length > 0
                ? names[(context.getTicksInBook() / 20) % names.length]
                : Component.literal(I18n.get(heading.getString()));
        Font font = Minecraft.getInstance().font;
        FormattedCharSequence line = current.copy().withStyle(context.getFont()).getVisualOrderText();
        graphics.drawString(font, line, x - font.width(line) / 2, y, context.getHeaderColor(), false);
    }
}
