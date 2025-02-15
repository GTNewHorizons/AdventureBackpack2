package com.darkona.adventurebackpack.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.lwjgl.opengl.GL11;

import com.darkona.adventurebackpack.common.Constants;
import com.darkona.adventurebackpack.config.ConfigHandler;
import com.darkona.adventurebackpack.util.LogHelper;
import com.darkona.adventurebackpack.util.TipUtils;

import codechicken.lib.render.TextureUtils;

public class GuiTank {

    private final int height;
    private final int width;
    private final int startX;
    private final int startY;
    private int offsetX = 0;
    private int offsetY = 0;
    private final int resolution;
    private int liquidPerPixel;
    private float zLevel;
    private FluidTank tank;

    /**
     * Draws the fluid from a fluidTank in a GUI.
     *
     * @param X          The startX coordinate to start drawing from.
     * @param Y          The startY coordinate to start drawing from.
     * @param H          The height in pixels of the tank.
     * @param W          The width in pixels of the tank.
     * @param resolution The resolution of the fluid painted in the tank. Higher values mean smaller and more numerous
     *                   boxes. Values can be 1, 2, 4, 8, 16. Other values are untested, but they should probably always
     *                   be integer divisors of the width, with modulus 0;
     */
    public GuiTank(int X, int Y, int H, int W, int resolution) {
        this.startX = X;
        this.startY = Y;
        this.height = H;
        this.width = W;
        this.resolution = resolution > 0 ? W / resolution : W;
    }

    public List<String> getTankTooltip() {
        FluidStack fluid = tank.getFluid();
        String fluidName = (fluid != null) ? fluid.getLocalizedName() : TipUtils.l10n("empty");
        String fluidAmount = ((fluid != null) ? fluid.amount : 0) + "/" + Constants.BASIC_TANK_CAPACITY;
        ArrayList<String> tankTips = new ArrayList<>();
        tankTips.add(fluidName);
        tankTips.add(fluidAmount);
        return tankTips;
    }

    public void draw(GuiWithTanks gui, FluidTank theFluid) {
        tank = theFluid;
        liquidPerPixel = tank.getCapacity() / this.height;
        this.zLevel = gui.getZLevel() + 1;
        switch (ConfigHandler.typeTankRender) {
            case 1:
                drawMethodOne(gui);
                break;
            case 2:
                drawMethodTwo();
                break;
            default:
                drawMethodThree();
                break;
        }
    }

    public void draw(GuiWithTanks gui, FluidTank theFluid, int X, int Y) {
        offsetX = X;
        offsetY = Y;
        draw(gui, theFluid);
    }

    private void drawMethodOne(GuiWithTanks gui) {
        if (tank.getFluid() != null) {
            FluidStack fluid = tank.getFluid();

            IIcon icon = fluid.getFluid().getStillIcon();
            int pixelsY = fluid.amount / liquidPerPixel;
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            int maxY = (startY + offsetY) + height;
            for (int i = (startX + offsetX); i < (startX + offsetX) + width; i += resolution) {
                for (int j = maxY - resolution; j >= maxY - pixelsY; j -= resolution) {
                    GL11.glPushMatrix();
                    int rgb = fluid.getFluid().getColor();
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    GL11.glColor4f(red / 256f, green / 256f, blue / 256f, 1f);
                    gui.drawTexturedModelRectFromIcon(i, j, icon, resolution, resolution);
                    GL11.glPopMatrix();
                }
            }
        }
    }

