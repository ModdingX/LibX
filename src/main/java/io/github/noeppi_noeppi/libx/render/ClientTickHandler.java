package io.github.noeppi_noeppi.libx.render;

import net.minecraftforge.event.TickEvent;

/**
 * On the client this counts the ticks in game. Useful for render code.
 */
public class ClientTickHandler {

    public static int ticksInGame = 0;

    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ticksInGame += 1;
        }
    }
}
