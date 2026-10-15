package com.moratan251.psitweaks.common.storage.connector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.moratan251.psitweaks.common.network.MessageConnectorTemplate;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSettingsSnapshot.Face;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSettingsSnapshot.Input;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSettingsSnapshot.Profile;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSettingsSnapshot.Slot;
import com.moratan251.psitweaks.common.storage.idea.FluidResourceKey;
import com.moratan251.psitweaks.common.storage.idea.ItemResourceKey;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Clipboard text for Ideaspace Connector settings (format version 1).
 *
 * <p>Settings are plain JSON. Item and fluid NBT stays SNBT strings, because converting NBT through JSON changes
 * number types and would turn NBT resources into different resources. Items keep their NBT ({@code nbt}) and Forge
 * capability data ({@code forge_caps}) apart, as the stack saves them. Parsing validates the whole text before anything
 * is applied; only resources this environment cannot use are emptied and counted. Text copied on 1.21.1 stores
 * data components ({@code components}), which have no NBT form here, so such resources are emptied and counted too;
 * its plain chemical IDs are accepted.
 */
public final class ConnectorSettingsClipboard {
    public static final String TYPE = "psitweaks:ideaspace_connector";
    public static final int VERSION = 1;
    public static final int MAX_LENGTH = 1 << 20;
    public static final int MAX_DETAIL_LENGTH = 256;
    private static final int SLOTS = IdeaspaceConnectorBlockEntity.SLOTS;
    private static final int TYPES = ConnectorExportSettings.TYPES;
    /** TagParser recurses without a limit; NBT network reads use the same 512 depth bound. */
    private static final int MAX_SNBT_DEPTH = 512;
    private static final int MAX_NUMBER_LENGTH = 40;
    private static final List<String> EXPORT_KEYS = List.of("item", "fluid", "chemical", "energy");
    private static final List<String> CHEMICAL_KINDS = List.of("gas", "infuse", "pigment", "slurry");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final TypeAdapter<JsonElement> JSON_ELEMENT = new Gson().getAdapter(JsonElement.class);

    private ConnectorSettingsClipboard() { }

    public enum Code {
        SUCCESS, EMPTY, TOO_LARGE, INVALID_JSON, NOT_CONNECTOR, NEWER_VERSION, INVALID_VALUE, NO_SETTINGS;

        public static Code byId(int id) {
            return id >= 0 && id < values().length ? values()[id] : INVALID_JSON;
        }
    }

    /** Settings exist only for {@link Code#SUCCESS}. The detail is the JSON path of an invalid value. */
    public record Result(Code code, @Nullable ConnectorSettingsSnapshot settings, int skipped, String detail) {
        static Result failure(Code code) {
            return new Result(code, null, 0, "");
        }
    }

