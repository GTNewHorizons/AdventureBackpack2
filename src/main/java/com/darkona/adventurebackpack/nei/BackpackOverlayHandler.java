package com.darkona.adventurebackpack.nei;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import com.darkona.adventurebackpack.inventory.ContainerBackpack;
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

    @Override
    protected Set<Slot> getCraftMatrixSlots(GuiContainer gui, IRecipeHandler handler) {
        final Set<Integer> emulatedCraftingMatrixInsideBackpack = ContainerBackpack.CRAFT_MATRIX_EMULATION_SET;
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
