package org.moddingx.libx.impl.command.common;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.moddingx.libx.config.ConfigManager;

public class ReloadCommonCommand implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        ConfigManager.reloadCommon();
        if (FMLEnvironment.getDist() == Dist.DEDICATED_SERVER) {
            ConfigManager.synchronize(context.getSource().getServer());
        }
        context.getSource().sendSuccess(() -> Component.translatable("libx.command.reload.common"), true);
        return 0;
    }
}
