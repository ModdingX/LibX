package org.moddingx.libx.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.moddingx.libx.LibX;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockEntityUpdateQueue {

    private static final Map<ServerLevel, Set<BlockPos>> updateQueue = new HashMap<>();

    public static void scheduleUpdate(Level level, BlockPos pos) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (!updateQueue.containsKey(serverLevel)) {
                updateQueue.put(serverLevel, new HashSet<>());
            }
            updateQueue.get(serverLevel).add(pos);
        }
    }

    public static void tick(ServerTickEvent.Post event) {
        for (Map.Entry<ServerLevel, Set<BlockPos>> entry : updateQueue.entrySet()) {
            for (BlockPos pos : entry.getValue()) {
                LibX.getNetwork().updateBE(entry.getKey(), pos);
            }
            entry.getValue().clear();
        }
    }
}
