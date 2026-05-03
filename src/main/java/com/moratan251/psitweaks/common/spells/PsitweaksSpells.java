package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.Psitweaks;
import java.util.Collection;
import java.util.List;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.SpellPiece;

public final class PsitweaksSpells {
    public static final DeferredRegister<Class<? extends SpellPiece>> SPELL_PIECES =
            DeferredRegister.create(PsiAPI.SPELL_PIECE_REGISTRY_TYPE_KEY, Psitweaks.MOD_ID);
    public static final DeferredRegister<Collection<Class<? extends SpellPiece>>> ADVANCEMENT_GROUPS =
            DeferredRegister.create(PsiAPI.ADVANCEMENT_GROUP_REGISTRY_KEY, Psitweaks.MOD_ID);

    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickSupremeInfusion>> TRICK_SUPREME_INFUSION =
            SPELL_PIECES.register("trick_supreme_infusion", () -> PieceTrickSupremeInfusion.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> SUPREME_INFUSION =
            ADVANCEMENT_GROUPS.register("trick_supreme_infusion", () -> List.of(PieceTrickSupremeInfusion.class));

    private PsitweaksSpells() {
    }

    public static void register(IEventBus eventBus) {
        SPELL_PIECES.register(eventBus);
        ADVANCEMENT_GROUPS.register(eventBus);
    }
}
