package com.darkona.adventurebackpack.util;

import com.darkona.adventurebackpack.reference.LoadedMods;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TinkersUtils {

    public static final ResourceLocation GUI_ICONS = new ResourceLocation("tinker", "textures/gui/icons.png");

    private static final String PACKAGE_TCONSTRUCT = "tconstruct";
    private static final String PACKAGE_TOOLS = "tconstruct.items.tools";
    private static final String PACKAGE_AMMO = "tconstruct.weaponry.ammo"; // arrows and bolts
    private static final String PACKAGE_WEAPONS = "tconstruct.weaponry.weapons"; // bows, crossbows, throwing weapons

    private TinkersUtils() {}

    public static boolean isToolOrWeapon(@Nullable ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        final String cn = stack.getItem().getClass().getName();
        return cn.startsWith(PACKAGE_TCONSTRUCT)
               && (cn.startsWith(PACKAGE_TOOLS) || cn.startsWith(PACKAGE_WEAPONS) || cn.startsWith(PACKAGE_AMMO));
    }

    public static boolean isTool(@Nonnull ItemStack stack) {
        return LoadedMods.TCONSTRUCT && stack.getItem().getClass().getName().startsWith(PACKAGE_TOOLS);
    }

    public static boolean isTool(String clazzName) {
        return LoadedMods.TCONSTRUCT && clazzName.startsWith(PACKAGE_TOOLS);
    }

    public static float getToolRotationAngle(ItemStack stack, boolean isLowerSlot) {
        return isLowerSlot ? -45F : 45F;
    }
}
