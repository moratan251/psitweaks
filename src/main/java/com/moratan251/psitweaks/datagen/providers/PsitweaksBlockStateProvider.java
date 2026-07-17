package com.moratan251.psitweaks.datagen.providers;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class PsitweaksBlockStateProvider extends BlockStateProvider {
    public PsitweaksBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Psitweaks.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        cubeAll(PsitweaksBlocks.CAD_DISASSEMBLER.get(), "cad_disassembler");
        cubeAll(PsitweaksBlocks.ORE_ANTINITE.get(), "ore_antinite");
        cubeAll(PsitweaksBlocks.ANTINITE_BLOCK.get(), "antinite_block");
        cubeAll(PsitweaksBlocks.CHAOTIC_PSIMETAL_BLOCK.get(), "chaotic_psimetal_block");
        cubeAll(PsitweaksBlocks.FLASHMETAL_BLOCK.get(), "flashmetal_block");
        cubeAll(PsitweaksBlocks.HEAVY_PSIMETAL_BLOCK.get(), "heavy_psimetal_block");
        cubeAll(PsitweaksBlocks.PLUTONIUM_BLOCK.get(), "plutonium_block");
        cubeAll(PsitweaksBlocks.POLONIUM_BLOCK.get(), "polonium_block");
        cubeAll(PsitweaksBlocks.RAW_ANTINITE_BLOCK.get(), "raw_antinite_block");
        cubeAll(PsitweaksBlocks.SPELLMACHINERY_CASING.get(), "spellmachinery_casing");

        machine(PsitweaksMekanismBlocks.SCULK_ERODER.getBlock(), "sculk_eroder", "sculk_eroder");
        machineSingleTexture(PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.getBlock(), "program_researcher");
        machine(PsitweaksMekanismBlocks.MATERIAL_MUTATOR.getBlock(), "material_mutator", "material_mutator");
        machine(PsitweaksMekanismBlocks.PSIONIC_GENERATOR.getBlock(), "psionic_generator", "psi_link_generator");
        transcendentCable();
        energyCube(PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE.getBlock(), "transcendent_energy_cube", "transcendent");
    }

    private void cubeAll(Block block, String name) {
        ModelFile model = models().cubeAll(name, blockTexture(name));
        simpleBlockWithItem(block, model);
    }

    private void machine(Block block, String name, String textureBase) {
        ModelFile model = models().getBuilder(name)
                .parent(unchecked(resource("mekanism", "block/machine")))
                .texture("sides", modLoc("block/" + textureBase + "/right"))
                .texture("front", modLoc("block/" + textureBase + "/front"))
                .texture("west", modLoc("block/" + textureBase + "/right"))
                .texture("east", modLoc("block/" + textureBase + "/left"))
                .texture("south", modLoc("block/" + textureBase + "/back"))
                .texture("up", modLoc("block/" + textureBase + "/top"))
                .texture("down", modLoc("block/" + textureBase + "/bottom"));
        ModelFile activeModel = models().getBuilder(name + "_active")
                .parent(unchecked(modLoc("block/" + name)))
                .texture("front", modLoc("block/" + textureBase + "/front_active"));

        DirectionProperty facing = (DirectionProperty) block.getStateDefinition().getProperty("facing");
        BooleanProperty active = (BooleanProperty) block.getStateDefinition().getProperty("active");
        getVariantBuilder(block)
                .partialState().with(facing, Direction.NORTH).with(active, false)
                .modelForState().modelFile(model).addModel()
                .partialState().with(facing, Direction.SOUTH).with(active, false)
                .modelForState().modelFile(model).rotationY(180).addModel()
                .partialState().with(facing, Direction.EAST).with(active, false)
                .modelForState().modelFile(model).rotationY(90).addModel()
                .partialState().with(facing, Direction.WEST).with(active, false)
                .modelForState().modelFile(model).rotationY(270).addModel()
                .partialState().with(facing, Direction.NORTH).with(active, true)
                .modelForState().modelFile(activeModel).addModel()
                .partialState().with(facing, Direction.SOUTH).with(active, true)
                .modelForState().modelFile(activeModel).rotationY(180).addModel()
                .partialState().with(facing, Direction.EAST).with(active, true)
                .modelForState().modelFile(activeModel).rotationY(90).addModel()
                .partialState().with(facing, Direction.WEST).with(active, true)
                .modelForState().modelFile(activeModel).rotationY(270).addModel();
        simpleBlockItem(block, model);
    }

    private void machineSingleTexture(Block block, String name) {
        ModelFile model = models().cubeAll(name, modLoc("block/" + name));
        ModelFile activeModel = models().cubeAll(name + "_active", modLoc("block/" + name));

        DirectionProperty facing = (DirectionProperty) block.getStateDefinition().getProperty("facing");
        BooleanProperty active = (BooleanProperty) block.getStateDefinition().getProperty("active");
        getVariantBuilder(block)
                .partialState().with(facing, Direction.NORTH).with(active, false)
                .modelForState().modelFile(model).addModel()
                .partialState().with(facing, Direction.SOUTH).with(active, false)
                .modelForState().modelFile(model).rotationY(180).addModel()
                .partialState().with(facing, Direction.EAST).with(active, false)
                .modelForState().modelFile(model).rotationY(90).addModel()
                .partialState().with(facing, Direction.WEST).with(active, false)
                .modelForState().modelFile(model).rotationY(270).addModel()
                .partialState().with(facing, Direction.NORTH).with(active, true)
                .modelForState().modelFile(activeModel).addModel()
                .partialState().with(facing, Direction.SOUTH).with(active, true)
                .modelForState().modelFile(activeModel).rotationY(180).addModel()
                .partialState().with(facing, Direction.EAST).with(active, true)
                .modelForState().modelFile(activeModel).rotationY(90).addModel()
                .partialState().with(facing, Direction.WEST).with(active, true)
                .modelForState().modelFile(activeModel).rotationY(270).addModel();
        simpleBlockItem(block, model);
    }

    private void transcendentCable() {
        models().getBuilder("block/universal_cable/transcendent")
                .parent(unchecked(resource("mekanism", "block/transmitter/small/small")))
                .texture("side", modLoc("block/models/multipart/transcendent_universal_cable_vertical"))
                .texture("center_down", modLoc("block/models/multipart/transcendent_universal_cable"))
                .texture("side_opaque", modLoc("block/models/multipart/opaque/transcendent_universal_cable_vertical"))
                .texture("center_opaque", modLoc("block/models/multipart/transcendent_universal_cable"));
        ModelFile model = models().getBuilder("transcendent_universal_cable")
                .parent(unchecked(modLoc("block/universal_cable/transcendent")));
        simpleBlockWithItem(PsitweaksMekanismBlocks.TRANSCENDENT_CABLE.getBlock(), model);
    }

    private void energyCube(Block block, String name, String tier) {
        ModelFile model = models().getBuilder("block/energy_cube/" + tier)
                .parent(unchecked(resource("mekanism", "block/energy_cube/base")))
                .texture("corner", resource("mekanism", "block/models/energy_cube_" + tier + "_corner"));
        DirectionProperty facing = (DirectionProperty) block.getStateDefinition().getProperty("facing");
        getVariantBuilder(block)
                .partialState().with(facing, Direction.NORTH)
                .modelForState().modelFile(model).addModel()
                .partialState().with(facing, Direction.SOUTH)
                .modelForState().modelFile(model).rotationY(180).addModel()
                .partialState().with(facing, Direction.EAST)
                .modelForState().modelFile(model).rotationY(90).addModel()
                .partialState().with(facing, Direction.WEST)
                .modelForState().modelFile(model).rotationY(270).addModel();
        itemModels().getBuilder(name)
                .parent(unchecked("builtin/entity"))
                .transforms()
                .transform(ItemDisplayContext.GUI)
                .rotation(30, 315, 0)
                .scale(0.625F)
                .end()
                .transform(ItemDisplayContext.GROUND)
                .rotation(0, 90, 0)
                .translation(0, 2, 0)
                .scale(0.25F)
                .end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(75, 135, 0)
                .translation(0, 2.5F, 0)
                .scale(0.375F)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, 135, 0)
                .scale(0.4F)
                .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(75, 135, 0)
                .translation(0, 2.5F, 0)
                .scale(0.375F)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(0, 135, 0)
                .scale(0.4F)
                .end()
                .end();
    }

    private static net.minecraft.resources.ResourceLocation resource(String namespace, String path) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private static ModelFile unchecked(net.minecraft.resources.ResourceLocation location) {
        return new ModelFile.UncheckedModelFile(location);
    }

    private static ModelFile unchecked(String location) {
        return new ModelFile.UncheckedModelFile(location);
    }

    private net.minecraft.resources.ResourceLocation blockTexture(String name) {
        return switch (name) {
            case "ore_antinite" -> modLoc("block/antinite_ore");
            default -> modLoc("block/" + name);
        };
    }
}
