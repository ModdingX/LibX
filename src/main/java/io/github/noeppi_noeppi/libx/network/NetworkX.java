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
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * A class implementing network logic. You should subclass it and create an instance in your
 * mods constructor. {@link NetworkX#registerPackets()} will then automatically be called
 * during setup phase. You can register custom packets there. The order in which they are
 * registered is important.
 */
public abstract class NetworkX {

    private static final Object LOCK = new Object();
    
    public final SimpleChannel channel;
    private final Protocol protocol;
    private int discriminator = 0;

    public NetworkX(ModX mod) {
        this.protocol = this.getProtocol();
        this.channel = NetworkRegistry.newSimpleChannel(
                mod.resource("netchannel"),
                this.protocol::version,
                remote -> this.protocol.client().predicate.test(this.protocol.version(), remote),
                remote -> this.protocol.server().predicate.test(this.protocol.version(), remote)
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
            this.channel.registerMessage(this.discriminator++, serializer.messageClass(), serializer::encode, serializer::decode, realHandler, Optional.of(direction));
        }
    }

    /**
     * Gets the {@link Protocol protocol} for this network.
     */
    protected abstract Protocol getProtocol();

    /**
     * You can register your own packets here. The order is important.
     */
    protected abstract void registerPackets();

    /**
     * A protocol defines when a connection is accepted or rejected.
     * 
     * @param version The protocol version. This must be equal on client and server
     * @param client The behaviour for the client
     * @param server The behaviour for the dedicated server
     */
    public static record Protocol(String version, ProtocolSide client, ProtocolSide server) {

        /**
         * Creates a new protocol with the given version, that is required on both sides.
         */
        public static Protocol of(String version) {
            return new Protocol(version, ProtocolSide.REQUIRED, ProtocolSide.REQUIRED);
        }
    }

    /**
     * Defines when a connection should be accepted.
     */
    public enum ProtocolSide {

        /**
         * The connection is only accepted if the protocol is present on the local and remote side
         */
        REQUIRED(String::equals),

        /**
         * The connection is accepted if the remote side is running on forge. However, it is not required
         * that the protocol is present on the other side.
         */
        OPTIONAL(REQUIRED.predicate.or((version, remote) -> NetworkRegistry.ABSENT.equals(remote))),

        /**
         * The connection is accepted if the remote side is running on forge or vanilla. However, it is not
         * required that the protocol is present on the other side.
         */
        VANILLA(OPTIONAL.predicate.or((version, remote) -> NetworkRegistry.ACCEPTVANILLA.equals(remote))),

        /**
         * The connection is always rejected.
         */
        REJECTED((version, remote) -> false);
        
        private final BiPredicate<String, String> predicate;

        ProtocolSide(BiPredicate<String, String> predicate) {
            this.predicate = predicate;
        }
    }
}
