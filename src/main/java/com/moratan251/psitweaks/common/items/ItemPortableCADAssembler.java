package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.common.menu.PortableCADAssemblerMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ItemPortableCADAssembler extends Item {
    public static final String TAG_INSTANCE_ID = "portableCadAssemblerId";
    public static final String TAG_ASSEMBLER_DATA = "portableCadAssemblerData";

    public ItemPortableCADAssembler(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        UUID portableId = getOrCreateInstanceId(stack);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new PortableCADAssemblerMenu.Provider(hand, portableId), buf -> {
                buf.writeEnum(hand);
                buf.writeUUID(portableId);
            });
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static UUID getOrCreateInstanceId(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.hasUUID(TAG_INSTANCE_ID)) {
            tag.putUUID(TAG_INSTANCE_ID, UUID.randomUUID());
        }
        return tag.getUUID(TAG_INSTANCE_ID);
    }

    public static @Nullable UUID getInstanceId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.hasUUID(TAG_INSTANCE_ID)) {
            return null;
        }
        return tag.getUUID(TAG_INSTANCE_ID);
    }
}
