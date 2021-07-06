package io.github.noeppi_noeppi.libx.network;

import net.minecraft.network.PacketBuffer;

/**
 * An interface implementing the logic on how to serialise and deserialize a message and how to handle it.
 */
public interface PacketSerializer<T> {

    Class<T> messageClass();

    void encode(T msg, PacketBuffer buffer);

    T decode(PacketBuffer buffer);
}
