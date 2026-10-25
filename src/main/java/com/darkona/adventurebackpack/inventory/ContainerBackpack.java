package com.darkona.adventurebackpack.inventory;

import static com.darkona.adventurebackpack.common.Constants.BUCKET_IN_LEFT;
import static com.darkona.adventurebackpack.common.Constants.BUCKET_IN_RIGHT;
import static com.darkona.adventurebackpack.common.Constants.BUCKET_OUT_LEFT;
import static com.darkona.adventurebackpack.common.Constants.BUCKET_OUT_RIGHT;
import static com.darkona.adventurebackpack.common.Constants.TOOL_LOWER;
import static com.darkona.adventurebackpack.common.Constants.TOOL_UPPER;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraftforge.fluids.FluidTank;

import com.darkona.adventurebackpack.common.Constants;
import com.darkona.adventurebackpack.common.Constants.Source;
import com.darkona.adventurebackpack.config.ConfigHandler;
import com.darkona.adventurebackpack.util.TinkerCraftingStationBridge;
import com.darkona.adventurebackpack.util.TinkersUtils;

public class ContainerBackpack extends ContainerAdventure {

    private static final int BACK_INV_ROWS = 6;
    private static final int BACK_INV_COLUMNS = 8;
    private static final int BACK_INV_START = PLAYER_INV_END + 1;
    private static final int BACK_INV_END = PLAYER_INV_END + (BACK_INV_ROWS * BACK_INV_COLUMNS);
    private static final int TOOL_START = BACK_INV_END + 1;
    private static final int TOOL_END = TOOL_START + 1;
    private static final int BUCKET_LEFT = TOOL_END + 1;
    private static final int BUCKET_RIGHT = BUCKET_LEFT + 2;
    private static final int MATRIX_DIMENSION = 3;
    private static final int CRAFT_RESULT = BUCKET_RIGHT + 2 + (MATRIX_DIMENSION * MATRIX_DIMENSION);
    private static final int[] CRAFT_MATRIX_EMULATION = findCraftMatrixEmulationIDs();

    // since it's public I made it unmodifiable
    public static final Set<Integer> CRAFT_MATRIX_EMULATION_SET =
            Collections.unmodifiableSet(findCraftMatrixEmulationIDsAsSet());

    /**
     * Container indices of the backpack storage slots, excluding the 3x3 block that mirrors the hidden crafting matrix.
     * Items merged into those slots would land in the crafting grid. Computed once; do not modify.
     */
    private static final int[] BACKPACK_INV_IDS = backpackInvIDs();

    protected final InventoryCraftingBackpack craftMatrix = new InventoryCraftingBackpack(
            this,
            MATRIX_DIMENSION,
            MATRIX_DIMENSION);
    private final IInventory craftResult = new InventoryCraftResult();
    private final TinkerCraftingStationBridge tinkerCraftingStationBridge = new TinkerCraftingStationBridge();

    public ContainerBackpack(EntityPlayer player, IInventoryBackpack backpack, Source source) {
        super(player, backpack, source);
        makeSlots(player.inventory);
        inventory.openInventory();
    }

    public IInventoryBackpack getInventoryBackpack() {
        return (IInventoryBackpack) inventory;
    }

