package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.client.spells.ModeOverlayRenderer;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.mode.ModeConfigurableSpellPiece;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.WildcardStringMatcher;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

abstract class PieceTrickItemTransferBase extends PieceTrick implements ModeConfigurableSpellPiece {

    private static final String TAG_MODE = "psitweaksMode";
    private static final String ERROR_NULL_ITEM = "psitweaks.spellerror.nullitem";
    private static final String ERROR_ACCESS_DENIED = "psitweaks.spellerror.accessdenyed";
    private static final List<PsitweaksModeOption> MODES = List.of(PsitweaksModeOptions.STRING,
            PsitweaksModeOptions.ITEM, PsitweaksModeOptions.ITEM_STRICT);

    private static final int POTENCY = 50;
    private static final int COST = 100;

    private PsitweaksModeOption mode = PsitweaksModeOptions.STRING;

    protected SpellParam<Vector3> position;
    protected SpellParam<Vector3> direction;
    protected SpellParam<Number> max;
    private SpellParam<String> type;
    private SpellParam<SpellItemValue> item;

    protected PieceTrickItemTransferBase(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void initParams() {
        rebuildParams(null);
    }

    @Override
    public List<PsitweaksModeOption> getAvailableModeOptions() {
        return MODES;
    }

    @Override
    public PsitweaksModeOption getModeOption() {
        return normalizeModeOption(mode);
    }

    @Override
    public void setModeOption(PsitweaksModeOption modeOption) {
        PsitweaksModeOption nextMode = normalizeModeOption(modeOption);
        if (nextMode.id().equals(getModeOption().id())) {
            return;
        }

        Map<String, SpellParam.Side> savedSides = saveParamSides();
        mode = nextMode;
        rebuildParams(savedSides);
    }

    @Override
    public void drawAdditional(PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        ModeOverlayRenderer.drawModeOverlay(poseStack, bufferSource, light, getModeOption());
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        mode = normalizeModeOption(PsitweaksModeOptions.byId(tag.getString(TAG_MODE)).orElse(null));
        rebuildParams(null);
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    private boolean isItemMode() {
        ResourceLocation id = getModeOption().id();
        return id.equals(PsitweaksModeOptions.ITEM.id()) || id.equals(PsitweaksModeOptions.ITEM_STRICT.id());
    }

    private boolean isItemStrictMode() {
        return getModeOption().id().equals(PsitweaksModeOptions.ITEM_STRICT.id());
    }

    private void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        params.clear();
        paramSides.clear();
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(direction = new ParamVector("psi.spellparam.direction", SpellParam.GREEN, false, false));
        if (isItemMode()) {
            addParam(item = new ParamSpellItemValue(PsitweaksSpellParams.ITEM, PsitweaksSpellParams.ITEM_COLOR,
                    true, false));
        } else {
            addParam(type = new ParamString(PsitweaksSpellParams.STRING, PsitweaksSpellParams.STRING_COLOR,
                    true, false));
        }
        addParam(max = new ParamNumber(PsitweaksSpellParams.MAX, SpellParam.RED, true, false));
        restoreParamSides(savedSides);
    }

    private Map<String, SpellParam.Side> saveParamSides() {
        Map<String, SpellParam.Side> savedSides = new LinkedHashMap<>();
        paramSides.forEach((param, side) -> savedSides.put(param.name, side));
        return savedSides;
    }

    private void restoreParamSides(Map<String, SpellParam.Side> savedSides) {
        if (savedSides == null) {
            return;
        }

        for (SpellParam<?> param : params.values()) {
            SpellParam.Side side = savedSides.get(param.name);
            if (side != null) {
                paramSides.put(param, side);
            }
        }
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = getParamValue(context, position);
        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        Vector3 directionVal = getParamValue(context, direction);
        if (directionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        Level level = context.caster.level();
        // サーバーサイドでのみ実行
        if (level.isClientSide) {
            return null;
        }

        // 最大は正の整数のみ受け付ける
        int limit = resolveLimit(context);
        if (limit == 0) {
            throw new SpellRuntimeException(ERROR_NULL_ITEM);
        }

        BlockPos pos = positionVal.toBlockPos();
        if (!level.isLoaded(pos)) {
            throw new SpellRuntimeException(ERROR_NULL_ITEM);
        }
        if (!level.mayInteract(context.caster, pos)) {
            throw new SpellRuntimeException(ERROR_ACCESS_DENIED);
        }

        Direction facing = Direction.getNearest(directionVal.x, directionVal.y, directionVal.z);
        // FTB Chunks などの保護Modがチェスト操作を捕捉できるよう、右クリック相当のイベントで問い合わせる
        if (isProtectedFromInteraction(context.caster, pos, facing)) {
            throw new SpellRuntimeException(ERROR_ACCESS_DENIED);
        }
        IItemHandler handler = getBlockItemHandler(level, pos, facing);
        if (handler == null) {
            throw new SpellRuntimeException(ERROR_NULL_ITEM);
        }

        if (!transfer(context, handler, buildFilter(context), limit)) {
            throw new SpellRuntimeException(ERROR_NULL_ITEM);
        }
        return null;
    }

    private static IItemHandler getBlockItemHandler(Level level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        IItemHandler handler = Capabilities.ItemHandler.BLOCK.getCapability(level, pos, state, blockEntity, facing);
        if (handler != null) {
            return handler;
        }
        if (blockEntity instanceof WorldlyContainer worldlyContainer) {
            return new SidedInvWrapper(worldlyContainer, facing);
        }
        if (blockEntity instanceof Container container) {
            return new InvWrapper(container);
        }
        return null;
    }

    /**
     * Asks protection mods whether the caster may interact with the block's inventory by
     * posting a right-click equivalent event. Canceled or block-use denied means protected.
     */
    private static boolean isProtectedFromInteraction(Player caster, BlockPos pos, Direction facing) {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), facing, pos, false);
        PlayerInteractEvent.RightClickBlock event = NeoForge.EVENT_BUS.post(
                new PlayerInteractEvent.RightClickBlock(caster, InteractionHand.MAIN_HAND, pos, hit));
        return event.isCanceled() || event.getUseBlock() == TriState.FALSE;
    }

