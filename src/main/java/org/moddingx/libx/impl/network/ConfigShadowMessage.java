package org.moddingx.libx.impl.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.config.ConfigImpl;
import org.moddingx.libx.impl.config.ConfigState;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;
import org.moddingx.libx.util.Misc;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record ConfigShadowMessage(@Nullable ConfigImpl config, @Nullable ConfigState state) {

    public static class Serializer implements PacketSerializer<ConfigShadowMessage> {

        @Override
        public Class<ConfigShadowMessage> messageClass() {
            return ConfigShadowMessage.class;
        }

        @Override
        public void encode(ConfigShadowMessage msg, FriendlyByteBuf buffer) {
            if (msg.config() == null || msg.state() == null) {
                buffer.writeResourceLocation(Misc.MISSINGNO);
            } else {
                buffer.writeResourceLocation(msg.config().id);
                FriendlyByteBuf b = new FriendlyByteBuf(Unpooled.buffer());
                msg.state().write(b);
                buffer.writeVarInt(b.writerIndex());
                buffer.writeBytes(b);
            }
        }

        @Override
        public ConfigShadowMessage decode(FriendlyByteBuf buffer) {
            ResourceLocation configId = buffer.readResourceLocation();
            if (Misc.MISSINGNO.equals(configId)) {
                return new ConfigShadowMessage(null, null);
            }
            ConfigImpl config = ConfigImpl.getConfigNullable(configId);
            int size = buffer.readVarInt();
            if (config == null) {
                LibX.logger.warn("Received shadow message for unknown config: '" + configId + "'. Ignoring");
                // Skip the bytes we don't know about.
                buffer.skipBytes(size);
                return new ConfigShadowMessage(null, null);
            } else if (config.clientConfig) {
                LibX.logger.warn("Received shadow message for not-synced config: '" + configId + "'. Ignoring");
                // Skip the bytes we don't know about.
                buffer.skipBytes(size);
                return new ConfigShadowMessage(null, null);
            } else {
                return new ConfigShadowMessage(config, config.readState(buffer));
            }
        }
    }

    public static class Handler implements PacketHandler<ConfigShadowMessage> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(ConfigShadowMessage msg, Supplier<NetworkEvent.Context> ctx) {
            if (msg.config() != null && msg.state() != null) {
                msg.config().shadowBy(msg.state());
            }
            return true;
        }
    }
}
