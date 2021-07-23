package io.github.noeppi_noeppi.libx.impl.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TeUpdateHandler {

    public static void handle(TeUpdateSerializer.TeUpdateMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null)
                return;
            BlockEntity be = world.getBlockEntity(msg.pos);
            if (be != null && msg.id.equals(be.getType().getRegistryName())) {
                be.handleUpdateTag(level.getBlockState(msg.pos), msg.nbt);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
