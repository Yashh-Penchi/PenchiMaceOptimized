package com.penchi.maceoptimized.config;

public class ModConfig {
    public static boolean masterToggle = true;
    public static boolean breachEnabled = true;
    public static boolean densityEnabled = true;
    public static boolean pearlCatchEnabled = true;
    public static boolean indicatorsEnabled = true;
    public static boolean autoPearlCatch = true;
    public static boolean chatNotifications = false;
    public static boolean elytraLaunchEnabled = false;
    public static int elytraBoostTrigger = 0;  // 0=Hold Space, 1=Press Key
    public static boolean windChargeJumpBoostEnabled = false;
    public static boolean glideWeaponSwapEnabled = false;
    public static boolean elytraChestSwapEnabled = false;
    public static boolean shieldStunEnabled = true;  // NEW
    public static boolean shieldDrainFallback = true;  // NEW
    public static int axeSlot = -1;  // -1 = auto search  // NEW — default OFF
    public static int glideWeaponChoice = 0;               // NEW — 0=Mace, 1=Sword

    public static boolean elytraLaunchKeyPressed = false;
    public static boolean windChargeBoostKeyPressed = false;


    public static int swapDelay = 1;
    public static int revertDelay = 1;
    public static int hitTriggerDelay = 1;
    public static float fallDistanceThreshold = 1.5f;
    public static int autoPearlCatchDelay = 2;
    public static int breachDelayTicks = 2; // ✅ Ye line add karein
    public static boolean useCustomTextures = true; // Texture toggle ke liye
    public static int densitySwapDelay = 1;  // 0.05s wait before holding mace
    public static int breachSwapDelay = 1;   // 0.05s wait before holding mace
    public static int revertDelayTicks = 2;  // 0.1s wait before switching back to sword
    public static boolean randomizePearlDelay = true;
    public static int minPearlCatchDelay = 2;
    public static int maxPearlCatchDelay = 4;
}
