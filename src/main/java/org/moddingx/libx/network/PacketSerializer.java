package org.moddingx.libx.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * An interface implementing the logic on how to serialise and deserialize a message.
 */
public interface PacketSerializer<T> {

    /**
     * The class of the message serialised by this serialiser.
     */
    Class<T> messageClass();

    /**
     * Encodes the message to a {@link FriendlyByteBuf}.
     */
    void encode(T msg, FriendlyByteBuf buffer);

    /**
     * Decodes a message from a {@link FriendlyByteBuf}.
     */
    T decode(FriendlyByteBuf buffer);
}
