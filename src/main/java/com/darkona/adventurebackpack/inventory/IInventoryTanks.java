package com.darkona.adventurebackpack.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidTank;

public interface IInventoryTanks extends IAsynchronousInventory {

    ItemStack[] getInventory();

    FluidTank[] getTanksArray();

    int[] getSlotsOnClosing();

    void loadFromNBT(NBTTagCompound compound);

    void saveToNBT(NBTTagCompound compound);

    void dirtyInventory();

    boolean updateTankSlots();

    void dirtyTanks();

    default ItemStack getParentItem() {
        return null;
    }
}
