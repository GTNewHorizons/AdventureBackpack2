package com.darkona.adventurebackpack.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import com.darkona.adventurebackpack.config.ConfigHandler;
import com.gtnewhorizons.dashboard.api.HudBounds;
import com.gtnewhorizons.dashboard.api.HudElement;
import com.gtnewhorizons.dashboard.api.HudRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Lets players move, resize and hide the potion icons and tanks in Dashboard's HUD editor. The only class using
 * Dashboard, so only call it while Dashboard is loaded.
 */
@SideOnly(Side.CLIENT)
public final class DashboardHud {

    private static final HudElement POTION_ICONS = new HudElement(
            "adventurebackpack:potion_icons",
            DashboardHud::getPotionIconsBounds);

    private static final HudElement TANKS = new HudElement("adventurebackpack:tanks", DashboardHud::getTanksBounds);

    private DashboardHud() {}

    public static void register() {
        HudRegistry.register(POTION_ICONS);
        HudRegistry.register(TANKS);
    }

    /** One icon per effect, growing away from the configured screen edge. */
    private static HudBounds getPotionIconsBounds(int screenWidth, int screenHeight) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        int iconCount = player == null ? 1 : Math.max(1, player.getActivePotionEffects().size());
        int step = GuiOverlay.BUFF_ICON_SIZE + GuiOverlay.BUFF_ICON_SPACING;
        int width = step * iconCount - GuiOverlay.BUFF_ICON_SPACING;
        int firstIconX = GuiOverlay.getPotionIconsX(screenWidth, player);
        int x = ConfigHandler.statusOverlayLeft ? firstIconX : firstIconX + GuiOverlay.BUFF_ICON_SIZE - width;
        return new HudBounds(x, GuiOverlay.getPotionIconsY(screenHeight), width, GuiOverlay.BUFF_ICON_SIZE);
    }

    /** Two tanks, plus the tool icons drawn left of them. */
    private static HudBounds getTanksBounds(int screenWidth, int screenHeight) {
        return new HudBounds(
                GuiOverlay.getTanksX(screenWidth) - GuiOverlay.TANK_WIDTH,
                GuiOverlay.getTanksY(screenHeight),
                GuiOverlay.TANK_WIDTH * 3 + 1,
                GuiOverlay.TANK_HEIGHT);
    }
}
