package com.penchi.maceoptimized.combat;

import com.penchi.maceoptimized.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class ElytraHandler {

    private static int tickDelay = 0;
    private static boolean shouldSelectRocket = false;
    private static boolean wasWearingElytra = false;
    private static long lastSwapTime = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ElytraHandler::onTick);
    }

    private static void onTick(MinecraftClient client) {
        // NULL aur SCREEN checks (Safety first)
        if (client.player == null || client.world == null || !ModConfig.masterToggle) return;

        // Agar koi screen (Chest/Inventory) khuli hai toh swap ya auto-click mat karo
        if (client.currentScreen != null) return;

        ClientPlayerEntity player = client.player;

        // 1. DAMAGE SWAP CHECK (Elytra -> Chestplate)
        if (ModConfig.elytraChestSwapEnabled && player.hurtTime > 0) {
            handleDamageSwap(client, player);
        }

        // 2. AUTO ROCKET SELECT
        if (ModConfig.elytraLaunchEnabled) {
            ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
            boolean isWearingElytra = chest.isOf(Items.ELYTRA);

            // Elytra pehente hi trigger
            if (isWearingElytra && !wasWearingElytra) {
                shouldSelectRocket = true;
                tickDelay = 5; // 5 tick ka saaf delay
            }
            wasWearingElytra = isWearingElytra;

            if (shouldSelectRocket) {
                if (tickDelay <= 0) {
                    selectRocket(client, player);
                    shouldSelectRocket = false;
                } else {
                    tickDelay--;
                }
            }
        }
    }

    private static void handleDamageSwap(MinecraftClient client, ClientPlayerEntity player) {
        // Ground par ho aur gliding na kar raha ho
        if (!player.isOnGround() || player.isGliding()) return;

        long now = System.currentTimeMillis();
        if (now - lastSwapTime < 1000) return; // Spam prevention

        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (!chest.isOf(Items.ELYTRA)) return;

        int chestplateSlot = findHotbarChestplate(player);
        if (chestplateSlot != -1) {
            swapWithChestSlot(client, player, chestplateSlot);
            lastSwapTime = now;
        }
    }

    private static void selectRocket(MinecraftClient client, ClientPlayerEntity player) {
        if (client.getNetworkHandler() == null) return;

        int slot = findHotbarItem(player, Items.FIREWORK_ROCKET);
        if (slot == -1) slot = findHotbarItem(player, Items.WIND_CHARGE);

        if (slot != -1 && player.getInventory().getSelectedSlot() != slot) {
            player.getInventory().setSelectedSlot(slot);
            client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        }
    }

    private static void swapWithChestSlot(MinecraftClient client, ClientPlayerEntity player, int hotbarSlot) {
        if (client.interactionManager == null) return;

        // 1.21+ Slot IDs: Armor Chest = 6, Hotbar = 36-44
        int armorSlotId = 6;
        int inventorySlotId = 36 + hotbarSlot;

        // Pickup Hotbar -> Click Armor -> Put back in Hotbar
        client.interactionManager.clickSlot(player.playerScreenHandler.syncId, inventorySlotId, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(player.playerScreenHandler.syncId, armorSlotId, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(player.playerScreenHandler.syncId, inventorySlotId, 0, SlotActionType.PICKUP, player);
    }

    private static int findHotbarItem(ClientPlayerEntity player, net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    private static int findHotbarChestplate(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);

            var equippable = stack.getComponents().get(net.minecraft.component.DataComponentTypes.EQUIPPABLE);

            if (equippable != null && equippable.slot() == EquipmentSlot.CHEST) {
                return i;
            }
        }
        return -1;
    }
}