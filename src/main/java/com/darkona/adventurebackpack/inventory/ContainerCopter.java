package com.darkona.adventurebackpack.inventory;

import static com.darkona.adventurebackpack.common.Constants.Copter.BUCKET_IN;
import static com.darkona.adventurebackpack.common.Constants.Copter.BUCKET_OUT;

import com.darkona.adventurebackpack.playerProperties.BackpackProperty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidTank;

import com.darkona.adventurebackpack.common.Constants.Source;

public class ContainerCopter extends ContainerAdventure {

    private static final int COPTER_INV_START = PLAYER_INV_END + 1;
    
    // client-side interpolation state
    private float fuelAnchorAmount = -1;
    private long fuelAnchorTick = 0;
    private float fuelStepAmount = 0; // consumption per 3-tick interval   
    
    public ContainerCopter(EntityPlayer player, InventoryCopterPack copter, Source source) {
        super(player, copter, source);
        makeSlots(player.inventory);
        inventory.openInventory();
    }

    private void makeSlots(InventoryPlayer invPlayer) {
        bindPlayerInventory(invPlayer, 8, 84);

        addSlotToContainer(new SlotFluidFuel(inventory, BUCKET_IN, 44, 23));
        addSlotToContainer(new SlotFluidFuel(inventory, BUCKET_OUT, 44, 53));
    }

    @Override
    public boolean transferStackToPack(ItemStack stack) {
        if (SlotFluid.isContainer(stack) && !skipFluidSlots) {
            FluidTank fuelTank = ((InventoryCopterPack) inventory).getFuelTank();
            ItemStack stackOut = getSlot(COPTER_INV_START + 1).getStack();

            boolean isFuelTankEmpty = SlotFluid.isEmpty(fuelTank);
            boolean suitableToTank = SlotFluid.isEqualAndCanFit(stack, fuelTank);
            boolean areSameType = InventoryActions.areContainersOfSameType(stack, stackOut);

            if (SlotFluid.isFilled(stack)) {
                if ((stackOut == null || areSameType) && SlotFluidFuel.isValidItem(stack))
                    if (isFuelTankEmpty || suitableToTank) return mergeBucket(stack);
            } else if (SlotFluid.isEmpty(stack)) {
                if ((stackOut == null || areSameType) && SlotFluidFuel.isValidItem(stack))
                    if (!isFuelTankEmpty) return mergeBucket(stack);
            }
        }
        return true;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (source != Source.TILE) {
            if (source == Source.WEARING) {
                inventory.openInventory();
            }

            ItemStack parentItem = inventory.getParentItem();
            if (parentItem != null) {
                ItemStack stack = source == Source.HOLDING ? player.getCurrentEquippedItem()
                        : BackpackProperty.get(player).getWearable();
                if (!ItemStack.areItemStacksEqual(stack, parentItem)) {
                    player.closeScreen();
                    return;
                }
            }

            if (requestedUpdate && player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).sendContainerAndContentsToPlayer(this, this.getInventory());
                requestedUpdate = false;
            }

            // only item changes trigger the full resync now
            if (detectItemChanges()) {
                requestedUpdate = true;
            }

            // fluid gets its own dedicated update path. No reason for the copter pack to be using the weird fluid one
            if (detectFluidChanges() && player instanceof EntityPlayerMP) {
                int amount = inventory.getTanksArray()[0].getFluidAmount();
                for (Object listener : crafters) {
                    ((ICrafting) listener).sendProgressBarUpdate(this, 0, amount);
                }
            }
        }
    }
    
    // We abuse this to show the fluid amount in the tank. Idk a better way to do this.
    @Override
    public void updateProgressBar(int id, int value) {
        if (id == 0) {
            long currentTick = player.worldObj.getTotalWorldTime();

            if (fuelAnchorAmount >= 0 && value < fuelAnchorAmount) {
                long elapsedTicks = currentTick - fuelAnchorTick;
                long stepsOccurred = Math.max(1, elapsedTicks / 3);
                fuelStepAmount = (fuelAnchorAmount - value) / stepsOccurred;
            } else {
                // first sync, or a refuel/increase - nothing to step from
                fuelStepAmount = 0;
            }

            fuelAnchorAmount = value;
            fuelAnchorTick = currentTick;
        }
    }

    public int getInterpolatedFuelAmount(int capacity) {
        if (fuelAnchorAmount < 0) return 0;

        long currentTick = player.worldObj.getTotalWorldTime();
        long elapsedTicks = currentTick - fuelAnchorTick;
        long stepsElapsed = elapsedTicks / 3;

        float displayed = fuelAnchorAmount - fuelStepAmount * stepsElapsed;
        if (displayed < 0) displayed = 0;
        if (displayed > capacity) displayed = capacity;
        return Math.round(displayed);
    }
    
    private boolean mergeBucket(ItemStack stack) {
        return mergeItemStack(stack, COPTER_INV_START, COPTER_INV_START + 1, false);
    }
}
