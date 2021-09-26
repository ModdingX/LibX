package io.github.noeppi_noeppi.libx.network;

import io.github.noeppi_noeppi.libx.impl.ModInternal;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * A class implementing network logic. You should subclass it and create an instance in your
 * mods constructor. {@link NetworkX#registerPackets()} will then automatically be called
 * during setup phase. You can register custom packets there. The order in which they are
 * registered is important.
 */
public abstract class NetworkX {

    private static final Object LOCK = new Object();
    
    public final SimpleChannel instance;
    private final String protocolVersion;
    private int discriminator = 0;

    public NetworkX(ModX mod) {
        this.protocolVersion = this.getProtocolVersion();
        this.instance = NetworkRegistry.newSimpleChannel(
                mod.resource("netchannel"),
                () -> this.protocolVersion,
                this.protocolVersion::equals,
                this.protocolVersion::equals
        );
        ModInternal.get(mod).addSetupTask(this::registerPackets);
    }

    /**
     * Registers a packet handler.
     *
     * @param handler The double lambda is required to prevent classloading on the server.
     * @param direction The network direction the packet should go.
     */
    protected <T> void register(PacketSerializer<T> serializer, Supplier<BiConsumer<T, Supplier<NetworkEvent.Context>>> handler, NetworkDirection direction) {
        synchronized (LOCK) {
            Objects.requireNonNull(direction);
            BiConsumer<T, Supplier<NetworkEvent.Context>> realHandler;
            if (direction == NetworkDirection.PLAY_TO_CLIENT || direction == NetworkDirection.LOGIN_TO_CLIENT) {
                realHandler = DistExecutor.unsafeRunForDist(() -> handler, () -> () -> (msg, ctx) -> {});
            } else {
                realHandler = handler.get();
            }
            this.instance.registerMessage(this.discriminator++, serializer.messageClass(), serializer::encode, serializer::decode, realHandler, Optional.of(direction));
        }
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
