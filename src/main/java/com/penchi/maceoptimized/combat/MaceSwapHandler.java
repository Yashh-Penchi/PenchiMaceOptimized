package com.penchi.maceoptimized.combat;

import com.penchi.maceoptimized.config.ModConfig;
import com.penchi.maceoptimized.util.ChatUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class MaceSwapHandler {
    private static int revertTimer = -1;
    private static int originalSlot = -1;
    private static boolean isSwapping = false;

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!ModConfig.masterToggle || !world.isClient()) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND || !(entity instanceof LivingEntity) || isSwapping) return ActionResult.PASS;

            LivingEntity target = (LivingEntity) entity;
            if (isShielding(target)) return ActionResult.PASS;

            // 1. Check Conditions
            boolean targetHasArmor = false;
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                if (!target.getEquippedStack(slot).isEmpty()) {
                    targetHasArmor = true;
                    break;
                }
            }

            boolean isHighFalling = (player.fallDistance >= 1.5f && player.getVelocity().y < 0);

            ItemStack held = player.getMainHandStack();
            boolean holdingWeapon = held.getItem() instanceof AxeItem || held.getItem().getName().getString().toLowerCase().contains("sword");
            if (!holdingWeapon) return ActionResult.PASS;

            // 2. Scan Hotbar for Mace Types
            int densitySlot = -1;
            int breachSlot = -1;
            int normalMaceSlot = -1;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (!stack.isOf(Items.MACE)) continue;

                if (normalMaceSlot == -1) normalMaceSlot = i;
                if (getEnchantmentLevel(stack, "density") > 0) densitySlot = i;
                if (getEnchantmentLevel(stack, "breach") > 0) breachSlot = i;
            }

            // 3. Priority Logic
            int finalSlot = -1;
            String swapType = "";

            if (isHighFalling) {
                // High Fall Priority: Density > Breach > Normal Mace
                if (ModConfig.densityEnabled && densitySlot != -1) {
                    finalSlot = densitySlot;
                    swapType = "density";
                } else if (ModConfig.breachEnabled && breachSlot != -1) {
                    finalSlot = breachSlot;
                    swapType = "breach";
                } else {
                    finalSlot = normalMaceSlot;
                    swapType = "mace";
                }
            } else {
                // Ground/Low Fall Priority: Only Breach (if target has armor)
                if (targetHasArmor && ModConfig.breachEnabled && breachSlot != -1) {
                    finalSlot = breachSlot;
                    swapType = "breach";
                }
            }

            // 4. Execution
            if (finalSlot == -1 || player.getInventory().getSelectedSlot() == finalSlot) return ActionResult.PASS;

            originalSlot = player.getInventory().getSelectedSlot();
            isSwapping = true;
            player.getInventory().setSelectedSlot(finalSlot);

            if (MinecraftClient.getInstance().getNetworkHandler() != null) {
                MinecraftClient.getInstance().getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(finalSlot));
            }

            // Chat notification based on selection
            if (swapType.equals("density")) ChatUtils.sendDensitySwap();
            else if (swapType.equals("breach")) ChatUtils.sendBreachSwap();
            else if (swapType.equals("mace")) ChatUtils.sendMaceSwap();

            revertTimer = ModConfig.revertDelayTicks + 1;
            return ActionResult.PASS;
        });

        // Tick handler (Unchanged)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (revertTimer > 0) {
                revertTimer--;
            } else if (revertTimer == 0 && originalSlot != -1) {
                client.player.getInventory().setSelectedSlot(originalSlot);
                if (client.getNetworkHandler() != null) {
                    client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
                }
                originalSlot = -1;
                revertTimer = -1;
                isSwapping = false;
            }
        });
    }

    private static int getEnchantmentLevel(ItemStack stack, String enchantName) {
        if (stack == null || stack.isEmpty()) return 0;
        ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants == null) return 0;
        for (RegistryEntry<Enchantment> entry : enchants.getEnchantments()) {
            if (entry.getIdAsString().toLowerCase().contains(enchantName.toLowerCase())) {
                return enchants.getLevel(entry);
            }
        }
        return 0;
    }

    private static boolean isShielding(LivingEntity entity) {
        // Check both hands for active shield
        ItemStack mainHand = entity.getMainHandStack();
        ItemStack offHand = entity.getOffHandStack();

        boolean hasShield = mainHand.isOf(Items.SHIELD) || offHand.isOf(Items.SHIELD);
        if (!hasShield) return false;

        // isUsingItem() = actively holding right click (shield raised)
        return entity.isUsingItem();
    }
}