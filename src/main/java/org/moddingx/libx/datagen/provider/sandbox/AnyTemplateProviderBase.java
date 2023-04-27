package org.moddingx.libx.datagen.provider.sandbox;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for {@link TemplateProviderBase} and {@link TemplateExtensionProviderBase}. These have been split up as
 * they need to run in different stages.
 */
public sealed abstract class AnyTemplateProviderBase extends RegistryProviderBase permits TemplateProviderBase, TemplateExtensionProviderBase {

    protected AnyTemplateProviderBase(DatagenContext ctx, DatagenStage requiredStage) {
        super(ctx, requiredStage);
    }
    
    public abstract class TemplateBuilder<T extends TemplateBuilder<T>> {
        
        private final List<PoolEntry> elements;
        private StructureTemplatePool.Projection currentProjection;

        protected TemplateBuilder() {
            this.elements = new ArrayList<>();
            this.currentProjection = StructureTemplatePool.Projection.RIGID;
        }
        
        protected abstract T self();
        
        protected List<Pair<StructurePoolElement, Integer>> elements() {
            return this.elements.stream().map(PoolEntry::build).toList();
        }

        /**
         * Changes the projection used for new templates to the given value.
         */
        public T projection(StructureTemplatePool.Projection projection) {
            this.currentProjection = projection;
            return this.self();
        }

        /**
         * Add an empty template to this pool.
         */
        public T empty() {
            return this.empty(1);
        }
        
        /**
         * Add an empty template to this pool.
         */
        public T empty(int weight) {
            return this.element(weight, EmptyPoolElement.INSTANCE);
        }
        
        /**
         * Add a feature template to this pool.
         */
        public T feature(Holder<PlacedFeature> feature) {
            return this.feature(1, feature);
        }
        
        /**
         * Add a feature template to this pool.
         */
        public T feature(int weight, Holder<PlacedFeature> feature) {
            return this.element(weight, new FeaturePoolElement(feature, this.currentProjection));
        }

        /**
         * Add a single pool element to this pool.
         */
        public T single(String templatePath) {
            return this.single(1, templatePath);
        }

        /**
         * Add a single pool element to this pool.
         */
        public T single(int weight, String templatePath) {
            return this.single(weight, templatePath, AnyTemplateProviderBase.this.holder(ProcessorLists.EMPTY));
        }

        /**
         * Add a single pool element to this pool.
         */
        public T single(String namespace, String path) {
            return this.single(1, namespace, path);
        }

        /**
         * Add a single pool element to this pool.
         */
        public T single(int weight, String namespace, String path) {
            return this.single(weight, namespace, path, AnyTemplateProviderBase.this.holder(ProcessorLists.EMPTY));
        }

        /**
         * Add a single pool element to this pool.
         */
        public T single(ResourceLocation templateId) {
            return this.single(1, templateId);
        }

        /**
         * Add a single pool element to this pool.
         */
        public T single(int weight, ResourceLocation templateId) {
            return this.single(weight, templateId, AnyTemplateProviderBase.this.holder(ProcessorLists.EMPTY));
        }

        /**
         * Add a single pool element to this pool.
         */
        public T single(String templatePath, Holder<StructureProcessorList> processor) {
            return this.single(1, templatePath, processor);
        }
        
        /**
         * Add a single pool element to this pool.
         */
        public T single(int weight, String templatePath, Holder<StructureProcessorList> processor) {
            return this.single(weight, AnyTemplateProviderBase.this.mod.resource(templatePath), processor);
        }

        /**
         * Add a single pool element to this pool.
         */
        public T single(String namespace, String path, Holder<StructureProcessorList> processor) {
            return this.single(1, namespace, path, processor);
        }

        /**
         * Add a single pool element to this pool.
         */
        public T single(int weight, String namespace, String path, Holder<StructureProcessorList> processor) {
            return this.single(weight, new ResourceLocation(namespace, path), processor);
        }

        /**
         * Add a single pool element to this pool.
         */
        public T single(ResourceLocation templateId, Holder<StructureProcessorList> processor) {
            return this.single(1, templateId, processor);
        }

