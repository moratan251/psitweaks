package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper.DropEntry;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper.StackAccumulator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Harvesting for Trick: Harvest, Harvest Sequence and Mass Harvest. It works like the right-click harvest mods:
 * the drops come from the crop's loot table, one seed of them replants the crop, and the rest goes to the caster.
 * <ul>
 *     <li>Crops ({@link CropBlock} at its last age), cocoa and nether wart are replanted at age 0.</li>
 *     <li>Sweet berry bushes and glow berry vines are picked, as by hand.</li>
 *     <li>Sugar cane, cactus and bamboo lose everything above their lowest block.</li>
 *     <li>A block that an attached stem grew (melon, pumpkin) is taken; the stem stays.</li>
 * </ul>
 */
public final class HarvestHelper {
    /** How far a sugar cane, cactus or bamboo column is followed from the targeted block. */
    static final int MAX_COLUMN_LENGTH = 32;
    /** Break effects shown per cast; a larger harvest shows them at a random sample of its crops. */
    static final int MAX_EFFECTS = 16;

    private HarvestHelper() {
    }

    /** Starts a harvest for the caster of {@code context}; null off the server or when no player casts the spell. */
    @Nullable
    public static Harvest begin(SpellContext context) {
        if (!(context.caster instanceof ServerPlayer caster) || !(context.focalPoint.level() instanceof ServerLevel level)) {
            return null;
        }
        return new Harvest(caster, level, harvestTool(context));
    }

    /** The tool the loot tables see (its Fortune counts); a caster without a CAD harvests by hand. */
    private static ItemStack harvestTool(SpellContext context) {
        try {
            return context.getHarvestTool().copy();
        } catch (SpellRuntimeException noCad) {
            return ItemStack.EMPTY;
        }
    }

    /** Puts the items into the caster's main inventory, onto the stacks they carry first; the rest drops at the caster. */
    public static void deliverToCaster(Player caster, StackAccumulator items) {
        new Delivery(items.entries()).handOver(caster, Long.MAX_VALUE);
    }

    /** The crop at age 0 that keeps the other properties of the grown one, such as the rope of a climbing crop. */
    static BlockState replantedCrop(CropBlock crop, BlockState grown) {
        BlockState fresh = crop.getStateForAge(0);
        if (crop.getMaxAge() < 1 || !fresh.is(grown.getBlock())) {
            return fresh;
        }
        // The age property is the one that differs between age 0 and age 1.
        BlockState aged = crop.getStateForAge(1);
        BlockState result = fresh;
        for (Property<?> property : grown.getProperties()) {
            if (fresh.hasProperty(property) && aged.hasProperty(property)
                    && fresh.getValue(property).equals(aged.getValue(property))) {
                result = copyValue(result, grown, property);
            }
        }
        return result;
    }

    private static <T extends Comparable<T>> BlockState copyValue(BlockState target, BlockState source, Property<T> property) {
        return target.setValue(property, source.getValue(property));
    }

    /** The crops one cast harvests; the caster gets all of them at once in {@link #finish()}. */
    public static final class Harvest {
        private final ServerPlayer caster;
        private final ServerLevel level;
        private final ItemStack tool;
        private final StackAccumulator drops = new StackAccumulator();
        private final List<Effect> effects = new ArrayList<>();
        private int harvested;
        @Nullable
        private Delivery delivery;

        private Harvest(ServerPlayer caster, ServerLevel level, ItemStack tool) {
            this.caster = caster;
            this.level = level;
            this.tool = tool;
        }

        /** Harvests the crop at {@code pos}; false when nothing there is ripe or the caster may not harvest it. */
        public boolean harvest(BlockPos pos) {
            if (level.isOutsideBuildHeight(pos) || !level.isLoaded(pos)) {
                return false;
            }
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                return false;
            }
            Block block = state.getBlock();
            boolean done;
            if (block instanceof SugarCaneBlock || block instanceof CactusBlock || block instanceof BambooStalkBlock) {
                done = harvestColumn(pos, block);
            } else if (block instanceof CropBlock crop) {
                done = crop.isMaxAge(state) && replant(pos, state, replantedCrop(crop, state));
            } else if (block instanceof CocoaBlock) {
                done = state.getValue(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE
                        && replant(pos, state, state.setValue(CocoaBlock.AGE, 0));
            } else if (block instanceof NetherWartBlock) {
                done = state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE
                        && replant(pos, state, state.setValue(NetherWartBlock.AGE, 0));
            } else if (block instanceof SweetBerryBushBlock) {
                done = pickSweetBerries(pos, state);
            } else if (block instanceof CaveVines) {
                done = pickGlowBerries(pos, state);
            } else {
                done = isGrownByStem(pos) && takeFruit(pos, state);
            }
            if (done) {
                harvested++;
            }
            return done;
        }

