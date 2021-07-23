package io.github.noeppi_noeppi.libx.impl.commands;

import net.minecraftforge.event.RegisterCommandsEvent;

import static io.github.noeppi_noeppi.libx.command.EnumArgument2.enumArgument;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.CompoundTagArgument.compoundTag;
import static net.minecraft.commands.arguments.EntityArgument.entities;
import static net.minecraft.commands.arguments.NbtPathArgument.nbtPath;

public class CommandsImpl {

    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("libx").requires(source -> source.hasPermission(2)).then(
                literal("hand")
                        .executes(new HandCommand())
                        .then(argument("output_format", enumArgument(NbtOutputType.class)).executes(new HandCommand())
                                .then(argument("nbt_path", nbtPath()).executes(new HandCommand())))
        ).then(
                literal("entitydata").then(argument("entities", entities()).then(argument("nbt", compoundTag()).executes(new EntityDataCommand())))
        ).then(
                literal("reload").executes(new ReloadCommand())
        ).then(
                literal("modlist").executes(new ModListCommand(false)).then(literal("detailed").executes(new ModListCommand(true)))
        ));
    }
}
