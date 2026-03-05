package com.darkona.adventurebackpack.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;

import com.darkona.adventurebackpack.config.ConfigHandler;
import com.darkona.adventurebackpack.reference.LoadedMods;

public final class PotionAndEnchantUtils {

    // -3 - disabled by config
    // -2 - mod not found
    // -1 - enchantment not found
    private static final int SOUL_BOUND_ID = setSoulBoundID();
    private static final int TRANSLUCENCY_ID = setTranslucencyID();
    private static final int STICKY_ITEMS_ID = setStickyItemsID();

    private PotionAndEnchantUtils() {}

    private static int setSoulBoundID() {
        if (!ConfigHandler.allowSoulBound) return -3;

        if (!LoadedMods.ENDERIO) return -2;

        for (Enchantment ench : Enchantment.enchantmentsList)
            if (ench != null && ench.getName().equals("enchantment.enderio.soulBound")) return ench.effectId;

        return -1;
    }

    private static int setTranslucencyID() {
        if (!ConfigHandler.allowTranslucency) return -3;

        if (!LoadedMods.WITCHINGGADGETS) return -2;

        for (Enchantment ench : Enchantment.enchantmentsList)
            if (ench != null && ench.getName().equals("enchantment.wg.invisibleGear")) return ench.effectId;

        return -1;
    }

    private static int setStickyItemsID() {
        if (!ConfigHandler.allowStickyItems) return -3;

        if (!LoadedMods.WITCHERY) return -2;

        for (Potion potion : Potion.potionTypes)
            if (potion != null && potion.getName().equals("witchery:potion.keepinventory")) return potion.getId();

        return -1;
    }

    public static boolean isSoulBounded(ItemStack stack) {
        if (SOUL_BOUND_ID > 0) return EnchantmentHelper.getEnchantmentLevel(SOUL_BOUND_ID, stack) > 0;
        else return false;
    }

    public static int getTranslucencyLevel(ItemStack stack) {
        if (TRANSLUCENCY_ID > 0) return EnchantmentHelper.getEnchantmentLevel(TRANSLUCENCY_ID, stack);
        else return 0;
    }

    public static boolean isSoulBook(ItemStack book) {
        if (EnchantmentHelper.getEnchantments(book).size() == 1) { // only pure soulbook allowed
            return EnchantmentHelper.getEnchantments(book).get(SOUL_BOUND_ID) != null;
        }
        return false;
    }

    public static boolean isTranslucencyBook(ItemStack book) {
        if (EnchantmentHelper.getEnchantments(book).size() == 1) {
            return EnchantmentHelper.getEnchantments(book).get(TRANSLUCENCY_ID) != null;
        }
        return false;
    }

    public static boolean hasStickyItems(EntityPlayer player) {
        return (STICKY_ITEMS_ID > 0) && player.isPotionActive(STICKY_ITEMS_ID);
    }
}
