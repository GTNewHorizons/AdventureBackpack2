package com.darkona.adventurebackpack.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.modifier.IModifyable;
import tconstruct.library.tools.AbilityHelper;

public class TinkerCompat {

    public static boolean handleCraft(ContainerBackpack eventHandler, EntityPlayer player, ItemStack stack) {
        ItemStack tool = eventHandler.craftMatrix.getStackInSlot(4);

        if (!(stack.getItem() instanceof IModifyable) || tool == null
                || !(tool.getItem() instanceof IModifyable)
                || stack.getTagCompound() == null) {
            return false;
        }
        IModifyable modifyable = (IModifyable) stack.getItem();
        NBTTagCompound tags = stack.getTagCompound().getCompoundTag(modifyable.getBaseTagName());
        int[] toRemoveArray = tags.hasKey("ToRemove") ? tags.getIntArray("ToRemove") : null;
        IInventory inventory = consumeInventoryItems(eventHandler, toRemoveArray);
        tags.removeTag("ToRemove");

        inventory.setInventorySlotContents(4, null);
        player.worldObj.playSoundEffect(
                player.posX,
                player.posY,
                player.posZ,
                "tinker:little_saw",
                1.0F,
                (AbilityHelper.random.nextFloat() - AbilityHelper.random.nextFloat()) * 0.2F + 1.0F);

        return true;
    }

    private static IInventory consumeInventoryItems(ContainerBackpack eventHandler, int[] toRemoveArray) {
        int toRemoveIndex = 0;
        IInventory inventory = eventHandler.craftMatrix;

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (i == 4) continue;
            ItemStack item = inventory.getStackInSlot(i);
            if (item == null) {
                continue;
            }
            if (toRemoveArray == null || toRemoveIndex >= toRemoveArray.length) {
                inventory.decrStackSize(i, 1);
            } else {
                inventory.decrStackSize(i, toRemoveArray[toRemoveIndex]);
                toRemoveIndex++;
            }
        }
        return inventory;
    }
}
