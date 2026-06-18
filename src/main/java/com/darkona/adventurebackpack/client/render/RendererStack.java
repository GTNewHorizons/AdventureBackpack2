package com.darkona.adventurebackpack.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;

import com.darkona.adventurebackpack.util.GregtechUtils;
import com.darkona.adventurebackpack.util.ThaumcraftUtils;
import com.darkona.adventurebackpack.util.TinkersUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RendererStack extends ModelRenderer {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private final boolean isLowerSlot;
    private ItemStack stack;

    public RendererStack(ModelBase modelBase, boolean isLowerSlot) {
        super(modelBase);
        this.isLowerSlot = isLowerSlot;
        addChild(new Thing(modelBase));
    }

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

            IItemRenderer customRenderer = MinecraftForgeClient
                    .getItemRenderer(stack, IItemRenderer.ItemRenderType.ENTITY);

            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            if (isLowerSlot) {
                GL11.glScalef(0.6F, 0.6F, 0.6F);
                GL11.glPushMatrix();
                GL11.glRotatef(-90F, 0, 1, 0);
            } else {
                GL11.glScalef(0.7F, 0.7F, 0.7F);
                GL11.glPushMatrix();
            }
            GL11.glRotatef(getToolRotationAngle(stack, isLowerSlot), 0, 0, 1);

            if (customRenderer != null) {
                TextureManager tm = MC.getTextureManager();
                tm.bindTexture(tm.getResourceLocation(stack.getItemSpriteNumber()));
                customRenderer.renderItem(IItemRenderer.ItemRenderType.ENTITY, stack);
            } else {
                CopygirlRenderUtils.renderItemIn3d(stack);
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
