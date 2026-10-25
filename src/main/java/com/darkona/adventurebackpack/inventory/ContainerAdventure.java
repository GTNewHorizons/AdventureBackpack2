package com.darkona.adventurebackpack.inventory;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.darkona.adventurebackpack.common.Constants.Source;

@SuppressWarnings("WeakerAccess")
public abstract class ContainerAdventure extends Container {

    protected static final int PLAYER_INV_ROWS = 3;
    protected static final int PLAYER_INV_COLUMNS = 9;
    protected static final int PLAYER_HOT_START = 0;
    protected static final int PLAYER_HOT_END = PLAYER_HOT_START + PLAYER_INV_COLUMNS - 1;
    protected static final int PLAYER_INV_END = PLAYER_HOT_END + PLAYER_INV_COLUMNS * PLAYER_INV_ROWS;
    protected static final int PLAYER_INV_LENGTH = PLAYER_INV_END + 1;

    protected final EntityPlayer player;
    protected final IInventoryTanks inventory;
    protected final Source source;

    private final int[] fluidsAmount;
    private int itemsCount;
    private boolean requestedUpdate;
    public boolean skipFluidSlots;

    protected ContainerAdventure(EntityPlayer player, IInventoryTanks inventory, Source source) {
        this.player = player;
        this.inventory = inventory;
        this.source = source;
        this.fluidsAmount = new int[this.inventory.getTanksArray().length];
    }

