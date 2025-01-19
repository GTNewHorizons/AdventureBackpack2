package com.darkona.adventurebackpack.fluids.effects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;

import adventurebackpack.api.FluidEffect;

public class MilkEffect extends FluidEffect {

    public MilkEffect() {
        super(FluidRegistry.getFluid("milk"), 7);
    }

    @Override
    public void affectDrinker(World world, Entity entity) {
        if (entity instanceof EntityPlayer) {
            ((EntityPlayer) entity).curePotionEffects(new ItemStack(Items.milk_bucket));
        }
    }
}
