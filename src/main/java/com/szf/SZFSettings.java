package com.szf;

import carpet.api.settings.Rule;

public class SZFSettings {
    public static final String SZF = "carpet-SZF";
    @Rule(categories = SZF)
    public static boolean playerDropsHead = false;
    @Rule(categories = SZF)
    public static boolean endPortalInstantBreak = false;
    @Rule(categories = SZF)
    public static float endPortalFrameHardness = -1.0f;
    @Rule(categories = SZF)
    public static boolean blockPlayerDuZhaoYang = true;
    @Rule(categories = SZF)
    public static boolean decoratedPotCanExtract = false;
    @Rule(categories = SZF)
    public static int customFireworkCooldown = 0;
    @Rule(categories = SZF)
    public static boolean hopperCreativeToggle = false;
    @Rule(categories = SZF)
    public static boolean totemInInventory = false;
}
