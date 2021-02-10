package io.github.noeppi_noeppi.libx.impl.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.github.noeppi_noeppi.libx.config.ConfigManager;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;

public class ReloadCommand implements Command<CommandSource> {

    @Override
    public int run(CommandContext<CommandSource> context) {
        ConfigManager.reloadAll();
        context.getSource().sendFeedback(new TranslationTextComponent("libx.commamnd.reload"), true);
        return 0;
    }
}