        public int harvested() {
            return harvested;
        }

        UUID casterId() {
            return caster.getUUID();
        }

        /** Whether the caster can still take the harvest: alive, online and in the level of the crops. */
        public boolean isCasterPresent() {
            return caster.isAlive() && !caster.hasDisconnected() && caster.level() == level;
        }

        /** Ends a harvest whose caster is gone: what the caster has not got yet drops at {@code pos}. */
        public void abandon(BlockPos pos) {
            delivery().dropRest(level, pos);
        }

        /** Gives the harvest to the caster and shows the break effects. */
        public void finish() {
            finish(Long.MAX_VALUE);
        }

        /**
         * Gives the caster as much of the harvest as fits in the time before {@code deadline} ({@link System#nanoTime()});
         * true once all of it is given, when the break effects are shown too.
         */
        public boolean finish(long deadline) {
            if (!delivery().handOver(caster, deadline)) {
                return false;
            }
            List<Effect> shown = effects;
            if (shown.size() > MAX_EFFECTS) {
                shown = new ArrayList<>(effects);
                Util.shuffle(shown, level.random);
                shown = shown.subList(0, MAX_EFFECTS);
            }
            for (Effect effect : shown) {
                effect.play(level);
            }
            return true;
        }

        private Delivery delivery() {
            if (delivery == null) {
                delivery = new Delivery(drops.entries());
            }
            return delivery;
        }

        private boolean replant(BlockPos pos, BlockState grown, BlockState replanted) {
            if (!mayHarvest(pos, grown)) {
                return false;
            }
            List<ItemStack> loot = lootOf(pos, grown);
            removeOne(loot, seedOf(pos, grown));
            level.setBlock(pos, replanted, Block.UPDATE_ALL);
            collect(loot);
            effects.add(Effect.broken(pos, grown));
            return true;
        }

        private boolean pickSweetBerries(BlockPos pos, BlockState state) {
            int age = state.getValue(SweetBerryBushBlock.AGE);
            if (age < 2 || !mayHarvest(pos, state)) {
                return false;
            }
            // The yield of picking by hand.
            int count = 1 + level.random.nextInt(2) + (age == SweetBerryBushBlock.MAX_AGE ? 1 : 0);
            drops.add(new ItemStack(Items.SWEET_BERRIES), count);
            level.setBlock(pos, state.setValue(SweetBerryBushBlock.AGE, 1), Block.UPDATE_CLIENTS);
            effects.add(Effect.picked(pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES));
            return true;
        }

        private boolean pickGlowBerries(BlockPos pos, BlockState state) {
            if (!CaveVines.hasGlowBerries(state) || !mayHarvest(pos, state)) {
                return false;
            }
            drops.add(new ItemStack(Items.GLOW_BERRIES), 1);
            level.setBlock(pos, state.setValue(CaveVines.BERRIES, false), Block.UPDATE_CLIENTS);
            effects.add(Effect.picked(pos, SoundEvents.CAVE_VINES_PICK_BERRIES));
            return true;
        }

        /** Takes every block above the lowest one of the column, from the top down so nothing is left floating. */
        private boolean harvestColumn(BlockPos pos, Block block) {
            BlockPos bottom = pos;
            for (int i = 0; i < MAX_COLUMN_LENGTH && level.getBlockState(bottom.below()).is(block); i++) {
                bottom = bottom.below();
            }
            List<BlockPos> column = new ArrayList<>();
            List<BlockState> states = new ArrayList<>();
            for (BlockPos cursor = bottom.above(); column.size() < MAX_COLUMN_LENGTH && !level.isOutsideBuildHeight(cursor);
                    cursor = cursor.above()) {
                BlockState state = level.getBlockState(cursor);
                if (!state.is(block)) {
                    break;
                }
                column.add(cursor);
                states.add(state);
            }
            if (column.isEmpty()) {
                return false;
            }
            // Ask about every block first, so a protected block cannot leave the ones above it floating.
            for (int i = 0; i < column.size(); i++) {
                if (!mayHarvest(column.get(i), states.get(i))) {
                    return false;
                }
            }
            for (int i = column.size() - 1; i >= 0; i--) {
                List<ItemStack> loot = lootOf(column.get(i), states.get(i));
                level.setBlock(column.get(i), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                collect(loot);
            }
            effects.add(Effect.broken(column.get(0), states.get(0)));
            return true;
        }

        private boolean isGrownByStem(BlockPos pos) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockState neighbor = level.getBlockState(pos.relative(direction));
                if (neighbor.getBlock() instanceof AttachedStemBlock
                        && neighbor.getValue(AttachedStemBlock.FACING) == direction.getOpposite()) {
                    return true;
                }
            }
            return false;
        }

