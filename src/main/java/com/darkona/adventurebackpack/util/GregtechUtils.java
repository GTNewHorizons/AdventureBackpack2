package com.darkona.adventurebackpack.util;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import com.darkona.adventurebackpack.reference.LoadedMods;

public final class GregtechUtils {

    private static final String TOOLS_NAME = "gt.metatool.01";
    private static final int[] ROTATED_45_TOOLS = { 16, 26, 30, 130 };
    private static final int[] ROTATED_90_TOOLS = { 10, 14, 18, 22, 34, 150, 160 };

    private GregtechUtils() {}

    public static boolean isTool(@Nonnull ItemStack stack) {
        return LoadedMods.GREGTECH && stack.getItem().getUnlocalizedName().equals(TOOLS_NAME);
    }

    public static boolean isTool(String itemName) {
        return LoadedMods.GREGTECH && itemName.equals(TOOLS_NAME);
    }

    public static float getToolRotationAngle(ItemStack stack, boolean isLowerSlot) {
        int meta = stack.getItemDamage();

        for (int rotated45 : ROTATED_45_TOOLS) if (meta == rotated45) return isLowerSlot ? 0F : 90F;

        for (int rotated90 : ROTATED_90_TOOLS) if (meta == rotated90) return isLowerSlot ? 45F : 135F;

        return isLowerSlot ? -45F : 45F;
    }
}
