package com.darkona.adventurebackpack.nei;

import java.util.Set;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import com.darkona.adventurebackpack.inventory.SlotBackpack;

import codechicken.nei.recipe.DefaultOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;

public class BackpackOverlayHandler extends DefaultOverlayHandler {

    public BackpackOverlayHandler() {
        super(127, 55);
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
        // The crafting grid is the bottom right 3x3 of the backpack storage, which NEI would take for one big grid
        // spanning the whole storage and empty it
        Set<Slot> slots = super.getCraftMatrixSlots(gui, handler);
        slots.removeIf(slot -> slot.xDisplayPosition < offsetx + 25 || slot.yDisplayPosition < offsety + 6);
        return slots;
    }
}