    private void makeSlots(InventoryPlayer invPlayer) {
        bindPlayerInventory(invPlayer, 44, 125); // 4*9 playerInventory, 36 Slots [0-35]

        int startX = 62;
        int startY = 7;
        for (int row = 0; row < BACK_INV_ROWS; row++) // 6*8 inventory, 48 Slots (#1-#48) [36-83]
            for (int col = 0; col < BACK_INV_COLUMNS; col++) addSlotToContainer(
                    new SlotBackpack(
                            inventory,
                            (row * BACK_INV_COLUMNS + col),
                            (startX + 18 * col),
                            (startY + 18 * row)));

        addSlotToContainer(new SlotTool(inventory, TOOL_UPPER, 44, 79)); // #49 [84]
        addSlotToContainer(new SlotTool(inventory, TOOL_LOWER, 44, 97)); // #50 [85]

        addSlotToContainer(new SlotFluid(inventory, BUCKET_IN_LEFT, 6, 7)); // #51 [86]
        addSlotToContainer(new SlotFluid(inventory, BUCKET_OUT_LEFT, 6, 37)); // #52 [87]
        addSlotToContainer(new SlotFluid(inventory, BUCKET_IN_RIGHT, 226, 7)); // #53 [88]
        addSlotToContainer(new SlotFluid(inventory, BUCKET_OUT_RIGHT, 226, 37)); // #54 [89]

        startX = 215;
        startY = -2500; // startY = LoadedMods.DEV_ENV ? 125 : -2500;
        for (int row = 0; row < MATRIX_DIMENSION; row++) // craftMatrix, usually 9 slots, [90-98]
            for (int col = 0; col < MATRIX_DIMENSION; col++) addSlotToContainer(
                    new SlotCraftMatrix(
                            craftMatrix,
                            (row * MATRIX_DIMENSION + col),
                            (startX + 18 * col),
                            (startY + 18 * row)));

        addSlotToContainer(new SlotCraftResult(this, invPlayer.player, craftMatrix, craftResult, 0, 226, 97)); // craftResult
        // [99]
        syncCraftMatrixWithInventory(true);
    }

    @Override
    public void detectAndSendChanges() {
        syncCraftMatrixWithInventory(true);
        syncCraftResultToClient();
        super.detectAndSendChanges();
    }

    /**
     * Sends the craft result slot to the client when it changes.
     * <p>
     * Vanilla never syncs crafting result slots ({@code EntityPlayerMP.sendSlotContents} skips {@code SlotCrafting})
     * because the client normally computes the result itself. Here the client's hidden craft matrix is only refilled on
     * clicks, so its result can go stale, and the server pushes the real one instead.
     * <p>
     * Only this one slot is sent. A full container resync here would overwrite the client's predicted state in the
     * middle of a burst of clicks (e.g. NEI filling the grid), and later quantity-only updates are suppressed, which
     * left a ghost stack on the cursor.
     */
    @SuppressWarnings("unchecked")
    private void syncCraftResultToClient() {
        ItemStack stackA = ((Slot) inventorySlots.get(CRAFT_RESULT)).getStack();
        ItemStack stackB = (ItemStack) inventoryItemStacks.get(CRAFT_RESULT);

        if (!ItemStack.areItemStacksEqual(stackB, stackA)) {
            stackB = stackA == null ? null : stackA.copy();
            inventoryItemStacks.set(CRAFT_RESULT, stackB);
            if (player instanceof EntityPlayerMP) {
                S2FPacketSetSlot packet = new S2FPacketSetSlot(this.windowId, CRAFT_RESULT, stackB);
                ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
            }
        }
    }

    @Override
    protected boolean transferStackToPack(ItemStack stack) {
        if (SlotTool.isValidTool(stack)) {
            if (!mergeToolSlot(stack)) if (SlotBackpack.isValidItem(stack)) return mergeBackpackInv(stack);
        } else if (SlotFluid.isContainer(stack) && !isHoldingSpace() && !skipFluidSlots) {
            return transferFluidContainer(stack);
        } else if (SlotBackpack.isValidItem(stack)) {
            return mergeBackpackInv(stack);
        }
        return true;
    }

    @Override
    protected boolean transferFromSlot(int fromSlot, ItemStack stack) {
        final int localFromSlot = fromSlot - BACK_INV_START;

        final boolean slotIndexIsInCraftingMatrix = CRAFT_MATRIX_EMULATION_SET.contains(localFromSlot);
        if (fromSlot < PLAYER_INV_LENGTH) {
            return transferStackToPack(stack);
        } else if (slotIndexIsInCraftingMatrix) {
            return transferToPackThenPlayerInv(stack);
        } else {
            return mergePlayerInv(stack);
        }
    }

