package org.moddingx.libx.base.decoration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.moddingx.libx.impl.base.decoration.BaseMaterial;

import javax.annotation.Nullable;

/**
 * Provides some context about the block being decorated.
 */
public interface DecorationMaterial {

    /**
     * A material without any special properties.
     */
    DecorationMaterial GENERIC = BaseMaterial.GENERIC;
    
    /**
     * A material for wood blocks that adds a {@link BlockSetType} and a {@link WoodType}.
     */
    DecorationMaterial WOOD = BaseMaterial.WOOD;
    
    /**
     * A material for stone blocks that adds a {@link BlockSetType}.
     */
    DecorationMaterial STONE = BaseMaterial.STONE;
    
    /**
     * A material for metal blocks that adds a {@link BlockSetType}.
     */
    DecorationMaterial METAL = BaseMaterial.METAL;

    /**
     * Gets whether the block is a wood block.
     */
    boolean isWood();
    
    /**
     * Gets whether the block is a stone block.
     */
    boolean isStone();
    
    /**
     * Gets whether the block is a metal block.
     */
    boolean isMetal();

    /**
     * Initialises some material properties.
     * 
     * @param id The id of the {@link DecoratedBlock}.
     */
    MaterialProperties init(ResourceLocation id);
    
    class MaterialProperties {

        @Nullable private final BlockSetType blockSetType;
        @Nullable private final WoodType woodType;

        public MaterialProperties(@Nullable BlockSetType blockSetType, @Nullable WoodType woodType) {
            this.blockSetType = blockSetType;
            this.woodType = woodType;
        }
        
        public BlockSetType blockSetType() {
            if (this.blockSetType == null) throw new IllegalStateException("Decoration material has no BlockSetType available.");
            return this.blockSetType;
        }
        
        public WoodType woodType() {
            if (this.woodType == null) throw new IllegalStateException("Decoration material has no WoodType available.");
            return this.woodType;
        }
    }
}
