package io.github.noeppi_noeppi.libx.impl.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;
import java.util.UUID;

/**
 * As the "new" /data merge entity command can only merge data to one entity at a time and you always have to
 * prefix ist with a /execute here's a version that can merge data into multiple entities at once.
 * This command can also merge data into players. Be careful with this.
 */
public class EntityDataCommand implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        EntitySelector sel = context.getArgument("entities", EntitySelector.class);
        List<? extends Entity> entities = sel.findEntities(context.getSource());
        CompoundTag nbt = context.getArgument("nbt", CompoundTag.class);
        boolean players = false;
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                if (!context.getSource().hasPermission(4)) {
                    throw new SimpleCommandExceptionType(new TranslatableComponent("libx.command.entitydata.player_modify_no_permission")).create();
                } else {
                    players = true;
                }
            }
        }
        
        for (Entity entity : entities) {
            UUID uid = entity.getUUID();
            CompoundTag entityNBT = entity.saveWithoutId(new CompoundTag());
            entityNBT.merge(nbt);
            entity.load(entityNBT);
            entity.setUUID(uid);
        }
        context.getSource().sendSuccess(new TranslatableComponent(players ? "libx.command.entitydata.modified_player" : "libx.command.entitydata.modified", entities.size()), true);
        
        return 0;
    }
}
