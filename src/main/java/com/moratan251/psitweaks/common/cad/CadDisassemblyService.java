package com.moratan251.psitweaks.common.cad;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.cad.ISocketable;

public final class CadDisassemblyService {
    public static final String UNSUPPORTED_TIC_CAD_MESSAGE = "message.psitweaks.cad_disassembler.unsupported_tic_cad";

    private static final List<CadDisassemblyStrategy> STRATEGIES = List.of(
            PsiExTicCadDisassemblyStrategy.INSTANCE,
            PsiCadDisassemblyStrategy.INSTANCE
    );

    private CadDisassemblyService() {
    }

    public static boolean canDisassemble(ItemStack cadStack) {
        return STRATEGIES.stream().anyMatch(strategy -> strategy.matches(cadStack));
    }

    public static CadDisassemblyResult disassemble(ItemStack cadStack) {
        for (CadDisassemblyStrategy strategy : STRATEGIES) {
            if (!strategy.matches(cadStack)) {
                continue;
            }

            CadDisassemblyResult componentResult = strategy.collectComponentDrops(cadStack);
            if (!componentResult.successful()) {
                return componentResult;
            }

            List<ItemStack> drops = new ArrayList<>(componentResult.drops());
            collectSocketBullets(cadStack, drops);
            return CadDisassemblyResult.success(drops);
        }

        return CadDisassemblyResult.failure(null);
    }

    private static void collectSocketBullets(ItemStack cadStack, List<ItemStack> drops) {
        if (!ISocketable.isSocketable(cadStack)) {
            return;
        }

        ISocketable socketable = ISocketable.socketable(cadStack);
        int lastSlot = socketable.getLastSlot();
        for (int slot = 0; slot <= lastSlot; slot++) {
            if (!socketable.isSocketSlotAvailable(slot)) {
                continue;
            }

            ItemStack bulletStack = socketable.getBulletInSocket(slot);
            if (!bulletStack.isEmpty()) {
                drops.add(bulletStack.copy());
            }
        }
    }
}
