package com.darkona.adventurebackpack.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTank;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.darkona.adventurebackpack.block.TileAdventureBackpack;
import com.darkona.adventurebackpack.common.Constants;
import com.darkona.adventurebackpack.common.Constants.Source;
import com.darkona.adventurebackpack.config.ConfigHandler;
import com.darkona.adventurebackpack.init.ModNetwork;
import com.darkona.adventurebackpack.inventory.ContainerBackpack;
import com.darkona.adventurebackpack.inventory.IInventoryBackpack;
import com.darkona.adventurebackpack.inventory.InventoryBackpack;
import com.darkona.adventurebackpack.network.PlayerActionPacket;
import com.darkona.adventurebackpack.network.SleepingBagPacket;
import com.darkona.adventurebackpack.reference.LoadedMods;
import com.darkona.adventurebackpack.util.Resources;
import com.darkona.adventurebackpack.util.TinkersUtils;
import com.darkona.adventurebackpack.util.TipUtils;

import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAdvBackpack extends GuiWithTanks {

    private static final ResourceLocation TEXTURE = Resources.guiTextures("guiBackpackNew");
    private static final int TINKERS_SLOT = 38; // ContainerBackpack.CRAFT_MATRIX_EMULATION[4]

    private static final GuiImageButtonNormal bedButton = new GuiImageButtonNormal(5, 91, 18, 18);
    private static final GuiImageButtonNormal equipButton = new GuiImageButtonNormal(5, 91, 18, 18);
    private static final GuiImageButtonNormal unequipButton = new GuiImageButtonNormal(5, 91, 18, 18);
    private static final GuiTank tankLeft = new GuiTank(25, 7, 100, 16, ConfigHandler.typeTankRender);
    private static final GuiTank tankRight = new GuiTank(207, 7, 100, 16, ConfigHandler.typeTankRender);

    private final IInventoryBackpack inventory;

    private boolean isHoldingSpace;

    public GuiAdvBackpack(EntityPlayer player, TileAdventureBackpack tileBackpack, Source source) {
        super(new ContainerBackpack(player, tileBackpack, source));
        this.player = player;
        inventory = tileBackpack;
        this.source = source;
        xSize = 248;
        ySize = 207;
    }

    public GuiAdvBackpack(EntityPlayer player, InventoryBackpack inventoryBackpack, Source source) {
        super(new ContainerBackpack(player, inventoryBackpack, source));
        this.player = player;
        inventory = inventoryBackpack;
        this.source = source;
        xSize = 248;
        ySize = 207;
    }

    private boolean isBedButtonCase() {
        return source == Source.TILE
                || (ConfigHandler.portableSleepingBag && source == Source.WEARING && GuiScreen.isShiftKeyDown());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        GL11.glColor4f(1, 1, 1, 1);
        this.mc.renderEngine.bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Buttons and button highlight
        if (isBedButtonCase()) {
            if (bedButton.inButton(this, mouseX, mouseY)) bedButton.draw(this, 20, 227);
            else bedButton.draw(this, 1, 227);
        } else if (source == Source.WEARING) {
            if (unequipButton.inButton(this, mouseX, mouseY)) unequipButton.draw(this, 96, 227);
            else unequipButton.draw(this, 77, 227);
        } else if (source == Source.HOLDING) {
            if (equipButton.inButton(this, mouseX, mouseY)) equipButton.draw(this, 96, 208);
            else equipButton.draw(this, 77, 208);
        }

        if (LoadedMods.TCONSTRUCT && ConfigHandler.tinkerToolsMaintenance) {
            if (inventory.getStackInSlot(TINKERS_SLOT) == null) {
                this.mc.getTextureManager().bindTexture(TinkersUtils.GUI_ICONS);
                this.drawTexturedModalRect(this.guiLeft + 169, this.guiTop + 77, 0, 233, 18, 18);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        inventory.openInventory();
        FluidTank lft = inventory.getLeftTank();
        FluidTank rgt = inventory.getRightTank();
        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LIGHTING_BIT);

        tankLeft.draw(this, lft);
        tankRight.draw(this, rgt);

        GL11.glPopAttrib();
    }

    @Override
    protected GuiImageButtonNormal getEquipButton() {
        return equipButton;
    }

    @Override
    protected GuiImageButtonNormal getUnequipButton() {
        return unequipButton;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (isBedButtonCase() && bedButton.inButton(this, mouseX, mouseY)) {
            if (source == Source.TILE) {
                TileAdventureBackpack te = (TileAdventureBackpack) inventory;
                ModNetwork.net
                        .sendToServer(new SleepingBagPacket.SleepingBagMessage(true, te.xCoord, te.yCoord, te.zCoord));
            } else {
                int posX = MathHelper.floor_double(player.posX);
                int posY = MathHelper.floor_double(player.posY) - 1;
                int posZ = MathHelper.floor_double(player.posZ);
                ModNetwork.net.sendToServer(new SleepingBagPacket.SleepingBagMessage(false, posX, posY, posZ));
            }
        } else {
            super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (!isHoldingSpace) {
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                isHoldingSpace = true;
                ModNetwork.net.sendToServer(new PlayerActionPacket.ActionMessage(PlayerActionPacket.GUI_HOLDING_SPACE));
                inventory.getExtendedProperties().setBoolean(Constants.TAG_HOLDING_SPACE, true);
            }
        } else {
            if (!Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                isHoldingSpace = false;
                ModNetwork.net
                        .sendToServer(new PlayerActionPacket.ActionMessage(PlayerActionPacket.GUI_NOT_HOLDING_SPACE));
                inventory.getExtendedProperties().removeTag(Constants.TAG_HOLDING_SPACE);
            }
        }
    }

    /**
     * An instance of this class will handle tooltips for all instances of GuiAdvBackpack
     */
    public static class TooltipHandler implements IContainerTooltipHandler {

        @Override
        public List<String> handleTooltip(GuiContainer gui, int mouseX, int mouseY, List<String> currenttip) {
            if (gui instanceof GuiAdvBackpack) {
                GuiAdvBackpack backpackGui = (GuiAdvBackpack) gui;

                if (GuiContainerManager.shouldShowTooltip(backpackGui) && currenttip.isEmpty()) {
                    // Fluid tank tooltips
                    if (tankLeft.inTank(backpackGui, mouseX, mouseY)) currenttip.addAll(tankLeft.getTankTooltip());
                    if (tankRight.inTank(backpackGui, mouseX, mouseY)) currenttip.addAll(tankRight.getTankTooltip());

                    // equip/unequip button
                    if (backpackGui.source == Source.HOLDING && equipButton.inButton(backpackGui, mouseX, mouseY)) {
                        currenttip.add(TipUtils.l10n("backpack.equip"));
                    } else if (backpackGui.isBedButtonCase() && bedButton.inButton(backpackGui, mouseX, mouseY)) {
                        currenttip.add(TipUtils.l10n("backpack.sleepingbag"));
                    } else if (backpackGui.source == Source.WEARING
                            && unequipButton.inButton(backpackGui, mouseX, mouseY)) {
                                currenttip.add(TipUtils.l10n("backpack.unequip.key1"));
                                currenttip.add(TipUtils.l10n("backpack.unequip.key2"));
                            }
                }
            }

            return currenttip;
        }

    }

    static {
        // Only instantiate TooltipHandler if enabled in config.
        if (ConfigHandler.showGuiTooltips) {
            GuiContainerManager.addTooltipHandler(new TooltipHandler());
        }
    }
}
