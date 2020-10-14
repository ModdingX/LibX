package io.github.noeppi_noeppi.libx.impl.commands;

import net.minecraftforge.event.RegisterCommandsEvent;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.arguments.NBTPathArgument.nbtPath;

public class CommandsImpl {

    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("libx").requires(source -> source.hasPermissionLevel(2)).then(
                literal("hand").executes(new HandCommand()).then(argument("nbt_path", nbtPath()).executes(new HandCommand()))
        ));
    }
}
