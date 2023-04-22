package org.moddingx.libx.sourcegen;

import com.google.common.hash.HashCode;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.data.event.GatherDataEvent;

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
            sourceFile.append("import net.minecraft.resources.ResourceLocation;\n");
            sourceFile.append("import ").append(this.registryClass.getName().replace('$', '.')).append(";\n\n");
            sourceFile.append("public class ").append(this.className).append(" {\n\n");
            sourceFile.append("    private ").append(this.className).append("() {}\n\n");
            sourceFile.append("    private static final ResourceKey<Registry<").append(type).append(">> REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation(")
                    .append(quote(this.registry.location().getNamespace())).append(",").append(quote(this.registry.location().getPath()))
                    .append("));\n\n");
            for (ResourceLocation key : lookup.listElementIds().map(ResourceKey::location).sorted(ResourceLocation::compareNamespaced).toList()) {
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
                sourceFile.append("    public static final ResourceKey<").append(type).append("> ").append(fn).append(" = ResourceKey.create(REGISTRY, new ResourceLocation(")
                        .append(quote(key.getNamespace())).append(",").append(quote(key.getPath()))
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

    private static String quote(String str) {
        StringBuilder sb = new StringBuilder("\"");
        for (char chr : str.toCharArray()) {
            if (chr == '\\') {
                sb.append("\\\\");
            } else if (chr == '\"') {
                sb.append("\\\"");
            } else if (chr == '\'') {
                sb.append("\\'");
            } else if (chr == '\n') {
                sb.append("\\n");
            } else if (chr == '\r') {
                sb.append("\\r");
            } else if (chr == '\t') {
                sb.append("\\t");
            } else if (chr == '\b') {
                sb.append("\\b");
            } else if (chr <= 0x1F || chr > 0xFF) {
                sb.append(String.format("\\u%04d", (int) chr));
            } else {
                sb.append(chr);
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
