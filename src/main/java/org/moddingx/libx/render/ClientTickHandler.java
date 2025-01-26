package org.moddingx.libx.render;

import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * On the client, this counts the ticks in game. Useful for rendering code.
 */
public class ClientTickHandler {

    private static int ticksInGame = 0;

    public static int ticksInGame() {
        return ticksInGame;
    }
    
    public static void tick(ClientTickEvent.Pre event) {
        ticksInGame += 1;
    }
}
