package io.github.noeppi_noeppi.libx.impl.base.decoration;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import io.github.noeppi_noeppi.libx.base.decoration.DecoratedBlock;
import io.github.noeppi_noeppi.libx.base.decoration.DecorationType;
import io.github.noeppi_noeppi.libx.impl.base.decoration.blocks.*;
import net.minecraft.world.level.block.PressurePlateBlock;

import java.util.function.Function;

// Extra class, so we can access the types with impl classes in their generic signature
// but expose them with non impl classes.
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
public class DecorationTypes {
    
    public static final DecorationType<DecoratedBlock> BASE = new BaseDecorationType<>("", Function.identity());
    
    public static final DecorationType<DecoratedSlabBlock> SLAB = new BlockDecorationType<>("slab", DecoratedSlabBlock::new);
    public static final DecorationType<DecoratedStairBlock> STAIR = new BlockDecorationType<>("stair", DecoratedStairBlock::new);
    public static final DecorationType<DecoratedWallBlock> WALL = new BlockDecorationType<>("wall", DecoratedWallBlock::new);
    public static final DecorationType<DecoratedFenceBlock> FENCE = new BlockDecorationType<>("fence", DecoratedFenceBlock::new);
    public static final DecorationType<DecoratedFenceGateBlock> FENCE_GATE = new BlockDecorationType<>("fence_gate", DecoratedFenceGateBlock::new);
    public static final DecorationType<DecoratedWoodButton> WOOD_BUTTON = new BlockDecorationType<>("button", DecoratedWoodButton::new);
    public static final DecorationType<DecoratedStoneButton> STONE_BUTTON = new BlockDecorationType<>("button", DecoratedStoneButton::new);
    public static final DecorationType<DecoratedPressurePlate> WOOD_PRESSURE_PLATE = new BlockDecorationType<>("pressure_plate", block -> new DecoratedPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, block));
    public static final DecorationType<DecoratedPressurePlate> STONE_PRESSURE_PLATE = new BlockDecorationType<>("pressure_plate", block -> new DecoratedPressurePlate(PressurePlateBlock.Sensitivity.MOBS, block));
    public static final DecorationType<DecoratedDoorBlock> DOOR = new BlockDecorationType<>("door", DecoratedDoorBlock::new);
    public static final DecorationType<DecoratedTrapdoorBlock> TRAPDOOR = new BlockDecorationType<>("trapdoor", DecoratedTrapdoorBlock::new);
    public static final DecorationType<DecoratedSign> SIGN = new BaseDecorationType<>("sign", (mod, context, block) -> new DecoratedSign(mod, block));
}