    /** Reads the owner menu's state shape: {@code Selected}, {@code InputFilters} and the connection settings. */
    public static ConnectorSettingsSnapshot fromState(CompoundTag state) {
        List<Face> faces = faces(state.getIntArray("Sides"), state.getIntArray("Redstone"), state.getInt("Automatic"), 0);
        List<ConnectorExportSettings> export = export(state.getIntArray("ExportAmounts"), state.getIntArray("ExportIntervals"),
                state.getLongArray("ExportMinimums"), state.getLongArray("ExportTargets"), 0);
        ListTag selected = state.getList("Selected", Tag.TAG_COMPOUND);
        int overrides = state.getInt("SlotOverrides");
        int[] slotSides = state.getIntArray("SlotSides"), slotRedstone = state.getIntArray("SlotRedstone");
        int[] slotAutomatic = state.getIntArray("SlotAutomatic");
        int[] slotAmounts = state.getIntArray("SlotExportAmounts"), slotIntervals = state.getIntArray("SlotExportIntervals");
        long[] slotMinimums = state.getLongArray("SlotExportMinimums"), slotTargets = state.getLongArray("SlotExportTargets");
        List<Slot> slots = new ArrayList<>(SLOTS);
        for (int slot = 0; slot < SLOTS; slot++) {
            ConnectorResource resource = slot < selected.size()
                    ? ConnectorResource.load(selected.getCompound(slot)) : ConnectorResource.EMPTY;
            Profile individual = (overrides & (1 << slot)) == 0 ? null : new Profile(
                    faces(slotSides, slotRedstone, slot < slotAutomatic.length ? slotAutomatic[slot] : 0, slot * ConnectorSettingsSnapshot.FACES),
                    export(slotAmounts, slotIntervals, slotMinimums, slotTargets, slot * TYPES));
            slots.add(new Slot(resource, individual));
        }
        ListTag filterTags = state.getList("InputFilters", Tag.TAG_COMPOUND);
        List<ConnectorResource> filters = new ArrayList<>(SLOTS);
        for (int slot = 0; slot < SLOTS; slot++) {
            ConnectorResource filter = slot < filterTags.size()
                    ? ConnectorResource.load(filterTags.getCompound(slot)) : ConnectorResource.EMPTY;
            filters.add(filter.kind() == ConnectorResource.Kind.ENERGY ? ConnectorResource.EMPTY : filter);
        }
        return new ConnectorSettingsSnapshot(faces, export, slots,
                new Input(ConnectorInputMode.byId(state.getInt("InputMode")), filters));
    }

    private static List<Face> faces(int[] sides, int[] redstone, int automaticMask, int offset) {
        List<Face> faces = new ArrayList<>(ConnectorSettingsSnapshot.FACES);
        for (int face = 0; face < ConnectorSettingsSnapshot.FACES; face++) {
            int index = offset + face;
            faces.add(new Face(ConnectorSideMode.byId(index < sides.length ? sides[index] : 0),
                    (automaticMask & (1 << face)) != 0,
                    ConnectorRedstoneMode.byId(index < redstone.length ? redstone[index] : 0)));
        }
        return faces;
    }

    private static List<ConnectorExportSettings> export(int[] amounts, int[] intervals, long[] minimums, long[] targets, int offset) {
        List<ConnectorExportSettings> values = new ArrayList<>(TYPES);
        for (int type = 0; type < TYPES; type++)
            values.add(ConnectorExportSettings.read(type, amounts, intervals, minimums, targets, offset + type));
        return values;
    }

    public static String toJson(ConnectorSettingsSnapshot settings) {
        JsonObject root = new JsonObject();
        root.addProperty("type", TYPE);
        root.addProperty("version", VERSION);
        if (settings.faces() != null) root.add("faces", facesJson(settings.faces()));
        if (settings.export() != null) root.add("export", exportJson(settings.export()));
        if (settings.slots() != null) {
            JsonArray slots = new JsonArray();
            for (Slot slot : settings.slots()) {
                if (slot.resource().kind() == ConnectorResource.Kind.EMPTY && slot.individual() == null) {
                    slots.add(JsonNull.INSTANCE);
                    continue;
                }
                JsonObject entry = new JsonObject();
                entry.add("resource", resourceJson(slot.resource()));
                if (slot.individual() != null) {
                    JsonObject individual = new JsonObject();
                    individual.add("faces", facesJson(slot.individual().faces()));
                    individual.add("export", exportJson(slot.individual().export()));
                    entry.add("individual", individual);
                }
                slots.add(entry);
            }
            root.add("slots", slots);
        }
        if (settings.input() != null) {
            JsonObject input = new JsonObject();
            input.addProperty("mode", name(settings.input().mode()));
            JsonArray filters = new JsonArray();
            for (ConnectorResource filter : settings.input().filters()) filters.add(resourceJson(filter));
            input.add("filters", filters);
            root.add("input", input);
        }
        return GSON.toJson(root);
    }

    private static JsonObject facesJson(List<Face> faces) {
        JsonObject json = new JsonObject();
        for (Direction direction : Direction.values()) {
            Face face = faces.get(direction.ordinal());
            JsonObject entry = new JsonObject();
            entry.addProperty("mode", name(face.mode()));
            entry.addProperty("auto", face.automatic());
            entry.addProperty("redstone", name(face.redstone()));
            json.add(direction.getName(), entry);
        }
        return json;
    }

