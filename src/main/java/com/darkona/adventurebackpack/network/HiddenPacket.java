package com.darkona.adventurebackpack.network;

import net.minecraft.entity.player.EntityPlayerMP;

import com.darkona.adventurebackpack.common.ServerActions;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class HiddenPacket implements IMessageHandler<HiddenPacket.HiddenPacketMessage, IMessage> {

    @Override
    public IMessage onMessage(HiddenPacketMessage message, MessageContext ctx) {
        if (!ctx.side.isServer()) {
            return null;
        }

        EntityPlayerMP player = ctx.getServerHandler().playerEntity;

        if (player == null || player.isDead) return null;

        ServerActions.toggleHiddenBackpack(player, message.isHidden);

        return null;
    }

    public static class HiddenPacketMessage implements IMessage {

        private boolean isHidden;

        public HiddenPacketMessage() {}

        public HiddenPacketMessage(boolean isHidden) {
            this.isHidden = isHidden;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            isHidden = buf.readBoolean();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeBoolean(isHidden);
        }
    }

}
