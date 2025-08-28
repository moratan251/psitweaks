package com.moratan251.psitweaks.common.items.armor;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.items.armor.unused.ItemMovalSuitXBoots;
import com.moratan251.psitweaks.common.items.armor.unused.ItemMovalSuitXChestplate;
import com.moratan251.psitweaks.common.items.armor.unused.ItemMovalSuitXHelmet;
import com.moratan251.psitweaks.common.items.armor.unused.ItemMovalSuitXLeggings;
import com.moratan251.psitweaks.player.ModCapabilities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.psi.common.core.handler.PlayerDataHandler;


@Mod.EventBusSubscriber(modid = "psitweaks")
public class MovalSuitXEvent {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        float damage = event.getAmount();

        if (feet.getItem() instanceof ItemMovalSuitXBoots) {
            if(Math.random() >= 0.5){
                damage = 0.0f;

            }
        }
        if (legs.getItem() instanceof ItemMovalSuitXLeggings) {
            if(damage <= 10.0f){
                damage = 0.0f;

            }
        }
        // ダメージ半減
        if (chest.getItem() instanceof ItemMovalSuitXChestplate) {
            damage *= 0.5f;// ダメージ半減
        }

        //受けるダメージの上限を3にする
        if (head.getItem() instanceof ItemMovalSuitXHelmet) {
            if(damage > 3.0f){
                damage = 3.0f;

            }
        }
        event.setAmount(damage);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        float rawDamage = event.getAmount(); // 攻撃予定ダメージ
        if(rawDamage <= 10.0f || Math.random() >= 0.5f){rawDamage = 0.0f;}
        rawDamage *= 0.5f;
        if(rawDamage > 3.0f){rawDamage = 3.0f;}
        int cost = (int) (rawDamage * 100);
        //DamageSource source = event.getSource();

        if (isMovalSuitXFullset(player) &&  PlayerDataHandler.get(player).getAvailablePsi() >= cost) {
            PlayerDataHandler.get(player).deductPsi(cost,0,false,true);
            event.setCanceled(true);


            }
        }




    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        player.getCapability(ModCapabilities.FLIGHT_DATA).ifPresent(data -> {
            boolean hasArmor = player.getInventory().armor.get(1).getItem() == PsitweaksItems.MOVAL_SUIT_LEGGINGS.get();
/*
            if (hasArmor != data.hasFlightArmor()) {
                data.setHasFlightArmor(hasArmor);

                if (hasArmor) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                } else {
                    if (!player.isCreative() && !player.isSpectator()) {
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                    }
                }
            }
        });

 */
            if (!player.isCreative() && !player.isSpectator()) {
                if (hasArmor != data.hasFlightArmor()) {
                    data.setHasFlightArmor(hasArmor);

                    if (hasArmor) {
                        player.getAbilities().mayfly = true;
                        player.onUpdateAbilities();
                    } else {
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                    }
                }
            } else {
                // クリエ・スペク時は内部フラグだけ更新
                data.setHasFlightArmor(hasArmor);
            }
        });

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.isEmpty() && helmet.getItem() instanceof ItemMovalSuitXHelmet) {
            // 水中呼吸
            player.addEffect(new MobEffectInstance(
                    MobEffects.WATER_BREATHING, 210, 0, true, false, true));

            // 火炎耐性
            player.addEffect(new MobEffectInstance(
                    MobEffects.FIRE_RESISTANCE, 210, 0, true, false, true));
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            player.getCapability(ModCapabilities.FLIGHT_DATA).ifPresent(data -> {
                if (data.hasFlightArmor()) {
                    event.setCanceled(true);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEquip(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getSlot() == EquipmentSlot.FEET) {
            // 装備アイテム
            ItemStack newStack = event.getTo();
            ItemStack oldStack = event.getFrom();

            if (newStack.getItem() instanceof ItemMovalSuitXBoots) {

                PlayerDataHandler.get(player).regen += 25;
                //  PlayerDataHandler.get(player).totalPsi += 2500;


            }

            if (oldStack.getItem() instanceof ItemMovalSuitXBoots) {
                PlayerDataHandler.get(player).regen -= 25;
                //  PlayerDataHandler.get(player).totalPsi -= 2500;

            }
        }
    }

    private static boolean isMovalSuitXFullset(Player player) {
        Item head = player.getItemBySlot(EquipmentSlot.HEAD).getItem();
        Item chest = player.getItemBySlot(EquipmentSlot.CHEST).getItem();
        Item legs = player.getItemBySlot(EquipmentSlot.LEGS).getItem();
        Item feet = player.getItemBySlot(EquipmentSlot.FEET).getItem();

        return head instanceof ItemMovalSuitXHelmet
                && chest instanceof ItemMovalSuitXChestplate
                && legs instanceof ItemMovalSuitXLeggings
                && feet instanceof ItemMovalSuitXBoots;
    }


    public static void register() {
        MinecraftForge.EVENT_BUS.register(MovalSuitXEvent.class);
    }
}