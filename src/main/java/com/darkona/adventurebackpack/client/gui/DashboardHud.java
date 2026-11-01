package com.darkona.adventurebackpack.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

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
        POTION_ICONS.setPreview(
                (screenWidth, screenHeight) -> HudRegistry
                        .render(POTION_ICONS, () -> drawPotionIconsPreview(screenWidth, screenHeight)));
        TANKS.setPreview(
                (screenWidth, screenHeight) -> HudRegistry
                        .render(TANKS, () -> drawTanksPreview(screenWidth, screenHeight)));
        HudRegistry.register(POTION_ICONS);
        HudRegistry.register(TANKS);
    }

    /** False means the player hid the icons, so skip drawing them and do not call {@link #endRender()}. */
    public static boolean beginPotionIcons(int screenWidth, int screenHeight) {
        return HudRegistry.beginRender(POTION_ICONS, screenWidth, screenHeight);
    }

    /** False means the player hid the tanks, so skip drawing them and do not call {@link #endRender()}. */
    public static boolean beginTanks(int screenWidth, int screenHeight) {
        return HudRegistry.beginRender(TANKS, screenWidth, screenHeight);
    }

    public static void endRender() {
        HudRegistry.endRender();
    }

    /** As many sample icons as fit the box. */
    private static void drawPotionIconsPreview(int screenWidth, int screenHeight) {
        HudBounds bounds = POTION_ICONS.getDefaultBounds(screenWidth, screenHeight);
        bindPreviewTexture(GuiOverlay.GUI_INVENTORY);
        int step = GuiOverlay.BUFF_ICON_SIZE + GuiOverlay.BUFF_ICON_SPACING;
        int iconCount = (bounds.width + GuiOverlay.BUFF_ICON_SPACING) / step;
        for (int i = 0; i < iconCount; i++) {
            Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(
                    bounds.x + i * step,
                    bounds.y,
                    i * GuiOverlay.BUFF_ICON_SIZE,
                    198,
                    GuiOverlay.BUFF_ICON_SIZE,
                    GuiOverlay.BUFF_ICON_SIZE);
        }
    }

    /** Two empty tank frames, right of where the tool icons go. */
    private static void drawTanksPreview(int screenWidth, int screenHeight) {
        bindPreviewTexture(GuiOverlay.TANKS_OVERLAY);
        int tanksX = GuiOverlay.getTanksX(screenWidth);
        int y = GuiOverlay.getTanksY(screenHeight);
        Minecraft.getMinecraft().ingameGUI
                .drawTexturedModalRect(tanksX, y, 10, 0, GuiOverlay.TANK_WIDTH, GuiOverlay.TANK_HEIGHT);
        Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(
                tanksX + GuiOverlay.TANK_WIDTH + 1,
                y,
                10,
                0,
                GuiOverlay.TANK_WIDTH,
                GuiOverlay.TANK_HEIGHT);
    }

    private static void bindPreviewTexture(ResourceLocation texture) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
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
