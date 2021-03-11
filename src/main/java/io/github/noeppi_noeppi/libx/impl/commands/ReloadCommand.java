package io.github.noeppi_noeppi.libx.impl.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.github.noeppi_noeppi.libx.config.ConfigManager;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ReloadCommand implements Command<CommandSource> {

    @Override
    public int run(CommandContext<CommandSource> context) {
        ConfigManager.reloadAll();
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            ConfigManager.forceResync(null);
        }
        context.getSource().sendFeedback(new TranslationTextComponent("libx.commamnd.reload"), true);
        return 0;
    }
}
