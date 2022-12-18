package org.moddingx.libx.impl.base.decoration;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.PressurePlateBlock;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.base.decoration.DecorationType;
import org.moddingx.libx.impl.base.decoration.blocks.*;

import java.util.function.Function;

// Extra class, so we can access the types with impl classes in their generic signature
// but expose them with non impl classes.
public class DecorationTypes {
    
    public static final DecorationType<DecoratedBlock> BASE = new BaseDecorationType<>("", null, Function.identity());
    
    public static final DecorationType<DecoratedSlabBlock> SLAB = new BlockDecorationType<>("slab", Registries.BLOCK, DecoratedSlabBlock::new);
    public static final DecorationType<DecoratedStairBlock> STAIRS = new BlockDecorationType<>("stairs", Registries.BLOCK, DecoratedStairBlock::new);
    public static final DecorationType<DecoratedWallBlock> WALL = new BlockDecorationType<>("wall", Registries.BLOCK, DecoratedWallBlock::new);
    public static final DecorationType<DecoratedFenceBlock> FENCE = new BlockDecorationType<>("fence", Registries.BLOCK, DecoratedFenceBlock::new);
    public static final DecorationType<DecoratedFenceGateBlock> FENCE_GATE = new BlockDecorationType<>("fence_gate", Registries.BLOCK, DecoratedFenceGateBlock::new);
    public static final DecorationType<DecoratedButton> WOOD_BUTTON = new BlockDecorationType<>("button", Registries.BLOCK, block -> new DecoratedButton(block, 30, true, SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundEvents.WOODEN_BUTTON_CLICK_ON));
    public static final DecorationType<DecoratedButton> STONE_BUTTON = new BlockDecorationType<>("button", Registries.BLOCK, block -> new DecoratedButton(block, 20, false, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
    public static final DecorationType<DecoratedPressurePlate> WOOD_PRESSURE_PLATE = new BlockDecorationType<>("pressure_plate", Registries.BLOCK, block -> new DecoratedPressurePlate(PressurePlateBlock.Sensitivity.EVERYTHING, block));
    public static final DecorationType<DecoratedPressurePlate> STONE_PRESSURE_PLATE = new BlockDecorationType<>("pressure_plate", Registries.BLOCK, block -> new DecoratedPressurePlate(PressurePlateBlock.Sensitivity.MOBS, block));
    public static final DecorationType<DecoratedDoorBlock> DOOR = new BlockDecorationType<>("door", Registries.BLOCK, DecoratedDoorBlock::new);
    public static final DecorationType<DecoratedTrapdoorBlock> TRAPDOOR = new BlockDecorationType<>("trapdoor", Registries.BLOCK, DecoratedTrapdoorBlock::new);
    public static final DecorationType<DecoratedSign> SIGN = new BaseDecorationType<>("sign", null, (mod, context, block) -> new DecoratedSign(mod, block));
}
