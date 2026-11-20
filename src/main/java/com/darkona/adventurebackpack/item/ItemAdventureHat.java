package com.darkona.adventurebackpack.item;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.darkona.adventurebackpack.client.models.ModelAdventureHat;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemAdventureHat extends ArmorAB {

    public ItemAdventureHat() {
        super(2, 0);
        setMaxDamage(Items.leather_helmet.getMaxDamage() + 45);
        setUnlocalizedName("adventureHat");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot) {
        return ModelAdventureHat.instance;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return repair.isItemEqual(new ItemStack(Items.leather));
    }
}
