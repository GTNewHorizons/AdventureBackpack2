package com.darkona.adventurebackpack.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.darkona.adventurebackpack.inventory.ContainerAdventure;
import com.darkona.adventurebackpack.util.EnchUtils;

@SuppressWarnings("WeakerAccess")
public abstract class ItemAdventure extends ItemAB implements IBackWearableItem {

    public ItemAdventure() {
        super();
        setFull3D();
        setMaxStackSize(1);
        setMaxDamage(1000);
    }

    /*
     * @Override public boolean isDamageable() { return false; }
     */

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player) {
        if (stack != null && player instanceof EntityPlayerMP && player.openContainer instanceof ContainerAdventure)
            player.closeScreen();

        return super.onDroppedByPlayer(stack, player);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return EnchUtils.isSoulBook(book) || EnchUtils.isTranslucencyBook(book);

    }
}
