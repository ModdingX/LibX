package io.github.noeppi_noeppi.libx.impl.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.UUID;

/**
 * As the "new" /data merge entity command can only merge data to one enttiy at a time and you always have to
 * prefix ist with a /execute here's a version that can merge data into multiple entities at once.
 * This command can also merge data into players. Be careful with this.
 */
public class EntityDataCommand implements Command<CommandSource> {

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        EntitySelector sel = context.getArgument("entities", EntitySelector.class);
        List<? extends Entity> entities = sel.select(context.getSource());
        CompoundNBT nbt = context.getArgument("nbt", CompoundNBT.class);
        boolean players = false;
        for (Entity entity : entities) {
            if (entity instanceof PlayerEntity) {
                if (!context.getSource().hasPermissionLevel(4)) {
                    throw new SimpleCommandExceptionType(new TranslationTextComponent("libx.command.entitydata.player_modify_no_permission")).create();
                } else {
                    players = true;
                }
            }
        }
        
        for (Entity entity : entities) {
            UUID uid = entity.getUniqueID();
            CompoundNBT entityNBT = entity.writeWithoutTypeId(new CompoundNBT());
            entityNBT.merge(nbt);
            entity.read(entityNBT);
            entity.setUniqueId(uid);
        }
        context.getSource().sendFeedback(new TranslationTextComponent(players ? "libx.command.entitydata.modified_player" : "libx.command.entitydata.modified", entities.size()), true);
        
        return 0;
    }
}
