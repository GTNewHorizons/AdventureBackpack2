package com.darkona.adventurebackpack.nei;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.darkona.adventurebackpack.client.gui.GuiAdvBackpack;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IStackPositioner;

public class BackpackStackPositioner implements IStackPositioner {

    /**
     *
     * @param stacks The item stacks the GUI wants to overlay
     * @return ArrayList of stacks that include the offset of the crafting area in the backpack if it's an adventure
     *         backpack else it returns the default positions.
     */
    @Override
    public ArrayList<PositionedStack> positionStacks(ArrayList<PositionedStack> stacks) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (!(screen instanceof GuiAdvBackpack)) {
            return stacks;
        }
        for (PositionedStack stack : stacks) {
            stack.relx += 127;
            stack.rely += 55;
        }
        return stacks;
    }
}
