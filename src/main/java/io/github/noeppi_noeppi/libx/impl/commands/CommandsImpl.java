package io.github.noeppi_noeppi.libx.impl.commands;

import net.minecraftforge.event.RegisterCommandsEvent;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static io.github.noeppi_noeppi.libx.command.UppercaseEnumArgument.enumArgument;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.arguments.EntityArgument.entities;
import static net.minecraft.command.arguments.NBTCompoundTagArgument.nbt;
import static net.minecraft.command.arguments.NBTPathArgument.nbtPath;

public class CommandsImpl {

    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("libx").requires(source -> source.hasPermissionLevel(2)).then(
                literal("hand")
                        .executes(new HandCommand())
                        .then(argument("output_format", enumArgument(NbtOutputType.class)).executes(new HandCommand())
                                .then(argument("nbt_path", nbtPath()).executes(new HandCommand())))
        ).then(
                literal("entitydata").then(argument("entities", entities()).then(argument("nbt", nbt()).executes(new EntityDataCommand())))
        ).then(
                literal("reload").executes(new ReloadCommand())
        ).then(
                literal("modlist").executes(new ModListCommand(false)).then(literal("detailed").executes(new ModListCommand(true)))
        ));
    }
}
