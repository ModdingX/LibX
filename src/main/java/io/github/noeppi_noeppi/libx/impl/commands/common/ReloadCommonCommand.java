package io.github.noeppi_noeppi.libx.impl.commands.common;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.github.noeppi_noeppi.libx.config.ConfigManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ReloadCommonCommand implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        ConfigManager.reloadCommon();
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            ConfigManager.forceResync(null);
        }
        context.getSource().sendSuccess(new TranslatableComponent("libx.command.reload.common"), true);
        return 0;
    }
}
