package org.moddingx.libx.annotation.processor.modinit;

import org.moddingx.libx.annotation.processor.Classes;
import org.moddingx.libx.annotation.processor.modinit.codec.GeneratedCodec;
import org.moddingx.libx.annotation.processor.modinit.config.RegisteredConfig;
import org.moddingx.libx.annotation.processor.modinit.config.RegisteredMapper;
import org.moddingx.libx.annotation.processor.modinit.model.LoadableModel;
import org.moddingx.libx.annotation.processor.modinit.register.RegistrationEntry;

import javax.annotation.Nullable;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class ModInit  {

    public static final List<String> DEFAULT_PARAM_CODEC_FIELDS = List.of("CODEC", "DIRECT_CODEC");

    public final String modid;
    public final Element modClass;
    private final Map<Integer, List<RegistrationEntry>> registration = new HashMap<>();
    private final List<LoadableModel> models = new ArrayList<>();
    private final List<RegisteredMapper> configMappers = new ArrayList<>();
    private final List<RegisteredConfig> configs = new ArrayList<>();
    private final List<GeneratedCodec> codecs = new ArrayList<>();

    public ModInit(String modid, Element modClass, Messager messager) {
        this.modid = modid;
        this.modClass = modClass;
        if (modClass.getEnclosingElement().getKind() != ElementKind.PACKAGE || !(modClass.getEnclosingElement() instanceof PackageElement)) {
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

    public void addConfigMapper(String classFqn, String targetTypeSource, @Nullable String requiresMod, boolean genericType) {
        this.configMappers.add(new RegisteredMapper(classFqn, targetTypeSource, requiresMod, genericType));
    }

    public void addConfig(String name, boolean client, @Nullable String requiresMod, String classFqn) {
        this.configs.add(new RegisteredConfig(name, client, requiresMod, classFqn));
    }

    public void addCodec(GeneratedCodec codec) {
        this.codecs.add(codec);
    }
    
    public void write(Filer filer, Messager messager) {
        try {
            List<RegistrationEntry> allReg = this.registration.entrySet().stream()
                    .sorted(Comparator.comparingInt(e -> -e.getKey()))
                    .flatMap(e -> e.getValue().stream()).toList();
            
            JavaFileObject file = filer.createSourceFile(((PackageElement) this.modClass.getEnclosingElement()).getQualifiedName() + "." + this.modClass.getSimpleName() + "$", this.modClass);
            Writer writer = file.openWriter();
            writer.write("package " + ((PackageElement) this.modClass.getEnclosingElement()).getQualifiedName() + ";");
            writer.write("@" + SuppressWarnings.class.getCanonicalName() + "({\"all\",\"unchecked\",\"rawtypes\",\"deprecation\"})");
            writer.write("public class " + this.modClass.getSimpleName() + "${");
            writer.write("private static " + Classes.sourceName(Classes.MODX) + " mod=null;");
            if (!this.codecs.isEmpty()) {
                writer.write("public static final " + Map.class.getCanonicalName() + "<Class<?>," + Classes.sourceName(Classes.CODEC) + "<?>>codecs=buildCodecs();");
                writer.write("private static final " + Map.class.getCanonicalName() + "<Class<?>," + Classes.sourceName(Classes.CODEC) + "<?>>buildCodecs(){");
                writer.write(Classes.sourceName(Classes.LAZY_MAP_BUILDER) + " builder=" + Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".lazyMapBuilder();");
                for (GeneratedCodec codec : this.codecs) {
                    writer.write("builder.put(" + codec.fqn() + ".class,");
                    writer.write("() -> " + Classes.sourceName(Classes.RECORD_CODEC_BUILDER) + ".<" + codec.fqn() + ">create(instance->");
                    writer.write("instance.group(");
                    for (int i = 0; i < codec.params().size(); i++) {
                        GeneratedCodec.CodecElement param = codec.params().get(i);
                        writer.write("(");
                        param.writeCode(writer);
                        writer.write(")");
                        if (i < codec.params().size() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write(").apply(instance,instance.stable(");
                    writer.write("(");
                    for (int i = 0; i < codec.params().size(); i++) {
                        GeneratedCodec.CodecElement param = codec.params().get(i);
                        writer.write(param.typeFqnBoxed + " ctorArg" + i);
                        if (i < codec.params().size() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write(")->{");
                    writer.write("return new " + codec.fqn() + "(");
                    for (int i = 0; i < codec.params().size(); i++) {
                        writer.write("ctorArg" + i);
                        if (i < codec.params().size() - 1) {
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
            writer.write("public static void init(" + Classes.sourceName(Classes.MODX) + " mod){");
            writer.write(this.modClass.getSimpleName() + "$.mod=mod;");
            for (RegisteredMapper mapper : this.configMappers) {
                if (mapper.requiresMod() != null) {
                    writer.write("if(" + Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".isModLoaded(" + quote(mapper.requiresMod()) + ")){");
                }
                writer.write(Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".registerConfigMapper(mod,(" + mapper.targetTypeSource() + ")new " + mapper.classFqn() + (mapper.genericType() ? "<>" : "") + "());");
                if (mapper.requiresMod() != null) {
                    writer.write("}");
                }
            }
            for (RegisteredConfig config : this.configs) {
                if (config.requiresMod() != null) {
                    writer.write("if(" + Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".isModLoaded(" + quote(config.requiresMod()) + ")){");
                }
                writer.write(Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".registerConfig(mod," + quote(config.name()) + "," + config.classFqn() + ".class," + config.client() + ");");
                if (config.requiresMod() != null) {
                    writer.write("}");
                }
            }
            if (!allReg.isEmpty()) {
                writer.write("((" + Classes.sourceName(Classes.MODX_REGISTRATION) + ")mod).addRegistrationHandler(" + this.modClass.getSimpleName() + "$::register);");
            }
            if (!this.models.isEmpty()) {
                writer.write(Classes.sourceName(Classes.DIST_EXECUTOR) + ".unsafeRunWhenOn(" + Classes.sourceName(Classes.DIST) + ".CLIENT,()->()->{");
                writer.write(Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".addModListener(" + Classes.sourceName(Classes.MODEL_REGISTRY_EVENT) + ".class," + this.modClass.getSimpleName() + "$::registerModels);");
                writer.write(Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".addLowModListener(" + Classes.sourceName(Classes.MODEL_BAKE_EVENT) + ".class," + this.modClass.getSimpleName() + "$::bakeModels);");
                writer.write("});");
            }
            writer.write("}");
            if (!allReg.isEmpty()) {
                writer.write("private static void register(){");
                writer.write(Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".runUnchecked(()->{");
                for (RegistrationEntry entry : allReg) {
                    writer.write(Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".register(mod," + (entry.registryFqn() == null ? "null" : entry.registryFqn()) + "," + quote(entry.name()) + "," + entry.fieldClassFqn() + "." + entry.fieldName() + ",()->{return " + entry.fieldClassFqn() + ".class.getDeclaredField(" + quote(entry.fieldName()) + ");});");
                }
                writer.write("});");
                writer.write("}");
            }
            if (!this.models.isEmpty()) {
                writer.write("@" + Classes.sourceName(Classes.ONLY_IN) + "(" + Classes.sourceName(Classes.DIST) + ".CLIENT)");
                writer.write("private static void registerModels(" + Classes.sourceName(Classes.MODEL_REGISTRY_EVENT) + " event){");
                for (LoadableModel model : this.models) {
                    writer.write(Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".addSpecialModel(event," + Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".newRL(" + quote(model.modelNamespace()) + "," + quote(model.modelPath()) + "));");
                }
                writer.write("}");
                writer.write("@" + Classes.sourceName(Classes.ONLY_IN) + "(" + Classes.sourceName(Classes.DIST) + ".CLIENT)");
                writer.write("private static void bakeModels(" + Classes.sourceName(Classes.MODEL_BAKE_EVENT) + " event){");
                for (LoadableModel model : this.models) {
                    writer.write(model.classFqn() + "." + model.fieldName() + "=" + Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".getSpecialModel(event," + Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".newRL(" + quote(model.modelNamespace()) + "," + quote(model.modelPath()) + "));");
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
