package com.moratan251.psitweaks.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class PsitweaksConfig {

    // ===========================================
    // COMMON CONFIG（サーバー・クライアント両方で使用）
    // ===========================================
    public static class Common {
        public final ModConfigSpec.DoubleValue globalSpellPowerMultiplier;
        public final ModConfigSpec.BooleanValue requireSpellUnlocks;
        // `safeToPlayers` は server.properties の pvp 設定連動へ移行したため無効化。
        // public final ModConfigSpec.BooleanValue safeToPlayers;
        // 分子ディバイダー関連
        public final ModConfigSpec.DoubleValue molecularDividerDamageMultiplier;
        public final ModConfigSpec.DoubleValue phononMaserDamageMultiplier;
        public final ModConfigSpec.DoubleValue aquaCutterDamageMultiplier;
        public final ModConfigSpec.DoubleValue blazeBallDamageMultiplier;
        public final ModConfigSpec.DoubleValue activeAirMineDamageMultiplier;
        public final ModConfigSpec.DoubleValue flareCircleDamageMultiplier;
        public final ModConfigSpec.DoubleValue iceCircleDamageMultiplier;
        public final ModConfigSpec.DoubleValue radiationInjectionMultiplier;
        public final ModConfigSpec.DoubleValue guillotineDamageMultiplier;
        public final ModConfigSpec.LongValue gasBurningGeneratorEnergyCapacity;
        public final ModConfigSpec.LongValue psionicGeneratorEnergyCapacity;

        public Common(ModConfigSpec.Builder builder) {
            builder.comment("Psitweaks Common Configuration")
                    .push("spells"); // カテゴリ開始

            globalSpellPowerMultiplier = builder
                    .comment("Psitweaks攻撃スペル全体に適用する一律倍率",
                            "Global multiplier for all Psitweaks offensive spell damage")
                    .defineInRange("globalSpellPowerMultiplier", 1.0, 0.1, 1000000.0);

            // safeToPlayers = builder
            //         .comment("Trueにすると、Psitweaksの全攻撃スペルがプレイヤーをダメージ対象から除外します（フレンドリーファイア防止）",
            //                 "If true, all Psitweaks offensive spells will not damage players (friendly fire prevention)")
            //         .define("safeToPlayers", false);

            builder.comment("Spell unlock requirements")
                    .push("unlocks");

            requireSpellUnlocks = builder
                    .comment("特定スペルの利用に解禁を要求するかどうか",
                            "Whether configured spells require unlock progression in Spell Programmer")
                    .define("requireSpellUnlocks", true);

            builder.pop(); // unlocks カテゴリ終了

            // --- 分子ディバイダー ---
            builder.comment("Molecular Divider Settings")
                    .push("molecular_divider");

            molecularDividerDamageMultiplier = builder
                    .comment("分子ディバイダーのダメージ倍率",
                            "Damage multiplier for Molecular Divider")
                    .defineInRange("damageMultiplier", 1.0, 0.1, 1000000.0);

            builder.pop(); // molecular_divider カテゴリ終了

            // --- フォノンメーザー ---
            builder.comment("Phonon Maser Settings")
                    .push("phonon_maser");

            phononMaserDamageMultiplier = builder
                    .comment("フォノンメーザーのダメージ倍率", "Damage multiplier for Phonon Maser" )
                    .defineInRange("damageMultiplier", 1.0, 0.1, 1000000.0);

            builder.pop(); // phonon_maser カテゴリ終了

            // --- アクアカッター ---
            builder.comment("Aqua Cutter Settings")
                    .push("aqua_cutter");

            aquaCutterDamageMultiplier = builder
                    .comment("アクアカッターのダメージ倍率", "Damage multiplier for Aqua Cutter")
                    .defineInRange("damageMultiplier", 1.0, 0.1, 1000000.0);

            builder.pop(); // aqua_cutter カテゴリ終了

            // --- ブレイズボール ---
            builder.comment("Blaze Ball Settings")
                    .push("blaze_ball");

            blazeBallDamageMultiplier = builder
                    .comment("ブレイズボールのダメージ倍率", "Damage multiplier for Blaze Ball")
                    .defineInRange("damageMultiplier", 1.0, 0.1, 1000000.0);

            builder.pop(); // blaze_ball カテゴリ終了

            // --- 能動空中機雷 ---
            builder.comment("Active Air Mine Settings")
                    .push("active_air_mine");

            activeAirMineDamageMultiplier = builder
                    .comment("作動式: 能動空中機雷 のダメージ倍率",
                            "Damage multiplier for Trick: Active Air Mine")
                    .defineInRange("damageMultiplier", 1.0, 0.1, 1000000.0);

            builder.pop(); // active_air_mine カテゴリ終了

            // --- フレアサークル ---
            builder.comment("Flare Circle Settings")
                    .push("flare_circle");

            flareCircleDamageMultiplier = builder
                    .comment("作動式: フレアサークル の威力倍率（与ダメージ倍率）",
                            "Power (damage) multiplier for Trick: Flare Circle")
                    .defineInRange("damageMultiplier", 1.0, 0.1, 1000000.0);

            builder.pop(); // flare_circle カテゴリ終了

            // --- アイスサークル ---
            builder.comment("Ice Circle Settings")
                    .push("ice_circle");

            iceCircleDamageMultiplier = builder
                    .comment("作動式: アイスサークル の威力倍率（与ダメージ倍率）",
                            "Power (damage) multiplier for Trick: Ice Circle")
                    .defineInRange("damageMultiplier", 1.0, 0.1, 1000000.0);

            builder.pop(); // ice_circle カテゴリ終了

            // --- 放射線注入 ---
            builder.comment("Radiation Injection Settings")
                    .push("radiation_injection");

            radiationInjectionMultiplier = builder
                    .comment("作動式: 放射線注入 の被ばく量倍率",
                            "Radiation amount multiplier for Trick: Radiation Injection")
                    .defineInRange("radiationMultiplier", 1.0, 0.0, 1000000.0);

            builder.pop(); // radiation_injection カテゴリ終了

            // --- ギロチン ---
            builder.comment("Guillotine Settings")
                    .push("guillotine");

            guillotineDamageMultiplier = builder
                    .comment("作動式: ギロチン のダメージ倍率",
                            "Damage multiplier for Trick: Guillotine")
                    .defineInRange("damageMultiplier", 1.0, 0.1, 1000000.0);

            builder.pop(); // guillotine カテゴリ終了

            builder.pop(); // spells カテゴリ終了

            builder.comment("Mekanism Integration Settings")
                    .push("mekanism");

            gasBurningGeneratorEnergyCapacity = builder
                    .comment("ガス燃焼発電機の内部エネルギー容量オーバーライド（J）",
                            "-1に設定するとMekanismのデフォルト動作（HydrogenEnergyDensityを基準）を使用します。",
                            "Gas-Burning Generator internal energy capacity override in Joules.",
                            "Set to -1 to use Mekanism default behavior (HydrogenEnergyDensity based).")
                    .defineInRange("gasBurningGeneratorEnergyCapacity", 2000000, -1L, Long.MAX_VALUE);

            psionicGeneratorEnergyCapacity = builder
                    .comment("サイリンク発電機の内部エネルギー容量（J）",
                            "Internal energy capacity for the Psi-Link Generator in Joules.")
                    .defineInRange("psionicGeneratorEnergyCapacity", 1600000, 1L, Long.MAX_VALUE);

            builder.pop(); // mekanism カテゴリ終了
        }
    }

    // ===========================================
    // SERVER CONFIG（サーバーのみ、ワールドごとに保存）
    // ===========================================
    /*
    public static class Server {

        public Server(ModConfigSpec.Builder builder) {
            builder.comment("Psitweaks Server Configuration")
                    .push("server");
            builder.pop();
        }
    }

    // ===========================================
    // CLIENT CONFIG（クライアントのみ、描画関連など）
    // ===========================================
    public static class Client {
        public Client(ModConfigSpec.Builder builder) {
            builder.comment("Psitweaks Client Configuration")
                    .push("client");

            builder.pop();
        }
    }

     */

    // ===========================================
    // 静的フィールドとビルダー
    // ===========================================

    // Common Config
    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        final Pair<Common, ModConfigSpec> commonPair = new ModConfigSpec.Builder()
                .configure(Common::new);
        COMMON_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();
    }

  /*  // Server Config
    public static final ModConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        final Pair<Server, ModConfigSpec> serverPair = new ModConfigSpec.Builder()
                .configure(Server::new);
        SERVER_SPEC = serverPair.getRight();
        SERVER = serverPair.getLeft();
    }

    // Client Config
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        final Pair<Client, ModConfigSpec> clientPair = new ModConfigSpec.Builder()
                .configure(Client::new);
        CLIENT_SPEC = clientPair.getRight();
        CLIENT = clientPair.getLeft();
    }

   */
}
