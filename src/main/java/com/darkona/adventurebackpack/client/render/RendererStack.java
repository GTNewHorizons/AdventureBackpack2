package com.darkona.adventurebackpack.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import com.darkona.adventurebackpack.util.GregtechUtils;
import com.darkona.adventurebackpack.util.ThaumcraftUtils;
import com.darkona.adventurebackpack.util.TinkersUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RendererStack extends ModelRenderer {

    private final boolean isLowerSlot;
    private ItemStack stack;

    public RendererStack(ModelBase modelBase, boolean isLowerSlot) {
        super(modelBase);
        this.isLowerSlot = isLowerSlot;
        addChild(new Thing(modelBase));
    }

    private final EntityItem renderEntity = new EntityItem(null, 0, 0, 0, new ItemStack(Items.feather));

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    private class Thing extends ModelRenderer {

        public Thing(ModelBase modelBase) {
            super(modelBase);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void render(float par1) {
            if (stack == null) return;

            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            if (isLowerSlot) {
                GL11.glScalef(0.8F, 0.8F, 0.8F);
                GL11.glPushMatrix();
                GL11.glRotatef(-90F, 0, 1, 0);
            } else {
                GL11.glScalef(0.9F, 0.9F, 0.9F);
                GL11.glPushMatrix();
            }
            GL11.glRotatef(getToolRotationAngle(stack, isLowerSlot), 0, 0, 1);

            try {
                renderEntity.setEntityItemStack(stack);
                renderEntity.setWorld(Minecraft.getMinecraft().theWorld);
                renderEntity.hoverStart = 0.0F;
                RenderManager.instance.renderEntityWithPosYaw(renderEntity, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
            } finally {
                renderEntity.setWorld(null);
            }

            GL11.glPopAttrib();
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        }

        private float getToolRotationAngle(ItemStack stack, boolean isLowerSlot) {
            if (GregtechUtils.isTool(stack)) return GregtechUtils.getToolRotationAngle(stack, isLowerSlot);
            if (TinkersUtils.isTool(stack)) return TinkersUtils.getToolRotationAngle(stack, isLowerSlot);
            if (ThaumcraftUtils.isTool(stack)) return ThaumcraftUtils.getToolRotationAngle(stack, isLowerSlot);
            return isLowerSlot ? -225F : 45F;
        }
    }
}
