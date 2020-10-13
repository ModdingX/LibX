package io.github.noeppi_noeppi.libx.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * An interface implementing the logic on how to serialise and deserialise a message and how to handle it.
 * For an example see {@link io.github.noeppi_noeppi.libx.impl.network.TeUpdateHandler}.
 */
public interface NetworkHandler<T> {

    Class<T> messageClass();

    void encode(T msg, PacketBuffer buffer);

    T decode(PacketBuffer buffer);

    void handle(T msg, Supplier<NetworkEvent.Context> ctx);
}
