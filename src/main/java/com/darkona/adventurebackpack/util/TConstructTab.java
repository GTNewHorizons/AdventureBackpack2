package com.darkona.adventurebackpack.util;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.util.ResourceLocation;

import com.darkona.adventurebackpack.init.ModNetwork;
import com.darkona.adventurebackpack.network.GUIPacket;
import com.darkona.adventurebackpack.reference.BackpackTypes;

import tconstruct.client.tabs.AbstractTab;
import tconstruct.client.tabs.TabRegistry;

public class TConstructTab {

    public static void registerTab() {
        TabRegistry.registerTab(new Tab());
    }

    public static void updateTabValues(int cornerX, int cornerY, Class<?> selectedButton) {
        TabRegistry.updateTabValues(cornerX, cornerY, selectedButton);
    }

    public static void addTabsToList(List<?> buttonList) {
        TabRegistry.addTabsToList(buttonList);
    }

    public static class Tab extends AbstractTab {

        public static final RenderItem itemRenderer = new RenderItem();
        public static final ResourceLocation texture = new ResourceLocation(
                "textures/gui/container/creative_inventory/tabs.png");

        public Tab() {
            super(0, 0, 0, BackpackUtils.createBackpackStack(BackpackTypes.STANDARD));
        }

        @Override
        public void onTabClicked() {
            EntityClientPlayerMP playerMP = Minecraft.getMinecraft().thePlayer;
            byte backpackType;
            if (Wearing.getWearingBackpack(playerMP) != null) backpackType = GUIPacket.BACKPACK_GUI;
            else if (Wearing.getWearingCopter(playerMP) != null) backpackType = GUIPacket.COPTER_GUI;
            else if (Wearing.getWearingJetpack(playerMP) != null) backpackType = GUIPacket.JETPACK_GUI;
            else return;
            ModNetwork.net.sendToServer(new GUIPacket.GUImessage(backpackType, GUIPacket.FROM_WEARING));

        }

        @Override
        public boolean shouldAddToList() {
            return Wearing.isWearingWearable(Minecraft.getMinecraft().thePlayer);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            drawButton(mc, Wearing.getWearingWearable(mc.thePlayer), true);
        }
    }
}
