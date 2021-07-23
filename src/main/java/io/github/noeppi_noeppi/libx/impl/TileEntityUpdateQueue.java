package io.github.noeppi_noeppi.libx.impl;

import io.github.noeppi_noeppi.libx.LibX;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TileEntityUpdateQueue {

    private static final Map<Level, Set<BlockPos>> updateQueue = new HashMap<>();

    public static void scheduleUpdate(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            if (!updateQueue.containsKey(level)) {
                updateQueue.put(level, new HashSet<>());
            }
            updateQueue.get(level).add(pos);
        }
    }

    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for (Map.Entry<Level, Set<BlockPos>> entry : updateQueue.entrySet()) {
                for (BlockPos pos : entry.getValue()) {
                    LibX.getNetwork().updateTE(entry.getKey(), pos);
                }
                entry.getValue().clear();
            }
        }
    }
}
