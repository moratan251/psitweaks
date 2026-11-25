package com.moratan251.psitweaks.mixin;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Trick: Cocytus keeps a frozen Wither's boss bar on its health, which only the Wither's AI updates. */
@Mixin(WitherBoss.class)
public interface WitherBossAccessor {
    @Accessor("bossEvent")
    ServerBossEvent psitweaks$getBossEvent();
}