    private static JsonObject exportJson(List<ConnectorExportSettings> export) {
        JsonObject json = new JsonObject();
        for (int type = 0; type < TYPES; type++) {
            ConnectorExportSettings value = export.get(type);
            JsonObject entry = new JsonObject();
            entry.addProperty("amount", value.amount());
            entry.addProperty("interval", value.interval());
            entry.addProperty("minimum_stock", value.minimumStock());
            entry.add("target_stock", value.targetStock() < 0 ? JsonNull.INSTANCE : new JsonPrimitive(value.targetStock()));
            json.add(EXPORT_KEYS.get(type), entry);
        }
        return json;
    }

    private static JsonElement resourceJson(ConnectorResource resource) {
        JsonObject json = new JsonObject();
        switch (resource.kind()) {
            case EMPTY -> { return JsonNull.INSTANCE; }
            case ITEM -> {
                json.addProperty("kind", "item");
                if (resource.item().save() instanceof CompoundTag tag) {
                    json.addProperty("id", tag.getString("id"));
                    snbtProperty(json, "nbt", tag.get("tag"));
                    snbtProperty(json, "forge_caps", tag.get("ForgeCaps"));
                }
            }
            case FLUID -> {
                json.addProperty("kind", "fluid");
                if (resource.fluid().save() instanceof CompoundTag tag) {
                    json.addProperty("id", tag.getString("FluidName"));
                    snbtProperty(json, "nbt", tag.get("Tag"));
                }
            }
            case CHEMICAL -> {
                json.addProperty("kind", "chemical");
                json.addProperty("id", resource.chemical().toString());
            }
            case ENERGY -> json.addProperty("kind", "energy");
        }
        return json;
    }

    private static void snbtProperty(JsonObject json, String key, @Nullable Tag value) {
        if (value instanceof CompoundTag compound && !compound.isEmpty()) json.addProperty(key, compound.toString());
    }

