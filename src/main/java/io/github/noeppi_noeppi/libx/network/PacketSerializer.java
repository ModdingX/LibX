package io.github.noeppi_noeppi.libx.network;

import io.github.noeppi_noeppi.libx.impl.network.TeUpdateSerializer;
import net.minecraft.network.PacketBuffer;

/**
 * An interface implementing the logic on how to serialise and deserialise a message and how to handle it.
 * For an example see {@link TeUpdateSerializer}.
 */
public interface PacketSerializer<T> {

    Class<T> messageClass();

    void encode(T msg, PacketBuffer buffer);

    T decode(PacketBuffer buffer);
}
