package com.darkona.adventurebackpack.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import org.lwjgl.input.Mouse;

import com.darkona.adventurebackpack.common.Constants.Source;
import com.darkona.adventurebackpack.config.Keybindings;
import com.darkona.adventurebackpack.init.ModNetwork;
import com.darkona.adventurebackpack.inventory.ContainerAdventure;
import com.darkona.adventurebackpack.inventory.SlotFluid;
import com.darkona.adventurebackpack.network.EquipUnequipBackWearablePacket;
import com.darkona.adventurebackpack.reference.LoadedMods;
import com.darkona.adventurebackpack.util.TConstructTab;

public abstract class GuiWithTanks extends GuiContainer {

    protected EntityPlayer player;
    protected Source source;

    GuiWithTanks(Container container) {
        super(container);
    }

    int getLeft() {
        return guiLeft;
    }

    int getTop() {
        return guiTop;
    }

    float getZLevel() {
        return zLevel;
    }

    protected abstract GuiImageButtonNormal getEquipButton();

    protected abstract GuiImageButtonNormal getUnequipButton();

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (source == Source.WEARING) {
            if (getUnequipButton().inButton(this, mouseX, mouseY)) {
                ModNetwork.net.sendToServer(
                        new EquipUnequipBackWearablePacket.Message(EquipUnequipBackWearablePacket.UNEQUIP_WEARABLE));
                player.closeScreen();
            }
        } else if (source == Source.HOLDING) {
            if (getEquipButton().inButton(this, mouseX, mouseY)) {
                ModNetwork.net.sendToServer(
                        new EquipUnequipBackWearablePacket.Message(EquipUnequipBackWearablePacket.EQUIP_WEARABLE));
                player.closeScreen();
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char key, int keycode) {
        if (keycode == Keybindings.openInventory.getKeyCode()) {
            player.closeScreen();
        }

        super.keyTyped(key, keycode);
    }

    @Override
    public void handleMouseInput() {
        if (Mouse.getEventDWheel() != 0 && theSlot instanceof SlotFluid) {
            return; // fluid slot processing is server-only, scroll would desync
        }

        // skip fluid slot routing on scroll so containers go to regular inventory client-side
        if (Mouse.getEventDWheel() != 0 && inventorySlots instanceof ContainerAdventure) {
            ((ContainerAdventure) inventorySlots).skipFluidSlots = true;
            try {
                super.handleMouseInput();
            } finally {
                ((ContainerAdventure) inventorySlots).skipFluidSlots = false;
            }
            return;
        }

        super.handleMouseInput();
    }

    @Override
    public void initGui() {
        super.initGui();
        if (LoadedMods.TCONSTRUCT && source == Source.WEARING) {
            TConstructTab.updateTabValues(guiLeft, guiTop, TConstructTab.Tab.class);
            TConstructTab.addTabsToList(this.buttonList);
        }
    }

}
