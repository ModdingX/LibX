package io.github.noeppi_noeppi.libx.impl.commands;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraftforge.event.RegisterCommandsEvent;

public class CommandsImpl {

    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("libx").requires(source -> source.hasPermission(2)).then(
                Commands.literal("hand").executes(new HandCommand()).then(
                                Commands.argument("nbt_path", NbtPathArgument.nbtPath()).executes(new HandCommand())
                )
        ).then(
                Commands.literal("entitydata").then(
                        Commands.argument("entities", EntityArgument.entities())
                                .then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes(new EntityDataCommand()))
                )
        ).then(
                Commands.literal("reload").executes(new ReloadCommand())
        ).then(
                Commands.literal("modlist").executes(new ModListCommand(false)).then(
                        Commands.literal("detailed").executes(new ModListCommand(true))
                )
        ));
    }
}
