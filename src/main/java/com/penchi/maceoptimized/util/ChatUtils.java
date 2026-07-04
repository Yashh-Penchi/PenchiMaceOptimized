package com.penchi.maceoptimized.util;

import com.penchi.maceoptimized.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ChatUtils {

    public static void sendActionBar(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message), true);
        }
    }

    public static void sendChat(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message), false);
        }
    }

    public static void sendDensitySwap() {
        
        if (!ModConfig.chatNotifications) return;
        // Premium: clean single line only
        sendChat("§6§l[§eYASHH MACE§6§l] §c§lDensity §7swap attempted!");
    }

    public static void sendBreachSwap() {
        if (!ModConfig.chatNotifications) return;
        sendChat("§6§l[§eYASHH MACE§6§l] §b§lBreach §7swap attempted!");
    }

    public static void sendPearlCatch() {
        if (!ModConfig.chatNotifications) return;
        sendChat("§6§l[§eYASHH MACE§6§l] §d§lPearlCatch §7attempted!");
    }

    public static void sendMaceSwap() {
        if (!ModConfig.chatNotifications) return;
        sendChat("§6§l[§eYASHH MACE§6§l] §e§lMace §7swap attempted!");
    }

    // fallback generic
    public static void sendPrefixed(String message) {
        if (!ModConfig.chatNotifications) return;
        sendChat("§6§l[§eYASHH MACE§6§l] §r" + message);
    }
}
