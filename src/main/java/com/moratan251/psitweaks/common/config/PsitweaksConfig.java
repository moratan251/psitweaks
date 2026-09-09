package com.moratan251.psitweaks.common.config;

import com.moratan251.psitweaks.common.storage.idea.IdeaStorageDefaults;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class PsitweaksConfig {

    // ===========================================
    // COMMON CONFIG（サーバー・クライアント両方で使用）
    // ===========================================
    public static class Common {
        private static final double MIN_DAMAGE_MULTIPLIER = 0.1;
        private static final double MAX_DAMAGE_MULTIPLIER = 100.0;
        private static final double MAX_PROJECTILE_DAMAGE = Integer.MAX_VALUE;

        public final ModConfigSpec.DoubleValue globalSpellPowerMultiplier;
        public final ModConfigSpec.BooleanValue requireSpellUnlocks;
        public final ModConfigSpec.BooleanValue disableDamagePsiDeduction;
        public final ModConfigSpec.BooleanValue disableRegenCooldown;
        // `safeToPlayers` は server.properties の pvp 設定連動へ移行したため無効化。
        // public final ModConfigSpec.BooleanValue safeToPlayers;
        // 分子ディバイダー関連
        public final ModConfigSpec.DoubleValue molecularDividerDamageMultiplier;
        public final ModConfigSpec.DoubleValue phononMaserDamageMultiplier;
        public final ModConfigSpec.DoubleValue aquaCutterDamageMultiplier;
        public final ModConfigSpec.DoubleValue dryMeteorDamageMultiplier;
        public final ModConfigSpec.DoubleValue blazeBallDamageMultiplier;
        public final ModConfigSpec.DoubleValue activeAirMineDamageMultiplier;
        public final ModConfigSpec.DoubleValue flareCircleDamageMultiplier;
        public final ModConfigSpec.DoubleValue iceCircleDamageMultiplier;
        public final ModConfigSpec.DoubleValue radiationInjectionMultiplier;
        public final ModConfigSpec.DoubleValue guillotineDamageMultiplier;
        public final ModConfigSpec.DoubleValue gravstringerBaseDamage;
        public final ModConfigSpec.DoubleValue tunnelerMinimumDamage;
        public final ModConfigSpec.LongValue gasBurningGeneratorEnergyCapacity;
        // イデアストレージ関連
        public final ModConfigSpec.IntValue ideaStorageMaxItemTypes;
        public final ModConfigSpec.IntValue ideaStorageItemStacksPerType;
        public final ModConfigSpec.IntValue ideaStorageMaxFluidTypes;
        public final ModConfigSpec.LongValue ideaStorageMaxFluidPerType;
        public final ModConfigSpec.IntValue ideaStorageMaxChemicalTypes;
        public final ModConfigSpec.LongValue ideaStorageMaxChemicalPerType;
        public final ModConfigSpec.LongValue ideaStorageMaxEnergy;

        public Common(ModConfigSpec.Builder builder) {
            builder.comment("Psitweaks Common Configuration")
                    .translation("psitweaks.configuration.spells")
                    .push("spells"); // カテゴリ開始

            globalSpellPowerMultiplier = builder
                    .comment("Psitweaks攻撃スペル全体に適用する一律倍率",
                            "Global multiplier for all Psitweaks offensive spell damage")
                    .translation("psitweaks.configuration.spells.global_spell_power_multiplier")
                    .defineInRange("globalSpellPowerMultiplier", 1.0, MIN_DAMAGE_MULTIPLIER, MAX_DAMAGE_MULTIPLIER);

            // safeToPlayers = builder
            //         .comment("Trueにすると、Psitweaksの全攻撃スペルがプレイヤーをダメージ対象から除外します（フレンドリーファイア防止）",
            //                 "If true, all Psitweaks offensive spells will not damage players (friendly fire prevention)")
            //         .define("safeToPlayers", false);

            builder.comment("Spell unlock requirements")
                    .translation("psitweaks.configuration.spells.unlocks")
                    .push("unlocks");

            requireSpellUnlocks = builder
                    .comment("特定スペルの利用に解禁を要求するかどうか",
                            "Whether configured spells require unlock progression in Spell Programmer")
                    .translation("psitweaks.configuration.spells.unlocks.require_spell_unlocks")
                    .define("requireSpellUnlocks", true);

            builder.pop(); // unlocks カテゴリ終了

            // --- 分子ディバイダー ---
            builder.comment("Molecular Divider Settings")
                    .translation("psitweaks.configuration.spells.molecular_divider")
                    .push("molecular_divider");

            molecularDividerDamageMultiplier = builder
                    .comment("分子ディバイダーのダメージ倍率",
                            "Damage multiplier for Molecular Divider")
                    .translation("psitweaks.configuration.spells.molecular_divider.damage_multiplier")
                    .defineInRange("damageMultiplier", 1.0, MIN_DAMAGE_MULTIPLIER, MAX_DAMAGE_MULTIPLIER);

            builder.pop(); // molecular_divider カテゴリ終了

            // --- フォノンメーザー ---
            builder.comment("Phonon Maser Settings")
                    .translation("psitweaks.configuration.spells.phonon_maser")
                    .push("phonon_maser");

            phononMaserDamageMultiplier = builder
                    .comment("フォノンメーザーのダメージ倍率", "Damage multiplier for Phonon Maser" )
                    .translation("psitweaks.configuration.spells.phonon_maser.damage_multiplier")
                    .defineInRange("damageMultiplier", 1.0, MIN_DAMAGE_MULTIPLIER, MAX_DAMAGE_MULTIPLIER);

            builder.pop(); // phonon_maser カテゴリ終了

            // --- アクアカッター ---
            builder.comment("Aqua Cutter Settings")
                    .translation("psitweaks.configuration.spells.aqua_cutter")
                    .push("aqua_cutter");

            aquaCutterDamageMultiplier = builder
                    .comment("アクアカッターのダメージ倍率", "Damage multiplier for Aqua Cutter")
                    .translation("psitweaks.configuration.spells.aqua_cutter.damage_multiplier")
                    .defineInRange("damageMultiplier", 1.0, MIN_DAMAGE_MULTIPLIER, MAX_DAMAGE_MULTIPLIER);

            builder.pop(); // aqua_cutter カテゴリ終了

            // --- ドライミーティア ---
            builder.comment("Dry Meteor Settings")
                    .translation("psitweaks.configuration.spells.dry_meteor")
                    .push("dry_meteor");

            dryMeteorDamageMultiplier = builder
                    .comment("ドライミーティアのダメージ倍率", "Damage multiplier for Dry Meteor")
                    .translation("psitweaks.configuration.spells.dry_meteor.damage_multiplier")
                    .defineInRange("damageMultiplier", 1.0, MIN_DAMAGE_MULTIPLIER, MAX_DAMAGE_MULTIPLIER);

            builder.pop(); // dry_meteor カテゴリ終了

            // --- ブレイズボール ---
            builder.comment("Blaze Ball Settings")
                    .translation("psitweaks.configuration.spells.blaze_ball")
                    .push("blaze_ball");

            blazeBallDamageMultiplier = builder
                    .comment("ブレイズボールのダメージ倍率", "Damage multiplier for Blaze Ball")
                    .translation("psitweaks.configuration.spells.blaze_ball.damage_multiplier")
                    .defineInRange("damageMultiplier", 1.0, MIN_DAMAGE_MULTIPLIER, MAX_DAMAGE_MULTIPLIER);

            builder.pop(); // blaze_ball カテゴリ終了

            // --- 能動空中機雷 ---
            builder.comment("Active Air Mine Settings")
                    .translation("psitweaks.configuration.spells.active_air_mine")
                    .push("active_air_mine");

            activeAirMineDamageMultiplier = builder
                    .comment("作動式: 能動空中機雷 のダメージ倍率",
                            "Damage multiplier for Trick: Active Air Mine")
                    .translation("psitweaks.configuration.spells.active_air_mine.damage_multiplier")
                    .defineInRange("damageMultiplier", 1.0, MIN_DAMAGE_MULTIPLIER, MAX_DAMAGE_MULTIPLIER);

            builder.pop(); // active_air_mine カテゴリ終了

            // --- フレアサークル ---
            builder.comment("Flare Circle Settings")
                    .translation("psitweaks.configuration.spells.flare_circle")
                    .push("flare_circle");

            flareCircleDamageMultiplier = builder
                    .comment("作動式: フレアサークル の威力倍率（与ダメージ倍率）",
                            "Power (damage) multiplier for Trick: Flare Circle")
                    .translation("psitweaks.configuration.spells.flare_circle.damage_multiplier")
                    .defineInRange("damageMultiplier", 1.0, MIN_DAMAGE_MULTIPLIER, MAX_DAMAGE_MULTIPLIER);

            builder.pop(); // flare_circle カテゴリ終了

            // --- アイスサークル ---
            builder.comment("Ice Circle Settings")
                    .translation("psitweaks.configuration.spells.ice_circle")
                    .push("ice_circle");

            iceCircleDamageMultiplier = builder
                    .comment("作動式: アイスサークル の威力倍率（与ダメージ倍率）",
                            "Power (damage) multiplier for Trick: Ice Circle")
                    .translation("psitweaks.configuration.spells.ice_circle.damage_multiplier")
                    .defineInRange("damageMultiplier", 1.0, MIN_DAMAGE_MULTIPLIER, MAX_DAMAGE_MULTIPLIER);

            builder.pop(); // ice_circle カテゴリ終了

            // --- 放射線注入 ---
            builder.comment("Radiation Injection Settings")
                    .translation("psitweaks.configuration.spells.radiation_injection")
                    .push("radiation_injection");

            radiationInjectionMultiplier = builder
                    .comment("作動式: 放射線注入 の被ばく量倍率",
                            "Radiation amount multiplier for Trick: Radiation Injection")
                    .translation("psitweaks.configuration.spells.radiation_injection.radiation_multiplier")
                    .defineInRange("radiationMultiplier", 1.0, 0.0, 1000000.0);

            builder.pop(); // radiation_injection カテゴリ終了

            // --- ギロチン ---
            builder.comment("Guillotine Settings")
                    .translation("psitweaks.configuration.spells.guillotine")
                    .push("guillotine");

            guillotineDamageMultiplier = builder
                    .comment("作動式: ギロチン のダメージ倍率",
                            "Damage multiplier for Trick: Guillotine")
                    .translation("psitweaks.configuration.spells.guillotine.damage_multiplier")
                    .defineInRange("damageMultiplier", 1.0, MIN_DAMAGE_MULTIPLIER, MAX_DAMAGE_MULTIPLIER);

            builder.pop(); // guillotine カテゴリ終了

            builder.pop(); // spells カテゴリ終了

            builder.comment("Item Settings")
                    .translation("psitweaks.configuration.items")
                    .push("items");

            builder.comment("Gravstringer Settings")
                    .translation("psitweaks.configuration.items.gravstringer")
                    .push("gravstringer");

            gravstringerBaseDamage = builder
                    .comment("グラヴストリンガーが矢に設定する基礎ダメージ",
                            "Base damage assigned to arrows fired by the Gravstringer")
                    .translation("psitweaks.configuration.items.gravstringer.base_damage")
                    .defineInRange("baseDamage", 12.0, 0.0, MAX_PROJECTILE_DAMAGE);

            builder.pop(); // gravstringer

            builder.comment("Tunneler Settings")
                    .translation("psitweaks.configuration.items.tunneler")
                    .push("tunneler");

            tunnelerMinimumDamage = builder
                    .comment("トンネラーの最低ダメージ",
                            "Minimum damage dealt by the Tunneler")
                    .translation("psitweaks.configuration.items.tunneler.minimum_damage")
                    .defineInRange("minimumDamage", 36.0, 0.0, MAX_PROJECTILE_DAMAGE);

            builder.pop(); // tunneler
            builder.pop(); // items

            builder.comment("Psi Behavior Settings")
                    .translation("psitweaks.configuration.psi")
                    .push("psi");

            disableDamagePsiDeduction = builder
                    .comment("Whether taking damage should not deduct Psi")
                    .translation("psitweaks.configuration.psi.disable_damage_psi_deduction")
                    .define("disableDamagePsiDeduction", true);

            disableRegenCooldown = builder
                    .comment("Whether Psi consumption should not apply a regeneration cooldown")
                    .translation("psitweaks.configuration.psi.disable_regen_cooldown")
                    .define("disableRegenCooldown", true);

            builder.pop(); // psi

            builder.comment("Mekanism Integration Settings")
                    .translation("psitweaks.configuration.mekanism")
                    .push("mekanism");

            gasBurningGeneratorEnergyCapacity = builder
                    .comment("ガス燃焼発電機の内部エネルギー容量オーバーライド（J）",
                            "-1に設定するとMekanismのデフォルト動作（HydrogenEnergyDensityを基準）を使用します。",
                            "Gas-Burning Generator internal energy capacity override in Joules.",
                            "Set to -1 to use Mekanism default behavior (HydrogenEnergyDensity based).")
                    .translation("psitweaks.configuration.mekanism.gas_burning_generator_energy_capacity")
                    .defineInRange("gasBurningGeneratorEnergyCapacity", 2000000, -1L, Long.MAX_VALUE);

            builder.pop(); // mekanism カテゴリ終了

            builder.comment("Ideaspace Storage Settings")
                    .translation("psitweaks.configuration.idea_storage")
                    .push("ideaStorage");

            ideaStorageMaxItemTypes = builder
                    .comment("イデアストレージに格納できるアイテムの最大種類数",
                            "Maximum number of distinct item types per player in the Ideaspace Storage")
                    .translation("psitweaks.configuration.idea_storage.max_item_types")
                    .defineInRange("maxItemTypes", 256, 1, 100_000);

            ideaStorageItemStacksPerType = builder
                    .comment("アイテム1種類あたりの上限(最大スタック数に掛ける係数)",
                            "Per-type item limit as a multiplier of the item's max stack size")
                    .translation("psitweaks.configuration.idea_storage.item_stacks_per_type")
                    .defineInRange("itemStacksPerType", 16_384, 1, Integer.MAX_VALUE);

            ideaStorageMaxFluidTypes = builder
                    .comment("イデアストレージに格納できる液体の最大種類数",
                            "Maximum number of distinct fluid types per player in the Ideaspace Storage")
                    .translation("psitweaks.configuration.idea_storage.max_fluid_types")
                    .defineInRange("maxFluidTypes", 64, 1, 100_000);

            ideaStorageMaxFluidPerType = builder
                    .comment("液体1種類あたりの上限(内部単位: mB、既定値は1,048,576 B)",
                            "Maximum amount per fluid type in internal mB units (default: 1,048,576 B)")
                    .translation("psitweaks.configuration.idea_storage.max_fluid_per_type")
                    .defineInRange("maxFluidPerType", IdeaStorageDefaults.FLUID_PER_TYPE_RAW,
                            1L, Long.MAX_VALUE);

            ideaStorageMaxChemicalTypes = builder
                    .comment("イデアストレージに格納できるChemicalの最大種類数",
                            "Maximum number of distinct chemical types per player in the Ideaspace Storage")
                    .translation("psitweaks.configuration.idea_storage.max_chemical_types")
                    .defineInRange("maxChemicalTypes", 64, 1, 100_000);

            ideaStorageMaxChemicalPerType = builder
                    .comment("Chemical 1種類あたりの上限(内部単位、既定値は33,554,432 B)",
                            "Maximum amount per chemical type in internal units (default: 33,554,432 B)")
                    .translation("psitweaks.configuration.idea_storage.max_chemical_per_type")
                    .defineInRange("maxChemicalPerType", IdeaStorageDefaults.CHEMICAL_PER_TYPE_RAW,
                            1L, Long.MAX_VALUE);

            ideaStorageMaxEnergy = builder
                    .comment("Maximum FE stored per player")
                    .translation("psitweaks.configuration.idea_storage.max_energy")
                    .defineInRange("maxEnergy", Long.MAX_VALUE, 1L, Long.MAX_VALUE);

            builder.pop(); // ideaStorage カテゴリ終了
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
