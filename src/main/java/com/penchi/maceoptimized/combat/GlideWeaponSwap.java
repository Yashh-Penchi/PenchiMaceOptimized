package com.penchi.maceoptimized.combat;

import com.penchi.maceoptimized.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.RegistryKeys;

public final class GlideWeaponSwap {

    // State tracking (lightweight, no copying)
    private static ItemStack lastChest = ItemStack.EMPTY;
    private static boolean wasGliding = false;

    private GlideWeaponSwap() {} // prevent instantiation

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (!ModConfig.masterToggle || !ModConfig.glideWeaponSwapEnabled) return;
            if (client.player == null || client.getNetworkHandler() == null) return;

            ClientPlayerEntity player = client.player;

            // Current states
            ItemStack currentChest = player.getEquippedStack(EquipmentSlot.CHEST);
            boolean isGliding = player.isGliding();

            // ----------- FAST PATH (Instant Chestplate Equip Detection) -----------
            if (!isChestplate(lastChest) && isChestplate(currentChest)) {
                triggerSwap(client, player);
            }

            // ----------- FALLBACK (Glide End Detection) -----------
            else if (wasGliding && !isGliding && isChestplate(currentChest)) {
                triggerSwap(client, player);
            }

            // Update states (no copy = faster)
            lastChest = currentChest;
            wasGliding = isGliding;
        });
    }

    // -------------------- CORE SWAP --------------------

    private static void triggerSwap(MinecraftClient client, ClientPlayerEntity player) {
        int slot = (ModConfig.glideWeaponChoice == 0)
                ? findBestMace(player)
                : findSword(player);

        if (slot != -1 && player.getInventory().getSelectedSlot() != slot) {
            player.getInventory().setSelectedSlot(slot);
            client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        }
    }

    // -------------------- ITEM DETECTION --------------------

    private static boolean isChestplate(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        return stack.isOf(Items.NETHERITE_CHESTPLATE)
                || stack.isOf(Items.DIAMOND_CHESTPLATE)
                || stack.isOf(Items.IRON_CHESTPLATE)
                || stack.isOf(Items.GOLDEN_CHESTPLATE)
                || stack.isOf(Items.LEATHER_CHESTPLATE)
                || stack.isOf(Items.CHAINMAIL_CHESTPLATE);
    }

    private static int findSword(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);

            if (stack.isOf(Items.NETHERITE_SWORD)
                    || stack.isOf(Items.DIAMOND_SWORD)
                    || stack.isOf(Items.IRON_SWORD)
                    || stack.isOf(Items.GOLDEN_SWORD)
                    || stack.isOf(Items.STONE_SWORD)
                    || stack.isOf(Items.WOODEN_SWORD)) {
                return i;
            }
        }
        return -1;
    }

    private static int findBestMace(ClientPlayerEntity player) {
        int bestSlot = -1;
        int bestPriority = -1;

        var registry = player.getEntityWorld()
                .getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT);

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);

            if (!stack.isOf(Items.MACE)) continue;

            int priority = 0;

            if (EnchantmentHelper.getLevel(registry.getOrThrow(Enchantments.DENSITY), stack) > 0) {
                priority = 2;
            } else if (EnchantmentHelper.getLevel(registry.getOrThrow(Enchantments.BREACH), stack) > 0) {
                priority = 1;
            }

            if (priority > bestPriority) {
                bestPriority = priority;
                bestSlot = i;

                if (priority == 2) break; // max priority, stop early
            }
        }

        return bestSlot;
    }
}