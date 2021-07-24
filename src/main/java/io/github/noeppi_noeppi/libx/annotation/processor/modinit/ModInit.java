package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import io.github.noeppi_noeppi.libx.annotation.impl.ProcessorInterface;
import io.github.noeppi_noeppi.libx.config.ConfigManager;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

// TODO Check ModInit as things have been refactored. Verify that code generates correctly.
public class ModInit  {

    // TODO check all the classes in 1.17
    public static final String MOD_ANNOTATION_TYPE = "net.minecraftforge.fml.common.Mod";
    public static final String MODEL_TYPE = "net.minecraft.client.renderer.model.IBakedModel";
    public static final String REGISTRY_TYPE = "net.minecraft.util.registry.Registry";
    public static final String CODEC_TYPE = "com.mojang.serialization.Codec";
    public static final String RECORD_CODEC_BUILDER_TYPE = "com.mojang.serialization.codecs.RecordCodecBuilder";

    public static final List<String> DEFAULT_PARAM_CODEC_FIELDS = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            "CODEC", "DIRECT_CODEC"
    )));

    // When something is added here, also add it to ProcessorInterface.getCodecDefaultRegistryKey
    public static final Set<String> ALLOWED_REGISTRY_CODEC_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "net.minecraft.world.biome.Biome",
            "net.minecraft.world.gen.DimensionSettings"
            // TODO Add everything from RegistryAccess to the list.
    )));
    
    public final String modid;
    public final Element modClass;
    private final Map<Integer, List<RegistrationEntry>> registration = new HashMap<>();
    private final List<LoadableModel> models = new ArrayList<>();
    private final List<RegisteredConfig> configs = new ArrayList<>();
    private final List<GeneratedCodec> codecs = new ArrayList<>();

    public ModInit(String modid, Element modClass, Messager messager) {
        this.modid = modid;
        this.modClass = modClass;
        if (!(modClass.getEnclosingElement() instanceof PackageElement)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Parent element of mod class is not a package", modClass);
        }
    }

    public void addRegistration(int priority, List<RegistrationEntry> entries) {
        if (!this.registration.containsKey(priority)) {
            this.registration.put(priority, new ArrayList<>());
        }
        List<RegistrationEntry> list = this.registration.get(priority);
        list.addAll(entries);
    }
    
    public void addModel(String classFqn, String fieldName, String modelNamespace, String modelPath) {
        this.models.add(new LoadableModel(classFqn, fieldName, modelNamespace.isEmpty() ? this.modid : modelNamespace, modelPath));
    }
    
    public void addConfig(String name, boolean client, String classFqn) {
        this.configs.add(new RegisteredConfig(name, client, classFqn));
    }
    
    public void addCodec(GeneratedCodec codec) {
        this.codecs.add(codec);
    }
    
    public void write(Filer filer, Messager messager) {
        try {
            List<RegistrationEntry> allReg = this.registration.entrySet().stream()
                    .sorted(Comparator.comparingInt(e -> -e.getKey()))
                    .flatMap(e -> e.getValue().stream())
                    .collect(Collectors.toList());
            
            JavaFileObject file = filer.createSourceFile(((PackageElement) this.modClass.getEnclosingElement()).getQualifiedName() + "." + this.modClass.getSimpleName() + "$", this.modClass);
            Writer writer = file.openWriter();
            writer.write("package " + ((PackageElement) this.modClass.getEnclosingElement()).getQualifiedName() + ";");
            writer.write("public class " + this.modClass.getSimpleName() + "${");
            writer.write("private static " + ModX.class.getCanonicalName() + " mod=null;");
            if (!this.codecs.isEmpty()) {
                writer.write("public static final " + Map.class.getCanonicalName() + "<Class<?>," + CODEC_TYPE + "<?>>codecs=buildCodecs();");
                writer.write("private static final " + Map.class.getCanonicalName() + "<Class<?>," + CODEC_TYPE + "<?>>buildCodecs(){");
                //noinspection deprecation
                writer.write(ProcessorInterface.LazyMapBuilder.class.getCanonicalName() + " builder=" + ProcessorInterface.class.getCanonicalName() + ".lazyMapBuilder();");
                for (GeneratedCodec codec : this.codecs) {
                    writer.write("builder.put(" + codec.fqn + ".class,");
                    writer.write("() -> " + RECORD_CODEC_BUILDER_TYPE + ".<" + codec.fqn + ">create(instance->");
                    writer.write("instance.group(");
                    for (int i = 0; i < codec.params.size(); i++) {
                        GeneratedCodec.CodecElement param = codec.params.get(i);
                        writer.write("(");
                        param.writeCode(writer);
                        writer.write(")");
                        if (i < codec.params.size() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write(").apply(instance,instance.stable(");
                    writer.write("(");
                    for (int i = 0; i < codec.params.size(); i++) {
                        GeneratedCodec.CodecElement param = codec.params.get(i);
                        writer.write(param.typeFqnBoxed + " ctorArg" + i);
                        if (i < codec.params.size() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write(")->{");
                    writer.write("return new " + codec.fqn + "(");
                    for (int i = 0; i < codec.params.size(); i++) {
                        writer.write("ctorArg" + i);
                        if (i < codec.params.size() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write(");");
                    writer.write("}");
                    writer.write("))");
                    writer.write("));");
                }
                writer.write("return builder.build();");
                writer.write("}");
            }
            writer.write("public static void init(" + ModX.class.getCanonicalName() + " mod){");
            writer.write(this.modClass.getSimpleName() + "$.mod=mod;");
            for (RegisteredConfig config : this.configs) {
                //noinspection deprecation
                writer.write(ConfigManager.class.getCanonicalName() + ".registerConfig(" + ProcessorInterface.class.getCanonicalName() + ".newRL(\"" + quote(this.modid) + "\",\"" + quote(config.name) + "\")," + config.classFqn + ".class," + config.client + ");");
            }
            if (!allReg.isEmpty()) {
                writer.write("((" + ModXRegistration.class.getCanonicalName() + ")mod).addRegistrationHandler(" + this.modClass.getSimpleName() + "$::register);");
            }
            if (!this.models.isEmpty()) {
                writer.write("net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->{");
                //noinspection deprecation
                writer.write( ProcessorInterface.class.getCanonicalName() + ".addModListener(net.minecraftforge.client.event.ModelRegistryEvent.class," + this.modClass.getSimpleName() + "$::registerModels);");
                //noinspection deprecation
                writer.write( ProcessorInterface.class.getCanonicalName() + ".addModListener(net.minecraftforge.client.event.ModelBakeEvent.class," + this.modClass.getSimpleName() + "$::bakeModels);");
                writer.write("});");
            }
            writer.write("}");
            if (!allReg.isEmpty()) {
                writer.write("private static void register(){");
                for (RegistrationEntry entry : allReg) {
                    writer.write("((" + ModXRegistration.class.getCanonicalName() + ")mod).register(\"" + quote(entry.registryName) + "\"," + entry.fqn + ");");
                }
                writer.write("}");
            }
            if (!this.models.isEmpty()) {
                writer.write("@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)");
                writer.write("private static void registerModels(net.minecraftforge.client.event.ModelRegistryEvent event){");
                for (LoadableModel model : this.models) {
                    //noinspection deprecation
                    writer.write("net.minecraftforge.client.model.ModelLoader.addSpecialModel(" + ProcessorInterface.class.getCanonicalName() + ".newRL(\"" + quote(model.modelNamespace) + "\",\"" + quote(model.modelPath) + "\"));");
                }
                writer.write("}");
                writer.write("@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)");
                writer.write("private static void bakeModels(net.minecraftforge.client.event.ModelBakeEvent event){");
                for (LoadableModel model : this.models) {
                    //noinspection deprecation
                    writer.write(model.classFqn + "." + quote(model.fieldName) + "=event.getModelRegistry().get(" + ProcessorInterface.class.getCanonicalName() + ".newRL(\"" + quote(model.modelNamespace) + "\",\"" + quote(model.modelPath) + "\"));");
                }
                writer.write("}");
            }
            writer.write("}\n");
            writer.close();
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to generate source code: " + e, this.modClass);
        }
    }
    
    public static String quote(String str) {
        StringBuilder sb = new StringBuilder();
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
        return sb.toString();
    }
}
