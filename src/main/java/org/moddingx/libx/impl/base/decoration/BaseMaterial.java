package org.moddingx.libx.impl.base.decoration;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.moddingx.libx.base.decoration.DecorationMaterial;

import java.util.function.Function;

public record BaseMaterial(boolean isWood, boolean isStone, boolean isMetal, Function<Identifier, DecorationMaterial.MaterialProperties> factory) implements DecorationMaterial {

    public static final BaseMaterial GENERIC = new BaseMaterial(false, false, false, id -> new DecorationMaterial.MaterialProperties(null, null));
    
    public static final BaseMaterial WOOD = new BaseMaterial(true, false, false, id -> {
        BlockSetType setType = new BlockSetType(
                id.toString(), true, true, true,
                BlockSetType.PressurePlateSensitivity.EVERYTHING, SoundType.WOOD,
                SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN,
                SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN,
                SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON,
                SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundEvents.WOODEN_BUTTON_CLICK_ON
        );
        WoodType woodType = new WoodType(
                id.toString(), setType, SoundType.WOOD, SoundType.HANGING_SIGN,
                SoundEvents.FENCE_GATE_CLOSE, SoundEvents.FENCE_GATE_OPEN
        );
        return new DecorationMaterial.MaterialProperties(setType, woodType);
    });

    public static final BaseMaterial NETHER_WOOD = new BaseMaterial(true, false, false, id -> {
        BlockSetType setType = new BlockSetType(
                id.toString(), true, true, true,
                BlockSetType.PressurePlateSensitivity.EVERYTHING, SoundType.NETHER_WOOD,
                SoundEvents.NETHER_WOOD_DOOR_CLOSE, SoundEvents.NETHER_WOOD_DOOR_OPEN,
                SoundEvents.NETHER_WOOD_TRAPDOOR_CLOSE, SoundEvents.NETHER_WOOD_TRAPDOOR_OPEN,
                SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_ON,
                SoundEvents.NETHER_WOOD_BUTTON_CLICK_OFF, SoundEvents.NETHER_WOOD_BUTTON_CLICK_ON
        );
        WoodType woodType = new WoodType(
                id.toString(), setType, SoundType.NETHER_WOOD, SoundType.NETHER_WOOD_HANGING_SIGN,
                SoundEvents.NETHER_WOOD_FENCE_GATE_CLOSE, SoundEvents.NETHER_WOOD_FENCE_GATE_OPEN
        );
        return new DecorationMaterial.MaterialProperties(setType, woodType);
    });
    
    public static final BaseMaterial STONE = new BaseMaterial(false, true, false, id -> {
        BlockSetType setType = new BlockSetType(
                id.toString(), true, false, false,
                BlockSetType.PressurePlateSensitivity.MOBS, SoundType.STONE,
                SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
                SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
                SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,
                SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);
        return new DecorationMaterial.MaterialProperties(setType, null);
    });
    
    public static final BaseMaterial IRON = new BaseMaterial(false, false, true, id -> {
        BlockSetType setType = new BlockSetType(
                id.toString(), false, false, false,
                BlockSetType.PressurePlateSensitivity.EVERYTHING, SoundType.METAL,
                SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
                SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
                SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
                SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);
        return new DecorationMaterial.MaterialProperties(setType, null);
    });

    public static final BaseMaterial COPPER = new BaseMaterial(false, false, true, id -> {
        BlockSetType setType = new BlockSetType(
                id.toString(), true, true, false,
                BlockSetType.PressurePlateSensitivity.EVERYTHING, SoundType.COPPER,
                SoundEvents.COPPER_DOOR_CLOSE, SoundEvents.COPPER_DOOR_OPEN,
                SoundEvents.COPPER_TRAPDOOR_CLOSE, SoundEvents.COPPER_TRAPDOOR_OPEN,
                SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
                SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);
        return new DecorationMaterial.MaterialProperties(setType, null);
    });

    @Override
    public MaterialProperties init(Identifier id) {
        return this.factory.apply(id);
    }
}
