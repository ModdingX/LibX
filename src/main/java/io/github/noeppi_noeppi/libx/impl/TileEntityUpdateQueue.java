package io.github.noeppi_noeppi.libx.impl;

import io.github.noeppi_noeppi.libx.LibX;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;

import java.util.*;

public class TileEntityUpdateQueue {

    private static final Map<World, Set<BlockPos>> updateQueue = new HashMap<>();

    public static void scheduleUpdate(World world, BlockPos pos) {
        if (!world.isRemote) {
            if (!updateQueue.containsKey(world)) {
                updateQueue.put(world, new HashSet<>());
            }
            updateQueue.get(world).add(pos);
        }
    }

    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for (Map.Entry<World, Set<BlockPos>> entry : updateQueue.entrySet()) {
                for (BlockPos pos : entry.getValue()) {
                    LibX.getNetwork().updateTE(entry.getKey(), pos);
                }
                entry.getValue().clear();
            }
        }
    }
}
