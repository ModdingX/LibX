package io.github.noeppi_noeppi.libx.network;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;

public abstract class NetworkX {

    private final String protocolVersion;
    public final SimpleChannel instance;
    private int discriminator = 0;

    public NetworkX(ModX mod) {
        this.protocolVersion = this.getProtocolVersion();
        this.instance = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(mod.modid, "netchannel"),
                () -> this.protocolVersion,
                this.protocolVersion::equals,
                this.protocolVersion::equals
        );
        //noinspection deprecation
        mod.addSetupTask(this::registerPackets);
    }

    protected  <T> void register(NetworkHandler<T> handler, NetworkDirection direction) {
        this.instance.registerMessage(this.discriminator++, handler.messageClass(), handler::encode, handler::decode, handler::handle, Optional.of(direction));
    }

    protected abstract String getProtocolVersion();
    protected abstract void registerPackets();
}
