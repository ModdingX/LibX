package org.moddingx.libx.impl.base.decoration;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.PressurePlateBlock;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.base.decoration.DecorationType;
import org.moddingx.libx.impl.base.decoration.blocks.*;

import java.util.function.Function;

// Extra class, so we can access the types with impl classes in their generic signature
// but expose them with non impl classes.
public class DecorationTypes {
    
    public static final DecorationType<DecoratedBlock> BASE = new BaseDecorationType<>("", null, Function.identity());
    
    public static final DecorationType<DecoratedSlabBlock> SLAB = new BlockDecorationType<>("slab", Registry.BLOCK_REGISTRY, DecoratedSlabBlock::new);
    public static final DecorationType<DecoratedStairBlock> STAIRS = new BlockDecorationType<>("stairs", Registry.BLOCK_REGISTRY, DecoratedStairBlock::new);
    public static final DecorationType<DecoratedWallBlock> WALL = new BlockDecorationType<>("wall", Registry.BLOCK_REGISTRY, DecoratedWallBlock::new);
    public static final DecorationType<DecoratedFenceBlock> FENCE = new BlockDecorationType<>("fence", Registry.BLOCK_REGISTRY, DecoratedFenceBlock::new);
    public static final DecorationType<DecoratedFenceGateBlock> FENCE_GATE = new BlockDecorationType<>("fence_gate", Registry.BLOCK_REGISTRY, DecoratedFenceGateBlock::new);
    public static final DecorationType<DecoratedWoodButton> WOOD_BUTTON = new BlockDecorationType<>("button", Registry.BLOCK_REGISTRY, DecoratedWoodButton::new);
    public static final DecorationType<DecoratedStoneButton> STONE_BUTTON = new BlockDecorationType<>("button", Registry.BLOCK_REGISTRY, DecoratedStoneButton::new);
    public static final DecorationType<DecoratedPressurePlate> WOOD_PRESSURE_PLATE = new BlockDecorationType<>("pressure_plate", Registry.BLOCK_REGISTRY, block -> new DecoratedPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, block));
    public static final DecorationType<DecoratedPressurePlate> STONE_PRESSURE_PLATE = new BlockDecorationType<>("pressure_plate", Registry.BLOCK_REGISTRY, block -> new DecoratedPressurePlate(PressurePlateBlock.Sensitivity.MOBS, block));
    public static final DecorationType<DecoratedDoorBlock> DOOR = new BlockDecorationType<>("door", Registry.BLOCK_REGISTRY, DecoratedDoorBlock::new);
    public static final DecorationType<DecoratedTrapdoorBlock> TRAPDOOR = new BlockDecorationType<>("trapdoor", Registry.BLOCK_REGISTRY, DecoratedTrapdoorBlock::new);
    public static final DecorationType<DecoratedSign> SIGN = new BaseDecorationType<>("sign", null, (mod, context, block) -> new DecoratedSign(mod, block));
}
