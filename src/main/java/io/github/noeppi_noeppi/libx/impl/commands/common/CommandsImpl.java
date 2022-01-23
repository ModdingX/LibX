package io.github.noeppi_noeppi.libx.impl.commands.common;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraftforge.event.RegisterCommandsEvent;

public class CommandsImpl {

    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("libx").then(
                Commands.literal("hand").requires(source -> source.hasPermission(2)).executes(new HandCommand()).then(
                                Commands.argument("nbt_path", NbtPathArgument.nbtPath()).executes(new HandCommand())
                )
        ).then(
                Commands.literal("entitydata").requires(source -> source.hasPermission(2)).then(
                        Commands.argument("entities", EntityArgument.entities())
                                .then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes(new EntityDataCommand()))
                )
        ).then(
                Commands.literal("reload").requires(source -> source.hasPermission(2)).executes(new ReloadCommand())
        ));
    }
}
