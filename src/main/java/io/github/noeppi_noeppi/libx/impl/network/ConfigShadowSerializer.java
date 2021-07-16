package io.github.noeppi_noeppi.libx.impl.network;

import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.impl.config.ConfigImpl;
import io.github.noeppi_noeppi.libx.impl.config.ConfigState;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class ConfigShadowSerializer implements PacketSerializer<ConfigShadowSerializer.ConfigShadowMessage> {

    @Override
    public Class<ConfigShadowMessage> messageClass() {
        return ConfigShadowMessage.class;
    }

    @Override
    public void encode(ConfigShadowMessage msg, PacketBuffer buffer) {
        buffer.writeResourceLocation(msg.config.id);
        PacketBuffer b = new PacketBuffer(Unpooled.buffer());
        msg.state.write(b);
        buffer.writeVarInt(b.writerIndex());
        buffer.writeBytes(b);
    }

    @Override
    public ConfigShadowMessage decode(PacketBuffer buffer) {
        ResourceLocation configId = buffer.readResourceLocation();
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

    public static class ConfigShadowMessage {

        public ConfigImpl config;
        public ConfigState state;

        public ConfigShadowMessage(ConfigImpl config, ConfigState state) {
            this.config = config;
            this.state = state;
        }
    }
}
