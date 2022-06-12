package org.moddingx.libx.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * An interface implementing the logic on how to serialise and deserialize a message and how to handle it.
 */
public interface PacketSerializer<T> {

    Class<T> messageClass();

    void encode(T msg, FriendlyByteBuf buffer);

    T decode(FriendlyByteBuf buffer);
}
