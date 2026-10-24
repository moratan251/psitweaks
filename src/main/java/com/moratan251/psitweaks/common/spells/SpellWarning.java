package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.network.MessageSpellWarningPosition;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import vazkii.psi.api.spell.SpellContext;

/**
 * A spell warning: a message for the caster that, unlike Psi's {@link vazkii.psi.api.spell.SpellRuntimeException},
 * does not stop the spell. It is shown in yellow and followed, like Psi's errors, by the grid position of the piece
 * that raised it ({@link #POSITION}); both are hidden when the spell has an Error Suppressor.
 * Every warning key is listed here, as Psi lists its error keys in {@code SpellRuntimeException}; write the message
 * with a "警告: " / "WARNING: " prefix in the language provider.
 */
public final class SpellWarning {
    /** The line after each warning; arguments: the piece's column and row, from 1. */
    public static final String POSITION = "psitweaks.spellwarning.position";
    /** Arguments: the CAD memory slot (from 1), the original length, the stored length. */
    public static final String CAD_MEMORY_STRING_TRUNCATED = "psitweaks.spellwarning.cad_memory_string_truncated";
    /** A targeted combat trick skipped its target because it has already fallen. */
    public static final String TARGET_FALLEN = "psitweaks.spellwarning.target_fallen";
    /** Trick: Breed found no food in the caster's inventory for an animal; argument: the first such animal's name. */
    public static final String NO_BREEDING_FOOD = "psitweaks.spellwarning.no_breeding_food";

    public final String key;
    public final Object[] arguments;

    public SpellWarning(String key, Object... arguments) {
        this.key = key;
        this.arguments = arguments;
    }

    public MutableComponent toComponent() {
        return Component.translatable(key, arguments).withStyle(ChatFormatting.YELLOW);
    }

    /** Shows this warning to the caster of {@code context}, unless the spell suppresses errors. */
    public void send(@Nullable SpellContext context) {
        if (context == null || context.caster == null || context.caster.level().isClientSide
                || context.shouldSuppressErrors()) {
            return;
        }
        context.caster.sendSystemMessage(toComponent());
        if (context.currentAction == null) {
            return;
        }
        var piece = context.currentAction.piece;
        if (context.caster instanceof AutonomousCasterPlayer caster) {
            // The Autonomous Caster's activity log shows the position the way it shows Psi's error positions.
            caster.sendSystemMessage(AutonomousCasterPlayer.gridPosition(POSITION, piece.x + 1, piece.y + 1)
                    .withStyle(ChatFormatting.YELLOW));
        } else if (context.caster instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            // Other fake players (machines casting spells) have no client to show the position to.
            PacketDistributor.sendToPlayer(player, new MessageSpellWarningPosition(piece.x + 1, piece.y + 1));
        }
    }

    /** Shorthand for {@code new SpellWarning(key, arguments).send(context)}. */
    public static void warn(@Nullable SpellContext context, String key, Object... arguments) {
        new SpellWarning(key, arguments).send(context);
    }
}
