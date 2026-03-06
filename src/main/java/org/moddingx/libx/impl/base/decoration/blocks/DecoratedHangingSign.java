package org.moddingx.libx.impl.base.decoration.blocks;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.moddingx.libx.base.decoration.DecoratedBlock;
import org.moddingx.libx.base.decoration.HangingSignAccess;
import org.moddingx.libx.impl.base.decoration.DecorationBlockIdContext;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;
import org.moddingx.libx.registration.SetupContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Set;
import java.util.function.Supplier;

public class DecoratedHangingSign implements Registerable, HangingSignAccess {

    public final ModX mod;
    public final DecoratedBlock parent;

    private final Ceiling ceiling;
    private final Wall wall;
    private final HangingSignItem item;
    private final BlockEntityType<Entity> beType;

    public DecoratedHangingSign(ModX mod, DecoratedBlock parent) {
        this.mod = mod;
        this.parent = parent;

        ResourceKey<Block> signKey = DecorationBlockIdContext.get();

        this.ceiling = new Ceiling(this.parent, this::getBlockEntityType, this.parent.getMaterialProperties().woodType());

        if (signKey != null) {
            DecorationBlockIdContext.set(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(
                    signKey.location().getNamespace(), signKey.location().getPath() + "_wall")));
        }

        this.wall = new Wall(this.parent, this::getBlockEntityType, this.parent.getMaterialProperties().woodType());
        if (signKey != null) {
            DecorationBlockIdContext.set(signKey);
        }

        //noinspection ConstantConditions
        this.beType = new BlockEntityType<>((pos, state) -> new Entity(this.getBlockEntityType(), pos, state), Set.of(this.ceiling, this.wall));

        Item.Properties itemProps = new Item.Properties().stacksTo(16).useBlockDescriptionPrefix();
        if (signKey != null) {
            itemProps.setId(ResourceKey.create(Registries.ITEM, signKey.location()));
        }
        this.item = new HangingSignItem(this.ceiling, this.wall, itemProps) {

            @Override
            public boolean isEnabled(@Nonnull FeatureFlagSet enabledFeatures) {
                return parent.isEnabled(enabledFeatures);
            }
        };
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        builder.register(Registries.BLOCK, this.ceiling);
        builder.register(Registries.BLOCK_ENTITY_TYPE, this.beType);
        builder.register(Registries.ITEM, this.item);
        builder.registerNamed(Registries.BLOCK, "wall", this.wall);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setupClient(SetupContext ctx) {
        BlockEntityRenderers.register(this.beType, HangingSignRenderer::new);
        // Add sign texture to sheet.
        ctx.enqueue(() -> Sheets.addWoodType(this.parent.getMaterialProperties().woodType()));
    }

    @Nonnull
    @Override
    public Item asItem() {
        if (this.item == null) throw new IllegalStateException("Can't get hanging sign item before registration");
        return this.item;
    }

    @Override
    public CeilingHangingSignBlock getCeilingBlock() {
        if (this.ceiling == null) throw new IllegalStateException("Can't get ceiling sign before registration");
        return this.ceiling;
    }

    @Override
    public WallHangingSignBlock getWallBlock() {
        if (this.wall == null) throw new IllegalStateException("Can't get wall sign before registration");
        return this.wall;
    }

    private BlockEntityType<Entity> getBlockEntityType() {
        if (this.beType == null) throw new IllegalStateException("Can't get hanging sign block entity type before registration");
        return this.beType;
    }

    public static class Ceiling extends CeilingHangingSignBlock {

        public final DecoratedBlock parent;
        private final Supplier<BlockEntityType<Entity>> beType;

        public Ceiling(DecoratedBlock parent, Supplier<BlockEntityType<Entity>> beType, WoodType wood) {
            super(wood, DecorationBlockIdContext.applyId(Properties.ofFullCopy(parent)));
            this.parent = parent;
            this.beType = beType;
        }

        @Nullable
        @Override
        @SuppressWarnings("NullableProblems")
        public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
            return this.beType.get().create(pos, state);
        }

        @Override
        public boolean isEnabled(@Nonnull FeatureFlagSet enabledFeatures) {
            return this.parent.isEnabled(enabledFeatures);
        }
    }

    public static class Wall extends WallHangingSignBlock {

        public final DecoratedBlock parent;
        private final Supplier<BlockEntityType<Entity>> beType;

        public Wall(DecoratedBlock parent, Supplier<BlockEntityType<Entity>> beType, WoodType wood) {
            super(wood, DecorationBlockIdContext.applyId(Properties.ofFullCopy(parent)));
            this.parent = parent;
            this.beType = beType;
        }

        @Nullable
        @Override
        @SuppressWarnings("NullableProblems")
        public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
            return this.beType.get().create(pos, state);
        }

        @Override
        public boolean isEnabled(@Nonnull FeatureFlagSet enabledFeatures) {
            return this.parent.isEnabled(enabledFeatures);
        }
    }

    public static class Entity extends HangingSignBlockEntity {

        // BlockEntity.<init> calls validateBlockState() -> HangingSignBlockEntity#getType
        // BEFORE the Entity constructor body runs, so this.signType is still null at that point.
        // We pass the type through a ThreadLocal so getType() can return it during the
        // super() chain, then store it in signType once super() returns.
        private static final ThreadLocal<BlockEntityType<?>> INIT_TYPE = new ThreadLocal<>();
        private final BlockEntityType<?> signType;

        public Entity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(initType(type, pos), state);
            this.signType = INIT_TYPE.get();
            INIT_TYPE.remove();
        }

        private static BlockPos initType(BlockEntityType<?> type, BlockPos pos) {
            INIT_TYPE.set(type);
            return pos;
        }

        @Nonnull
        @Override
        public BlockEntityType<?> getType() {
            BlockEntityType<?> init = INIT_TYPE.get();
            return init != null ? init : this.signType;
        }
    }
}