        private boolean takeFruit(BlockPos pos, BlockState state) {
            if (!mayHarvest(pos, state)) {
                return false;
            }
            List<ItemStack> loot = lootOf(pos, state);
            // The attached stem turns back into a grown stem on its own.
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            collect(loot);
            effects.add(Effect.broken(pos, state));
            return true;
        }

        private boolean mayHarvest(BlockPos pos, BlockState state) {
            if (!level.mayInteract(caster, pos) || !caster.mayBuild()) {
                return false;
            }
            // Protection mods guard crops through the break event. A right-click event is not posted here: the
            // right-click harvest mods answer it by harvesting the crop themselves.
            return !NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, caster)).isCanceled();
        }

        private List<ItemStack> lootOf(BlockPos pos, BlockState state) {
            return new ArrayList<>(Block.getDrops(state, level, pos, level.getBlockEntity(pos), caster, tool));
        }

        private ItemStack seedOf(BlockPos pos, BlockState state) {
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            return state.getCloneItemStack(hit, level, pos, caster);
        }

        private static void removeOne(List<ItemStack> loot, ItemStack item) {
            if (item.isEmpty()) {
                return;
            }
            for (ItemStack stack : loot) {
                if (!stack.isEmpty() && ItemStack.isSameItem(stack, item)) {
                    stack.shrink(1);
                    return;
                }
            }
        }

        private void collect(List<ItemStack> loot) {
            for (ItemStack stack : loot) {
                drops.add(stack);
            }
        }
    }

    /** Items handed to a player a stack at a time, so that a large harvest can be handed over across ticks. */
    private static final class Delivery {
        private final List<DropEntry> entries;
        private int index;
        private long handed;

        Delivery(List<DropEntry> entries) {
            this.entries = entries;
        }

        /** Hands stacks over until {@code deadline}; true once everything is handed over. */
        boolean handOver(Player caster, long deadline) {
            // Inventory.add deletes what does not fit in creative mode (PIT-029), so go through the item handler.
            IItemHandler inventory = new PlayerMainInvWrapper(caster.getInventory());
            while (index < entries.size()) {
                DropEntry entry = entries.get(index);
                if (handed >= entry.count()) {
                    index++;
                    handed = 0;
                    continue;
                }
                if (System.nanoTime() >= deadline) {
                    return false;
                }
                int count = (int) Math.min(entry.count() - handed, entry.maxStackSize());
                handed += count;
                ItemStack rest = ItemHandlerHelper.insertItemStacked(inventory, entry.copyStack(count), false);
                if (!rest.isEmpty()) {
                    caster.drop(rest, false);
                }
            }
            return true;
        }

        /** Drops what is not handed over yet at {@code pos}. */
        void dropRest(ServerLevel level, BlockPos pos) {
            for (; index < entries.size(); index++, handed = 0) {
                DropEntry entry = entries.get(index);
                while (handed < entry.count()) {
                    int count = (int) Math.min(entry.count() - handed, entry.maxStackSize());
                    handed += count;
                    Block.popResource(level, pos, entry.copyStack(count));
                }
            }
        }
    }

    private record Effect(BlockPos pos, @Nullable BlockState state, @Nullable SoundEvent pickSound) {
        static Effect broken(BlockPos pos, BlockState state) {
            return new Effect(pos.immutable(), state, null);
        }

        static Effect picked(BlockPos pos, SoundEvent sound) {
            return new Effect(pos.immutable(), null, sound);
        }

        void play(ServerLevel level) {
            if (pickSound != null) {
                level.playSound(null, pos, pickSound, SoundSource.BLOCKS, 1.0F, Mth.randomBetween(level.random, 0.8F, 1.2F));
            } else if (state != null) {
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            }
        }
    }
}
