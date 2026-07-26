package org.moddingx.libx.sourcegen;

import com.google.common.hash.HashCode;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.moddingx.libx.annotation.processor.modinit.ModInit;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

// Generate java sources with registry keys from vanilla registries
public class RegistryKeyProvider<T> implements DataProvider {

    public static final String PACKAGE = "org.moddingx.libx.vanilla";

    private final Class<T> registryClass;
    private final ResourceKey<? extends Registry<T>> registry;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final String className;

    private final PackOutput output;

    public static <T> void create(GatherDataEvent event, Class<T> registryClass, ResourceKey<? extends Registry<T>> registry, String className) {
        RegistryKeyProvider<T> provider = new RegistryKeyProvider<>(registryClass, registry, event.getLookupProvider(), className, event.getGenerator().getPackOutput());
        event.getGenerator().addProvider(true, provider);
    }
    
    private RegistryKeyProvider(Class<T> registryClass, ResourceKey<? extends Registry<T>> registry, CompletableFuture<HolderLookup.Provider> lookupProvider, String className, PackOutput output) {
        this.registryClass = registryClass;
        this.registry = registry;
        this.lookupProvider = lookupProvider;
        this.className = className;
        this.output = output;
    }

    @Nonnull
    @Override
    public String getName() {
        return "Registry keys: " + this.registry;
    }

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput output) {
        return this.lookupProvider.thenCompose(provider -> {
            Path target = this.output.getOutputFolder().toAbsolutePath().getParent().resolve("java")
                .resolve(PACKAGE.replace(".", File.separator))
                .resolve(this.className + ".java")
                .toAbsolutePath().normalize();

            String type = this.registryClass.getSimpleName();
            HolderLookup<T> lookup = provider.lookupOrThrow(this.registry);
    
            StringBuilder sourceFile = new StringBuilder();
            sourceFile.append("package ").append(PACKAGE).append(";\n\n");
            sourceFile.append("import net.minecraft.core.Registry;\n");
            sourceFile.append("import net.minecraft.resources.ResourceKey;\n");
            sourceFile.append("import net.minecraft.resources.Identifier;\n");
            sourceFile.append("import ").append(this.registryClass.getName().replace('$', '.')).append(";\n\n");
            sourceFile.append("public class ").append(this.className).append(" {\n\n");
            sourceFile.append("    private ").append(this.className).append("() {}\n\n");
            sourceFile.append("    private static final ResourceKey<Registry<").append(type).append(">> REGISTRY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(")
                    .append(ModInit.quote(this.registry.identifier().getNamespace())).append(",").append(ModInit.quote(this.registry.identifier().getPath()))
                    .append("));\n\n");
            for (Identifier key : lookup.listElementIds().map(ResourceKey::identifier).sorted(Identifier::compareNamespaced).toList()) {
                StringBuilder fnb = new StringBuilder();
                if ("realms".equals(key.getNamespace())) {
                    fnb.append("REALMS_");
                } else if (!"minecraft".equals(key.getNamespace())) {
                    continue;
                }
                for (int chr : key.getPath().chars().toArray()) {
                    if (!Character.isJavaIdentifierPart((char) chr)) {
                        fnb.append("_");
                    } else {
                        fnb.append(Character.toString(chr).toUpperCase(Locale.ROOT));
                    }
                }
                String fn = fnb.toString();
                sourceFile.append("    public static final ResourceKey<").append(type).append("> ").append(fn).append(" = ResourceKey.create(REGISTRY, Identifier.fromNamespaceAndPath(")
                        .append(ModInit.quote(key.getNamespace())).append(",").append(ModInit.quote(key.getPath()))
                        .append("));\n");
            }
            sourceFile.append("}\n");

            ByteBuffer enc = StandardCharsets.UTF_8.encode(sourceFile.toString());
            byte[] data = new byte[enc.remaining()];
            enc.get(data);
            
            try {
                output.writeIfNeeded(target, data, HashCode.fromBytes(data));
                return CompletableFuture.completedFuture(null);
            } catch (IOException e) {
                return CompletableFuture.failedFuture(e);
            }
        });
    }
}
