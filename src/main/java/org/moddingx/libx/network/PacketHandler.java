package org.moddingx.libx.network;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * An interface implementing the logic on how to handle a type of packet.
 * 
 * <b>Note that {@link PacketSerializer} and {@link PacketHandler} may not be implemented o the same class.</b>
 */
public interface PacketHandler<T> {

    /**
     * The target thread, this handler should run on.
     */
    public Target target();

    /**
     * Handles the given message.
     * 
     * @return Whether the message was handles. This is ignored on the {@link Target#MAIN_THREAD main thread} target.
     */
    public boolean handle(T msg, Supplier<NetworkEvent.Context> ctx);

    /**
     * A thread target for a {@link PacketHandler}.
     */
    public enum Target {

        /**
         * The main thread, where the game logic happens.
         */
        MAIN_THREAD,

        /**
         * An async network thread. This target may also run on the game thread, but it doesn't need to..
         */
        NETWORK_THREAD
    }
}
