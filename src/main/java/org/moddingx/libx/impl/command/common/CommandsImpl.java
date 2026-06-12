package org.moddingx.libx.impl.command.common;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CommandsImpl {

    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("libx").then(
                Commands.literal("hand").requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)).executes(new HandCommand())
        ).then(
                Commands.literal("entitydata").requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)).then(
                        Commands.argument("entities", EntityArgument.entities())
                                .then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes(new EntityDataCommand()))
                )
        ).then(
                Commands.literal("reload").then(
                        Commands.literal("common").requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)).executes(new ReloadCommonCommand())
                )
        ));
    }
}
