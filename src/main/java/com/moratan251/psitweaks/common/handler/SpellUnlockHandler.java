package com.moratan251.psitweaks.common.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.network.MessageSpellUnlockSync;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import vazkii.psi.api.spell.PieceKnowledgeEvent;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.core.handler.PlayerData;
import vazkii.psi.common.core.handler.PsiSoundHandler;
import vazkii.psi.common.network.PsiNetwork;
import vazkii.psi.common.network.message.MessageDataSync;

@EventBusSubscriber(modid = Psitweaks.MOD_ID)
public final class SpellUnlockHandler {
    /** {@code groupId} が指定された定義は、そのスペルピースグループ全体の解禁も担う。 */
    public record SpellUnlockDefinition(String commandId, ResourceLocation pieceId, ResourceLocation unlockItemId, String unlockTag,
                                        @Nullable ResourceLocation groupId) {
        Component spellNameComponent() {
            return Component.translatable(pieceId.getNamespace() + ".spellpiece." + pieceId.getPath());
        }
    }

    /** 定義と各索引をまとめて公開し、リロード中に索引どうしが食い違った状態を読まないようにする。 */
    private record Snapshot(List<SpellUnlockDefinition> definitions,
                            Map<String, SpellUnlockDefinition> byCommand,
                            Map<ResourceLocation, SpellUnlockDefinition> byPiece,
                            Map<ResourceLocation, SpellUnlockDefinition> byGroup,
                            Map<ResourceLocation, List<SpellUnlockDefinition>> byItem) {
    }

    /** 同期パケットで送れる上限。これを超える定義はリロード時に不採用にする。 */
    public static final int MAX_SYNC_DEFINITIONS = 4096;
    public static final int MAX_SYNC_STRING_LENGTH = 32767;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SPELL_UNLOCK_DIRECTORY = "spell_unlocks";
    private static final TypeAdapter<JsonElement> JSON_ELEMENT_ADAPTER = new Gson().getAdapter(JsonElement.class);
    private static final DynamicCommandExceptionType UNKNOWN_SPELL = new DynamicCommandExceptionType(
            id -> Component.translatable("message.psitweaks.spell_unlock.command.unknown", id));

    private static final List<SpellUnlockDefinition> DEFAULT_SPELL_UNLOCKS = List.of(
            definition("cocytus", "trick_cocytus", "program_cocytus"),
            definition("time_accelerate", "trick_time_accelerate", "program_time_accelerate"),
            definition("flight", "trick_flight", "program_flight"),
            definition("phonon_maser", "trick_phonon_maser", "program_phonon_maser"),
            definition("meteor_line", "trick_meteor_line", "program_meteor_line"),
            definition("dry_meteor", "trick_dry_meteor", "program_dry_meteor"),
            definition("supreme_infusion", "trick_supreme_infusion", "program_supreme_infusion"),
            definition("molecular_divider", "trick_molecular_divider", "program_molecular_divider"),
            definition("radiation_injection", "trick_radiation_injection", "program_radiation_injection"),
            definition("radiation_filter", "trick_radiation_filter", "program_radiation_filter"),
            definition("cure_radiation", "trick_cure_radiation", "program_cure_radiation"),
            definition("guillotine", "trick_guillotine", "program_guillotine"),
            definition("active_air_mine", "trick_active_air_mine", "program_active_air_mine"),
            definition("die_flex", "trick_die_flex", "program_die_flex"),
            definition("jump_flex", "trick_jump_flex", "program_jump_flex"),
            definition("switch_flex", "trick_switch_flex", "program_switch_flex"),
            definition("material_mutation", "trick_material_mutation", "program_material_mutation"),
            definition("mass_block_break", "trick_mass_block_break", "program_mass_block_break"),
            // イデアストレージ系ピースは共通の unlock_tag を共有し、1つのプログラムで全ピースを解禁する
            definition("idea_storage_absorb_fe", "trick_idea_storage_absorb_fe", "program_idea_storage",
                    Psitweaks.MOD_ID + ".unlock.idea_storage"),
            definition("idea_storage_supply_fe", "trick_idea_storage_supply_fe", "program_idea_storage",
                    Psitweaks.MOD_ID + ".unlock.idea_storage"),
            definition("idea_storage_energy", "selector_idea_storage_energy", "program_idea_storage",
                    Psitweaks.MOD_ID + ".unlock.idea_storage"),
            definition("ideaspace_connector", "trick_ideaspace_connector", "program_idea_storage",
                    Psitweaks.MOD_ID + ".unlock.idea_storage"),
            definition("idea_storage", "trick_idea_storage_view", "program_idea_storage",
                    Psitweaks.MOD_ID + ".unlock.idea_storage", "idea_storage")
    );

