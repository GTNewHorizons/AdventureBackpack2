package com.darkona.adventurebackpack.item;

import java.util.HashMap;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.darkona.adventurebackpack.entity.EntityInflatableBoat;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemComponent extends ItemAB {

    private final HashMap<String, IIcon> componentIcons = new HashMap<>();
    private final String[] names = { "sleepingBag", "backpackTank", "hoseHead", "macheteHandle", "copterEngine",
            "copterBlades", "inflatableBoat", "inflatableBoatMotorized", "hydroBlades", };
    private final static String defaultIconName = "copterBlades";

    public ItemComponent() {
        setNoRepair();
        setHasSubtypes(true);
        setMaxStackSize(16);
        this.setUnlocalizedName("backpackComponent");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        for (String name : names) {
            IIcon temporalIcon = iconRegister
                    .registerIcon(super.getUnlocalizedName(name).substring(this.getUnlocalizedName().indexOf(".") + 1));
            componentIcons.put(name, temporalIcon);
        }
        itemIcon = iconRegister.registerIcon(
                super.getUnlocalizedName("sleepingBag").substring(this.getUnlocalizedName().indexOf(".") + 1));
    }

    @Override
    public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
        return super.getIcon(stack, renderPass, player, usingItem, useRemaining);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        // Some places like the statistics menu render Items with metadata 0, which is not valid for this item,
        // so just fall back to a valid icon.
        if (damage < 1 || damage > names.length) return componentIcons.get(defaultIconName);

        return componentIcons.get(names[damage - 1]);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int damage = getDamage(stack);
        // Some places like the statistics menu render Items with metadata 0, which is not valid for this item,
        // so just fall back to the generic component item name.
        if (damage < 1 || damage > names.length) return getUnlocalizedName();

        return super.getUnlocalizedName(names[damage - 1]);
    }

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list) {
        for (int i = 1; i <= names.length; i++) {
            list.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (stack.getItemDamage() == 7) return placeBoat(stack, world, player, false);
        if (stack.getItemDamage() == 8) return placeBoat(stack, world, player, true);
        return stack;
    }

    private ItemStack placeBoat(ItemStack stack, World world, EntityPlayer player, boolean motorized) {
        float f = 1.0F;
        float f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
        float f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
        double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double) f;
        double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double) f + 1.62D - (double) player.yOffset;
        double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) f;
        Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
        float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
        float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        float f6 = MathHelper.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = 5.0D;
        Vec3 vec31 = vec3.addVector((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
        MovingObjectPosition movingobjectposition = world.rayTraceBlocks(vec3, vec31, true);

        if (movingobjectposition == null) {
            return stack;
        } else {
            Vec3 vec32 = player.getLook(f);
            boolean flag = false;
            float f9 = 1.0F;
            List list = world.getEntitiesWithinAABBExcludingEntity(
                    player,
                    player.boundingBox.addCoord(vec32.xCoord * d3, vec32.yCoord * d3, vec32.zCoord * d3)
                            .expand(f9, f9, f9));
            int i;

            for (i = 0; i < list.size(); ++i) {
                Entity entity = (Entity) list.get(i);

                if (entity.canBeCollidedWith()) {
                    float f10 = entity.getCollisionBorderSize();
                    AxisAlignedBB axisalignedbb = entity.boundingBox.expand(f10, f10, f10);

                    if (axisalignedbb.isVecInside(vec3)) {
                        flag = true;
                    }
                }
            }

            if (!flag) {
                if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    i = movingobjectposition.blockX;
                    int j = movingobjectposition.blockY;
                    int k = movingobjectposition.blockZ;

                    if (world.getBlock(i, j, k) == Blocks.snow_layer) {
                        --j;
                    }

                    EntityInflatableBoat inflatableBoat = new EntityInflatableBoat(
                            world,
                            i + 0.5,
                            j + 1.0,
                            k + 0.5,
                            motorized);

                    inflatableBoat.rotationYaw = (float) (((MathHelper
                            .floor_double((player.rotationYaw * 4.0 / 360.0) + 0.5D) & 3) - 1) * 90);
                    if (!world.getCollidingBoundingBoxes(
                            inflatableBoat,
                            inflatableBoat.boundingBox.expand(-0.1, -0.1, -0.1)).isEmpty()) {
                        return stack;
                    }

                    if (!world.isRemote) {
                        world.spawnEntityInWorld(inflatableBoat);
                    }

                    if (!player.capabilities.isCreativeMode) {
                        --stack.stackSize;
                    }
                }
            }
            return stack;
        }
    }
}
