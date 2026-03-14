package com.darkona.adventurebackpack.playerProperties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

import com.darkona.adventurebackpack.init.ModNetwork;
import com.darkona.adventurebackpack.item.IBackWearableItem;
import com.darkona.adventurebackpack.network.SyncPropertiesPacket;

public class BackpackProperty implements IExtendedEntityProperties {

    private static final String PROPERTY_NAME = "abp.property";
    private static final String TAG_STORED_SPAWN = "storedSpawn";

    private EntityPlayer player;
    private ItemStack wearable = null;
    private ChunkCoordinates storedSpawn = null;
    private ChunkCoordinates campFire = null;
    private boolean forceCampFire = false;
    private int dimension = 0;

    private boolean isWakingUpInPortableBag = false;
    private boolean isWakingUpInDeployedBag = false;

    public void setWakingUpInPortableBag(boolean b) {
        this.isWakingUpInPortableBag = b;
    }

    public boolean isWakingUpInPortableBag() {
        return this.isWakingUpInPortableBag;
    }

    public void setWakingUpInDeployedBag(boolean b) {
        this.isWakingUpInDeployedBag = b;
    }

    public boolean isWakingUpInDeployedBag() {
        return this.isWakingUpInDeployedBag;
    }

    public static void sync(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            syncToNear((EntityPlayerMP) player);
        }
    }

    private static void syncToNear(EntityPlayerMP player) {
        try {
            player.getServerForPlayer().getEntityTracker().func_151248_b(
                    player,
                    ModNetwork.net.getPacketFrom(
                            new SyncPropertiesPacket.Message(player.getEntityId(), get(player).getData())));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public BackpackProperty(EntityPlayer player) {
        this.player = player;
    }

    public NBTTagCompound getData() {
        NBTTagCompound data = new NBTTagCompound();
        saveNBTData(data);

        return data;
    }

    public static void register(EntityPlayer player) {
        player.registerExtendedProperties(PROPERTY_NAME, new BackpackProperty(player));
    }

    public static BackpackProperty get(EntityPlayer player) {
        return (BackpackProperty) player.getExtendedProperties(PROPERTY_NAME);
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        if (wearable != null) compound.setTag("wearable", wearable.writeToNBT(new NBTTagCompound()));
        if (storedSpawn != null) {
            NBTTagCompound spawn = new NBTTagCompound();
            spawn.setInteger("posX", storedSpawn.posX);
            spawn.setInteger("posY", storedSpawn.posY);
            spawn.setInteger("posZ", storedSpawn.posZ);
            compound.setTag(TAG_STORED_SPAWN, spawn);
        }
        if (campFire != null) {
            compound.setInteger("campFireX", campFire.posX);
            compound.setInteger("campFireY", campFire.posY);
            compound.setInteger("campFireZ", campFire.posZ);
            compound.setInteger("campFireDim", dimension); // TODO use it for check dim
        }
        compound.setBoolean("forceCampFire", forceCampFire);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        if (compound != null) {
            setWearable(
                    compound.hasKey("wearable") ? ItemStack.loadItemStackFromNBT(compound.getCompoundTag("wearable"))
                            : null);
            if (compound.hasKey(TAG_STORED_SPAWN)) {
                NBTTagCompound spawn = compound.getCompoundTag(TAG_STORED_SPAWN);
                setStoredSpawn(
                        new ChunkCoordinates(
                                spawn.getInteger("posX"),
                                spawn.getInteger("posY"),
                                spawn.getInteger("posZ")));
            }
            setCampFire(
                    new ChunkCoordinates(
                            compound.getInteger("campFireX"),
                            compound.getInteger("campFireY"),
                            compound.getInteger("campFireZ")));
            dimension = compound.getInteger("campFireDim");
            forceCampFire = compound.getBoolean("forceCampFire");
        }
    }

    @Override
    public void init(Entity entity, World world) {
        this.player = (EntityPlayer) entity;
    }

    public void setWearable(ItemStack bp) {
        wearable = bp;
    }

    public ItemStack getWearable() {
        return wearable;
    }

    public void setStoredSpawn(ChunkCoordinates coords) {
        storedSpawn = coords;
    }

    public ChunkCoordinates getStoredSpawn() {
        return storedSpawn;
    }

    public void setCampFire(ChunkCoordinates cf) {
        campFire = cf;
    }

    public ChunkCoordinates getCampFire() {
        return campFire;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public boolean isForcedCampFire() {
        return forceCampFire;
    }

    public void setForceCampFire(boolean forceCampFire) {
        this.forceCampFire = forceCampFire;
    }

    // Scary names for methods because why not
    public void executeWearableUpdateProtocol() {
        if (wearable.getItem() instanceof IBackWearableItem) {
            ((IBackWearableItem) wearable.getItem()).onEquippedUpdate(player.getEntityWorld(), player, wearable);
        }
    }

    public void executeWearableEquipProtocol() {
        if (wearable.getItem() instanceof IBackWearableItem) {
            ((IBackWearableItem) wearable.getItem()).onEquipped(player.getEntityWorld(), player, wearable);
        }
    }

    public void executeWearableUnequipProtocol() {
        if (wearable.getItem() instanceof IBackWearableItem) {
            ((IBackWearableItem) wearable.getItem()).onUnequipped(player.getEntityWorld(), player, wearable);
        }
    }
}
