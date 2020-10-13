package io.github.noeppi_noeppi.libx.network;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;

/**
 * A class implementing network logic. You should subclass it and create an instance in your
 * mods constructor. {@link NetworkX#registerPackets()} will then automatically be called
 * during setup phase. You can register custom packets there. The order in which they are
 * registered is important.
 */
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

    /**
     * Registers a packet handler.
     *
     * @param direction The network direction the packet should go.
     */
    protected <T> void register(NetworkHandler<T> handler, NetworkDirection direction) {
        this.instance.registerMessage(this.discriminator++, handler.messageClass(), handler::encode, handler::decode, handler::handle, Optional.of(direction));
    }

    /**
     * Gets the protocol version for this network. This must be the same on client and server.
     * It's recommended to use whole numbers here and increase them when you change something in
     * a packet format or add a new packet.
     */
    protected abstract String getProtocolVersion();

    /**
     * You can register your own packets here. The order is important.
     */
    protected abstract void registerPackets();
}
