package com.moratan251.psitweaks.common.blocks;

import net.minecraft.world.level.block.SkullBlock;

/**
 * PsiTweaks の頭ブロックの種類。バニラの {@link SkullBlock.Types} と同じく、生成時に {@link SkullBlock.Type#TYPES} へ登録する。
 */
public enum PsitweaksSkullTypes implements SkullBlock.Type {
    MAGICIAN("psitweaks_magician");

    private final String name;

    PsitweaksSkullTypes(String name) {
        this.name = name;
        TYPES.put(name, this);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
