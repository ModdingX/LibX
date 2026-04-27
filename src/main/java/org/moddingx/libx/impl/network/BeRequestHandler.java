package org.moddingx.libx.impl.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.LibX;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class BeRequestHandler extends PacketHandler<BeRequestHandler.Message> {
    
    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(LibX.getInstance().resource("be_request"));

    protected BeRequestHandler() {
        super(TYPE, PacketFlow.SERVERBOUND, BlockPos.STREAM_CODEC.map(Message::new, Message::pos), HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer sender) {
            ServerLevel level = sender.level();
            if (level.hasChunk(SectionPos.blockToSectionCoord(msg.pos().getX()), SectionPos.blockToSectionCoord(msg.pos().getZ()))
                    && NetworkImpl.getImpl().canSend(sender, BeUpdateHandler.TYPE)) {
                BeUpdateHandler.Message reply = NetworkImpl.getImpl().getBeUpdateMessage(sender.level(), msg.pos());
                if (reply != null) ctx.reply(reply);
            }
        }
    }

    public record Message(BlockPos pos) implements CustomPacketPayload {
        
        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return BeRequestHandler.TYPE;
        }
    }
}
