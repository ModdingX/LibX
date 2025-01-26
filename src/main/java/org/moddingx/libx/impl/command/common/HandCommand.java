package org.moddingx.libx.impl.command.common;

import com.google.common.base.Suppliers;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.moddingx.libx.util.game.ComponentUtil;

// Not a client command as the client does not have the full stack data
public class HandCommand implements Command<CommandSourceStack> {

    private static final ResourceKey<Item> AIR = ResourceKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("air"));
    
    @Override
    public int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

        ResourceKey<Item> item = BuiltInRegistries.ITEM.getResourceKey(stack.getItem()).orElse(AIR);
        int count;
        DataComponentPatch components;

        if (stack.isEmpty() || item.equals(AIR)) {
            item = AIR;
            count = 0;
            components = DataComponentPatch.EMPTY;
        } else {
            count = stack.getCount();
            components = stack.getComponentsPatch();
        }
        
        MutableComponent message = ComponentUtil.withCopyAction(Component.literal(item.location().toString()), item.location().toString()).copy();

        if (count != 1) {
            message = message.append(Component.literal(" ")).append(Component.literal(Integer.toString(count)));
        }

        if (components.size() != 0) {
            Component componentsText = ComponentUtil.toPrettyComponent(Registries.DATA_COMPONENT_TYPE, components);
            componentsText = ComponentUtil.withCopyAction(componentsText, componentsText.getString());
            message = message.append(Component.literal(" ")).append(componentsText);
        }

        ctx.getSource().sendSuccess(Suppliers.ofInstance(message), true);

        return 0;
    }
}
