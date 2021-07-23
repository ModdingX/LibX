package io.github.noeppi_noeppi.libx.impl.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.github.noeppi_noeppi.libx.config.ConfigManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ReloadCommand implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        ConfigManager.reloadAll();
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            ConfigManager.forceResync(null);
        }
        context.getSource().sendSuccess(new TranslatableComponent("libx.command.reload"), true);
        return 0;
    }
}