        /**
         * Add a single pool element to this pool.
         */
        public T single(int weight, ResourceLocation templateId, Holder<StructureProcessorList> processor) {
            return this.element(weight, new SinglePoolElement(Either.left(templateId), processor, this.currentProjection));
        }

        /**
         * Add a legacy pool element to this pool.
         */
        public T legacy(String templatePath) {
            return this.legacy(1, templatePath);
        }

        /**
         * Add a legacy pool element to this pool.
         */
        public T legacy(int weight, String templatePath) {
            return this.legacy(weight, templatePath, AnyTemplateProviderBase.this.holder(ProcessorLists.EMPTY));
        }

        /**
         * Add a legacy pool element to this pool.
         */
        public T legacy(String namespace, String path) {
            return this.legacy(1, namespace, path);
        }

        public T legacy(int weight, String namespace, String path) {
            return this.legacy(weight, namespace, path, AnyTemplateProviderBase.this.holder(ProcessorLists.EMPTY));
        }

        /**
         * Add a legacy pool element to this pool.
         */
        public T legacy(ResourceLocation templateId) {
            return this.legacy(1, templateId);
        }

        /**
         * Add a legacy pool element to this pool.
         */
        public T legacy(int weight, ResourceLocation templateId) {
            return this.legacy(weight, templateId, AnyTemplateProviderBase.this.holder(ProcessorLists.EMPTY));
        }

        /**
         * Add a legacy pool element to this pool.
         */
        public T legacy(String templatePath, Holder<StructureProcessorList> processor) {
            return this.legacy(1, templatePath, processor);
        }

        /**
         * Add a legacy pool element to this pool.
         */
        public T legacy(int weight, String templatePath, Holder<StructureProcessorList> processor) {
            return this.legacy(weight, AnyTemplateProviderBase.this.mod.resource(templatePath), processor);
        }

        /**
         * Add a legacy pool element to this pool.
         */
        public T legacy(String namespace, String path, Holder<StructureProcessorList> processor) {
            return this.legacy(1, namespace, path, processor);
        }

        /**
         * Add a legacy pool element to this pool.
         */
        public T legacy(int weight, String namespace, String path, Holder<StructureProcessorList> processor) {
            return this.legacy(weight, new ResourceLocation(namespace, path), processor);
        }

        /**
         * Add a legacy pool element to this pool.
         */
        public T legacy(ResourceLocation templateId, Holder<StructureProcessorList> processor) {
            return this.legacy(1, templateId, processor);
        }

        /**
         * Add a legacy pool element to this pool.
         */
        public T legacy(int weight, ResourceLocation templateId, Holder<StructureProcessorList> processor) {
            return this.element(weight, new LegacySinglePoolElement(Either.left(templateId), processor, this.currentProjection));
        }

        /**
         * Add a legacy pool element to this pool.
         */
        public T list(StructurePoolElement... elements) {
            return this.list(1, elements);
        }

        /**
         * Add a list pool element that places all its children to this pool.
         */
        public T list(int weight, StructurePoolElement... elements) {
            return this.list(weight, Arrays.asList(elements));
        }

        /**
         * Add a list pool element that places all its children to this pool.
         */
        public T list(List<StructurePoolElement> elements) {
            return this.list(1, elements);
        }

        /**
         * Add a list pool element that places all its children to this pool.
         */
        public T list(int weight, List<StructurePoolElement> elements) {
            return this.element(weight, new ListPoolElement(List.copyOf(elements), this.currentProjection));
        }

        /**
         * Add a pool element to this pool.
         */
        public T element(StructurePoolElement element) {
            return this.element(1, element);
        }

        /**
         * Add a pool element to this pool.
         */
        public T element(int weight, StructurePoolElement element) {
            this.elements.add(new PoolEntry(element, weight));
            return this.self();
        }

        private record PoolEntry(StructurePoolElement element, int weight) {
            
            public Pair<StructurePoolElement, Integer> build() {
                return Pair.of(this.element(), this.weight());
            }
        }
    }
}
