package io.github.noeppi_noeppi.libx.impl.commands.client;

import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;

public class ClientCommandsImpl {

    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("libx").then(
                Commands.literal("modlist").executes(new ModListCommand(false)).then(
                        Commands.literal("detailed").executes(new ModListCommand(true))
                )
        ).then(
                Commands.literal("reload").then(
                        Commands.literal("client").executes(new ReloadClientCommand())
                )
        ));
    }
}
