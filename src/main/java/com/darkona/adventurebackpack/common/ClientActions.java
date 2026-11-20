package com.darkona.adventurebackpack.common;

import static com.darkona.adventurebackpack.common.Constants.Copter.TAG_FUEL_TANK;
import static com.darkona.adventurebackpack.common.Constants.Copter.TAG_STATUS;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.fluids.FluidStack;

import com.darkona.adventurebackpack.item.ItemCopterPack;
import com.darkona.adventurebackpack.network.WearableModePacket;
import com.darkona.adventurebackpack.util.BackpackUtils;

// Most of the server actions should really be split to client and server.
public class ClientActions {

    // On the client thread DO NOT WRITE NBT!! Let server be authoritative, or you're going to desync...
    public static void toggleCopterPack(EntityPlayer player, ItemStack copter, byte type) {
        String message = "";
        boolean actionPerformed = false;
        NBTTagCompound copterCompound = BackpackUtils.getOrCreateWearableCompound(copter);
        NBTTagCompound fuelTank = copterCompound.getCompoundTag(TAG_FUEL_TANK);
        if (fuelTank.hasKey("Empty") || FluidStack.loadFluidStackFromNBT(fuelTank).amount <= 0) {
            player.addChatComponentMessage(
                    new ChatComponentTranslation("adventurebackpack:messages.copterpack.outoffuel"));
            return;
        }
        byte mode = copterCompound.getByte(TAG_STATUS);

        if (type == WearableModePacket.COPTER_ON_OFF) {
            if (mode == ItemCopterPack.OFF_MODE) {
                message = "adventurebackpack:messages.copterpack.normal";
                actionPerformed = true;
            } else {
                message = "adventurebackpack:messages.copterpack.off";
                actionPerformed = true;
            }
        }

        if (type == WearableModePacket.COPTER_TOGGLE && mode != ItemCopterPack.OFF_MODE) {
            if (mode == ItemCopterPack.NORMAL_MODE) {
                message = "adventurebackpack:messages.copterpack.hover";
                actionPerformed = true;
            }
            if (mode == ItemCopterPack.HOVER_MODE) {
                message = "adventurebackpack:messages.copterpack.normal";
                actionPerformed = true;
            }
        }

        if (actionPerformed) {
            player.addChatComponentMessage(new ChatComponentTranslation(message));
        }
    }
}
