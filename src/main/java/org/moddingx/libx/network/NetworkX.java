package org.moddingx.libx.network;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.annotation.meta.RemoveIn;
import org.moddingx.libx.impl.ModInternal;
import org.moddingx.libx.mod.ModX;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
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
        ModInternal.get(mod).addSetupTask(this::registerPackets, false);
    }

    /**
     * Registers a packet handler.
     *
     * @param handler The double lambda is required to prevent classloading on the server.
     * @param direction The network direction the packet should go.
     *
     * @deprecated Use {@link #registerLogin(NetworkDirection, LoginPacketSerializer, Supplier)} or {@link #registerGame(NetworkDirection, PacketSerializer, Supplier)} instead.
     * @see #registerLogin(NetworkDirection, LoginPacketSerializer, Supplier)
     * @see #registerGame(NetworkDirection, PacketSerializer, Supplier)
     */
    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
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

    protected final <T> void registerGame(NetworkDirection direction, PacketSerializer<T> serializer, Supplier<Supplier<PacketHandler<T>>> handler) {
        validateMessage(direction, serializer, false);
        BiConsumer<T, Supplier<NetworkEvent.Context>> action = resolveHandler(direction, serializer, handler);
        synchronized (LOCK) {
            this.channel.registerMessage(this.discriminator++, serializer.messageClass(), serializer::encode, serializer::decode, action, Optional.of(direction));
            this.channel.messageBuilder(serializer.messageClass(), this.discriminator++, direction)
                    .encoder(serializer::encode)
                    .decoder(serializer::decode)
                    .consumerNetworkThread(action)
                    .noResponse()
                    .add();
        }
    }

    protected final <T extends IntSupplier> void registerLogin(NetworkDirection direction, LoginPacketSerializer<T> serializer, Supplier<Supplier<PacketHandler<T>>> handler) {
        validateMessage(direction, serializer, true);
        BiConsumer<T, Supplier<NetworkEvent.Context>> action = resolveHandler(direction, serializer, handler);
        synchronized (LOCK) {
            SimpleChannel.MessageBuilder<T> builder = this.channel.messageBuilder(serializer.messageClass(), this.discriminator++, direction)
                    .encoder(serializer::encode)
                    .decoder(serializer::decode)
                    .consumerNetworkThread(action)
                    .markAsLoginPacket()
                    .loginIndex(serializer::getLoginIndex, serializer::setLoginIndex)
                    .buildLoginPacketList((isLocal) -> {
                        List<LoginPacketSerializer.LoginPacket<T>> packets = serializer.buildLoginPackets(isLocal);
                        return packets.stream().map(p -> Pair.of(p.context(), p.message())).toList();
                    });
            if (!serializer.needsResponse()) {
                builder.noResponse();
            }
            builder.add();
        }
    }

    private static <T> void validateMessage(NetworkDirection direction, PacketSerializer<T> serializer, boolean login) {
        Objects.requireNonNull(direction, "No network direction");

        if (login) {
            if (direction == NetworkDirection.PLAY_TO_CLIENT || direction == NetworkDirection.PLAY_TO_SERVER) {
                throw new IllegalArgumentException("Use registerGame to register game packets.");
            }
        } else {
            if (direction == NetworkDirection.LOGIN_TO_CLIENT || direction == NetworkDirection.LOGIN_TO_SERVER) {
                throw new IllegalArgumentException("Use registerLogin to register login packets.");
            }
        }

        if (!Modifier.isFinal(serializer.messageClass().getModifiers())) {
            throw new IllegalArgumentException("Non-final message class");
        }
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    private static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> resolveHandler(NetworkDirection direction, PacketSerializer<T> serializer, Supplier<Supplier<PacketHandler<T>>> supplier) {
        PacketHandler<T> handler;
        if (direction.getReceptionSide() == LogicalSide.CLIENT) {
            handler = DistExecutor.unsafeRunForDist(supplier, () -> () -> null);
        } else {
            handler = supplier.get().get();
        }
        if (handler == null) {
            return (msg, ctx) -> {};
        } else if (handler.getClass() == serializer.getClass()) {
            throw new IllegalStateException("The packet handler must be a different class than the packet serializer.");
        } else {
            return switch (handler.target()) {
                case MAIN_THREAD -> (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> handler.handle(msg, ctx));
                    ctx.get().setPacketHandled(true);
                };
                case NETWORK_THREAD -> (msg, ctx) -> {
                    boolean handled = handler.handle(msg, ctx);
                    ctx.get().setPacketHandled(handled);
                };
            };
        }
    }
    
    /**
     * A protocol defines when a connection is accepted or rejected.
     * 
     * @param version The protocol version. This must be equal on client and server
     * @param client The behaviour for the client
     * @param server The behaviour for the dedicated server
     */
    public record Protocol(String version, ProtocolSide client, ProtocolSide server) {

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
