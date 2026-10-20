package com.darkona.adventurebackpack.nei;

import static com.darkona.adventurebackpack.inventory.ContainerBackpack.findCraftMatrixEmulationIDsAsSet;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import com.darkona.adventurebackpack.inventory.SlotBackpack;

import codechicken.nei.recipe.DefaultOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;

public class BackpackOverlayHandler extends DefaultOverlayHandler {

    public BackpackOverlayHandler(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean canMoveFrom(Slot slot, GuiContainer gui) {
        if (slot instanceof SlotBackpack) {
            return true;
        }
        return super.canMoveFrom(slot, gui);
    }

    /**
     * This method bootstraps the function the backpack uses to declare the slots that are used to emulate the crafting.
     * The overridden function searches the grid and assumes all adjacent slots are also parts of the crafting. The
     * overridden function would empty the backpack since it assumed all slots are for crafting and would only leave the
     * correct items.
     * 
     * @param gui     The graphical user interface that's displayed on the screen
     * @param handler The recipe that the user is trying to apply/overlay
     * @return Set of slots representing the crafting area
     */
    @Override
    protected Set<Slot> getCraftMatrixSlots(GuiContainer gui, IRecipeHandler handler) {
        final Set<Integer> emulatedCraftingMatrixInsideBackpack = findCraftMatrixEmulationIDsAsSet();
        final Set<Slot> slots = new HashSet<>();

        for (Object obj : gui.inventorySlots.inventorySlots) {
            if (!(obj instanceof SlotBackpack)) continue;
            Slot slot = (SlotBackpack) obj;

            final int slotIndex = slot.getSlotIndex();
            if (emulatedCraftingMatrixInsideBackpack.contains(slotIndex)) {
                slots.add(slot);
            }
        }
        return slots;
    }
}
