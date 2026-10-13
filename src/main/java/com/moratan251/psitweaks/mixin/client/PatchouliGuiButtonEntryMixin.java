package com.moratan251.psitweaks.mixin.client;

import com.moratan251.psitweaks.client.compat.PatchouliEntryListNames;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.gui.button.GuiButtonEntry;

/** Shortens long spell piece names only in Patchouli's entry list buttons. */
@Mixin(value = GuiButtonEntry.class, remap = false)
public abstract class PatchouliGuiButtonEntryMixin {
    @Redirect(method = "renderWidget",
            at = @At(value = "INVOKE", target = "Lvazkii/patchouli/client/book/BookEntry;getName()Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent psitweaks$listName(BookEntry entry) {
        return PatchouliEntryListNames.listName(entry.getName());
    }
}
