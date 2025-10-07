package com.darkona.adventurebackpack.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.darkona.adventurebackpack.config.ConfigHandler;
import com.darkona.adventurebackpack.item.IBackWearableItem;
import com.darkona.adventurebackpack.util.PotionAndEnchantUtils;
import com.darkona.adventurebackpack.util.Wearing;

public class RendererWearableEquipped extends RendererLivingEntity {

    public ResourceLocation texture;
    public ModelBiped modelBipedMain;

    public RendererWearableEquipped() {
        super(new ModelBiped(0.0F), 0.0F);
        renderManager = RenderManager.instance;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity par1Entity) {
        return texture;
    }

    public void render(Entity entity, double x, double y, double z, float rotX, float rotY, float rotZ, float yaw,
            float pitch) {
        final ItemStack wearable = Wearing.getWearingWearable((EntityPlayer) entity);
        if (wearable == null) {
            return;
        }
        if (!ConfigHandler.enableBackRendering) {
            return;
        }
        if (PotionAndEnchantUtils.getTranslucencyLevel(wearable) == 2) {
            return;
        }
        GL11.glPushAttrib(GL11.GL_TRANSFORM_BIT);
        ItemStack wearableCopy = wearable.copy();
        IBackWearableItem wearableItem = (IBackWearableItem) wearableCopy.getItem();
        modelBipedMain = wearableItem.getWearableModel(wearableCopy);
        texture = wearableItem.getWearableTexture(wearableCopy);
        modelBipedMain.bipedBody.rotateAngleX = rotX;
        modelBipedMain.bipedBody.rotateAngleY = rotY;
        modelBipedMain.bipedBody.rotateAngleZ = rotZ;
        try {
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            renderMainModel((EntityPlayer) entity, 0, 0, 0, 0, 0, 0.0625f);
        } catch (Exception ignored) {}
        GL11.glPopAttrib();
    }

    protected void renderMainModel(EntityLivingBase entity, float limbSwing1, float limbswing2, float z, float yaw,
            float whatever, float scale) {
        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_TEXTURE_BIT);

        bindTexture(this.texture);
        if (!entity.isInvisible()) {
            modelBipedMain.render(entity, limbSwing1, limbswing2, z, yaw, whatever, scale);
        } else if (!entity.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer)) {
            GL11.glPushMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.15F);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
            modelBipedMain.render(entity, limbSwing1, limbswing2, z, yaw, whatever, scale);
            GL11.glPopMatrix();
        } else {
            modelBipedMain.setRotationAngles(limbSwing1, limbswing2, z, yaw, whatever, scale, entity);
        }

        GL11.glPopAttrib();
    }
}
