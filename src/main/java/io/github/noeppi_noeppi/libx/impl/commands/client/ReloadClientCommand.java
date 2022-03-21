package io.github.noeppi_noeppi.libx.impl.commands.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.github.noeppi_noeppi.libx.config.ConfigManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;

public class ReloadClientCommand implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        ConfigManager.reloadClient();
        context.getSource().sendSuccess(new TranslatableComponent("libx.command.reload.client"), true);
        return 0;
    }
}
