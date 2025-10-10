package com.darkona.adventurebackpack.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.darkona.adventurebackpack.common.Constants;
import com.darkona.adventurebackpack.init.ModBlocks;
import com.darkona.adventurebackpack.inventory.InventoryBackpack;
import com.darkona.adventurebackpack.playerProperties.BackpackProperty;
import com.darkona.adventurebackpack.util.CoordsUtils;
import com.darkona.adventurebackpack.util.LogHelper;
import com.darkona.adventurebackpack.util.Resources;
import com.darkona.adventurebackpack.util.Wearing;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockSleepingBag extends BlockDirectional {

    private static final int[][] footBlockToHeadBlockMap = new int[][] { { 0, 1 }, { -1, 0 }, { 0, -1 }, { 1, 0 } };

    private static final String TAG_STORED_SPAWN = "storedSpawn";
    private static final String TAG_SPAWN_POS_X = "posX";
    private static final String TAG_SPAWN_POS_Y = "posY";
    private static final String TAG_SPAWN_POS_Z = "posZ";

    @SideOnly(Side.CLIENT)
    private IIcon[] endIcons;

    @SideOnly(Side.CLIENT)
    private IIcon[] sideIcons;

    @SideOnly(Side.CLIENT)
    private IIcon[] topIcons;

    public BlockSleepingBag() {
        super(Material.cloth);
        this.func_149978_e();
        setBlockName(getUnlocalizedName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected String getTextureName() {
        return this.textureName == null ? "MISSING_ICON_BLOCK_" + getIdFromBlock(this) + "_" + getUnlocalizedName()
                : this.textureName;
    }

    @Override
    public String getUnlocalizedName() {
        return "blockSleepingBag";
    }

    private void func_149978_e() {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
    }

    /**
     * Returns whether this bed block is the head of the bed.
     */
    private static boolean isBlockHeadOfBed(int meta) {
        return (meta & 8) != 0;
    }

    public static boolean isSleepingInPortableBag(EntityPlayer player) {
        return Wearing.isWearingBackpack(player)
                && Wearing.getWearingBackpackInv(player).getExtendedProperties().hasKey(Constants.TAG_SLEEPING_IN_BAG);
    }

    public static void packPortableSleepingBag(EntityPlayer player) {
        if (isSleepingInPortableBag(player)) {
            InventoryBackpack inv = Wearing.getWearingBackpackInv(player);
            inv.removeSleepingBag(player.worldObj);
            inv.getExtendedProperties().removeTag(Constants.TAG_SLEEPING_IN_BAG);
        }
    }

    public static void storeOriginalSpawn(EntityPlayer player, NBTTagCompound tag) {
        ChunkCoordinates spawn = player.getBedLocation(player.worldObj.provider.dimensionId);
        if (spawn != null) {
            NBTTagCompound storedSpawn = new NBTTagCompound();
            storedSpawn.setInteger(TAG_SPAWN_POS_X, spawn.posX);
            storedSpawn.setInteger(TAG_SPAWN_POS_Y, spawn.posY);
            storedSpawn.setInteger(TAG_SPAWN_POS_Z, spawn.posZ);
            tag.setTag(TAG_STORED_SPAWN, storedSpawn);
            LogHelper.info(
                    "Stored spawn data for " + player
                            .getDisplayName() + ": " + spawn + " dimID: " + player.worldObj.provider.dimensionId);
        } else {
            LogHelper.warn("Cannot store spawn data for " + player.getDisplayName() + ", because it is non-existent");
        }
    }

    public static void storeOriginalSpawn(EntityPlayer player) {
        ChunkCoordinates spawn = player.getBedLocation(player.worldObj.provider.dimensionId);
        final BackpackProperty props = BackpackProperty.get(player);

        if (spawn != null && props != null) {
            props.setStoredSpawn(spawn);
            LogHelper.info(
                    "Stored spawn data for " + player
                            .getDisplayName() + ": " + spawn + " dimID: " + player.worldObj.provider.dimensionId);
        } else {
            LogHelper.warn("Cannot store spawn data for " + player.getDisplayName() + ", because it is non-existent");
        }
    }

    public static void restoreOriginalSpawn(EntityPlayer player, NBTTagCompound tag) {
        if (tag.hasKey(TAG_STORED_SPAWN)) {
            NBTTagCompound storedSpawn = tag.getCompoundTag(TAG_STORED_SPAWN);
            ChunkCoordinates coords = new ChunkCoordinates(
                    storedSpawn.getInteger(TAG_SPAWN_POS_X),
                    storedSpawn.getInteger(TAG_SPAWN_POS_Y),
                    storedSpawn.getInteger(TAG_SPAWN_POS_Z));
            player.setSpawnChunk(coords, false, player.worldObj.provider.dimensionId);
            tag.removeTag(TAG_STORED_SPAWN);
            LogHelper.info(
                    "Restored spawn data for " + player
                            .getDisplayName() + ": " + coords + " dimID: " + player.worldObj.provider.dimensionId);
        } else {
            LogHelper.warn("No spawn data to restore for " + player.getDisplayName());
        }
    }

    public static void restoreOriginalSpawn(EntityPlayer player) {
        final BackpackProperty props = BackpackProperty.get(player);

        if (props != null) {
            final ChunkCoordinates oldSpawn = props.getStoredSpawn();
            if (oldSpawn != null) {
                player.setSpawnChunk(oldSpawn, false, player.worldObj.provider.dimensionId);
                LogHelper.info(
                        "Restored spawn data for " + player.getDisplayName()
                                + ": "
                                + oldSpawn
                                + " dimID: "
                                + player.worldObj.provider.dimensionId);
            }
        } else {
            LogHelper.warn("No spawn data to restore for " + player.getDisplayName());
        }
    }

    public void onPortableBlockActivated(World world, EntityPlayer player, int cX, int cY, int cZ) {
        if (world.isRemote) return;
        if (!isSleepingInPortableBag(player)) return;

        if (!onBlockActivated(world, cX, cY, cZ, player, 1, 0f, 0f, 0f)) packPortableSleepingBag(player);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int id, float f1, float f2,
            float f3) {
        if (world.isRemote) {
            return true;
        } else {
            int meta = world.getBlockMetadata(x, y, z);

            if (!isBlockHeadOfBed(meta)) {
                int dir = getDirection(meta);
                x += footBlockToHeadBlockMap[dir][0];
                z += footBlockToHeadBlockMap[dir][1];

                if (world.getBlock(x, y, z) != this) {
                    return false;
                }

                meta = world.getBlockMetadata(x, y, z);
            }

            if (world.provider.canRespawnHere() && world.getBiomeGenForCoords(x, z) != BiomeGenBase.hell) {
                if (isBedOccupied(meta)) {
                    EntityPlayer entityplayer1 = null;

                    for (Object o : world.playerEntities) {
                        EntityPlayer entityplayer2 = (EntityPlayer) o;

                        if (entityplayer2.isPlayerSleeping()) {
                            ChunkCoordinates chunkcoordinates = entityplayer2.playerLocation;

                            if (chunkcoordinates.posX == x && chunkcoordinates.posY == y
                                    && chunkcoordinates.posZ == z) {
                                entityplayer1 = entityplayer2;
                            }
                        }
                    }

                    if (entityplayer1 != null) {
                        player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.occupied"));
                        return false;
                    }

                    setBedOccupied(world, x, y, z, false);
                }

                EntityPlayer.EnumStatus enumstatus = player.sleepInBedAt(x, y, z);

                if (enumstatus == EntityPlayer.EnumStatus.OK) {
                    setBedOccupied(world, x, y, z, true);
                    // This is so the wake-up event can detect it. It fires before the player wakes up.
                    // and the bed location isn't set until then, normally.

                    if (isSleepingInPortableBag(player)) {
                        storeOriginalSpawn(player, Wearing.getWearingBackpackInv(player).getExtendedProperties());
                    } else {
                        LogHelper.info("Looking for a campfire nearby...");
                        ChunkCoordinates campfire = CoordsUtils
                                .findBlock3D(world, x, y, z, ModBlocks.blockCampFire, 8, 2);
                        if (campfire != null) {
                            LogHelper.info("Campfire Found, saving coordinates. " + campfire);
                            BackpackProperty.get(player).setCampFire(campfire);
                        } else {
                            LogHelper.info("No campfire found. Keeping spawnpoint at previous location");
                            storeOriginalSpawn(player);
                            BackpackProperty.get(player).setCampFire(null);
                        }
                    }
                    player.setSpawnChunk(new ChunkCoordinates(x, y, z), true, player.dimension);

                    return true;
                } else {
                    if (enumstatus == EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW) {
                        player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.noSleep"));
                    } else if (enumstatus == EntityPlayer.EnumStatus.NOT_SAFE) {
                        player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.notSafe"));
                    }

                    return false;
                }
            } else {
                double d2 = (double) x + 0.5D;
                double d0 = (double) y + 0.5D;
                double d1 = (double) z + 0.5D;
                world.setBlockToAir(x, y, z);
                int k1 = getDirection(meta);
                x += footBlockToHeadBlockMap[k1][0];
                z += footBlockToHeadBlockMap[k1][1];

                if (world.getBlock(x, y, z) == this) {
                    world.setBlockToAir(x, y, z);
                    d2 = (d2 + (double) x + 0.5D) / 2.0D;
                    d0 = (d0 + (double) y + 0.5D) / 2.0D;
                    d1 = (d1 + (double) z + 0.5D) / 2.0D;
                }

                world.newExplosion(null, (float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F, 5.0F, true, true);

                return false;
            }
        }
    }

    private static void setBedOccupied(World world, int x, int y, int z, boolean flag) {
        int l = world.getBlockMetadata(x, y, z);

        if (flag) {
            l |= 4;
        } else {
            l &= -5;
        }

        world.setBlockMetadataWithNotify(x, y, z, l, 4);
    }

    private static boolean isBedOccupied(int meta) {
        return (meta & 4) != 0;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        int meta = world.getBlockMetadata(x, y, z);
        int dir = getDirection(meta);

        if (isBlockHeadOfBed(meta)) {
            if (world.getBlock(x - footBlockToHeadBlockMap[dir][0], y, z - footBlockToHeadBlockMap[dir][1]) != this) {
                world.setBlockToAir(x, y, z);
            }
        } else
            if (world.getBlock(x + footBlockToHeadBlockMap[dir][0], y, z + footBlockToHeadBlockMap[dir][1]) != this) {
                world.setBlockToAir(x, y, z);

                if (!world.isRemote) {
                    this.dropBlockAsItem(world, x, y, z, meta, 0);
                }
            }
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        this.blockBoundsForRender();
    }

    private void blockBoundsForRender() {
        this.func_149978_e();
    }

    @Override
    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
        return null;
    }

    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {
        int direction = getDirection(meta);
        if (player.capabilities.isCreativeMode && isBlockHeadOfBed(meta)) {
            x -= footBlockToHeadBlockMap[direction][0];
            z -= footBlockToHeadBlockMap[direction][1];

            if (world.getBlock(x, y, z) == this) {
                world.setBlockToAir(x, y, z);
            }
        }
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion boom) {
        this.onBlockDestroyedByPlayer(world, x, y, z, world.getBlockMetadata(x, y, z));
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta) {
        // TODO make it work if player destroyed head block of sleeping bag (so backpack 1 more tile away)
        // LogHelper.info("onBlockDestroyedByPlayer() : BlockSleepingBag");
        int direction = getDirection(meta);
        int tileZ = z;
        int tileX = x;
        switch (meta) {
            case 0:
                tileZ--;
                break;
            case 1:
                tileX++;
                break;
            case 2:
                tileZ++;
                break;
            case 3:
                tileX--;
                break;
        }
        // LogHelper.info("onBlockDestroyedByPlayer() Looking for tile entity in x=" +tileX+" y="+y+" z="+tileZ+" while
        // breaking the block in x= "+x+" y="+y+" z="+z);
        if (world.getTileEntity(tileX, y, tileZ) != null
                && world.getTileEntity(tileX, y, tileZ) instanceof TileAdventureBackpack) {
            // LogHelper.info("onBlockDestroyedByPlayer() Found the tile entity in x=" +tileX+" y="+y+" z="+z+" while
            // breaking the block in x= "+x+" y="+y+" z="+z+" ...removing.");
            ((TileAdventureBackpack) world.getTileEntity(tileX, y, tileZ)).setSleepingBagDeployed(false);
        }
    }

    @Override
    public boolean isBed(IBlockAccess world, int x, int y, int z, EntityLivingBase player) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 0) {
            return Blocks.planks.getBlockTextureFromSide(side);
        } else {
            int k = getDirection(meta);
            int l = Direction.bedDirection[k][side];
            int isHead = isBlockHeadOfBed(meta) ? 1 : 0;
            return (isHead != 1 || l != 2) && (isHead != 0 || l != 3)
                    ? (l != 5 && l != 4 ? this.topIcons[isHead] : this.sideIcons[isHead])
                    : this.endIcons[isHead];
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        this.topIcons = new IIcon[] {
                iconRegister.registerIcon(Resources.blockTextures("sleepingBag_feet_top").toString()),
                iconRegister.registerIcon(Resources.blockTextures("sleepingBag_head_top").toString()) };

        this.endIcons = new IIcon[] {
                iconRegister.registerIcon(Resources.blockTextures("sleepingBag_feet_end").toString()),
                iconRegister.registerIcon(Resources.blockTextures("sleepingBag_head_end").toString()) };

        this.sideIcons = new IIcon[] {
                iconRegister.registerIcon(Resources.blockTextures("sleepingBag_feet_side").toString()),
                iconRegister.registerIcon(Resources.blockTextures("sleepingBag_head_side").toString()) };
    }

    @Override
    public int getRenderType() {
        return 14;
    }

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @Override
    public boolean isBlockNormalCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }
}
