package com.darkona.adventurebackpack.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.modifier.IModifyable;
import tconstruct.library.tools.AbilityHelper;

public class SlotCraftResult extends SlotCrafting {

    private final ContainerBackpack eventHandler;

    public SlotCraftResult(ContainerBackpack container, EntityPlayer player, IInventory craftMatrix,
            IInventory inventory, int slotIndex, int posX, int posY) {
        super(player, craftMatrix, inventory, slotIndex, posX, posY);
        this.eventHandler = container;
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        eventHandler.syncCraftMatrixWithInventory(true); // pre craft sync
        ItemStack tool = eventHandler.craftMatrix.getStackInSlot(4);
        if (stack.getItem() instanceof IModifyable && tool != null && tool.getItem() instanceof IModifyable) {
            IModifyable modifyable = (IModifyable) stack.getItem();
            NBTTagCompound tags = stack.getTagCompound().getCompoundTag(modifyable.getBaseTagName());
            int[] toRemoveArray = tags.hasKey("ToRemove") ? tags.getIntArray("ToRemove") : null;
            int toRemoveIndex = 0;
            IInventory inventory = eventHandler.craftMatrix;

            for (int i = 0; i <= inventory.getSizeInventory(); i++) {
                if (i == 4) continue;
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null) {
                    continue;
                }
                if (toRemoveArray == null) {
                    inventory.decrStackSize(i, 1);
                } else {
                    inventory.decrStackSize(i, toRemoveArray[toRemoveIndex]);
                    toRemoveIndex++;
                }
            }
            tags.removeTag("ToRemove");

            inventory.setInventorySlotContents(4, null);
            player.worldObj.playSoundEffect(
                    player.posX,
                    player.posY,
                    player.posZ,
                    "tinker:little_saw",
                    1.0F,
                    (AbilityHelper.random.nextFloat() - AbilityHelper.random.nextFloat()) * 0.2F + 1.0F);
        } else {
            super.onPickupFromSlot(player, stack);
        }
        eventHandler.syncCraftMatrixWithInventory(false); // post craft sync
    }
}
