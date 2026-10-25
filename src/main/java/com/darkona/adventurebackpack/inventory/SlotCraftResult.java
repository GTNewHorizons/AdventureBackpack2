package com.darkona.adventurebackpack.inventory;

import com.darkona.adventurebackpack.reference.LoadedMods;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;


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

        boolean didHandle = LoadedMods.TCONSTRUCT && TinkerCompat.handleCraft(eventHandler, player, stack);

        if (!didHandle) {
            super.onPickupFromSlot(player, stack);
        }
        eventHandler.syncCraftMatrixWithInventory(false); // post craft sync

    }
}
