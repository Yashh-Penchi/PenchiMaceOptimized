package com.penchi.maceoptimized.combat;

import com.penchi.maceoptimized.config.ModConfig;
import com.penchi.maceoptimized.util.ChatUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class PearlCatchHandler {

    private static long lastPearlTime = 0;
    private static boolean swapped = false;
    private static boolean autoUsed = false;
    private static int autoUseTimer = -1;

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient() && player.getStackInHand(hand).isOf(Items.ENDER_PEARL)) {
                lastPearlTime = System.currentTimeMillis();
                swapped = false;
                autoUsed = false;
                autoUseTimer = -1;
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!ModConfig.masterToggle || !ModConfig.pearlCatchEnabled) return;
            if (client.player == null || client.world == null) return;

            long timeSincePearl = System.currentTimeMillis() - lastPearlTime;
            if (timeSincePearl > 1000) return;

            if (client.player.isUsingItem() && client.player.getActiveItem().isOf(Items.SHIELD)) {
                return;
            }

            int windSlot = -1;
            for (int i = 0; i < 9; i++) {
                if (client.player.getInventory().getStack(i).isOf(Items.WIND_CHARGE)) {
                    windSlot = i;
                    break;
                }
            }
            if (windSlot == -1) return;

            // --- STEP 1: SWAP & TIMER INITIALIZATION ---
            if (!swapped && client.player.getPitch() < -70) {
                if (client.player.getInventory().getSelectedSlot() != windSlot) {
                    client.player.getInventory().setSelectedSlot(windSlot);
                    if (client.getNetworkHandler() != null) {
                        client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(windSlot));
                    }
                    swapped = true;

                    // Randomization logic yahan honi chahiye (Timer start hote waqt)
                    if (ModConfig.autoPearlCatch) {
                        if (ModConfig.randomizePearlDelay) {
                            int min = ModConfig.minPearlCatchDelay;
                            int max = ModConfig.maxPearlCatchDelay;
                            if (min >= max) {
                                autoUseTimer = min;
                            } else {
                                autoUseTimer = min + (int)(Math.random() * ((max - min) + 1));
                            }
                        } else {
                            autoUseTimer = ModConfig.autoPearlCatchDelay;
                        }
                    }
                    ChatUtils.sendPearlCatch();
                }
            }

            // --- STEP 2: COUNTDOWN & AUTO THROW ---
            if (ModConfig.autoPearlCatch && swapped && !autoUsed) {
                if (autoUseTimer > 0) {
                    autoUseTimer--;
                } else if (autoUseTimer == 0) {
                    if (client.interactionManager != null
                            && client.player.getInventory().getSelectedSlot() == windSlot) {

                        if (!client.player.isUsingItem()) {
                            client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                            client.player.swingHand(Hand.MAIN_HAND);
                            autoUsed = true;
                            autoUseTimer = -1;
                        }
                    }
                }
            }
        });
    }
}