    private static String name(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Parses clipboard text. {@code chemicalAvailable} decides whether a chemical ID can be used here,
     * so this class never touches Mekanism types.
     */
    public static Result parse(@Nullable String text, Predicate<ResourceLocation> chemicalAvailable) {
        if (text == null || text.isBlank()) return Result.failure(Code.EMPTY);
        if (text.length() > MAX_LENGTH) return Result.failure(Code.TOO_LARGE);
        JsonElement element;
        try {
            element = readStrict(text);
        } catch (IOException | RuntimeException e) {
            return Result.failure(Code.INVALID_JSON);
        }
        if (!(element instanceof JsonObject root) || !(root.get("type") instanceof JsonPrimitive type)
                || !type.isString() || !TYPE.equals(type.getAsString())) return Result.failure(Code.NOT_CONNECTOR);
        SettingsReader reader = new SettingsReader(chemicalAvailable);
        try {
            if (integer(root.get("version"), "version", 1, Long.MAX_VALUE) > VERSION) return Result.failure(Code.NEWER_VERSION);
            ConnectorSettingsSnapshot settings = new ConnectorSettingsSnapshot(
                    present(root, "faces") ? reader.faces(root.get("faces"), "faces") : null,
                    present(root, "export") ? reader.export(root.get("export"), "export") : null,
                    present(root, "slots") ? reader.slots(root.get("slots"), "slots") : null,
                    present(root, "input") ? reader.input(root.get("input"), "input") : null);
            if (settings.isEmpty()) return Result.failure(Code.NO_SETTINGS);
            return new Result(Code.SUCCESS, settings, reader.skipped, "");
        } catch (InvalidValue e) {
            String path = e.getMessage();
            return new Result(Code.INVALID_VALUE, null, 0,
                    path.length() > MAX_DETAIL_LENGTH ? path.substring(0, MAX_DETAIL_LENGTH) : path);
        }
    }

    /** Same strict syntax as vanilla data: one value and nothing after it. Gson builds the tree without recursion. */
    private static JsonElement readStrict(String text) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(text));
        reader.setLenient(false);
        JsonElement element = JSON_ELEMENT.read(reader);
        if (reader.peek() != JsonToken.END_DOCUMENT) throw new JsonParseException("Unexpected content after the JSON value");
        return element;
    }

    private static final class SettingsReader {
        private final Predicate<ResourceLocation> chemicalAvailable;
        private int skipped;

        private SettingsReader(Predicate<ResourceLocation> chemicalAvailable) {
            this.chemicalAvailable = chemicalAvailable;
        }

        /** Missing faces and fields take the values a new connector has. */
        private List<Face> faces(JsonElement element, String path) throws InvalidValue {
            JsonObject json = object(element, path);
            List<Face> faces = new ArrayList<>(ConnectorSettingsSnapshot.FACES);
            for (Direction direction : Direction.values()) {
                JsonElement value = json.get(direction.getName());
                if (value == null || value.isJsonNull()) {
                    faces.add(Face.DEFAULT);
                    continue;
                }
                String facePath = path + "." + direction.getName();
                JsonObject face = object(value, facePath);
                faces.add(new Face(choice(face, "mode", facePath, ConnectorSideMode.values(), ConnectorSideMode.BOTH),
                        bool(face, "auto", facePath),
                        choice(face, "redstone", facePath, ConnectorRedstoneMode.values(), ConnectorRedstoneMode.ALWAYS)));
            }
            return faces;
        }

        private List<ConnectorExportSettings> export(JsonElement element, String path) throws InvalidValue {
            JsonObject json = object(element, path);
            List<ConnectorExportSettings> values = new ArrayList<>(TYPES);
            for (int type = 0; type < TYPES; type++) {
                String key = EXPORT_KEYS.get(type), typePath = path + "." + key;
                ConnectorExportSettings fallback = ConnectorExportSettings.defaults(type);
                JsonElement value = json.get(key);
                if (value == null || value.isJsonNull()) {
                    values.add(fallback);
                    continue;
                }
                JsonObject entry = object(value, typePath);
                values.add(new ConnectorExportSettings(
                        (int) optionalInteger(entry, "amount", typePath, 1, ConnectorExportSettings.maximumAmount(type), fallback.amount()),
                        (int) optionalInteger(entry, "interval", typePath, 1, ConnectorExportSettings.MAX_INTERVAL, fallback.interval()),
                        optionalInteger(entry, "minimum_stock", typePath, 0, Long.MAX_VALUE, 0),
                        optionalInteger(entry, "target_stock", typePath, -1, Long.MAX_VALUE, -1)));
            }
            return values;
        }

        private List<Slot> slots(JsonElement element, String path) throws InvalidValue {
            JsonArray json = array(element, path);
            List<Slot> slots = new ArrayList<>(SLOTS);
            Set<ConnectorResource> published = new HashSet<>();
            for (int slot = 0; slot < SLOTS; slot++) {
                JsonElement value = slot < json.size() ? json.get(slot) : null;
                if (value == null || value.isJsonNull()) {
                    slots.add(Slot.EMPTY);
                    continue;
                }
                String slotPath = path + "[" + slot + "]";
                JsonObject entry = object(value, slotPath);
                ConnectorResource resource = resource(entry.get("resource"), slotPath + ".resource", true);
                // Same rule as loading a saved connector: the first copy of a published resource wins.
                if (resource.kind() != ConnectorResource.Kind.EMPTY && !published.add(resource)) {
                    skipped++;
                    resource = ConnectorResource.EMPTY;
                }
                Profile individual = present(entry, "individual") ? profile(entry.get("individual"), slotPath + ".individual") : null;
                slots.add(new Slot(resource, individual));
            }
            return slots;
        }

        private Profile profile(JsonElement element, String path) throws InvalidValue {
            JsonObject json = object(element, path);
            return new Profile(
                    present(json, "faces") ? faces(json.get("faces"), path + ".faces") : ConnectorSettingsSnapshot.defaultFaces(),
                    present(json, "export") ? export(json.get("export"), path + ".export") : ConnectorSettingsSnapshot.defaultExport());
        }

        private Input input(JsonElement element, String path) throws InvalidValue {
            JsonObject json = object(element, path);
            ConnectorInputMode mode = choice(json, "mode", path, ConnectorInputMode.values(), ConnectorInputMode.ANY);
            JsonArray list = present(json, "filters") ? array(json.get("filters"), path + ".filters") : new JsonArray();
            List<ConnectorResource> filters = new ArrayList<>(SLOTS);
            for (int slot = 0; slot < SLOTS; slot++) filters.add(slot < list.size()
                    ? resource(list.get(slot), path + ".filters[" + slot + "]", false) : ConnectorResource.EMPTY);
            return new Input(mode, filters);
        }

        private ConnectorResource resource(@Nullable JsonElement element, String path, boolean allowEnergy) throws InvalidValue {
            if (element == null || element.isJsonNull()) return ConnectorResource.EMPTY;
            JsonObject json = object(element, path);
            String kind = string(json, "kind", path).toLowerCase(Locale.ROOT);
            switch (kind) {
                case "item", "fluid" -> {
                    boolean item = kind.equals("item");
                    ResourceLocation id = id(json, path);
                    CompoundTag nbt = present(json, "nbt") ? snbt(json.get("nbt"), path + ".nbt") : null;
                    CompoundTag caps = item && present(json, "forge_caps") ? snbt(json.get("forge_caps"), path + ".forge_caps") : null;
                    // 1.21.1 data components cannot be turned into NBT; the resource would lose its identity.
                    if (present(json, "components")) return usable(Optional.empty());
                    CompoundTag tag = new CompoundTag();
                    if (item) {
                        tag.putString("id", id.toString());
                        tag.putByte("Count", (byte) 1);
                        if (nbt != null) tag.put("tag", nbt);
                        if (caps != null) tag.put("ForgeCaps", caps);
                    } else {
                        tag.putString("FluidName", id.toString());
                        tag.putInt("Amount", 1);
                        if (nbt != null) tag.put("Tag", nbt);
                    }
                    Optional<ConnectorResource> parsed;
                    try {
                        parsed = item ? ItemResourceKey.parse(tag).map(ConnectorResource::item)
                                : FluidResourceKey.parse(tag).map(ConnectorResource::fluid);
                    } catch (RuntimeException e) {
                        parsed = Optional.empty();
                    }
                    return usable(parsed);
                }
                case "chemical" -> {
                    return usable(chemicalKey(id(json, path)).map(ConnectorResource::chemical));
                }
                case "energy" -> {
                    if (!allowEnergy) throw new InvalidValue(path + ".kind");
                    return ConnectorResource.ENERGY;
                }
                default -> throw new InvalidValue(path + ".kind");
            }
        }

        /**
         * Chemical keys carry their Mekanism 10 kind (namespace:kind/path). Text copied on 1.21.1 names a chemical by
         * its plain registry ID, which means the first available kind in gas, infusion type, pigment, slurry order.
         */
        private Optional<ResourceLocation> chemicalKey(ResourceLocation id) {
            if (chemicalAvailable.test(id)) return Optional.of(id);
            if (id.getPath().indexOf('/') >= 0) return Optional.empty();
            for (String kind : CHEMICAL_KINDS) {
                ResourceLocation key = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), kind + "/" + id.getPath());
                if (chemicalAvailable.test(key)) return Optional.of(key);
            }
            return Optional.empty();
        }

        /** Unknown IDs, unreadable NBT and oversized templates depend on the environment, so only that slot is emptied. */
        private ConnectorResource usable(Optional<ConnectorResource> resource) {
            if (resource.isPresent() && resource.get().save().sizeInBytes() <= MessageConnectorTemplate.MAX_TEMPLATE_SIZE)
                return resource.get();
            skipped++;
            return ConnectorResource.EMPTY;
        }
    }

    private static boolean present(JsonObject json, String key) {
        JsonElement value = json.get(key);
        return value != null && !value.isJsonNull();
    }

    private static JsonObject object(JsonElement element, String path) throws InvalidValue {
        if (element instanceof JsonObject json) return json;
        throw new InvalidValue(path);
    }

    private static JsonArray array(JsonElement element, String path) throws InvalidValue {
        if (element instanceof JsonArray json && json.size() <= SLOTS) return json;
        throw new InvalidValue(path);
    }

    private static String string(JsonObject json, String key, String path) throws InvalidValue {
        if (json.get(key) instanceof JsonPrimitive value && value.isString()) return value.getAsString();
        throw new InvalidValue(path + "." + key);
    }

    private static ResourceLocation id(JsonObject json, String path) throws InvalidValue {
        ResourceLocation id = ResourceLocation.tryParse(string(json, "id", path));
        if (id == null) throw new InvalidValue(path + ".id");
        return id;
    }

    private static boolean bool(JsonObject json, String key, String path) throws InvalidValue {
        JsonElement value = json.get(key);
        if (value == null || value.isJsonNull()) return false;
        if (value instanceof JsonPrimitive primitive && primitive.isBoolean()) return primitive.getAsBoolean();
        throw new InvalidValue(path + "." + key);
    }

    private static <E extends Enum<E>> E choice(JsonObject json, String key, String path, E[] values, E fallback) throws InvalidValue {
        JsonElement value = json.get(key);
        if (value == null || value.isJsonNull()) return fallback;
        if (value instanceof JsonPrimitive primitive && primitive.isString())
            for (E candidate : values) if (candidate.name().equalsIgnoreCase(primitive.getAsString())) return candidate;
        throw new InvalidValue(path + "." + key);
    }

    private static long optionalInteger(JsonObject json, String key, String path, long minimum, long maximum, long fallback)
            throws InvalidValue {
        JsonElement value = json.get(key);
        return value == null || value.isJsonNull() ? fallback : integer(value, path + "." + key, minimum, maximum);
    }

    private static long integer(@Nullable JsonElement element, String path, long minimum, long maximum) throws InvalidValue {
        // Length guard first: converting a huge digit string to a number is quadratic.
        if (element instanceof JsonPrimitive primitive && primitive.isNumber()
                && primitive.getAsString().length() <= MAX_NUMBER_LENGTH) {
            try {
                long value = primitive.getAsBigDecimal().longValueExact();
                if (value >= minimum && value <= maximum) return value;
            } catch (ArithmeticException | NumberFormatException ignored) {
                // Fractions and out-of-range values are reported below.
            }
        }
        throw new InvalidValue(path);
    }

    private static CompoundTag snbt(JsonElement element, String path) throws InvalidValue {
        if (element instanceof JsonPrimitive primitive && primitive.isString()) {
            String text = primitive.getAsString();
            if (nestingDepth(text) <= MAX_SNBT_DEPTH) {
                try {
                    return TagParser.parseTag(text);
                } catch (CommandSyntaxException | RuntimeException ignored) {
                    // Reported as an invalid value below.
                }
            }
        }
        throw new InvalidValue(path);
    }

    /** Deepest bracket nesting outside SNBT quoted strings. */
    static int nestingDepth(String text) {
        int depth = 0, maximum = 0;
        char quote = 0;
        boolean escaped = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quote != 0) {
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == quote) quote = 0;
            } else if (c == '"' || c == '\'') {
                quote = c;
            } else if (c == '{' || c == '[') {
                maximum = Math.max(maximum, ++depth);
            } else if (c == '}' || c == ']') {
                depth--;
            }
        }
        return maximum;
    }

    /** Stack-trace-free signal carrying the JSON path of the first invalid value. */
    private static final class InvalidValue extends Exception {
        private InvalidValue(String path) {
            super(path, null, false, false);
        }
    }
}
