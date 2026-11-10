package com.moratan251.psitweaks.client.config;

import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.apache.commons.lang3.tuple.Pair;

public final class PsitweaksClientConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Client, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = pair.getLeft();
        CLIENT_SPEC = pair.getRight();
    }

    private PsitweaksClientConfig() {
    }

    public static final class Client {
        public final ModConfigSpec.BooleanValue cameraShake;
        public final ModConfigSpec.DoubleValue cameraShakeStrength;
        public final ModConfigSpec.BooleanValue barrierHud;
        public final ModConfigSpec.BooleanValue meteorLineNight;
        /** 領域の床の魔法陣の画質（使う画像の大きさの上限）。 */
        public final ModConfigSpec.EnumValue<ZoneCircleQuality> zoneCircleQuality;
        public final WindowPosition autonomousCasterSettingsWindow;

        private Client(ModConfigSpec.Builder builder) {
            builder.comment("Psitweaks client-only settings")
                    .push("spellProgrammer");

            builder.pop();

            builder.comment("Visual effects of combat spells")
                    .translation("psitweaks.configuration.effects")
                    .push("effects");

            cameraShake = builder
                    .comment("Shake the camera for ground shockwaves, meteor impacts and micro black hole collapses")
                    .translation("psitweaks.configuration.effects.camera_shake")
                    .define("cameraShake", true);

            cameraShakeStrength = builder
                    .comment("Strength of the camera shake (1.0 = default)")
                    .translation("psitweaks.configuration.effects.camera_shake_strength")
                    .defineInRange("cameraShakeStrength", 1.0, 0.0, 2.0);

            barrierHud = builder
                    .comment("Show the remaining shield and time of Trick: Barrier above the health bar")
                    .translation("psitweaks.configuration.effects.barrier_hud")
                    .define("barrierHud", true);

            meteorLineNight = builder
                    .comment("Darken the view while you stand in the night of Trick: Meteor Line")
                    .translation("psitweaks.configuration.effects.meteor_line_night")
                    .define("meteorLineNight", true);

            zoneCircleQuality = builder
                    .comment("Image quality of the magic circle on the floor of zone spellgrams: HIGH (1024 px), MEDIUM (512 px)",
                            "or LOW (256 px). Lower uses less video memory, and the lines look a little blurry up close.",
                            "Smaller images are still used when the circle looks small.")
                    .translation("psitweaks.configuration.effects.zone_circle_quality")
                    .defineEnum("zoneCircleQuality", ZoneCircleQuality.HIGH);

            builder.pop();

            builder.comment("Last positions of windows opened in machine screens, and whether they were pinned")
                    .translation("psitweaks.configuration.windows")
                    .push("windows");

            autonomousCasterSettingsWindow = new WindowPosition(builder, "autonomousCasterSettings", "autonomous_caster_settings",
                    "Autonomous Caster settings window");

            builder.pop();
        }
    }

    /**
     * 領域の床の魔法陣の画質: {@code TextureCircleRenderer} が画面に映る大きさで選ぶ画像の段の上限。低いほど VRAM が減り、
     * 近くで見たときの線が少しぼやける。小さく映るときは、これより小さい段を使う。
     */
    public enum ZoneCircleQuality implements TranslatableEnum {
        HIGH(1024),
        MEDIUM(512),
        LOW(256);

        private final int pixels;

        ZoneCircleQuality(int pixels) {
            this.pixels = pixels;
        }

        /** 上限の段の一辺（切り詰める前の画像の画素数）。 */
        public int pixels() {
            return pixels;
        }

        @Override
        public Component getTranslatedName() {
            return Component.translatable("psitweaks.configuration.effects.zone_circle_quality." + name().toLowerCase(Locale.ROOT));
        }
    }

    /**
     * Mekanism のウィンドウと同じく、閉じたときの位置とピン留めを覚える。ピン留めしたウィンドウは機械の画面を開くと一緒に開く。
     */
    public static final class WindowPosition {
        /** 位置をまだ覚えていないことを示す値。 */
        public static final int UNSET = Integer.MAX_VALUE;

        public final ModConfigSpec.IntValue x;
        public final ModConfigSpec.IntValue y;
        public final ModConfigSpec.BooleanValue pinned;

        private WindowPosition(ModConfigSpec.Builder builder, String path, String key, String name) {
            builder.comment(name)
                    .translation("psitweaks.configuration.windows." + key)
                    .push(path);

            x = builder
                    .comment("Last x position of the window, relative to the machine screen")
                    .translation("psitweaks.configuration.windows.x")
                    .defineInRange("x", UNSET, Integer.MIN_VALUE, Integer.MAX_VALUE);

            y = builder
                    .comment("Last y position of the window, relative to the machine screen")
                    .translation("psitweaks.configuration.windows.y")
                    .defineInRange("y", UNSET, Integer.MIN_VALUE, Integer.MAX_VALUE);

            pinned = builder
                    .comment("Open the window together with the machine screen")
                    .translation("psitweaks.configuration.windows.pinned")
                    .define("pinned", false);

            builder.pop();
        }

        /** 変わった値だけ書き換えて保存する。 */
        public void save(int newX, int newY, boolean newPinned) {
            boolean changed = false;
            if (x.get() != newX) {
                x.set(newX);
                changed = true;
            }
            if (y.get() != newY) {
                y.set(newY);
                changed = true;
            }
            if (pinned.get() != newPinned) {
                pinned.set(newPinned);
                changed = true;
            }
            if (changed) {
                CLIENT_SPEC.save();
            }
        }
    }
}
