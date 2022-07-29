package org.moddingx.libx.network;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * An interface that extends the logic in {@link PacketSerializer} to support login packets.
 */
public interface LoginPacketSerializer<T extends IntSupplier> extends PacketSerializer<T> {

    /**
     * Gets the login packet index from the packet.
     */
    int getLoginIndex(T msg);

    /**
     * Sets the login packet index to the packet.
     */
    void setLoginIndex(T msg, int idx);

    /**
     * Builds a list of login packets that are sent on login. The default implementation calls a {@code public} no-arg
     * constructor on the message class and returns a {@link List} with exactly one packet.
     */
    default List<LoginPacket<T>> buildLoginPackets(boolean isLocal) {
        try {
            return List.of(new LoginPacket<>(this.messageClass().getName(), this.messageClass().getConstructor().newInstance()));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No public no-arg constructor defined in " + this.messageClass() + ". Override LoginPacketSerializer#buildLoginPackets to create custom login packets.", e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to invoke public no-arg constructor defined in " + this.messageClass() + ".", e);
        }
    }

    /**
     * Gets whether this login packet needs a response. The default implementation returns {@code true}.
     */
    default boolean needsResponse() {
        return true;
    }

    /**
     * A login packet.
     * 
     * @param context Some unique key for the login packet. Used for logging.
     * @param message The packet to send.
     */
    record LoginPacket<T extends IntSupplier>(String context, T message) {}
}