    /**
     * Moves a stack from the emulated crafting grid into backpack storage, sending whatever doesn't fit to the player
     * inventory. This keeps grid items inside the backpack, including when NEI clears the grid by shift-clicking each
     * slot.
     *
     * @param stack the source slot's stack; its size is reduced by what was moved
     * @return true if any items moved to either inventory
     */
    private boolean transferToPackThenPlayerInv(ItemStack stack) {
        boolean changed = mergeBackpackInv(stack);
        if (stack.stackSize != 0) {
            changed |= mergePlayerInv(stack);
        }
        return changed;
    }

    private boolean mergeToolSlot(ItemStack stack) {
        return mergeItemStack(stack, TOOL_START, TOOL_END + 1, false);
    }

    private boolean mergeBackpackInv(ItemStack stack) {
        return mergeItemStack(stack, BACKPACK_INV_IDS);
    }

    private boolean mergeLeftBucket(ItemStack stack) {
        return mergeItemStack(stack, BUCKET_LEFT, BUCKET_LEFT + 1, false);
    }

    private boolean mergeRightBucket(ItemStack stack) {
        return mergeItemStack(stack, BUCKET_RIGHT, BUCKET_RIGHT + 1, false);
    }

    private boolean isHoldingSpace() {
        return getInventoryBackpack().getExtendedProperties().hasKey(Constants.TAG_HOLDING_SPACE);
    }

    private boolean transferFluidContainer(ItemStack container) {
        FluidTank leftTank = getInventoryBackpack().getLeftTank();
        FluidTank rightTank = getInventoryBackpack().getRightTank();
        ItemStack leftStackOut = getSlot(BUCKET_LEFT + 1).getStack();
        ItemStack rightStackOut = getSlot(BUCKET_RIGHT + 1).getStack();

        boolean isLeftTankEmpty = SlotFluid.isEmpty(leftTank);
        boolean isRightTankEmpty = SlotFluid.isEmpty(rightTank);
        boolean suitableToLeft = SlotFluid.isEqualAndCanFit(container, leftTank);
        boolean suitableToRight = SlotFluid.isEqualAndCanFit(container, rightTank);
        boolean areLeftSameType = InventoryActions.areContainersOfSameType(container, leftStackOut);
        boolean areRightSameType = InventoryActions.areContainersOfSameType(container, rightStackOut);

        if (SlotFluid.isFilled(container)) {
            if (isLeftTankEmpty) {
                if (!isRightTankEmpty && suitableToRight && (rightStackOut == null || areRightSameType))
                    return mergeRightBucket(container);
                else if (leftStackOut == null || areLeftSameType) return mergeLeftBucket(container);
            } else {
                if (suitableToLeft && (leftStackOut == null || areLeftSameType)) return mergeLeftBucket(container);
                else if ((isRightTankEmpty || suitableToRight) && (rightStackOut == null || areRightSameType))
                    return mergeRightBucket(container);
                else if (leftStackOut == null && rightStackOut == null && SlotBackpack.isValidItem(container))
                    return mergeBackpackInv(container);
            }
        } else if (SlotFluid.isEmpty(container)) {
            if (isLeftTankEmpty) {
                if (!isRightTankEmpty && (rightStackOut == null || areRightSameType))
                    return mergeRightBucket(container);
                else if (leftStackOut == null && rightStackOut == null && SlotBackpack.isValidItem(container))
                    return mergeBackpackInv(container);
            } else {
                if (leftStackOut == null || areLeftSameType) return mergeLeftBucket(container);
            }
        }
        return false;
    }

    @Override
    public ItemStack slotClick(int slot, int button, int flag, EntityPlayer player) {
        final ItemStack result = super.slotClick(slot, button, flag, player);
        syncCraftMatrixWithInventory(true);
        return result;
    }

    @Override
    protected void dropContentOnClose() {
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            this.craftMatrix.setInventorySlotContentsNoUpdate(i, null);
        }
        onCraftMatrixChanged(craftMatrix);

