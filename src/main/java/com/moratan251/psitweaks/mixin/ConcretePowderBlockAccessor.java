package com.moratan251.psitweaks.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ConcretePowderBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** コンクリートパウダーが水で固まったときのブロック（作動式: 湿潤領域で固めるのに使う）。 */
@Mixin(ConcretePowderBlock.class)
public interface ConcretePowderBlockAccessor {
    @Accessor("concrete")
    Block psitweaks$getConcrete();
}
