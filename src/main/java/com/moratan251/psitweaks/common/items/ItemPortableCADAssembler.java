package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.common.menu.PortableCADAssemblerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemPortableCADAssembler extends Item {

    public ItemPortableCADAssembler(Properties props) {
        super(props);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            MenuProvider provider = new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return Component.translatable("container.psitweaks.portable_cad_assembler");
                }

                @Override
                public @Nullable AbstractContainerMenu createMenu(int id, @NotNull net.minecraft.world.entity.player.Inventory inv, @NotNull Player p) {
                    return new PortableCADAssemblerMenu(id, inv, hand);
                }
            };

            NetworkHooks.openScreen(sp, provider, buf -> buf.writeEnum(hand));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}