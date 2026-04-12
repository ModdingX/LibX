package org.moddingx.libx.impl.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.config.ConfigImpl;
import org.moddingx.libx.impl.config.ConfigState;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.util.Misc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConfigShadowHandler extends PacketHandler<ConfigShadowHandler.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(LibX.getInstance().resource("config_shadow"));
    
    protected ConfigShadowHandler() {
        super(TYPE, PacketFlow.CLIENTBOUND, StreamCodec.of(ConfigShadowHandler::encode, ConfigShadowHandler::decode), HandlerThread.MAIN);
    }

    private static void encode(FriendlyByteBuf buffer, Message msg) {
        if (msg.config() == null || msg.state() == null || Misc.MISSINGNO.equals(msg.config().id)) {
            buffer.writeResourceLocation(Misc.MISSINGNO);
        } else {
            buffer.writeResourceLocation(msg.config().id);
            FriendlyByteBuf b = new FriendlyByteBuf(Unpooled.buffer());
            msg.state().write(b);
            buffer.writeVarInt(b.writerIndex());
            buffer.writeBytes(b);
        }
    }

    private static Message decode(FriendlyByteBuf buffer) {
        ResourceLocation configId = buffer.readResourceLocation();
        if (Misc.MISSINGNO.equals(configId)) {
            return new Message(null, null);
        }
        ConfigImpl config = ConfigImpl.getConfigNullable(configId);
        int size = buffer.readVarInt();
        if (config == null) {
            LibX.logger.warn("Received shadow message for unknown config: '{}'. Ignoring", configId);
            // Skip the bytes we don't know about.
            buffer.skipBytes(size);
            return new Message(null, null);
        } else if (config.clientConfig) {
            LibX.logger.warn("Received shadow message for not-synced config: '{}'. Ignoring", configId);
            // Skip the bytes we don't know about.
            buffer.skipBytes(size);
            return new Message(null, null);
        } else {
            return new Message(config, config.readState(buffer));
        }
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        if (msg.config() != null && msg.state() != null) {
            msg.config().shadowBy(msg.state());
        }
    }

    public record Message(@Nullable ConfigImpl config, @Nullable ConfigState state) implements CustomPacketPayload {

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ConfigShadowHandler.TYPE;
        }
    }
}
