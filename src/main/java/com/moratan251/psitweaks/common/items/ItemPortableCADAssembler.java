package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.common.menu.PortableCADAssemblerMenu;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ItemPortableCADAssembler extends Item {
    public static final String TAG_INSTANCE_ID = "portableCadAssemblerId";
    public static final String TAG_ASSEMBLER_DATA = "portableCadAssemblerData";

    public ItemPortableCADAssembler(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        UUID portableId = getOrCreateInstanceId(stack);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new PortableCADAssemblerMenu.Provider(hand, portableId), buf -> {
                buf.writeEnum(hand);
                buf.writeUUID(portableId);
            });
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static UUID getOrCreateInstanceId(ItemStack stack) {
        UUID id = getInstanceId(stack);
        if (id != null) {
            return id;
        }

        UUID newId = UUID.randomUUID();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putUUID(TAG_INSTANCE_ID, newId));
        return newId;
    }

    @Nullable
    public static UUID getInstanceId(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.hasUUID(TAG_INSTANCE_ID) ? tag.getUUID(TAG_INSTANCE_ID) : null;
    }
}
