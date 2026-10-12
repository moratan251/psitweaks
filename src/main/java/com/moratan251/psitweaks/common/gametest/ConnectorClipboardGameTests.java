package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.network.MessageConnectorPaste;
import com.moratan251.psitweaks.common.network.MessageConnectorPasteResult;
import com.moratan251.psitweaks.common.storage.connector.*;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSettingsClipboard.Code;
import com.moratan251.psitweaks.common.storage.idea.FluidResourceKey;
import com.moratan251.psitweaks.common.storage.idea.ItemResourceKey;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ConnectorClipboardGameTests {
    private static final ResourceLocation HYDROGEN = ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen");

    private static IdeaspaceConnectorBlockEntity place(GameTestHelper helper, BlockPos relative, UUID owner) {
        helper.setBlock(relative, PsitweaksBlocks.IDEASPACE_CONNECTOR.get());
        var connector = (IdeaspaceConnectorBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
        connector.initialize(owner, ItemStack.EMPTY);
        return connector;
    }

    private static ConnectorResource item(ItemStack stack) {
        return ConnectorResource.item(ItemResourceKey.of(stack).orElseThrow());
    }

    private static ConnectorResource fluid(FluidStack stack) {
        return ConnectorResource.fluid(FluidResourceKey.of(stack).orElseThrow());
    }

    /** Int and long custom data would become other NBT types if components were converted through JSON numbers. */
    private static ItemStack taggedApple() {
        ItemStack stack = new ItemStack(Items.APPLE);
        CompoundTag data = new CompoundTag();
        data.putInt("Level", 5);
        data.putLong("Big", 5_000_000_000L);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Apple \"double\" 'single' {brace} [bracket] \\ slash"));
        return stack;
    }

    private static ItemStack damagedSword() {
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        stack.set(DataComponents.DAMAGE, 5);
        return stack;
    }

    private static ConnectorExportSettings[] defaultExport() {
        var values = new ConnectorExportSettings[ConnectorExportSettings.TYPES];
        for (int type = 0; type < values.length; type++) values[type] = ConnectorExportSettings.defaults(type);
        return values;
    }

    private static String copy(GameTestHelper helper, IdeaspaceConnectorBlockEntity connector) {
        var registries = helper.getLevel().registryAccess();
        return ConnectorSettingsClipboard.toJson(
                ConnectorSettingsClipboard.fromState(connector.writeSettingsState(registries), registries), registries);
    }

    private static ConnectorSettingsClipboard.Result parse(GameTestHelper helper, String text) {
        return ConnectorSettingsClipboard.parse(text, helper.getLevel().registryAccess(), IdeaspaceConnectorMenu::chemicalAvailable);
    }

    /** Backticks stand for double quotes so the JSON stays readable in Java strings. */
    private static String clip(String sections) {
        return ("{`type`: `psitweaks:ideaspace_connector`, `version`: 1" + (sections.isEmpty() ? "" : ", " + sections) + "}")
                .replace('`', '"');
    }

    @GameTest(template = "connector_empty")
    public static void copiedSettingsRoundTripExactly(GameTestHelper helper) {
        var source = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        UUID targetOwner = UUID.randomUUID();
        var target = place(helper, new BlockPos(2, 1, 1), targetOwner);
        var apple = item(taggedApple());
        var sword = item(damagedSword());
        var water = fluid(new FluidStack(Fluids.WATER, 1));
        var lava = fluid(new FluidStack(Fluids.LAVA, 1));
        source.setResource(0, apple);
        source.setResource(2, water);
        source.setResource(4, ConnectorResource.ENERGY);
        if (MekanismCompat.isMekanismLoaded()) source.setResource(5, ConnectorResource.chemical(HYDROGEN));
        source.setSideMode(Direction.NORTH, ConnectorSideMode.INPUT);
        source.setAutomatic(Direction.EAST, true);
        source.setRedstoneMode(-1, Direction.SOUTH, ConnectorRedstoneMode.HIGH);
        var common = defaultExport();
        common[0] = new ConnectorExportSettings(12, 40, 100, 5_000_000_000L);
        common[3] = new ConnectorExportSettings(Integer.MAX_VALUE, ConnectorExportSettings.MAX_INTERVAL, 7, -1);
        helper.assertTrue(source.setExportSettings(-1, common), "Common rates rejected");
        source.setUsesCommonSettings(2, false);
        source.setSideMode(2, Direction.UP, ConnectorSideMode.OUTPUT);
        source.setAutomatic(2, Direction.UP, true);
        source.setRedstoneMode(2, Direction.DOWN, ConnectorRedstoneMode.LOW);
        var individual = defaultExport();
        individual[1] = new ConnectorExportSettings(250, 3, 0, 1000);
        helper.assertTrue(source.setExportSettings(2, individual), "Individual rates rejected");
        source.setUsesCommonSettings(7, false);
        source.setInputMode(ConnectorInputMode.ALLOW_LIST);
        source.setInputFilter(0, sword);
        source.setInputFilter(3, lava);

        String json = copy(helper, source);
        var result = parse(helper, json);
        helper.assertTrue(result.code() == Code.SUCCESS && result.skipped() == 0 && result.settings() != null,
                "Copied settings did not parse cleanly: " + result.code() + " " + result.detail());
        target.applySettings(result.settings());
        helper.assertTrue(copy(helper, target).equals(json), "Pasting changed the copied settings:\n" + json + "\n---\n" + copy(helper, target));
        helper.assertTrue(target.resource(0).equals(apple) && target.inputFilter(0).equals(sword) && target.resource(2).equals(water),
                "Component data did not survive the clipboard");
        helper.assertTrue(target.sideMode(Direction.NORTH) == ConnectorSideMode.INPUT && target.automatic(Direction.EAST)
                && target.redstoneMode(-1, Direction.SOUTH) == ConnectorRedstoneMode.HIGH, "Shared faces were not pasted");
        helper.assertTrue(target.exportSettings(0, 0).equals(common[0]) && target.exportSettings(0, 3).equals(common[3]),
                "Shared rates or stock rules were not pasted");
        helper.assertTrue(!target.usesCommonSettings(2) && target.sideMode(2, Direction.UP) == ConnectorSideMode.OUTPUT
                && target.automatic(2, Direction.UP) && target.redstoneMode(2, Direction.DOWN) == ConnectorRedstoneMode.LOW
                && target.exportSettings(2, 1).equals(individual[1]), "Individual slot settings were not pasted");
        helper.assertTrue(!target.usesCommonSettings(7) && target.resource(7).kind() == ConnectorResource.Kind.EMPTY,
                "An individual profile on an empty slot was lost");
        helper.assertTrue(target.inputMode() == ConnectorInputMode.ALLOW_LIST && target.passesInputFilter(sword)
                && target.passesInputFilter(lava) && !target.passesInputFilter(apple), "Pasted input filter does not filter");
        helper.assertTrue(targetOwner.equals(target.owner()), "Pasting changed the owner");
        if (MekanismCompat.isMekanismLoaded())
            helper.assertTrue(target.resource(5).equals(ConnectorResource.chemical(HYDROGEN)), "Chemical slot was not pasted");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void pasteReplacesOnlyPresentSections(GameTestHelper helper) {
        var connector = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        var apple = item(new ItemStack(Items.APPLE));
        connector.setResource(0, apple);
        connector.setSideMode(Direction.WEST, ConnectorSideMode.DISABLED);
        var rates = defaultExport();
        rates[2] = new ConnectorExportSettings(77, 9);
        connector.setExportSettings(-1, rates);

        var result = parse(helper, clip("`input`: {`mode`: `ALLOW_LIST`, `filters`: [{`kind`: `item`, `id`: `minecraft:carrot`}]}"));
        helper.assertTrue(result.code() == Code.SUCCESS, "Input-only clipboard rejected: " + result.code() + " " + result.detail());
        connector.applySettings(result.settings());
        helper.assertTrue(connector.inputMode() == ConnectorInputMode.ALLOW_LIST
                && connector.inputFilter(0).equals(item(new ItemStack(Items.CARROT)))
                && connector.inputFilter(1).kind() == ConnectorResource.Kind.EMPTY, "Input section was not applied");
        helper.assertTrue(connector.resource(0).equals(apple) && connector.sideMode(Direction.WEST) == ConnectorSideMode.DISABLED
                && connector.exportSettings(0, 2).amount() == 77, "Sections missing from the clipboard changed");

        result = parse(helper, clip("`faces`: {`north`: {`mode`: `output`}}, `export`: {`item`: {`amount`: 3}}"));
        helper.assertTrue(result.code() == Code.SUCCESS, "Partial sections rejected: " + result.code() + " " + result.detail());
        connector.applySettings(result.settings());
        helper.assertTrue(connector.sideMode(Direction.NORTH) == ConnectorSideMode.OUTPUT && !connector.automatic(Direction.NORTH)
                && connector.sideMode(Direction.WEST) == ConnectorSideMode.BOTH, "A present faces section must replace all faces");
        helper.assertTrue(connector.exportSettings(0, 0).amount() == 3 && connector.exportSettings(0, 0).interval() == 5
                && connector.exportSettings(0, 2).equals(ConnectorExportSettings.defaults(2)), "Missing export fields must use defaults");
        helper.assertTrue(connector.resource(0).equals(apple) && connector.inputMode() == ConnectorInputMode.ALLOW_LIST,
                "Slots or input changed without their sections");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void invalidClipboardChangesNothing(GameTestHelper helper) {
        var owner = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "clipboard-invalid"));
        var connector = place(helper, new BlockPos(1, 1, 1), owner.getUUID());
        owner.setPos(Vec3.atCenterOf(connector.getBlockPos()));
        connector.setResource(3, item(new ItemStack(Items.APPLE)));
        connector.setInputMode(ConnectorInputMode.DENY_LIST);
        String before = copy(helper, connector);
        long version = connector.settingsVersion();
        String deepSnbt = "{a:".repeat(600) + "1" + "}".repeat(600);
        Object[][] cases = {
                {"", Code.EMPTY, ""},
                {"   \n", Code.EMPTY, ""},
                {"x".repeat(ConnectorSettingsClipboard.MAX_LENGTH + 1), Code.TOO_LARGE, ""},
                {"{", Code.INVALID_JSON, ""},
                {"{} {}", Code.INVALID_JSON, ""},
                {"{'type': 'psitweaks:ideaspace_connector', 'version': 1, 'faces': {}}", Code.INVALID_JSON, ""},
                {"[]", Code.NOT_CONNECTOR, ""},
                {"{\"version\": 1, \"faces\": {}}", Code.NOT_CONNECTOR, ""},
                {"{\"type\": \"minecraft:chest\", \"version\": 1, \"faces\": {}}", Code.NOT_CONNECTOR, ""},
                {clip("").replace("1}", "2, `faces`: {}}".replace('`', '"')), Code.NEWER_VERSION, ""},
                {clip("").replace("1}", "\"1\", \"faces\": {}}"), Code.INVALID_VALUE, "version"},
                {clip(""), Code.NO_SETTINGS, ""},
                {clip("`faces`: null"), Code.NO_SETTINGS, ""},
                {clip("`faces`: {`north`: {`mode`: `sideways`}}"), Code.INVALID_VALUE, "faces.north.mode"},
                {clip("`faces`: {`up`: {`auto`: 1}}"), Code.INVALID_VALUE, "faces.up.auto"},
                {clip("`faces`: []"), Code.INVALID_VALUE, "faces"},
                {clip("`export`: {`item`: {`amount`: 65}}"), Code.INVALID_VALUE, "export.item.amount"},
                {clip("`export`: {`fluid`: {`interval`: 1.5}}"), Code.INVALID_VALUE, "export.fluid.interval"},
                {clip("`export`: {`chemical`: {`amount`: 1e400}}"), Code.INVALID_VALUE, "export.chemical.amount"},
                {clip("`export`: {`energy`: {`minimum_stock`: " + "9".repeat(60) + "}}"), Code.INVALID_VALUE, "export.energy.minimum_stock"},
                {clip("`export`: {`energy`: {`target_stock`: -2}}"), Code.INVALID_VALUE, "export.energy.target_stock"},
                {clip("`slots`: [null, null, null, null, null, null, null, null, null, null]"), Code.INVALID_VALUE, "slots"},
                {clip("`slots`: [{`resource`: {`kind`: `gas`, `id`: `minecraft:air`}}]"), Code.INVALID_VALUE, "slots[0].resource.kind"},
                {clip("`slots`: [null, {`resource`: {`kind`: `item`, `id`: `Not Valid`}}]"), Code.INVALID_VALUE, "slots[1].resource.id"},
                {clip("`slots`: [{`resource`: {`kind`: `item`, `id`: `minecraft:apple`, `components`: `{unclosed`}}]"),
                        Code.INVALID_VALUE, "slots[0].resource.components"},
                {clip("`slots`: [{`resource`: {`kind`: `item`, `id`: `minecraft:apple`, `components`: `" + deepSnbt + "`}}]"),
                        Code.INVALID_VALUE, "slots[0].resource.components"},
                {clip("`slots`: [{`resource`: null, `individual`: {`faces`: {`east`: {`redstone`: `sometimes`}}}}]"),
                        Code.INVALID_VALUE, "slots[0].individual.faces.east.redstone"},
                {clip("`input`: {`mode`: `everything`}"), Code.INVALID_VALUE, "input.mode"},
                {clip("`input`: {`filters`: [{`kind`: `energy`}]}"), Code.INVALID_VALUE, "input.filters[0].kind"},
                // A valid section never applies when a later one is invalid.
                {clip("`input`: {`mode`: `any`}, `faces`: {`north`: {`mode`: `bad`}}"), Code.INVALID_VALUE, "faces.north.mode"},
        };
        for (Object[] testCase : cases) {
            var result = parse(helper, (String) testCase[0]);
            helper.assertTrue(result.code() == testCase[1] && result.detail().equals(testCase[2]) && result.settings() == null,
                    "Unexpected result " + result.code() + " '" + result.detail() + "' for case expecting " + testCase[1] + " " + testCase[2]);
        }
        var menu = new IdeaspaceConnectorMenu(33, owner.getInventory(), connector);
        menu.handlePaste(owner, new MessageConnectorPaste(33, menu.session(), (String) cases[cases.length - 1][0]));
        helper.assertTrue(connector.settingsVersion() == version && copy(helper, connector).equals(before),
                "A rejected paste changed the connector");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void unusableResourcesAreEmptiedAndCounted(GameTestHelper helper) {
        var connector = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        String apple = "{`resource`: {`kind`: `item`, `id`: `minecraft:apple`}}";
        String huge = "{'minecraft:custom_data':{big:'" + "x".repeat(70_000) + "'}}";
        var result = parse(helper, clip("`slots`: ["
                + "{`resource`: {`kind`: `item`, `id`: `psitweaks:no_such_item`}}, "
                + apple + ", "
                + "{`resource`: {`kind`: `chemical`, `id`: `mekanism:no_such_chemical`}}, "
                + apple + ", "
                + "{`resource`: {`kind`: `item`, `id`: `minecraft:air`}}, "
                + "{`resource`: {`kind`: `fluid`, `id`: `minecraft:empty`}}, "
                + "{`resource`: {`kind`: `item`, `id`: `minecraft:stone`, `components`: `{'psitweaks:no_such_component':1}`}}, "
                + "{`resource`: {`kind`: `item`, `id`: `minecraft:paper`, `components`: `" + huge + "`}}, "
                + "{`resource`: {`kind`: `energy`}}], "
                + "`input`: {`mode`: `allow_list`, `filters`: [{`kind`: `fluid`, `id`: `minecraft:no_such_fluid`}, "
                + "{`kind`: `fluid`, `id`: `minecraft:water`}]}"));
        helper.assertTrue(result.code() == Code.SUCCESS && result.skipped() == 8,
                "Expected 8 unusable resources, got " + result.code() + " " + result.skipped() + " " + result.detail());
        connector.applySettings(result.settings());
        var expectedApple = item(new ItemStack(Items.APPLE));
        for (int slot = 0; slot < IdeaspaceConnectorBlockEntity.SLOTS; slot++) {
            ConnectorResource expected = slot == 1 ? expectedApple : slot == 8 ? ConnectorResource.ENERGY : ConnectorResource.EMPTY;
            helper.assertTrue(connector.resource(slot).equals(expected), "Slot " + slot + " should be " + expected.kind());
        }
        helper.assertTrue(connector.inputFilter(0).kind() == ConnectorResource.Kind.EMPTY
                && connector.inputFilter(1).equals(fluid(new FluidStack(Fluids.WATER, 1))), "Unusable filter was not emptied alone");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void pastePayloadRequiresOwnerSession(GameTestHelper helper) {
        var owner = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "clipboard-owner"));
        var connector = place(helper, new BlockPos(1, 1, 1), owner.getUUID());
        owner.setPos(Vec3.atCenterOf(connector.getBlockPos()));
        var menu = new IdeaspaceConnectorMenu(31, owner.getInventory(), connector);
        String json = clip("`input`: {`mode`: `deny_list`}");
        MessageConnectorPaste decoded;
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), owner.registryAccess());
        try {
            MessageConnectorPaste.STREAM_CODEC.encode(buffer, new MessageConnectorPaste(31, menu.session(), json));
            decoded = MessageConnectorPaste.STREAM_CODEC.decode(buffer);
        } finally { buffer.release(); }
        helper.assertTrue(decoded.text().equals(json), "Paste payload changed the clipboard text");

        long version = connector.settingsVersion();
        var outsider = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "clipboard-outsider"));
        outsider.setPos(owner.position());
        menu.handlePaste(outsider, decoded);
        menu.handlePaste(owner, new MessageConnectorPaste(31, UUID.randomUUID(), json));
        menu.handlePaste(owner, new MessageConnectorPaste(32, menu.session(), json));
        helper.assertTrue(connector.settingsVersion() == version && connector.inputMode() == ConnectorInputMode.ANY,
                "Paste accepted from another player, session or container");
        menu.handlePaste(owner, decoded);
        helper.assertTrue(connector.inputMode() == ConnectorInputMode.DENY_LIST, "Owner paste was not applied");

        var report = new MessageConnectorPasteResult(31, Code.INVALID_VALUE, 3, "slots[0].resource.id");
        buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), owner.registryAccess());
        try {
            MessageConnectorPasteResult.STREAM_CODEC.encode(buffer, report);
            helper.assertTrue(MessageConnectorPasteResult.STREAM_CODEC.decode(buffer).equals(report), "Paste result changed in transit");
        } finally { buffer.release(); }

        helper.getLevel().destroyBlock(connector.getBlockPos(), false);
        long removedVersion = connector.settingsVersion();
        menu.handlePaste(owner, new MessageConnectorPaste(31, menu.session(), clip("`input`: {`mode`: `any`}")));
        helper.assertTrue(connector.settingsVersion() == removedVersion, "Removed connector accepted a paste");
        helper.succeed();
    }
}
