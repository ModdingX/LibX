package org.moddingx.libx.impl.commands.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.config.ConfigManager;

public class ReloadClientCommand implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        ConfigManager.reloadClient();
        context.getSource().sendSuccess(() -> Component.translatable("libx.command.reload.client"), true);
        return 0;
    }
}
