package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.EntityTunnelerArrow;
import com.moratan251.psitweaks.common.items.ItemGravstringer;
import com.moratan251.psitweaks.common.items.ItemPortableCADAssembler;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.menu.PortableCADAssemblerMenu;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.cad.EnumCADComponent;

/** Items must not be doubled or lost when a screen is left open, or when one shot fires several arrows. */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ItemDuplicationGameTests {
    private static final String TEMPLATE = "connector_empty";

    private ItemDuplicationGameTests() {
    }

    private static FakePlayer player(GameTestHelper helper, String name) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 0))));
        return player;
    }

    /** F-007: the item follows every change while the screen is open, since logging out saves before it closes. */
    @GameTest(template = TEMPLATE)
    public static void portableCadAssemblerSavesWhileTheScreenIsOpen(GameTestHelper helper) {
        var player = player(helper, "portable_assembler");
        ItemStack portable = new ItemStack(PsitweaksItems.PORTABLE_CAD_ASSEMBLER.get());
        UUID id = ItemPortableCADAssembler.getOrCreateInstanceId(portable);
        player.setItemInHand(InteractionHand.MAIN_HAND, portable);
        var assembly = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("psi", "cad_assembly_iron")));
        helper.assertTrue(!assembly.isEmpty(), "Psi's iron assembly is missing");

        var menu = new PortableCADAssemblerMenu(1, player.getInventory(), InteractionHand.MAIN_HAND, id);
        int assemblySlot = 1;
        menu.slots.get(assemblySlot).set(assembly.copy());
        menu.broadcastChanges();
        // Not closed: as if the player logged out now and the saved item were opened again.
        var reopened = new PortableCADAssemblerMenu(2, player.getInventory(), InteractionHand.MAIN_HAND, id);
        helper.assertTrue(ItemStack.isSameItem(reopened.getAssembler().getStackForComponent(EnumCADComponent.ASSEMBLY), assembly),
                "The part put in was not saved to the item before the screen closed");

        menu.slots.get(assemblySlot).set(ItemStack.EMPTY);
        menu.broadcastChanges();
        var emptied = new PortableCADAssemblerMenu(3, player.getInventory(), InteractionHand.MAIN_HAND, id);
        helper.assertTrue(emptied.getAssembler().getStackForComponent(EnumCADComponent.ASSEMBLY).isEmpty(),
                "The part taken out was still in the item, so it could be taken twice");
        helper.succeed();
    }

    /** F-014: one Tunneler fires three arrows, but only the middle one may be picked up again. */
    @GameTest(template = TEMPLATE)
    public static void gravstringerFanShotLeavesOnlyOneArrowToPickUp(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        var shooter = player(helper, "gravstringer");
        ItemStack bow = new ItemStack(PsitweaksItems.GRAVSTRINGER.get());
        shooter.setItemInHand(InteractionHand.MAIN_HAND, bow);
        Method shoot = ItemGravstringer.class.getDeclaredMethod("shoot", ServerLevel.class, LivingEntity.class,
                InteractionHand.class, ItemStack.class, List.class, float.class, float.class, boolean.class, LivingEntity.class);
        shoot.setAccessible(true);
        shoot.invoke(bow.getItem(), level, shooter, InteractionHand.MAIN_HAND, bow,
                List.of(new ItemStack(PsitweaksItems.TUNNELER.get())), 3.0F, 0.0F, false, null);

        List<EntityTunnelerArrow> arrows = level.getEntitiesOfClass(EntityTunnelerArrow.class,
                new AABB(shooter.blockPosition()).inflate(8.0), arrow -> arrow.getOwner() == shooter);
        long pickable = arrows.stream().filter(arrow -> arrow.pickup == AbstractArrow.Pickup.ALLOWED).count();
        arrows.forEach(AbstractArrow::discard);
        helper.assertTrue(arrows.size() == 3, "A Tunneler should fire three arrows, got " + arrows.size());
        helper.assertTrue(pickable == 1, "Only one of the three arrows should be pickable, got " + pickable);
        helper.succeed();
    }
}
