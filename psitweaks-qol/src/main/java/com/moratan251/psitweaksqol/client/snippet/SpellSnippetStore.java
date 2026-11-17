package com.moratan251.psitweaksqol.client.snippet;

import com.moratan251.psitweaksqol.PsitweaksQol;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

/**
 * スニペットをゲームフォルダの {@code psitweaks_qol/snippets/} に 1 件 1 ファイル（SNBT）で読み書きする。
 * 中身は Psi の術式の書き出しと同じ形（{@code validSpell}・{@code spellName}・{@code modsRequired}・{@code spellList}）に
 * {@code psitweaksSnippet} を足したもので、そのまま Psi の「クリップボードからインポート」にも読ませられる。
 */
public final class SpellSnippetStore {
    public static final int MAX_NAME_LENGTH = SpellSnippetNames.MAX_NAME_LENGTH;
    /** 1 つのファイルの大きさの上限。9×9 のピースすべてに長いコメントを付けても収まる。 */
    static final long MAX_FILE_BYTES = 1L << 20;
    static final String EXTENSION = SpellSnippetNames.EXTENSION;
    static final int FORMAT_VERSION = 1;

    private static final String TAG_VALID = "validSpell";
    private static final String TAG_NAME = "spellName";
    private static final String TAG_MODS_REQUIRED = "modsRequired";
    private static final String TAG_MOD_NAME = "modName";
    private static final String TAG_MOD_VERSION = "modVersion";
    private static final String TAG_LIST = "spellList";
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_DATA = "data";
    private static final String TAG_SNIPPET = "psitweaksSnippet";
    private static final String TAG_FORMAT = "format";

    private SpellSnippetStore() {
    }

    public static Path directory() {
        return FMLPaths.GAMEDIR.get().resolve(PsitweaksQol.MOD_ID).resolve("snippets");
    }

