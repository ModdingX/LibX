package org.moddingx.libx.impl.base.decoration;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.PressurePlateBlock;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.base.decoration.DecorationType;
import org.moddingx.libx.impl.base.decoration.blocks.*;

import java.util.function.Function;

// Extra class, so we can access the types with impl classes in their generic signature
// but expose them with non impl classes.
public class DecorationTypes {
    
    public static final DecorationType<DecoratedBlock> BASE = new BaseDecorationType<>("", null, Function.identity());

    public static final DecorationType<DecoratedWoodBlock> STRIPPED_LOG = new BlockDecorationType<>("stripped_log", Registries.BLOCK, 1, (mod, ctx, block) -> new DecoratedWoodBlock(block, null, null));
    public static final DecorationType<DecoratedWoodBlock> LOG = new BlockDecorationType<>("log", Registries.BLOCK, 1, (mod, ctx, block) -> new DecoratedWoodBlock(block, null, STRIPPED_LOG));
    public static final DecorationType<DecoratedWoodBlock> STRIPPED_WOOD = new BlockDecorationType<>("stripped_wood", Registries.BLOCK, 1, (mod, ctx, block) -> new DecoratedWoodBlock(block, STRIPPED_LOG, null));
    public static final DecorationType<DecoratedWoodBlock> WOOD = new BlockDecorationType<>("wood", Registries.BLOCK, 1, (mod, ctx, block) -> new DecoratedWoodBlock(block, LOG, STRIPPED_WOOD));

    public static final DecorationType<DecoratedSlabBlock> SLAB = new BlockDecorationType<>("slab", Registries.BLOCK, 0.5, DecoratedSlabBlock::new);
    public static final DecorationType<DecoratedStairBlock> STAIRS = new BlockDecorationType<>("stairs", Registries.BLOCK, 1, DecoratedStairBlock::new);
    public static final DecorationType<DecoratedWallBlock> WALL = new BlockDecorationType<>("wall", Registries.BLOCK, 1, DecoratedWallBlock::new);
    public static final DecorationType<DecoratedFenceBlock> FENCE = new BlockDecorationType<>("fence", Registries.BLOCK, 1, DecoratedFenceBlock::new);
    public static final DecorationType<DecoratedFenceGateBlock> FENCE_GATE = new BlockDecorationType<>("fence_gate", Registries.BLOCK, 1, DecoratedFenceGateBlock::new);
    public static final DecorationType<DecoratedButton> WOOD_BUTTON = new BlockDecorationType<>("button", Registries.BLOCK, 1/3d, block -> new DecoratedButton(block, true));
    public static final DecorationType<DecoratedButton> STONE_BUTTON = new BlockDecorationType<>("button", Registries.BLOCK, 1/3d, block -> new DecoratedButton(block, false));
    public static final DecorationType<DecoratedPressurePlate> WOOD_PRESSURE_PLATE = new BlockDecorationType<>("pressure_plate", Registries.BLOCK, 1, block -> new DecoratedPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, block));
    public static final DecorationType<DecoratedPressurePlate> STONE_PRESSURE_PLATE = new BlockDecorationType<>("pressure_plate", Registries.BLOCK, 1, block -> new DecoratedPressurePlate(PressurePlateBlock.Sensitivity.MOBS, block));
    public static final DecorationType<DecoratedDoorBlock> DOOR = new BlockDecorationType<>("door", Registries.BLOCK, 2/3d, DecoratedDoorBlock::new);
    public static final DecorationType<DecoratedTrapdoorBlock> TRAPDOOR = new BlockDecorationType<>("trapdoor", Registries.BLOCK, 1, DecoratedTrapdoorBlock::new);
    public static final DecorationType<DecoratedSign> SIGN = new BaseDecorationType<>("sign", null, (mod, context, block) -> new DecoratedSign(mod, block));
    public static final DecorationType<DecoratedHangingSign> HANGING_SIGN = new BaseDecorationType<>("hanging_sign", null, (mod, context, block) -> new DecoratedHangingSign(mod, block));
}
