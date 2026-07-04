package com.penchi.maceoptimized.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ModMenuIntegration implements ModMenuApi {

    // Isse track karenge ki reload zaroori hai ya nahi (sirf texture change par)
    private boolean needsResourceReload = false;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("§6§lPenchi MaceOptimized §r§7Settings"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory all = builder.getOrCreateCategory(Text.literal("Settings"));

            // --- GENERAL ---
            all.addEntry(entryBuilder.startTextDescription(Text.literal("§e§l— General —")).build());

            all.addEntry(entryBuilder.startBooleanToggle(Text.literal("Master Toggle"), ModConfig.masterToggle)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Enable/Disable entire mod"))
                    .setSaveConsumer(v -> ModConfig.masterToggle = v)
                    .build());

            // --- MACE SWAP ---
            all.addEntry(entryBuilder.startTextDescription(Text.literal("§b§l— Mace Swap Toggle —")).build());

            all.addEntry(entryBuilder.startBooleanToggle(Text.literal("Density Swap"), ModConfig.densityEnabled)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Auto swap to Density Mace when falling"))
                    .setSaveConsumer(v -> ModConfig.densityEnabled = v)
                    .build());

            all.addEntry(entryBuilder.startBooleanToggle(Text.literal("Breach Swap"), ModConfig.breachEnabled)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Auto swap to Breach Mace when target has armor"))
                    .setSaveConsumer(v -> ModConfig.breachEnabled = v)
                    .build());

            all.addEntry(entryBuilder.startIntSlider(Text.literal("Universal Revert Delay"), ModConfig.revertDelayTicks, 1, 10)
                    .setDefaultValue(2)
                    .setTooltip(Text.literal("Ticks to wait AFTER hit before going back to original item"))
                    .setSaveConsumer(v -> ModConfig.revertDelayTicks = v)
                    .build());

            // --- CHAT NOTIFICATION ---
            all.addEntry(entryBuilder.startTextDescription(Text.literal("§b§l— Chat Notification Setting —")).build());

            all.addEntry(entryBuilder.startBooleanToggle(Text.literal("Chat Notifications"), ModConfig.chatNotifications)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Show chat message when swap is triggered"))
                    .setSaveConsumer(v -> ModConfig.chatNotifications = v)
                    .build());

            // --- ELYTRA & UTILITY (NEW) ---
            all.addEntry(entryBuilder.startTextDescription(Text.literal("§a§l— Elytra & Utility —")).build());

            all.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Elytra/Chest Swap"), ModConfig.elytraChestSwapEnabled)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Automatically swaps Elytra for Chestplate when taking damage on ground"))
                    .setSaveConsumer(v -> ModConfig.elytraChestSwapEnabled = v)
                    .build());

            all.addEntry(entryBuilder.startBooleanToggle(Text.literal("Auto Rocket Select"), ModConfig.elytraLaunchEnabled)
                    .setDefaultValue(false)
                    .setTooltip(Text.literal("Automatically selects Rocket/WindCharge when equipping Elytra"))
                    .setSaveConsumer(v -> ModConfig.elytraLaunchEnabled = v)
                    .build());

            // --- GLIDE WEAPON SWAP ---
            all.addEntry(entryBuilder.startTextDescription(Text.literal("§6§l— Glide Weapon Swap —")).build());

            all.addEntry(entryBuilder.startBooleanToggle(Text.literal("Glide Weapon Swap"), ModConfig.glideWeaponSwapEnabled)
                    .setDefaultValue(false)
                    .setTooltip(Text.literal("When elytra stops gliding + chestplate equipped\nauto switch to selected weapon"))
                    .setSaveConsumer(v -> ModConfig.glideWeaponSwapEnabled = v)
                    .build());

            all.addEntry(entryBuilder.startSelector(
                            Text.literal("Weapon Choice"),
                            new Object[]{"§aMace", "§bSword"},
                            ModConfig.glideWeaponChoice == 0 ? "§aMace" : "§bSword")
                    .setDefaultValue("§aMace")
                    .setTooltip(Text.literal("Click to cycle: Mace / Sword"))
                    .setSaveConsumer(v -> ModConfig.glideWeaponChoice = v.equals("§aMace") ? 0 : 1)
                    .build());

            // --- PEARL CATCH ---
            all.addEntry(entryBuilder.startTextDescription(Text.literal("§d§l— Pearl Catch —")).build());

            all.addEntry(entryBuilder.startBooleanToggle(Text.literal("Pearl Catch Enabled"), ModConfig.pearlCatchEnabled)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Auto swap to Wind Charge after throwing Ender Pearl"))
                    .setSaveConsumer(v -> ModConfig.pearlCatchEnabled = v)
                    .build());

            all.addEntry(entryBuilder.startBooleanToggle(Text.literal("Auto Pearl Catch"), ModConfig.autoPearlCatch)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("OFF = swap only, you click manually | ON = auto use Wind Charge after delay"))
                    .setSaveConsumer(v -> ModConfig.autoPearlCatch = v)
                    .build());

            all.addEntry(entryBuilder.startIntSlider(Text.literal("Auto Catch Delay (Ticks)"), ModConfig.autoPearlCatchDelay, 1, 10)
                    .setDefaultValue(2)
                    .setTooltip(Text.literal("Ticks after swap before auto using Wind Charge\nRange: 1-10 ticks"))
                    .setSaveConsumer(v -> ModConfig.autoPearlCatchDelay = v)
                    .build());

            all.addEntry(entryBuilder.startBooleanToggle(Text.literal("Randomize Delay"), ModConfig.randomizePearlDelay)
                    .setDefaultValue(false)
                    .setTooltip(Text.literal("Enable random delay to bypass anti-cheat detections"))
                    .setSaveConsumer(v -> ModConfig.randomizePearlDelay = v)
                    .build());

            all.addEntry(entryBuilder.startIntSlider(Text.literal("Min Catch Delay (Ticks)"), ModConfig.minPearlCatchDelay, 1, 10)
                    .setDefaultValue(2)
                    .setSaveConsumer(v -> ModConfig.minPearlCatchDelay = v)
                    .build());

            all.addEntry(entryBuilder.startIntSlider(Text.literal("Max Catch Delay (Ticks)"), ModConfig.maxPearlCatchDelay, 1, 10)
                    .setDefaultValue(4)
                    .setSaveConsumer(v -> ModConfig.maxPearlCatchDelay = v)
                    .build());

            // --- APPEARANCE ---
            all.addEntry(entryBuilder.startTextDescription(Text.literal("§d§l— Appearance —")).build());

            all.addEntry(entryBuilder.startBooleanToggle(Text.literal("Use Custom Mace Textures"), ModConfig.useCustomTextures)
                    .setDefaultValue(true)
                    .setSaveConsumer(v -> {
                        if (ModConfig.useCustomTextures != v) {
                            ModConfig.useCustomTextures = v;
                            needsResourceReload = true; // Mark kiya ki reload chahiye
                        }
                    })
                    .build());

            // --- FINAL SAVING LOGIC ---
            builder.setSavingRunnable(() -> {
                // Agar texture toggle change hua hai, tabhi reload karo
                if (needsResourceReload) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null && client.getResourcePackManager() != null) {
                        String packId = "maceoptimized:custom_textures";
                        List<String> enabledPacks = new ArrayList<>(client.options.resourcePacks);

                        if (ModConfig.useCustomTextures) {
                            if (!enabledPacks.contains(packId)) enabledPacks.add(packId);
                        } else {
                            enabledPacks.remove(packId);
                        }

                        client.getResourcePackManager().setEnabledProfiles(enabledPacks);
                        client.options.resourcePacks = enabledPacks;
                        client.options.write();
                        client.reloadResources();
                    }
                    needsResourceReload = false; // Reset flag
                }
                // Yahan aap apna config.save() logic bhi daal sakte hain
            });

            return builder.build();
        };
    }
}