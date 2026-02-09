package com.moratan251.psitweaks.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class PsitweaksConfig {

    // ===========================================
    // COMMON CONFIG（サーバー・クライアント両方で使用）
    // ===========================================
    public static class Common {
        public final ForgeConfigSpec.DoubleValue globalSpellPowerMultiplier;
        // 分子ディバイダー関連
        public final ForgeConfigSpec.DoubleValue molecularDividerDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue phononMaserDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue aquaCutterDamageMultiplier;
        public final ForgeConfigSpec.LongValue gasBurningGeneratorEnergyCapacity;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Psitweaks Common Configuration")
                    .push("spells"); // カテゴリ開始

            globalSpellPowerMultiplier = builder
                    .comment("Psitweaks攻撃スペル全体に適用する一律倍率",
                            "Global multiplier for all Psitweaks offensive spell damage")
                    .defineInRange("globalSpellPowerMultiplier", 1.0, 0.1, 1000000.0);

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


            builder.pop(); // spells カテゴリ終了

            builder.comment("Mekanism Integration Settings")
                    .push("mekanism");

            gasBurningGeneratorEnergyCapacity = builder
                    .comment("ガス燃焼発電機の内部エネルギー容量オーバーライド（J）",
                            "-1に設定するとMekanismのデフォルト動作（HydrogenEnergyDensityを基準）を使用します。",
                            "Gas-Burning Generator internal energy capacity override in Joules.",
                            "Set to -1 to use Mekanism default behavior (HydrogenEnergyDensity based).")
                    .defineInRange("gasBurningGeneratorEnergyCapacity", 2000000, -1L, Long.MAX_VALUE);

            builder.pop(); // mekanism カテゴリ終了
        }
    }

    // ===========================================
    // SERVER CONFIG（サーバーのみ、ワールドごとに保存）
    // ===========================================
    /*
    public static class Server {

        public Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Psitweaks Server Configuration")
                    .push("server");
            builder.pop();
        }
    }

    // ===========================================
    // CLIENT CONFIG（クライアントのみ、描画関連など）
    // ===========================================
    public static class Client {
        public Client(ForgeConfigSpec.Builder builder) {
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
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder()
                .configure(Common::new);
        COMMON_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();
    }

  /*  // Server Config
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        final Pair<Server, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder()
                .configure(Server::new);
        SERVER_SPEC = serverPair.getRight();
        SERVER = serverPair.getLeft();
    }

    // Client Config
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        final Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder()
                .configure(Client::new);
        CLIENT_SPEC = clientPair.getRight();
        CLIENT = clientPair.getLeft();
    }

   */
}