    private void drawMethodTwo() {
        if (tank.getFluid() != null) {
            FluidStack fluid = tank.getFluid();

            IIcon icon = fluid.getFluid().getStillIcon();
            int pixelsY = fluid.amount / liquidPerPixel;
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            int top = (startY + offsetY) + height - pixelsY;
            int maxY = (startY + offsetY) + height - 1;
            for (int i = (startX + offsetX); i < (startX + offsetX) + width; i += resolution) {
                int iconY = 7;
                for (int j = maxY; j >= top; j--) {
                    GL11.glPushMatrix();
                    int rgb = fluid.getFluid().getColor();
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    GL11.glColor4f(red / 256f, green / 256f, blue / 256f, 1f);
                    drawFluidPixelFromIcon(i, j, icon, resolution, 1, 0, iconY, resolution, 0, zLevel);
                    iconY = (iconY == 0) ? 7 : iconY - 1;
                    GL11.glPopMatrix();
                }
            }
        }
    }

    private void drawMethodThree() {
        if (tank.getFluid() != null) {
            FluidStack fluid = tank.getFluid();

            try {
                IIcon icon = fluid.getFluid().getStillIcon();
                TextureUtils.bindAtlas(fluid.getFluid().getSpriteNumber());
                int top = (startY + offsetY) + height - (fluid.amount / liquidPerPixel);
                for (int j = (startY + offsetY) + height - 1; j >= top; j--) {
                    for (int i = (startX + offsetX); i <= (startX + offsetX) + width - 1; i++) {
                        GL11.glPushMatrix();
                        int rgb = fluid.getFluid().getColor();
                        int red = (rgb >> 16) & 0xFF;
                        int green = (rgb >> 8) & 0xFF;
                        int blue = rgb & 0xFF;
                        if (j >= top + 5) {
                            GL11.glColor4f(red / 256f * 0.9f, green / 256f * 0.9f, blue / 256f * 0.9f, 1f);
                        } else {
                            GL11.glColor4f(red / 256f, green / 256f, blue / 256f, 1f);
                        }
                        drawFluidPixelFromIcon(i, j, icon, 1, 1, 0, 0, 0, 0, zLevel);
                        GL11.glPopMatrix();
                    }
                }
            } catch (Exception oops) {
                LogHelper.error("Exception while trying to render the fluid in the GUI");
                // oops.printStackTrace();
            }
        }
    }

    public boolean inTank(GuiWithTanks gui, int mouseX, int mouseY) {
        mouseX -= gui.getLeft();
        mouseY -= gui.getTop();
        return startX <= mouseX && mouseX <= (startX + offsetX) + width
                && (startY + offsetY) <= mouseY
                && mouseY <= (startY + offsetY) + height;
    }

    /**
     * Draws a box textured with the selected box of an icon.
     *
     * @param x    The startX coordinate where to start drawing the box.
     * @param y    The startY coordinate where to start drawing the box.
     * @param icon The icon to draw from.
     * @param w    The Width of the drawn box.
     * @param h    The height of the drawn box.
     * @param srcX The startX coordinate from the icon to start drawing from. Starts at 0.
     * @param srcY The startY coordinate from the icon to start drawing from. Starts at 0.
     * @param srcW The width of the selection in the icon to draw from. Starts at 0.
     * @param srcH The height of the selection in the icon to draw from. Starts at 0.
     */
    public static void drawFluidPixelFromIcon(int x, int y, IIcon icon, int w, int h, int srcX, int srcY, int srcW,
            int srcH, float zLevel) {
        double minU = icon.getMinU();
        double maxU = icon.getMaxU();
        double minV = icon.getMinV();
        double maxV = icon.getMaxV();

        double singleU = (maxU - minU) / icon.getIconHeight();
        double singleV = (maxV - minV) / icon.getIconWidth();

        double newMinU = minU + (singleU * srcX);
        double newMinV = minV + (singleV * srcY);

        double newMaxU = newMinU + (singleU * srcW);
        double newMaxV = newMinV + (singleV * srcH);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + h, zLevel, newMinU, newMaxV);
        tessellator.addVertexWithUV(x + w, y + h, zLevel, newMaxU, newMaxV);
        tessellator.addVertexWithUV(x + w, y, zLevel, newMaxU, newMinV);
        tessellator.addVertexWithUV(x, y, zLevel, newMinU, newMinV);
        tessellator.draw();
    }
}