        super.dropContentOnClose();
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        if (ConfigHandler.tinkerToolsMaintenance && TinkersUtils.isToolOrWeapon(craftMatrix.getStackInSlot(4))) {
            craftResult.setInventorySlotContents(0, this.tinkerCraftingStationBridge.getTinkersRecipe(craftMatrix));
        } else {
            craftResult.setInventorySlotContents(
                    0,
                    CraftingManager.getInstance().findMatchingRecipe(craftMatrix, player.worldObj));
        }
    }

    protected void syncCraftMatrixWithInventory(boolean preCraft) {
        boolean changesMade = false;

        for (int craftSlotID = 0; craftSlotID < CRAFT_MATRIX_EMULATION.length; craftSlotID++) {
            int invSlotID = CRAFT_MATRIX_EMULATION[craftSlotID];
            ItemStack invStack = inventory.getStackInSlot(invSlotID);
            ItemStack craftStack = craftMatrix.getStackInSlot(craftSlotID);

            if (!ItemStack.areItemStacksEqual(invStack, craftStack)) {
                if (preCraft) {
                    craftStack = invStack == null ? null : invStack.copy();
                    craftMatrix.setInventorySlotContentsNoUpdate(craftSlotID, craftStack);
                } else {
                    invStack = craftStack == null ? null : craftStack.copy();
                    inventory.setInventorySlotContentsNoSave(invSlotID, invStack);
                }
                changesMade = true;
            }
        }

        if (changesMade) {
            if (preCraft) onCraftMatrixChanged(craftMatrix);
            else inventory.markDirty();
        }
    }

    /**
     *
     * @return Crafting matrix emulation indices as a Set
     */
    public static Set<Integer> findCraftMatrixEmulationIDsAsSet() {
        final Set<Integer> emulatedCraftingMatrixIndicesAsSet = new HashSet<>();
        for (int i : CRAFT_MATRIX_EMULATION) {
            emulatedCraftingMatrixIndicesAsSet.add(i);
        }
        return emulatedCraftingMatrixIndicesAsSet;
    }

    /**
     * Returns the array of inventory slotIDs, emulates the craftMatrix in the lower right corner of the inventory.
     */
    private static int[] findCraftMatrixEmulationIDs() {
        int[] slotsArray = new int[MATRIX_DIMENSION * MATRIX_DIMENSION];
        for (int row = 0; row < MATRIX_DIMENSION; row++) {
            for (int col = 0; col < MATRIX_DIMENSION; col++) {
                int id = row * MATRIX_DIMENSION + col;
                int fullRowSum = (BACK_INV_ROWS - (MATRIX_DIMENSION - 1) + row) * BACK_INV_COLUMNS;
                int columnShift = (MATRIX_DIMENSION - 1) - col;
                slotsArray[id] = fullRowSum - columnShift - 1;
            }
        }
        return slotsArray;
    }

    private static int[] backpackInvIDs() {
        final int totalCraftingAreaSize = MATRIX_DIMENSION * MATRIX_DIMENSION;
        final int backpackInvSize = BACK_INV_ROWS * BACK_INV_COLUMNS - totalCraftingAreaSize;
        final int[] backpackInvIDs = new int[backpackInvSize];

        int numberOfCraftingSlotsPassed = 0;

        for (int i = 0; i < BACK_INV_ROWS; i++) {
            for (int j = 0; j < BACK_INV_COLUMNS; j++) {
                final int slotIndex = i * BACK_INV_COLUMNS + j;
                if (!CRAFT_MATRIX_EMULATION_SET.contains(slotIndex)) {
                    final int indexToSet = slotIndex - numberOfCraftingSlotsPassed;
                    backpackInvIDs[indexToSet] = slotIndex + BACK_INV_START;
                    continue;
                }
                numberOfCraftingSlotsPassed++;
            }
        }
        return backpackInvIDs;
    }
}