    private static final SpellUnlockReloadListener SPELL_UNLOCK_RELOAD_LISTENER = new SpellUnlockReloadListener();
    private static final Snapshot DEFAULT_SNAPSHOT = buildSnapshot(DEFAULT_SPELL_UNLOCKS, true);

    /** サーバー側(統合サーバーを含む)の有効な定義。JSON リロードで置き換わる。 */
    private static volatile Snapshot serverSnapshot = DEFAULT_SNAPSHOT;
    /** 接続中のサーバーから同期された定義。未受信・切断後は null。 */
    @Nullable
    private static volatile Snapshot clientSnapshot;
    private static final String UNLOCKS_DATA_KEY = Psitweaks.MOD_ID + ".spell_unlocks";

    private SpellUnlockHandler() {
    }

    private static SpellUnlockDefinition definition(String commandId, String piecePath, String itemPath) {
        return definition(commandId, piecePath, itemPath, Psitweaks.MOD_ID + ".unlock." + piecePath);
    }

    private static SpellUnlockDefinition definition(String commandId, String piecePath, String itemPath, String unlockTag) {
        return new SpellUnlockDefinition(
                commandId,
                Psitweaks.location(piecePath),
                Psitweaks.location(itemPath),
                unlockTag,
                null
        );
    }

    private static SpellUnlockDefinition definition(String commandId, String piecePath, String itemPath, String unlockTag,
                                                    String groupPath) {
        return new SpellUnlockDefinition(
                commandId,
                Psitweaks.location(piecePath),
                Psitweaks.location(itemPath),
                unlockTag,
                Psitweaks.location(groupPath)
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

    /** ログイン時と /reload 後に、サーバーの有効な定義をクライアントへ送る。 */
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        MessageSpellUnlockSync message = new MessageSpellUnlockSync(serverSnapshot.definitions());
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, message));
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        serverSnapshot = DEFAULT_SNAPSHOT;
    }

    /** サーバーから同期された定義をクライアント側の判定に使う。 */
    public static void applyClientDefinitions(List<SpellUnlockDefinition> definitions) {
        clientSnapshot = buildSnapshot(definitions, false);
    }

    /** 切断時に、接続先サーバー由来の定義を破棄する。 */
    public static void clearClientDefinitions() {
        clientSnapshot = null;
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> spellUnlockRoot = Commands.literal("spellunlock");
        spellUnlockRoot.then(createAllSpellCommand());
        spellUnlockRoot.then(createSpellCommand());

        dispatcher.register(
                Commands.literal("psitweaks")
                        .requires(source -> source.hasPermission(2))
                        .then(spellUnlockRoot)
        );
    }

    /** コマンド登録はリロード適用より前に行われるため、定義は実行時に現在のスナップショットから引く。 */
    private static RequiredArgumentBuilder<CommandSourceStack, String> createSpellCommand() {
        return Commands.argument("spell", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(serverSnapshot.byCommand().keySet(), builder))
                .then(Commands.literal("grant")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> setSpellUnlock(
                                        ctx.getSource(),
                                        EntityArgument.getPlayers(ctx, "targets"),
                                        getSpellDefinition(ctx),
                                        true))))
                .then(Commands.literal("revoke")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> setSpellUnlock(
                                        ctx.getSource(),
                                        EntityArgument.getPlayers(ctx, "targets"),
                                        getSpellDefinition(ctx),
                                        false))))
                .then(Commands.literal("status")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> showSpellUnlockStatus(
                                        ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "target"),
                                        getSpellDefinition(ctx)))));
    }

    private static SpellUnlockDefinition getSpellDefinition(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String commandId = StringArgumentType.getString(context, "spell");
        SpellUnlockDefinition definition = serverSnapshot.byCommand().get(commandId.toLowerCase(Locale.ROOT));
        if (definition == null) {
            throw UNKNOWN_SPELL.create(commandId);
        }
        return definition;
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

    public static void onPieceKnowledge(PieceKnowledgeEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        Snapshot snapshot = snapshotFor(player);
        SpellUnlockDefinition definition = findDefinition(snapshot, event.getPieceName(), event.getPieceGroup());
        if (definition == null) {
            return;
        }
        if (!PsitweaksConfig.COMMON.requireSpellUnlocks.get()) {
            return;
        }

        if (!isSpellUnlocked(player, definition)) {
            event.setCanceled(true);
        }
    }

    /** クライアント側は同期済み定義を優先する。未受信なら統合サーバーの定義(専用サーバー接続時は既定値)を使う。 */
    private static Snapshot snapshotFor(Player player) {
        Snapshot synced = clientSnapshot;
        return player.level().isClientSide() && synced != null ? synced : serverSnapshot;
    }

    /** ピース単位の定義を優先し、無ければグループ単位の定義で判定する(グループへのピース追加時の登録漏れ防止)。 */
    @Nullable
    private static SpellUnlockDefinition findDefinition(Snapshot snapshot, @Nullable ResourceLocation pieceName,
                                                        @Nullable ResourceLocation groupName) {
        SpellUnlockDefinition definition = pieceName == null ? null : snapshot.byPiece().get(pieceName);
        if (definition == null && groupName != null) {
            definition = snapshot.byGroup().get(groupName);
        }
        return definition;
    }

    @SubscribeEvent
    public static void onRightClickUnlockItem(PlayerInteractEvent.RightClickItem event) {
        List<SpellUnlockDefinition> definitions = getUnlockDefinitionsByItem(event.getItemStack());
        if (definitions.isEmpty()) {
            return;
        }

        if (event.getLevel().isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        boolean unlocked = false;
        for (SpellUnlockDefinition definition : definitions) {
            unlocked |= setSpellUnlocked(serverPlayer, definition, true);
        }
        SpellUnlockDefinition displayDefinition = definitions.get(0);
        if (unlocked) {
            serverPlayer.level().playSound(
                    null,
                    serverPlayer.getX(),
                    serverPlayer.getY(),
                    serverPlayer.getZ(),
                    PsiSoundHandler.levelUp.get(),
                    SoundSource.PLAYERS,
                    0.6F,
                    1.0F
            );
            serverPlayer.displayClientMessage(
                    Component.translatable("message.psitweaks.spell_unlock.unlocked", displayDefinition.spellNameComponent()),
                    true
            );
        } else {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.psitweaks.spell_unlock.already", displayDefinition.spellNameComponent()),
                    true
            );
        }
    }

    private static List<SpellUnlockDefinition> getUnlockDefinitionsByItem(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return serverSnapshot.byItem().getOrDefault(itemId, List.of());
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
        List<SpellUnlockDefinition> definitions = serverSnapshot.definitions();
        int changed = 0;
        int changedPlayers = 0;
        int totalSpells = definitions.size();
        int totalOperations = targets.size() * totalSpells;

        for (ServerPlayer target : targets) {
            // 共有 unlock_tag の定義は最初の1件で一緒に切り替わるため、件数は変更前の状態で数える(status と同じ定義数基準)
            int targetChanged = 0;
            for (SpellUnlockDefinition definition : definitions) {
                if (isSpellUnlocked(target, definition) != unlocked) {
                    targetChanged++;
                }
            }
            for (SpellUnlockDefinition definition : definitions) {
                setSpellUnlocked(target, definition, unlocked);
            }
            changed += targetChanged;
            if (targetChanged > 0) {
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
        List<SpellUnlockDefinition> definitions = serverSnapshot.definitions();
        int unlockedCount = 0;
        int totalSpells = definitions.size();
        for (SpellUnlockDefinition definition : definitions) {
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

    /**
     * 解禁状態を切り替え、実際の解禁状態(データ or 旧タグ)が変わったかを返す。
     * 旧形式のプレイヤータグだけが残っている場合も revoke で外し、タグの補修だけでは「変更」と報告しない。
     */
    private static boolean setSpellUnlocked(ServerPlayer player, SpellUnlockDefinition definition, boolean unlocked) {
        boolean wasUnlocked = isSpellUnlocked(player, definition);
        PlayerData data = PlayerDataHandler.get(player);
        CompoundTag unlockData = getUnlockData(data.getCustomData(), true);
        if (unlockData.getBoolean(definition.unlockTag()) != unlocked) {
            unlockData.putBoolean(definition.unlockTag(), unlocked);
            data.save();
            PsiNetwork.sendToPlayer(player, new MessageDataSync(data));
        }
        if (unlocked) {
            player.addTag(definition.unlockTag());
        } else {
            player.removeTag(definition.unlockTag());
        }

        return wasUnlocked != unlocked;
    }

    private static boolean isSpellUnlocked(Player player, SpellUnlockDefinition definition) {
        PlayerData data = PlayerDataHandler.get(player);
        CompoundTag unlockData = getUnlockData(data.getCustomData(), false);
        if (unlockData.getBoolean(definition.unlockTag())) {
            return true;
        }

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

    /**
     * 定義から索引を作る。strict(サーバーの既定値・JSON)では、黙ってスキップすると解禁制限が外れるため
     * 不正・重複・同期上限超過をすべて例外にする。同期受信側は strict にせず、従来どおりスキップする。
     */
    private static Snapshot buildSnapshot(List<SpellUnlockDefinition> definitions, boolean strict) {
        if (strict && definitions.size() > MAX_SYNC_DEFINITIONS) {
            throw new IllegalArgumentException("too many definitions for client sync: " + definitions.size());
        }
        Map<String, SpellUnlockDefinition> byCommand = new LinkedHashMap<>();
        Map<ResourceLocation, SpellUnlockDefinition> byPiece = new LinkedHashMap<>();
        Map<ResourceLocation, SpellUnlockDefinition> byGroup = new LinkedHashMap<>();
        Map<ResourceLocation, List<SpellUnlockDefinition>> byItem = new LinkedHashMap<>();
        List<SpellUnlockDefinition> ordered = new ArrayList<>();

        for (SpellUnlockDefinition original : definitions) {
            String commandId = normalizeCommandId(original.commandId());
            if (commandId == null) {
                skipOrThrow(strict, "invalid command id: " + original.commandId());
                continue;
            }

            SpellUnlockDefinition definition = new SpellUnlockDefinition(
                    commandId,
                    original.pieceId(),
                    original.unlockItemId(),
                    original.unlockTag(),
                    original.groupId()
            );

            if (byCommand.containsKey(commandId)) {
                skipOrThrow(strict, "duplicate command id '" + commandId + "'");
                continue;
            }
            if ("all".equals(commandId)) {
                skipOrThrow(strict, "reserved command id '" + commandId + "'");
                continue;
            }
            if (byPiece.containsKey(definition.pieceId())) {
                skipOrThrow(strict, "duplicate piece id '" + definition.pieceId() + "'");
                continue;
            }
            if (definition.groupId() != null && byGroup.containsKey(definition.groupId())) {
                skipOrThrow(strict, "duplicate group '" + definition.groupId() + "' in '" + commandId + "'");
                continue;
            }
            if (strict && !fitsSyncLimits(definition)) {
                throw new IllegalArgumentException("definition '" + commandId + "' exceeds client sync string limits");
            }

            byCommand.put(commandId, definition);
            byPiece.put(definition.pieceId(), definition);
            if (definition.groupId() != null) {
                byGroup.put(definition.groupId(), definition);
            }
            // 同じ unlock_item を共有する定義を許容する(共通 unlock_tag で複数ピースを解禁するため)
            byItem.computeIfAbsent(definition.unlockItemId(), key -> new ArrayList<>()).add(definition);
            ordered.add(definition);
        }

        Map<ResourceLocation, List<SpellUnlockDefinition>> byItemImmutable = new LinkedHashMap<>();
        byItem.forEach((item, defs) -> byItemImmutable.put(item, List.copyOf(defs)));

        return new Snapshot(List.copyOf(ordered), Map.copyOf(byCommand), Map.copyOf(byPiece), Map.copyOf(byGroup),
                Map.copyOf(byItemImmutable));
    }

    private static void skipOrThrow(boolean strict, String problem) {
        if (strict) {
            throw new IllegalArgumentException(problem);
        }
        LOGGER.warn("Skipping spell unlock definition: {}", problem);
    }

    private static boolean fitsSyncLimits(SpellUnlockDefinition definition) {
        return definition.commandId().length() <= MAX_SYNC_STRING_LENGTH
                && definition.unlockTag().length() <= MAX_SYNC_STRING_LENGTH
                && definition.pieceId().toString().length() <= MAX_SYNC_STRING_LENGTH
                && definition.unlockItemId().toString().length() <= MAX_SYNC_STRING_LENGTH
                && (definition.groupId() == null || definition.groupId().toString().length() <= MAX_SYNC_STRING_LENGTH);
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

    /** 数値や真偽値を文字列として受け入れないよう、JSON 文字列であることを確認する。 */
    private static String requireString(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("'" + key + "' must be a string");
        }
        return element.getAsString();
    }

    private static String optionalString(JsonObject json, String key, String fallback) {
        return json.has(key) ? requireString(json, key) : fallback;
    }

    private static ResourceLocation requireResourceLocation(JsonObject json, String key) {
        String rawValue = requireString(json, key);
        ResourceLocation parsed = ResourceLocation.tryParse(rawValue);
        if (parsed == null) {
            throw new JsonParseException("'" + key + "' is not a valid resource location: " + rawValue);
        }
        return parsed;
    }

    private static SpellUnlockDefinition parseDefinition(ResourceLocation sourceId, @Nullable JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            throw new JsonParseException("root is not a JSON object");
        }

        JsonObject json = element.getAsJsonObject();
        ResourceLocation pieceId = requireResourceLocation(json, "piece");
        ResourceLocation unlockItemId = requireResourceLocation(json, "unlock_item");
        ResourceLocation groupId = json.has("group") ? requireResourceLocation(json, "group") : null;
        String defaultCommandId = sourceId.getPath().replace('/', '_');
        String commandId = optionalString(json, "command_id", defaultCommandId);
        String unlockTag = optionalString(json, "unlock_tag", Psitweaks.MOD_ID + ".unlock." + pieceId.getPath());
        return new SpellUnlockDefinition(commandId, pieceId, unlockItemId, unlockTag, groupId);
    }

    /** バニラと同じ厳格な構文で1つの値を読み、後ろに余分な内容があれば不正とする。 */
    private static JsonElement readStrictJson(Reader reader) throws IOException {
        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(false);
        JsonElement element = JSON_ELEMENT_ADAPTER.read(jsonReader);
        if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
            throw new JsonParseException("unexpected content after the JSON value");
        }
        return element;
    }

    /** 読み込めなかったファイルも不正として数えるため、JSON の読み込みも自前で行う。 */
    private record PreparedDefinitions(Map<ResourceLocation, JsonElement> entries, List<ResourceLocation> unreadable) {
    }

    private static final class SpellUnlockReloadListener extends SimplePreparableReloadListener<PreparedDefinitions> {
        @Override
        protected PreparedDefinitions prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
            FileToIdConverter converter = FileToIdConverter.json(SPELL_UNLOCK_DIRECTORY);
            Map<ResourceLocation, JsonElement> entries = new LinkedHashMap<>();
            List<ResourceLocation> unreadable = new ArrayList<>();
            for (Map.Entry<ResourceLocation, Resource> entry : converter.listMatchingResources(resourceManager).entrySet()) {
                ResourceLocation id = converter.fileToId(entry.getKey());
                try (Reader reader = entry.getValue().openAsReader()) {
                    entries.put(id, readStrictJson(reader));
                } catch (IOException | RuntimeException e) {
                    unreadable.add(id);
                    LOGGER.error("Couldn't read spell unlock definition {} from {}: {}", id, entry.getKey(), e.getMessage());
                }
            }
            return new PreparedDefinitions(entries, unreadable);
        }

        @Override
        protected void apply(PreparedDefinitions prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
            List<SpellUnlockDefinition> loaded = new ArrayList<>();
            int invalid = prepared.unreadable().size();

            for (Map.Entry<ResourceLocation, JsonElement> entry : prepared.entries().entrySet()) {
                try {
                    loaded.add(parseDefinition(entry.getKey(), entry.getValue()));
                } catch (RuntimeException e) {
                    invalid++;
                    LOGGER.error("Invalid spell unlock definition {}: {}", entry.getKey(), e.getMessage());
                }
            }

            // 一部だけ採用すると解禁制限が外れるおそれがあるため、不正な定義が1つでもあれば全体を不採用にする
            if (invalid > 0) {
                rejectReload(invalid + " definition(s) are invalid");
                return;
            }
            if (loaded.isEmpty()) {
                LOGGER.warn("No spell unlock JSON found under data/*/{}; keeping {} existing definitions.",
                        SPELL_UNLOCK_DIRECTORY, serverSnapshot.definitions().size());
                return;
            }
            Snapshot snapshot;
            try {
                snapshot = buildSnapshot(loaded, true);
            } catch (IllegalArgumentException e) {
                rejectReload(e.getMessage());
                return;
            }

            serverSnapshot = snapshot;
            LOGGER.info("Loaded {} spell unlock definitions from JSON.", snapshot.definitions().size());
        }

        private static void rejectReload(String reason) {
            LOGGER.error("Rejecting spell unlock reload ({}); keeping {} existing definitions.",
                    reason, serverSnapshot.definitions().size());
        }
    }
}
