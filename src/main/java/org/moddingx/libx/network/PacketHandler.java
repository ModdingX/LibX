package org.moddingx.libx.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;

public abstract class PacketHandler<T extends CustomPacketPayload> {

    private final CustomPacketPayload.Type<T> type;
    private final PacketFlow direction;
    private final StreamCodec<? super RegistryFriendlyByteBuf, T> codec;
    private final HandlerThread thread;

    protected PacketHandler(PacketFlow direction, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, CustomPacketPayload.Type<T> type) {
        this(type, direction, codec, HandlerThread.NETWORK);
    }
    
    protected PacketHandler(CustomPacketPayload.Type<T> type, PacketFlow direction, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, HandlerThread thread) {
        this.type = type;
        this.direction = direction;
        this.codec = codec;
        this.thread = thread;
    }

    public CustomPacketPayload.Type<T> type() {
        return this.type;
    }

    public final PacketFlow direction() {
        return this.direction;
    }
    
    public final StreamCodec<? super RegistryFriendlyByteBuf, T> codec() {
        return this.codec;
    }
    
    public final HandlerThread target() {
        return this.thread;
    }
    
    public void handle(T msg, IPayloadContext ctx) {
        // This method does nothing.
        // It is not abstract to allow implementing handler methods with @OnlyIn in a safe way.
        // Due to dynamic binding, if the overriding method is removed by the side stripper this
        // method will be invoked instead and nothing happens. This is also the primary reason,
        // PacketHandler is an abstract class instead of an interface.
    }
}
