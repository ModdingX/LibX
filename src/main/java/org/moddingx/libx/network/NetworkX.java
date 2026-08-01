package org.moddingx.libx.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.moddingx.libx.impl.ModInternal;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * A class implementing network logic. You can use the {@link #register(PacketHandler) register} methods
 * in your constructor to register network packets.
 */
public abstract class NetworkX {

    private final Object lock;
    private final ModX mod;
    private final String version;
    @Nullable private HashSet<String> usedIds;
    private final List<PacketType<?>> packetTypes;
    
    protected NetworkX(ModX mod) {
        this.lock = new Object();
        this.mod = mod;
        this.version = this.getVersion();
        this.usedIds = new HashSet<>();
        this.packetTypes = new ArrayList<>();

        ModInternal.get(this.mod).modEventBus().addListener(this::runRegistration);
    }

    /**
     * Gets the network version for this network.
     */
    protected abstract String getVersion();

    /**
     * Registers a new packet handler to the system. This should be called in the constructor.
     */
    protected final <T extends CustomPacketPayload> void register(PacketHandler<T> handler) {
        this.doRegister(handler, false);
    }

    /**
     * Registers a new optional packet handler to the system. An optional packet handler is not required to be present
     * on the remote side for the connection negotiation to be successful. This should be called in the constructor.
     */
    protected final <T extends CustomPacketPayload> void registerOptional(PacketHandler<T> handler) {
        this.doRegister(handler, true);
    }

    private <T extends CustomPacketPayload> void doRegister(PacketHandler<T> handler, boolean optional) {
        synchronized (this.lock) {
            CustomPacketPayload.Type<T> type = handler.type();
            if (!Objects.equals(this.mod.modid, type.id().getNamespace())) {
                throw new IllegalArgumentException("Invalid packet namespace " + type.id().getNamespace() + ", expected " + this.mod.modid);
            } else if (this.usedIds == null) {
                throw new IllegalStateException("Network packet handler registered too late.");
            } else if (this.usedIds.add(type.id().getPath())) {
                this.packetTypes.add(new PacketType<>(type, handler, optional));
            } else {
                throw new IllegalStateException("Duplicate packet id: " + type.id());
            }
        }
    }

    private void runRegistration(RegisterPayloadHandlersEvent event) {
        synchronized (this.lock) {
            for (PacketType<?> packetType : this.packetTypes) {
                this.registerHandler(event, packetType);
            }
            this.usedIds = null;
        }
    }
    
    private <T extends CustomPacketPayload> void registerHandler(RegisterPayloadHandlersEvent event, PacketType<T> packetType) {
        synchronized (this.lock) {
            PayloadRegistrar registrar = event.registrar(this.version).executesOn(packetType.handler().target());
            if (packetType.optional()) registrar = registrar.optional();
            Void ignored = switch (packetType.handler().direction()) {
                case CLIENTBOUND -> {
                    registrar.playToClient(packetType.type(), packetType.handler().codec(), new WrappedHandler<>(packetType.handler()));
                    yield null;
                }
                case SERVERBOUND -> {
                    registrar.playToServer(packetType.type(), packetType.handler().codec(), new WrappedHandler<>(packetType.handler()));
                    yield null;
                }
            };
        }
    }

    /**
     * Checks whether a packet of a given type can currently be sent. On the physical client this checks that the
     * current connection supports the given packet type. On a dedicated server, this always returns {@code true}.
     */
    public boolean canSend(CustomPacketPayload.Type<?> type) {
        return switch(FMLEnvironment.getDist()) {
            case CLIENT -> ClientCanSendCheck.canSendOnClient(type);
            case DEDICATED_SERVER -> true;
        };
    }
    
    /**
     * Checks whether a packet of a given type can currently be sent to the given player. This check that the given
     * players connection supports the given packet type.
     */
    public boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        return player.connection.hasChannel(type);
    }

    private record PacketType<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, PacketHandler<T> handler, boolean optional) {}
    
    private record WrappedHandler<T extends CustomPacketPayload>(PacketHandler<T> handler) implements IPayloadHandler<T> {

        @Override
        public void handle(@Nonnull T payload, @Nonnull IPayloadContext context) {
            this.handler().handle(payload, context);
        }
    }
    
    private static class ClientCanSendCheck {
        
        public static boolean canSendOnClient(CustomPacketPayload.Type<?> type) {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            return connection != null && connection.hasChannel(type);
        }
    }
}
