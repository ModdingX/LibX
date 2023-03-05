package org.moddingx.libx.render;

import net.minecraftforge.event.TickEvent;

/**
 * On the client, this counts the ticks in game. Useful for rendering code.
 */
public class ClientTickHandler {

    private static int ticksInGame = 0;

    public static int ticksInGame() {
        return ticksInGame;
    }
    
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ticksInGame += 1;
        }
    }
}
