package io.github.noeppi_noeppi.libx.impl.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class BeUpdateHandler {

    public static void handle(BeUpdateSerializer.BeUpdateMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null)
                return;
            BlockEntity be = level.getBlockEntity(msg.pos());
            if (be != null && msg.id().equals(be.getType().getRegistryName())) {
                be.handleUpdateTag(msg.nbt());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
