package org.moddingx.libx.impl.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.config.ConfigImpl;
import org.moddingx.libx.impl.config.ConfigState;
import org.moddingx.libx.network.PacketSerializer;

public class ConfigShadowSerializer implements PacketSerializer<ConfigShadowSerializer.ConfigShadowMessage> {

    @Override
    public Class<ConfigShadowMessage> messageClass() {
        return ConfigShadowMessage.class;
    }

    @Override
    public void encode(ConfigShadowMessage msg, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(msg.config.id);
        FriendlyByteBuf b = new FriendlyByteBuf(Unpooled.buffer());
        msg.state.write(b);
        buffer.writeVarInt(b.writerIndex());
        buffer.writeBytes(b);
    }

    @Override
    public ConfigShadowMessage decode(FriendlyByteBuf buffer) {
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

    public record ConfigShadowMessage(ConfigImpl config, ConfigState state) {}
}
