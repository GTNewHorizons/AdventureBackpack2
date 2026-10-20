package com.darkona.adventurebackpack.nei;

import com.darkona.adventurebackpack.client.gui.GuiAdvBackpack;
import com.darkona.adventurebackpack.reference.ModInfo;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIConfig implements IConfigureNEI {
    /**
     * Starting X coordinates for crafting grid in backpack
     */
    final static private int X_START = 127;

    /**
     * Starting Y coordinates for crafting grid in backpack
     */
    final static private int Y_START = 55;



    @Override
    public void loadConfig() {
        API.registerGuiOverlay(GuiAdvBackpack.class, "crafting", X_START, Y_START);
        API.registerGuiOverlayHandler(GuiAdvBackpack.class, new BackpackOverlayHandler(X_START, Y_START), "crafting");
    }

    @Override
    public String getName() {
        return ModInfo.MOD_NAME;
    }

    @Override
    public String getVersion() {
        return ModInfo.MOD_VERSION;
    }
}
