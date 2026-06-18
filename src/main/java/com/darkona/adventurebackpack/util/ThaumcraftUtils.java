package com.darkona.adventurebackpack.util;

import net.minecraft.item.ItemStack;

import com.darkona.adventurebackpack.reference.LoadedMods;

public final class ThaumcraftUtils {

    public static final boolean DIAL_BOTTOM = setDialBottom();

    private static final String CLASS_CONFIG = "thaumcraft.common.config.Config";
    private static final String CLASS_WANDS = "thaumcraft.common.items.wands.ItemWandCasting";
    private static final String FIELD_DIAL_BOTTOM = "dialBottom";

    private ThaumcraftUtils() {}

    private static boolean setDialBottom() {
        if (!LoadedMods.THAUMCRAFT || Utils.inServer()) return false;

        try {
            return Class.forName(CLASS_CONFIG).getField(FIELD_DIAL_BOTTOM).getBoolean(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }

    public static boolean isTool(ItemStack stack) {
        if (!LoadedMods.THAUMCRAFT || stack == null) return false;

        try {
            return Class.forName(CLASS_WANDS).isInstance(stack.getItem());
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static float getToolRotationAngle(ItemStack stack, boolean isLowerSlot) {
        return isLowerSlot ? 0F : 90F;
    }
}