    private Predicate<ItemStack> buildFilter(SpellContext context) throws SpellRuntimeException {
        if (isItemMode()) {
            SpellItemValue itemVal = getParamValue(context, item);
            if (itemVal == null) {
                return stack -> true;
            }
            // 接続済みだが Item[empty] の場合は対象アイテム不在としてエラー
            if (itemVal.isEmpty()) {
                throw new SpellRuntimeException(ERROR_NULL_ITEM);
            }
            if (isItemStrictMode()) {
                // アイテム種 + Data Component(エンチャント・耐久値・カスタム名等)の一致。個数は無視
                ItemStack target = itemVal.snapshot();
                return stack -> ItemStack.isSameItemSameComponents(stack, target);
            }
            Item target = itemVal.snapshot().getItem();
            return stack -> stack.getItem() == target;
        }

        String typeVal = getParamValue(context, type);
        if (typeVal == null || typeVal.isEmpty()) {
            return stack -> true;
        }
        WildcardStringMatcher matcher = WildcardStringMatcher.compile(typeVal);
        boolean matchFullId = typeVal.indexOf(':') >= 0;
        return stack -> {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return matcher.matches(matchFullId ? id.toString() : id.getPath());
        };
    }

    private int resolveLimit(SpellContext context) throws SpellRuntimeException {
        Number maxVal = getParamValue(context, max);
        if (maxVal == null) {
            return -1;
        }
        if (!isPositiveInteger(maxVal)) {
            return 0;
        }
        return maxVal.intValue();
    }

    private static boolean isPositiveInteger(Number number) {
        double value = number.doubleValue();
        return Double.isFinite(value) && value > 0 && value == Math.floor(value);
    }

    /**
     * Moves up to one matching stack. {@code limit} is the max count or -1 when unspecified.
     *
     * @return true if at least one item was moved
     */
    protected abstract boolean transfer(SpellContext context, IItemHandler handler, Predicate<ItemStack> filter,
            int limit) throws SpellRuntimeException;

    protected static int transferAmount(ItemStack stack, int limit) {
        return ItemTransferAmount.calculate(stack.getCount(), stack.getMaxStackSize(), limit);
    }
}
