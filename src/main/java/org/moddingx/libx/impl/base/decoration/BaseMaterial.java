package org.moddingx.libx.impl.base.decoration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.moddingx.libx.base.decoration.DecorationMaterial;

import java.util.function.Function;

public record BaseMaterial(boolean isWood, boolean isStone, boolean isMetal, Function<ResourceLocation, DecorationMaterial.MaterialProperties> factory) implements DecorationMaterial {

    public static final BaseMaterial GENERIC = new BaseMaterial(false, false, false, id -> new DecorationMaterial.MaterialProperties(null, null));
    
    public static final BaseMaterial WOOD = new BaseMaterial(true, false, false, id -> {
        BlockSetType setType = new BlockSetType(id.toString(), SoundType.WOOD, SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundEvents.WOODEN_BUTTON_CLICK_ON);
        WoodType woodType = new WoodType(id.toString(), setType, SoundType.WOOD, SoundType.HANGING_SIGN, SoundEvents.FENCE_GATE_CLOSE, SoundEvents.FENCE_GATE_OPEN);
        return new DecorationMaterial.MaterialProperties(setType, woodType);
    });
    
    public static final BaseMaterial STONE = new BaseMaterial(false, true, false, id -> {
        BlockSetType setType = new BlockSetType(id.toString(), SoundType.STONE, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);
        return new DecorationMaterial.MaterialProperties(setType, null);
    });
    
    public static final BaseMaterial METAL = new BaseMaterial(false, true, true, id -> {
        BlockSetType setType = new BlockSetType(id.toString(), SoundType.METAL, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN, SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);
        return new DecorationMaterial.MaterialProperties(setType, null);
    });

    @Override
    public MaterialProperties init(ResourceLocation id) {
        return this.factory.apply(id);
    }
}