    /** 保存済みのスニペットを名前順（大文字と小文字は区別しない）で返す。読めないファイルは飛ばしてログに書く。 */
    public static List<SpellSnippet> loadAll() {
        Path directory = directory();
        if (!Files.isDirectory(directory)) {
            return List.of();
        }

        List<SpellSnippet> snippets = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*" + EXTENSION)) {
            for (Path file : stream) {
                load(file).ifPresent(snippets::add);
            }
        } catch (IOException e) {
            PsitweaksQol.LOGGER.warn("Failed to list spell snippets in {}", directory, e);
        }
        snippets.sort(Comparator.comparing((SpellSnippet snippet) -> snippet.name().toLowerCase(Locale.ROOT))
                .thenComparing(SpellSnippet::name));
        return List.copyOf(snippets);
    }

    private static Optional<SpellSnippet> load(Path file) {
        String fileName = file.getFileName().toString();
        try {
            if (!Files.isRegularFile(file) || Files.size(file) > MAX_FILE_BYTES) {
                PsitweaksQol.LOGGER.warn("Skipped spell snippet {}: not a regular file or too large", file);
                return Optional.empty();
            }
            CompoundTag tag = TagParser.parseTag(Files.readString(file, StandardCharsets.UTF_8));
            return Optional.of(fromTag(tag, fileName));
        } catch (IOException | CommandSyntaxException | RuntimeException e) {
            PsitweaksQol.LOGGER.warn("Skipped unreadable spell snippet {}", file, e);
            return Optional.empty();
        }
    }

    /** 同じ名前（同じファイル名になる名前）のスニペットがすでにあるか。 */
    public static boolean exists(String name) {
        return Files.exists(directory().resolve(fileNameFor(name)));
    }

    /** 名前のファイルへ書く。同じファイルがあれば置き換える。 */
    public static SpellSnippet save(String name, SpellPieceLayout layout) throws IOException {
        String validName = validateName(name)
                .orElseThrow(() -> new IllegalArgumentException("Invalid spell snippet name: " + name));
        if (layout.isEmpty()) {
            throw new IllegalArgumentException("Spell snippet has no pieces");
        }

        Path directory = directory();
        Files.createDirectories(directory);
        String fileName = fileNameFor(validName);
        Path target = directory.resolve(fileName);
        Path temporary = directory.resolve(fileName + ".tmp");
        String text = new SnbtPrinterTagVisitor().visit(toTag(validName, layout, SpellSnippetStore::modVersion));
        Files.writeString(temporary, text + System.lineSeparator(), StandardCharsets.UTF_8);
        try {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return new SpellSnippet(validName, layout, fileName);
    }

    public static void delete(SpellSnippet snippet) throws IOException {
        if (snippet.fileName() == null) {
            return;
        }
        Files.deleteIfExists(directory().resolve(snippet.fileName()));
    }

    /** 前後の空白を除いた名前。空、長すぎる、制御文字を含む名前は受け付けない。 */
    public static Optional<String> validateName(String name) {
        return SpellSnippetNames.validateName(name);
    }

    static String fileNameFor(String name) {
        return SpellSnippetNames.fileNameFor(name);
    }

    static CompoundTag toTag(String name, SpellPieceLayout layout, Function<String, Optional<String>> modVersion) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_VALID, true);
        tag.putString(TAG_NAME, name);

        Set<String> namespaces = new TreeSet<>();
        ListTag list = new ListTag();
        for (SpellPieceLayout.Entry entry : layout.entries()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt(TAG_X, entry.offsetX());
            entryTag.putInt(TAG_Y, entry.offsetY());
            entryTag.put(TAG_DATA, entry.pieceData().copy());
            list.add(entryTag);

            String key = SpellPieceLayout.pieceKey(entry.pieceData());
            int colon = key.indexOf(':');
            namespaces.add(colon < 0 ? "minecraft" : key.substring(0, colon));
        }

        ListTag mods = new ListTag();
        for (String namespace : namespaces) {
            CompoundTag mod = new CompoundTag();
            mod.putString(TAG_MOD_NAME, namespace);
            modVersion.apply(namespace).ifPresent(version -> mod.putString(TAG_MOD_VERSION, version));
            mods.add(mod);
        }
        tag.put(TAG_MODS_REQUIRED, mods);
        tag.put(TAG_LIST, list);

        CompoundTag snippet = new CompoundTag();
        snippet.putInt(TAG_FORMAT, FORMAT_VERSION);
        tag.put(TAG_SNIPPET, snippet);
        return tag;
    }

    /**
     * ファイルの中身からスニペットを作る。名前が空なら拡張子を除いたファイル名を使う。
     * 座標が盤面の外のもの、ピースの ID がないものは読み込みを取りやめる。
     */
    static SpellSnippet fromTag(CompoundTag tag, String fileName) {
        ListTag list = tag.getList(TAG_LIST, Tag.TAG_COMPOUND);
        List<SpellPieceLayout.Entry> entries = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            int x = entryTag.getInt(TAG_X);
            int y = entryTag.getInt(TAG_Y);
            if (x < 0 || y < 0 || x >= SpellPieceLayout.GRID_SIZE || y >= SpellPieceLayout.GRID_SIZE) {
                throw new IllegalArgumentException("Spell piece position out of grid: " + x + ", " + y);
            }
            entries.add(new SpellPieceLayout.Entry(x, y, entryTag.getCompound(TAG_DATA).copy()));
        }

        SpellPieceLayout layout = SpellPieceLayout.of(entries);
        if (layout.isEmpty()) {
            throw new IllegalArgumentException("Spell snippet has no pieces");
        }

        String name = validateName(tag.getString(TAG_NAME))
                .orElseGet(() -> SpellSnippetNames.nameFromFileName(fileName));
        return new SpellSnippet(name, layout, fileName);
    }

    private static Optional<String> modVersion(String namespace) {
        return ModList.get().getModContainerById(namespace)
                .map(container -> container.getModInfo().getVersion().toString());
    }
}
