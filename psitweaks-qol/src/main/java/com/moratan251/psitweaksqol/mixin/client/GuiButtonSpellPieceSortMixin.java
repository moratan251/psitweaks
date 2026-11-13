package com.moratan251.psitweaksqol.mixin.client;

import com.moratan251.psitweaksqol.client.gui.PiecePanelSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.client.gui.button.GuiButtonSpellPiece;

/**
 * ピース一覧の並び順を設定に合わせる。Psi は検索語のないときも、検索で同点のときも、このメソッドの返す名前で
 * 並べるので、ここを差し替えれば両方に効く。
 */
@Mixin(value = GuiButtonSpellPiece.class, remap = false)
public abstract class GuiButtonSpellPieceSortMixin {
    @Inject(method = "getPieceSortingName", at = @At("RETURN"), cancellable = true)
    private void psitweaks$sortNameForSetting(CallbackInfoReturnable<String> callback) {
        GuiButtonSpellPiece self = (GuiButtonSpellPiece) (Object) this;
        String psiSortName = callback.getReturnValue();
        if (psiSortName == null) {
            return;
        }
        String sortName = PiecePanelSettings.sortOrder().sortName(self.getPiece(), psiSortName);
        if (!sortName.equals(psiSortName)) {
            callback.setReturnValue(sortName);
        }
    }
}