    protected void bindPlayerInventory(InventoryPlayer invPlayer, int startX, int startY) {
        for (int col = 0; col < PLAYER_INV_COLUMNS; col++) // hotbar - 9 slots
            addSlotToContainer(new Slot(invPlayer, col, (startX + 18 * col), (58 + startY)));

        for (int row = 0; row < PLAYER_INV_ROWS; row++) // inventory - 3*9, 27 slots
            for (int col = 0; col < PLAYER_INV_COLUMNS; col++) addSlotToContainer(
                    new Slot(
                            invPlayer,
                            (PLAYER_INV_COLUMNS + row * PLAYER_INV_COLUMNS + col),
                            (startX + 18 * col),
                            (startY + row * 18)));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (source == Source.HOLDING) // used for refresh tooltips and redraw tanks content while GUI is open
        {
            // check if parent item is gone
            ItemStack parentItem = inventory.getParentItem();
            if (parentItem != null) {
                if (!ItemStack.areItemStacksEqual(player.getCurrentEquippedItem(), parentItem)) {
                    player.closeScreen();
                    return;
                }
            }

            // intentionally update container with 1 tick delay after detect changes due to visual glitches
            // in rare cases on some modded items, ex.: shift+q on blood magic lava crystals
            if (requestedUpdate && player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).sendContainerAndContentsToPlayer(this, this.getInventory());
                requestedUpdate = false;
            }

            if (detectItemChanges() | detectFluidChanges()) {
                requestedUpdate = true;
            }
        }
    }

    protected boolean detectItemChanges() {
        ItemStack[] inv = inventory.getInventory();
        int tempCount = 0;
        for (int i = 0; i < inv.length - inventory.getSlotsOnClosing().length; i++) {
            if (inv[i] != null) tempCount++;
        }
        if (itemsCount != tempCount) {
            itemsCount = tempCount;
            return true;
        }
        return false;
    }

    private boolean detectFluidChanges() {
        boolean changesDetected = false;
        for (int i = 0; i < fluidsAmount.length; i++) {
            int amount = inventory.getTanksArray()[i].getFluidAmount();
            if (fluidsAmount[i] != amount) {
                fluidsAmount[i] = amount;
                changesDetected = true;
            }
        }
        return changesDetected;
    }

    @Nullable
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int fromSlot) {
        Slot slot = getSlot(fromSlot);

        if (slot == null || slot.getStack() == null) return null;

        ItemStack stack = slot.getStack();
        ItemStack result = stack.copy();

        final boolean didTransfer = transferFromSlot(fromSlot, stack);
        if (!didTransfer) return null;

        if (stack.stackSize == 0) {
            slot.putStack(null);
        } else {
            slot.onSlotChanged();
        }

        if (stack.stackSize == result.stackSize) return null;

        slot.onPickupFromSlot(player, stack);
        return result;
    }

    protected boolean transferFromSlot(int fromSlot, ItemStack stack) {
        if (fromSlot < PLAYER_INV_LENGTH) {
            return transferStackToPack(stack);
        } else {
            return mergePlayerInv(stack);
        }
    }

    protected boolean mergePlayerInv(ItemStack stack) {
        return mergeItemStack(stack, PLAYER_HOT_START, PLAYER_INV_END + 1, false);
    }

    protected abstract boolean transferStackToPack(ItemStack stack);

    @Nullable
    @Override
    public ItemStack slotClick(int slot, int button, int flag, EntityPlayer player) {
        if (source == Source.HOLDING && slot >= 0) {
            if (getSlot(slot) != null && getSlot(slot).getStack() == player.getHeldItem()) {
                return null;
            }
            if (flag == 2 && getSlot(button).getStack() == player.getHeldItem()) {
                return null;
            }
        }
        return super.slotClick(slot, button, flag, player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return inventory.isUseableByPlayer(player);
    }


    @Override
    protected boolean mergeItemStack(ItemStack initStack, int minIndex, int maxIndex, boolean backward) {
        return mergeItemStack(initStack, getSlotIds(minIndex, maxIndex, backward));
    }

    protected boolean mergeItemStack(ItemStack initStack, int[] slotIds) {
        boolean changesMade = false;

        if (initStack.isStackable()) {
            for (int i : slotIds) {
                if (initStack.stackSize > 0) {
                    changesMade |= tryMergeTo(i, initStack);
                }
            }
        }
        if (initStack.stackSize > 0) {

            for (int i : slotIds) {
                changesMade |= moveItemsIntoEmptySlot(i, initStack);
            }
        }

        return changesMade;


    }

    private int[] getSlotIds(int minIndex, int maxIndex, boolean backward) {
        int activeIndex = (backward ? maxIndex - 1: minIndex);
        int[] slotIds = new int[maxIndex - minIndex];

        for (int i = 0; i < slotIds.length; i++) {
            slotIds[i] = activeIndex;
            activeIndex = (backward ? activeIndex - 1 : activeIndex + 1);
        }
        return slotIds;
    }

    private boolean tryMergeTo(int activeIndex, ItemStack initStack) {
        Slot activeSlot = (Slot) this.inventorySlots.get(activeIndex);
        ItemStack activeStack = activeSlot.getStack();
        if (activeStack == null) return false;

        boolean areItemsEqual = areItemsEqual(initStack, activeStack);

        if (areItemsEqual) {
            return tryMergeItemsInSlot(activeSlot, initStack);
        }
        return false;
    }

    private boolean moveItemsIntoEmptySlot(int activeIndex, ItemStack initStack) {
        Slot activeSlot = (Slot) this.inventorySlots.get(activeIndex);
        ItemStack activeStack = activeSlot.getStack();
        if (activeStack != null) return false;
        return tryPushItemsToSlot(activeSlot, initStack);
    }

    private boolean areItemsEqual(ItemStack initStack, ItemStack stack2) {
        boolean sameItem =  initStack.getItem() == stack2.getItem();
        boolean sameDamage = !initStack.getHasSubtypes() || initStack.getItemDamage() == stack2.getItemDamage();
        boolean sameTags = ItemStack.areItemStackTagsEqual(initStack, stack2);
        return  sameItem && sameDamage && sameTags;
    }

    private boolean tryMergeItemsInSlot(Slot slot, ItemStack stackToMove) {
        final int amountMoved = getAmountMovedBetweenSlots(slot, stackToMove);
        final int stackSize = stackToMove.stackSize;
        // <= is important, it protects from illegal stacks which would cause negative amounts being moved

        if (amountMoved <= 0) return false;
        if (stackSize > amountMoved && (slot instanceof SlotFluid)) return false; // reject fluid partial move
        mergeStackWithSlot(slot, stackToMove, amountMoved);
        return true;
    }


    private boolean tryPushItemsToSlot(Slot slot, ItemStack stackToMove) {
        final int amountMoved = getAmountMovedBetweenSlots(slot, stackToMove);

        if (amountMoved <= 0) return false;
        pushItemsToSlot(slot, stackToMove, amountMoved);
        return true;
    }

    private void pushItemsToSlot(Slot slot, ItemStack stackToMove, int amountMoved) {
        ItemStack stack = stackToMove.copy();
        stackToMove.stackSize -= amountMoved;
        stack.stackSize = amountMoved;
        slot.putStack(stack);
    }

    private void mergeStackWithSlot(Slot slot, ItemStack stackToMove, int amountMoved) {
        final ItemStack slotStack = slot.getStack();
        slotStack.stackSize += amountMoved;
        stackToMove.stackSize -= amountMoved;
        slot.onSlotChanged();
    }


    private int getAmountMovedBetweenSlots(Slot slot, ItemStack stackToMove) {
        final int addedStackSize = stackToMove.stackSize;
        final int maxStackSize = maxStackSize(slot, stackToMove);

        if (!slot.getHasStack()) return Math.min(addedStackSize, maxStackSize);

        final ItemStack slotStack = slot.getStack();
        final int slotStackSize = slotStack.stackSize;

        final int availableSize = maxStackSize - slotStackSize;
        return Math.min(addedStackSize, availableSize);
    }

    private int maxStackSize(Slot slot, ItemStack stack) {
        final int maxSlotStackSize = slot.getSlotStackLimit();

        final int maxStackSizeSlot = stack.getMaxStackSize();
        return Math.min(maxStackSizeSlot, maxSlotStackSize);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);

        if (source == Source.WEARING) // TODO no idea why this is here (preventing dupe on closing?), and why only for
        // wearing
        {
            this.crafters.remove(player);
        }

        if (!player.worldObj.isRemote) {
            dropContentOnClose();
        }
    }

    protected void dropContentOnClose() {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack itemstack = inventory.getStackInSlotOnClosing(i);
            if (itemstack != null) {
                inventory.setInventorySlotContents(i, null);
                player.dropPlayerItemWithRandomChoice(itemstack, false);
            }
        }
    }
}
