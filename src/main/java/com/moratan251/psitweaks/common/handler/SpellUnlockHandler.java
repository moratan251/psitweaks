package com.moratan251.psitweaks.common.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import vazkii.psi.api.spell.PieceKnowledgeEvent;
import vazkii.psi.api.spell.programmer.ProgrammerPopulateEvent;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.core.handler.PsiSoundHandler;
import vazkii.psi.common.network.MessageRegister;
import vazkii.psi.common.network.message.MessageDataSync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpellUnlockHandler {

    private record SpellUnlockDefinition(String commandId, ResourceLocation pieceId, ResourceLocation unlockItemId, String unlockTag) {
        Component spellNameComponent() {
            return Component.translatable(pieceId.getNamespace() + ".spellpiece." + pieceId.getPath());
        }
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson SPELL_UNLOCK_GSON = new GsonBuilder().create();
    private static final String SPELL_UNLOCK_DIRECTORY = "spell_unlocks";

    private static final List<SpellUnlockDefinition> DEFAULT_SPELL_UNLOCKS = List.of(
            definition("cocytus", "trick_cocytus", "program_cocytus"),
            definition("time_accelerate", "trick_time_accelerate", "program_time_accelerate"),
            definition("flight", "trick_flight", "program_flight"),
            definition("phonon_maser", "trick_phonon_maser", "program_phonon_maser"),
            definition("supreme_infusion", "trick_supreme_infusion", "program_supreme_infusion"),
            definition("molecular_divider", "trick_molecular_divider", "program_molecular_divider"),
            definition("radiation_injection", "trick_radiation_injection", "program_radiation_injection"),
            definition("guillotine", "trick_guillotine", "program_guillotine"),
            definition("active_air_mine", "trick_active_air_mine", "program_active_air_mine")
    );

    private static final SpellUnlockReloadListener SPELL_UNLOCK_RELOAD_LISTENER = new SpellUnlockReloadListener();

    private static volatile List<SpellUnlockDefinition> SPELL_UNLOCKS = List.of();
    private static volatile Map<ResourceLocation, SpellUnlockDefinition> UNLOCK_BY_PIECE = Map.of();
    private static volatile Map<ResourceLocation, SpellUnlockDefinition> UNLOCK_BY_ITEM = Map.of();
    private static final String UNLOCKS_DATA_KEY = Psitweaks.MOD_ID + ".spell_unlocks";

    static {
        applyDefinitions(DEFAULT_SPELL_UNLOCKS);
    }

    private static SpellUnlockDefinition definition(String commandId, String piecePath, String itemPath) {
        return new SpellUnlockDefinition(
                commandId,
                Psitweaks.location(piecePath),
                Psitweaks.location(itemPath),
                Psitweaks.MOD_ID + ".unlock." + piecePath
        );
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(SPELL_UNLOCK_RELOAD_LISTENER);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        registerCommands(event.getDispatcher());
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> spellUnlockRoot = Commands.literal("spellunlock");
        spellUnlockRoot.then(createAllSpellCommand());
        for (SpellUnlockDefinition definition : SPELL_UNLOCKS) {
            spellUnlockRoot.then(createSpellCommand(definition));
        }

        dispatcher.register(
                Commands.literal("psitweaks")
                        .requires(source -> source.hasPermission(2))
                        .then(spellUnlockRoot)
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createSpellCommand(SpellUnlockDefinition definition) {
        return Commands.literal(definition.commandId())
                .then(Commands.literal("grant")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> setSpellUnlock(
                                        ctx.getSource(),
                                        EntityArgument.getPlayers(ctx, "targets"),
                                        definition,
                                        true))))
                .then(Commands.literal("revoke")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> setSpellUnlock(
                                        ctx.getSource(),
                                        EntityArgument.getPlayers(ctx, "targets"),
                                        definition,
                                        false))))
                .then(Commands.literal("status")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> showSpellUnlockStatus(
                                        ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "target"),
                                        definition))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createAllSpellCommand() {
        return Commands.literal("all")
                .then(Commands.literal("grant")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> setAllSpellUnlock(
                                        ctx.getSource(),
                                        EntityArgument.getPlayers(ctx, "targets"),
                                        true))))
                .then(Commands.literal("revoke")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> setAllSpellUnlock(
                                        ctx.getSource(),
                                        EntityArgument.getPlayers(ctx, "targets"),
                                        false))))
                .then(Commands.literal("status")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> showAllSpellUnlockStatus(
                                        ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "target")))));
    }

    @SubscribeEvent
    public static void onPieceKnowledge(PieceKnowledgeEvent event) {
        ResourceLocation pieceName = event.getPieceName();
        if (pieceName == null) {
            return;
        }

        SpellUnlockDefinition definition = UNLOCK_BY_PIECE.get(pieceName);
        if (definition == null) {
            return;
        }
        if (!PsitweaksConfig.COMMON.requireSpellUnlocks.get()) {
            return;
        }

        if (!isSpellUnlocked(event.getPlayer(), definition)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onProgrammerPopulate(ProgrammerPopulateEvent event) {
        PsiExSpellGroupCompatHandler.addMissingPsiExPieceGroups(event.getSpellPieceRegistry());
    }

    @SubscribeEvent
    public static void onRightClickUnlockItem(PlayerInteractEvent.RightClickItem event) {
        SpellUnlockDefinition definition = getUnlockDefinitionByItem(event.getItemStack());
        if (definition == null) {
            return;
        }

        // This unlock item only sets progression; it should not consume or trigger other behavior.
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        boolean unlocked = setSpellUnlocked(serverPlayer, definition, true);
        if (unlocked) {
            serverPlayer.level().playSound(
                    null,
                    serverPlayer.getX(),
                    serverPlayer.getY(),
                    serverPlayer.getZ(),
                    PsiSoundHandler.levelUp,
                    SoundSource.PLAYERS,
                    0.6F,
                    1.0F
            );
            serverPlayer.displayClientMessage(
                    Component.translatable("message.psitweaks.spell_unlock.unlocked", definition.spellNameComponent()),
                    true
            );
        } else {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.psitweaks.spell_unlock.already", definition.spellNameComponent()),
                    true
            );
        }
    }

    private static SpellUnlockDefinition getUnlockDefinitionByItem(ItemStack stack) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) {
            return null;
        }

        return UNLOCK_BY_ITEM.get(itemId);
    }

    private static int setSpellUnlock(CommandSourceStack source, Collection<ServerPlayer> targets, SpellUnlockDefinition definition, boolean unlocked) {
        int changed = 0;
        for (ServerPlayer target : targets) {
            if (setSpellUnlocked(target, definition, unlocked)) {
                changed++;
            }
        }

        if (changed <= 0) {
            source.sendFailure(Component.translatable(
                    "message.psitweaks.spell_unlock.command.no_change",
                    definition.spellNameComponent()
            ));
            return 0;
        }

        if (targets.size() == 1) {
            ServerPlayer target = targets.iterator().next();
            source.sendSuccess(() -> Component.translatable(
                    unlocked
                            ? "message.psitweaks.spell_unlock.command.grant.single"
                            : "message.psitweaks.spell_unlock.command.revoke.single",
                    definition.spellNameComponent(),
                    target.getDisplayName()), true);
        } else {
            final int changedCount = changed;
            int total = targets.size();
            source.sendSuccess(() -> Component.translatable(
                    unlocked
                            ? "message.psitweaks.spell_unlock.command.grant.multi"
                            : "message.psitweaks.spell_unlock.command.revoke.multi",
                    definition.spellNameComponent(),
                    changedCount,
                    total), true);
        }

        return changed;
    }

    private static int setAllSpellUnlock(CommandSourceStack source, Collection<ServerPlayer> targets, boolean unlocked) {
        int changed = 0;
        int changedPlayers = 0;
        int totalSpells = SPELL_UNLOCKS.size();
        int totalOperations = targets.size() * totalSpells;

        for (ServerPlayer target : targets) {
            boolean targetChanged = false;
            for (SpellUnlockDefinition definition : SPELL_UNLOCKS) {
                if (setSpellUnlocked(target, definition, unlocked)) {
                    changed++;
                    targetChanged = true;
                }
            }
            if (targetChanged) {
                changedPlayers++;
            }
        }

        if (changed <= 0) {
            source.sendFailure(Component.translatable(
                    "message.psitweaks.spell_unlock.command.all.no_change",
                    targets.size(),
                    totalSpells
            ));
            return 0;
        }

        if (targets.size() == 1) {
            ServerPlayer target = targets.iterator().next();
            final int changedCount = changed;
            source.sendSuccess(() -> Component.translatable(
                    unlocked
                            ? "message.psitweaks.spell_unlock.command.all.grant.single"
                            : "message.psitweaks.spell_unlock.command.all.revoke.single",
                    target.getDisplayName(),
                    changedCount,
                    totalSpells
            ), true);
        } else {
            final int changedCount = changed;
            final int changedPlayerCount = changedPlayers;
            source.sendSuccess(() -> Component.translatable(
                    unlocked
                            ? "message.psitweaks.spell_unlock.command.all.grant.multi"
                            : "message.psitweaks.spell_unlock.command.all.revoke.multi",
                    changedCount,
                    totalOperations,
                    changedPlayerCount,
                    targets.size()
            ), true);
        }

        return changed;
    }

    private static int showSpellUnlockStatus(CommandSourceStack source, ServerPlayer target, SpellUnlockDefinition definition) {
        boolean unlocked = isSpellUnlocked(target, definition);
        source.sendSuccess(() -> Component.translatable(
                unlocked
                        ? "message.psitweaks.spell_unlock.command.status.unlocked"
                        : "message.psitweaks.spell_unlock.command.status.locked",
                target.getDisplayName(),
                definition.spellNameComponent()), false);
        return unlocked ? 1 : 0;
    }

    private static int showAllSpellUnlockStatus(CommandSourceStack source, ServerPlayer target) {
        int unlockedCount = 0;
        int totalSpells = SPELL_UNLOCKS.size();
        for (SpellUnlockDefinition definition : SPELL_UNLOCKS) {
            if (isSpellUnlocked(target, definition)) {
                unlockedCount++;
            }
        }

        final int unlockedTotal = unlockedCount;
        source.sendSuccess(() -> Component.translatable(
                "message.psitweaks.spell_unlock.command.all.status",
                target.getDisplayName(),
                unlockedTotal,
                totalSpells
        ), false);
        return unlockedCount;
    }

    private static boolean setSpellUnlocked(ServerPlayer player, SpellUnlockDefinition definition, boolean unlocked) {
        PlayerDataHandler.PlayerData data = PlayerDataHandler.get(player);
        CompoundTag unlockData = getUnlockData(data.getCustomData(), true);
        boolean current = unlockData.getBoolean(definition.unlockTag());
        if (current == unlocked) {
            return false;
        }

        unlockData.putBoolean(definition.unlockTag(), unlocked);
        data.save();
        MessageRegister.sendToPlayer(new MessageDataSync(data), player);

        // Keep the previous storage as compatibility fallback.
        if (unlocked) {
            player.addTag(definition.unlockTag());
        } else {
            player.removeTag(definition.unlockTag());
        }

        return true;
    }

    private static boolean isSpellUnlocked(Player player, SpellUnlockDefinition definition) {
        PlayerDataHandler.PlayerData data = PlayerDataHandler.get(player);
        CompoundTag unlockData = getUnlockData(data.getCustomData(), false);
        if (unlockData.getBoolean(definition.unlockTag())) {
            return true;
        }

        // Compatibility with previously saved tag-based unlock flags.
        return player.getTags().contains(definition.unlockTag());
    }

    private static CompoundTag getUnlockData(CompoundTag customData, boolean createIfMissing) {
        if (!customData.contains(UNLOCKS_DATA_KEY)) {
            if (!createIfMissing) {
                return new CompoundTag();
            }
            customData.put(UNLOCKS_DATA_KEY, new CompoundTag());
        }

        return customData.getCompound(UNLOCKS_DATA_KEY);
    }

    private static void applyDefinitions(List<SpellUnlockDefinition> definitions) {
        Map<String, SpellUnlockDefinition> byCommand = new LinkedHashMap<>();
        Map<ResourceLocation, SpellUnlockDefinition> byPiece = new LinkedHashMap<>();
        Map<ResourceLocation, SpellUnlockDefinition> byItem = new LinkedHashMap<>();
        List<SpellUnlockDefinition> ordered = new ArrayList<>();

        for (SpellUnlockDefinition original : definitions) {
            String commandId = normalizeCommandId(original.commandId());
            if (commandId == null) {
                LOGGER.warn("Skipping spell unlock definition with invalid command id: {}", original.commandId());
                continue;
            }

            SpellUnlockDefinition definition = new SpellUnlockDefinition(
                    commandId,
                    original.pieceId(),
                    original.unlockItemId(),
                    original.unlockTag()
            );

            if (byCommand.containsKey(commandId)) {
                LOGGER.warn("Skipping duplicate spell unlock command id '{}'.", commandId);
                continue;
            }
            if ("all".equals(commandId)) {
                LOGGER.warn("Skipping spell unlock command id '{}' because it is reserved.", commandId);
                continue;
            }
            if (byPiece.containsKey(definition.pieceId())) {
                LOGGER.warn("Skipping duplicate spell unlock piece id '{}'.", definition.pieceId());
                continue;
            }
            if (byItem.containsKey(definition.unlockItemId())) {
                LOGGER.warn("Skipping duplicate spell unlock item id '{}'.", definition.unlockItemId());
                continue;
            }

            byCommand.put(commandId, definition);
            byPiece.put(definition.pieceId(), definition);
            byItem.put(definition.unlockItemId(), definition);
            ordered.add(definition);
        }

        if (ordered.isEmpty()) {
            LOGGER.warn("No valid spell unlock definitions were provided; keeping {} existing definitions.", SPELL_UNLOCKS.size());
            return;
        }

        SPELL_UNLOCKS = List.copyOf(ordered);
        UNLOCK_BY_PIECE = Map.copyOf(byPiece);
        UNLOCK_BY_ITEM = Map.copyOf(byItem);
    }

    @Nullable
    private static String normalizeCommandId(String commandId) {
        if (commandId == null) {
            return null;
        }

        String normalized = commandId.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-')) {
                return null;
            }
        }
        return normalized;
    }

    @Nullable
    private static ResourceLocation readResourceLocation(JsonObject json, String key, ResourceLocation sourceId) {
        if (!json.has(key)) {
            LOGGER.warn("Skipping {} because '{}' is missing.", sourceId, key);
            return null;
        }

        String rawValue = GsonHelper.getAsString(json, key);
        ResourceLocation parsed = ResourceLocation.tryParse(rawValue);
        if (parsed == null) {
            LOGGER.warn("Skipping {} because '{}' is not a valid resource location: {}", sourceId, key, rawValue);
        }
        return parsed;
    }

    private static class SpellUnlockReloadListener extends SimpleJsonResourceReloadListener {

        private SpellUnlockReloadListener() {
            super(SPELL_UNLOCK_GSON, SPELL_UNLOCK_DIRECTORY);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
            List<SpellUnlockDefinition> loaded = new ArrayList<>();

            for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
                ResourceLocation sourceId = entry.getKey();
                JsonElement element = entry.getValue();
                if (!element.isJsonObject()) {
                    LOGGER.warn("Skipping {} because its root is not a JSON object.", sourceId);
                    continue;
                }

                JsonObject json = element.getAsJsonObject();
                ResourceLocation pieceId = readResourceLocation(json, "piece", sourceId);
                ResourceLocation unlockItemId = readResourceLocation(json, "unlock_item", sourceId);
                if (pieceId == null || unlockItemId == null) {
                    continue;
                }

                String defaultCommandId = sourceId.getPath().replace('/', '_');
                String commandId = GsonHelper.getAsString(json, "command_id", defaultCommandId);
                String unlockTag = GsonHelper.getAsString(json, "unlock_tag", Psitweaks.MOD_ID + ".unlock." + pieceId.getPath());

                loaded.add(new SpellUnlockDefinition(commandId, pieceId, unlockItemId, unlockTag));
            }

            if (loaded.isEmpty()) {
                LOGGER.warn("No valid spell unlock JSON found under data/*/{}; keeping {} existing definitions.",
                        SPELL_UNLOCK_DIRECTORY, SPELL_UNLOCKS.size());
                return;
            }

            applyDefinitions(loaded);
            LOGGER.info("Loaded {} spell unlock definitions from JSON.", SPELL_UNLOCKS.size());
        }
    }
}
