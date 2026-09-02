package com.darkona.adventurebackpack.util;

import com.darkona.adventurebackpack.reference.LoadedMods;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public final class TinkerCraftingStationBridge {

    private static final String CLASS_CRAFTING_LOGIC = "tconstruct.tools.logic.CraftingStationLogic";
    private static final String CLASS_CRAFTING_STATION = "tconstruct.tools.inventory.CraftingStationContainer";
    private static final String METHOD_ON_CRAFT_CHANGED = LoadedMods.DEV_ENV ? "onCraftMatrixChanged" : "func_75130_a";
    private static final String FIELD_CRAFT_MATRIX = "craftMatrix";
    private static final String FIELD_CRAFT_RESULT = "craftResult";

    private static Constructor<?> craftingLogicConstructor;
    private static Constructor<?> craftingStationConstructor;
    private static Field craftMatrixField;
    private static Field craftResultField;
    private static Method onCraftMatrixChangedMethod;

    static {
        if (LoadedMods.TCONSTRUCT) {
            try {
                final Class<?> craftingLogic = Class.forName(CLASS_CRAFTING_LOGIC);
                final Class<?> craftingStation = Class.forName(CLASS_CRAFTING_STATION);

                craftingLogicConstructor = craftingLogic.getConstructor();
                craftingStationConstructor = craftingStation.getConstructor(InventoryPlayer.class, craftingLogic, int.class, int.class, int.class);

                craftMatrixField = craftingStation.getField(FIELD_CRAFT_MATRIX);
                craftResultField = craftingStation.getField(FIELD_CRAFT_RESULT);
                onCraftMatrixChangedMethod = craftingStation.getMethod(METHOD_ON_CRAFT_CHANGED, IInventory.class);
            } catch (Exception e) {
                LogHelper.error("Error caching reflection for Tinkers Crafting Station: " + e);
            }
        }
    }

    private final Object craftingStationInstance;

    public TinkerCraftingStationBridge() {
        this.craftingStationInstance = createCraftingStationInstance();
    }

    private static Object createCraftingStationInstance() {
        if (craftingStationConstructor == null || craftingLogicConstructor == null) return null;
        try {
            final Object craftingLogicInstance = craftingLogicConstructor.newInstance();
            final InventoryPlayer invPlayer = getInventoryPlayer();
            return craftingStationConstructor.newInstance(invPlayer, craftingLogicInstance, 0, 0, 0);
        } catch (Exception e) {
            LogHelper.error("Error getting instance of Tinkers Crafting Station: " + e);
        }
        return null;
    }

    private static InventoryPlayer getInventoryPlayer() {
        final InventoryPlayer invPlayer;
        if (Utils.inServer()) {
            WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[0];
            UUID fakeUuid = UUID.fromString("521e749d-2ac0-3459-af7a-160b4be5c62b");
            GameProfile fakeProfile = new GameProfile(fakeUuid, "[Adventurer]");
            invPlayer = new InventoryPlayer(new FakePlayer(world, fakeProfile));
        } else {
            invPlayer = Minecraft.getMinecraft().thePlayer.inventory;
        }
        return invPlayer;
    }

    @Nullable
    public synchronized ItemStack getTinkersRecipe(InventoryCrafting craftMatrix) {
        if (craftingStationInstance == null) return null;
        try {
            craftMatrixField.set(craftingStationInstance, craftMatrix);
            onCraftMatrixChangedMethod.invoke(craftingStationInstance, craftMatrix);
            return ((IInventory) craftResultField.get(craftingStationInstance)).getStackInSlot(0);
        } catch (Exception e) {
            LogHelper.error("Error during reflection in getTinkersRecipe: " + e);
            return null;
        }
    }
}