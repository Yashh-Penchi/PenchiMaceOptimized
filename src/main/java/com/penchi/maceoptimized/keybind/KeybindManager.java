// Ladle Code Mat Dekh, Licenced hai CC BY-NC-ND 4.0

package com.penchi.maceoptimized.keybind;
import com.penchi.maceoptimized.hud.IndicatorRenderer;
import com.penchi.maceoptimized.config.ModConfig;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeybindManager {
    public static KeyBinding masterToggleKey;
    public static KeyBinding breachToggleKey;
    public static KeyBinding glideWeaponSwapKey;
    public static KeyBinding elytraChestSwapKey;
    public static KeyBinding elytraSwapToggleKey;

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("maceoptimized", "main"));

    public static void register() {
        masterToggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("1st Toggle PenchiMaceOptimized (Master)",
                        InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY)
        );

        breachToggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("2nd Toggle Breach_Swap",
                        InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), CATEGORY)
        );

        glideWeaponSwapKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("3rd Toggle Glide_Weapon_Swap",
                        InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), CATEGORY)
        );
    }

    private static void actionBar(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null)
            client.player.sendMessage(Text.literal(message), true);
    }

    public static void handleInputs() {
        if (MinecraftClient.getInstance().player == null) return;

        while (masterToggleKey.wasPressed()) {
            ModConfig.masterToggle = !ModConfig.masterToggle;
//            actionBar(ModConfig.masterToggle
//                    ? "§6§lPenchi MaceOptimized §aActive"
//                    : "§6§lPenchi MaceOptimized §cDisabled");

            // Blink trigger karo!
            IndicatorRenderer.trigger();

        }

        if (!ModConfig.masterToggle) return;

        while (breachToggleKey.wasPressed()) {
            ModConfig.breachEnabled = !ModConfig.breachEnabled;
            actionBar(ModConfig.breachEnabled
                    ? "§b§lBreach Swap §aEnabled"
                    : "§b§lBreach Swap §cDisabled");
        }

        while (glideWeaponSwapKey.wasPressed()) {
            ModConfig.glideWeaponSwapEnabled = !ModConfig.glideWeaponSwapEnabled;
            actionBar(ModConfig.glideWeaponSwapEnabled
                    ? "§6§lGlide Weapon Swap §aEnabled"
                    : "§6§lGlide Weapon Swap §cDisabled");
        }
    }
}
