package org.moddingx.libx.impl.datagen;

import net.minecraft.world.level.block.Block;
import org.moddingx.libx.base.decoration.DecorationContext;
import org.moddingx.libx.datagen.provider.CommonTagsProviderBase;
import org.moddingx.libx.impl.base.decoration.blocks.*;
import org.moddingx.libx.impl.tags.InternalTags;

public class DecorationTags {

    public static void addTags(Block block, CommonTagsProviderBase provider, Runnable initInternal) {
        if (block instanceof DecoratedSlabBlock decorated) {
            initInternal.run();
            provider.block(InternalTags.Blocks.SLABS).add(decorated);
            if (decorated.parent.getContext().baseMaterial() == DecorationContext.BaseMaterial.WOOD) {
                provider.block(InternalTags.Blocks.WOODEN_SLABS).add(decorated);
            }
        } else if (block instanceof DecoratedStairBlock decorated) {
            initInternal.run();
            provider.block(InternalTags.Blocks.STAIRS).add(decorated);
            if (decorated.parent.getContext().baseMaterial() == DecorationContext.BaseMaterial.WOOD) {
                provider.block(InternalTags.Blocks.WOODEN_STAIRS).add(decorated);
            }
        } else if (block instanceof DecoratedWallBlock decorated) {
            initInternal.run();
            provider.block(InternalTags.Blocks.WALLS).add(decorated);
        } else if (block instanceof DecoratedFenceBlock decorated) {
            initInternal.run();
            provider.block(InternalTags.Blocks.FENCES).add(decorated);
            if (decorated.parent.getContext().baseMaterial() == DecorationContext.BaseMaterial.WOOD) {
                provider.block(InternalTags.Blocks.WOODEN_FENCES).add(decorated);
            }
        } else if (block instanceof DecoratedFenceGateBlock decorated) {
            initInternal.run();
            provider.block(InternalTags.Blocks.FENCE_GATES).add(decorated);
        } else if (block instanceof DecoratedDoorBlock decorated) {
            initInternal.run();
            provider.block(InternalTags.Blocks.DOORS).add(decorated);
            if (decorated.parent.getContext().baseMaterial() == DecorationContext.BaseMaterial.WOOD) {
                provider.block(InternalTags.Blocks.WOODEN_DOORS).add(decorated);
            }
        } else if (block instanceof DecoratedTrapdoorBlock decorated) {
            initInternal.run();
            provider.block(InternalTags.Blocks.TRAPDOORS).add(decorated);
            if (decorated.parent.getContext().baseMaterial() == DecorationContext.BaseMaterial.WOOD) {
                provider.block(InternalTags.Blocks.WOODEN_TRAPDOORS).add(decorated);
            }
        } else if (block instanceof DecoratedButton decorated) {
            initInternal.run();
            provider.block(InternalTags.Blocks.BUTTONS).add(decorated);
            if (decorated.parent.getContext().baseMaterial() == DecorationContext.BaseMaterial.WOOD) {
                provider.block(InternalTags.Blocks.WOODEN_BUTTONS).add(decorated);
            }
        } else if (block instanceof DecoratedPressurePlate decorated) {
            initInternal.run();
            provider.block(InternalTags.Blocks.PRESSURE_PLATES).add(decorated);
            switch (decorated.sensitivity) {
                case EVERYTHING -> provider.block(InternalTags.Blocks.WOODEN_PRESSURE_PLATES).add(decorated);
                case MOBS -> provider.block(InternalTags.Blocks.STONE_PRESSURE_PLATES).add(decorated);
                default -> {}
            }
        } else if (block instanceof DecoratedSign.Standing decorated) {
            initInternal.run();
            provider.block(InternalTags.Blocks.SIGNS).add(decorated);
            provider.block(InternalTags.Blocks.STANDING_SIGNS).add(decorated);
        } else if (block instanceof DecoratedSign.Wall decorated) {
            initInternal.run();
            provider.block(InternalTags.Blocks.SIGNS).add(decorated);
            provider.block(InternalTags.Blocks.WALL_SIGNS).add(decorated);
        }
    }
}
