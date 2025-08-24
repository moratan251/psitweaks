package com.moratan251.psitweaks.common.items.armor;

import com.moratan251.psitweaks.common.items.ModItems;
import com.moratan251.psitweaks.player.ModCapabilities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.psi.common.core.handler.PlayerDataHandler;


@Mod.EventBusSubscriber(modid = "psitweaks")
public class MovalSuitEvent {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        float damage = event.getAmount();

        if (feet.getItem() instanceof ItemMovalSuitBoots) {
            if(Math.random() >= 0.5){
                damage = 0.0f;

            }
        }
        if (legs.getItem() instanceof ItemMovalSuitLeggings) {
            if(damage <= 10.0f){
                damage = 0.0f;

            }
        }
        // ダメージ半減
        if (chest.getItem() instanceof ItemMovalSuitChestplate) {
            damage *= 0.5f;// ダメージ半減
        }

        //受けるダメージの上限を3にする
        if (head.getItem() instanceof ItemMovalSuitHelmet) {
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

        if (isMovalSuitFullset(player) &&  PlayerDataHandler.get(player).getAvailablePsi() >= cost) {
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
            boolean hasArmor = player.getInventory().armor.get(1).getItem() == ModItems.MOVAL_SUIT_LEGGINGS.get();

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
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.isEmpty() && helmet.getItem() instanceof ItemMovalSuitHelmet) {
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

            if (newStack.getItem() instanceof ItemMovalSuitBoots) {

                PlayerDataHandler.get(player).regen += 25;
                //  PlayerDataHandler.get(player).totalPsi += 2500;


            }

            if (oldStack.getItem() instanceof ItemMovalSuitBoots) {
                PlayerDataHandler.get(player).regen -= 25;
                //  PlayerDataHandler.get(player).totalPsi -= 2500;

            }
        }
    }

    private static boolean isMovalSuitFullset(Player player) {
        Item head = player.getItemBySlot(EquipmentSlot.HEAD).getItem();
        Item chest = player.getItemBySlot(EquipmentSlot.CHEST).getItem();
        Item legs = player.getItemBySlot(EquipmentSlot.LEGS).getItem();
        Item feet = player.getItemBySlot(EquipmentSlot.FEET).getItem();

        return head instanceof ItemMovalSuitHelmet
                && chest instanceof ItemMovalSuitChestplate
                && legs instanceof ItemMovalSuitLeggings
                && feet instanceof ItemMovalSuitBoots;
    }


    public static void register() {
        MinecraftForge.EVENT_BUS.register(MovalSuitEvent.class);
    }
}