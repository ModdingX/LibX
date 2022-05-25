package io.github.noeppi_noeppi.libx.impl;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class Executor {

    public static final Executor INSTANCE = new Executor();

    private static final Queue<Consumer<MinecraftServer>> serverQueue = new LinkedList<>();
    private static final Queue<Runnable> clientQueue = new LinkedList<>();

    private Executor() {

    }

    @SubscribeEvent
    public void server(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.world.getServer() != null && event.world == event.world.getServer().overworld()) {
            synchronized (serverQueue) {
                while (true) {
                    Consumer<MinecraftServer> consumer = serverQueue.poll();
                    if (consumer == null) break;
                    consumer.accept(event.world.getServer());
                }
            }
        }
    }

    @SubscribeEvent
    public void client(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            synchronized (clientQueue) {
                while (true) {
                    Runnable runnable = clientQueue.poll();
                    if (runnable == null) break;
                    runnable.run();
                }
            }
        }
    }

    public static void enqueueServerWork(@Nonnull Consumer<MinecraftServer> consumer) {
        serverQueue.add(consumer);
    }

    public static void enqueueClientWork(@Nonnull Runnable runnable) {
        clientQueue.add(runnable);
    }
}
