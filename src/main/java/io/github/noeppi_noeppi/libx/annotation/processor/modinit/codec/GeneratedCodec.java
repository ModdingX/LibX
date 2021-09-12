package io.github.noeppi_noeppi.libx.annotation.processor.modinit.codec;

import io.github.noeppi_noeppi.libx.annotation.impl.ProcessorInterface;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModInit;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public record GeneratedCodec(String fqn, List<CodecElement> params) {

    private static int objCounter = 0;

    public GeneratedCodec(String fqn, List<CodecElement> params) {
        this.fqn = fqn;
        this.params = List.copyOf(params);
    }

    public static abstract class CodecElement {

        public final String typeFqn;
        public final String typeFqnBoxed;

        protected CodecElement(String typeFqn, String typeFqnBoxed) {
            this.typeFqn = typeFqn;
            this.typeFqnBoxed = typeFqnBoxed;
        }

        public abstract void writeCode(Writer writer) throws IOException;
    }

    public static class CodecParam extends CodecElement {

        public final String name;
        public final String codecFqn;
        public final boolean list;
        public final String getter;

        public CodecParam(String name, String typeFqn, String typeFqnBoxed, String codecFqn, boolean list, String getter) {
            super(typeFqn, typeFqnBoxed);
            this.name = name;
            this.codecFqn = codecFqn;
            this.list = list;
            this.getter = getter;
        }

        @Override
        public void writeCode(Writer writer) throws IOException {
            writer.write(this.codecFqn);
            if (this.list) {
                writer.write(".listOf()");
            }
            writer.write(".fieldOf(\"" + ModInit.quote(this.name) + "\")");
            writer.write(".forGetter(" + this.getter + ")");
        }
    }

    public static class CodecDynamic extends CodecElement {

        public final String name;
        public final String factoryFqn;
        public final String getter;

        public CodecDynamic(String name, String typeFqn, String typeFqnBoxed, String factoryFqn, String getter) {
            super(typeFqn, typeFqnBoxed);
            this.name = name;
            this.factoryFqn = factoryFqn;
            this.getter = getter;
        }

        @Override
        public void writeCode(Writer writer) throws IOException {
            writer.write("((" + ModInit.MAP_CODEC_TYPE + "<" + this.typeFqnBoxed + ">)");
            writer.write(this.factoryFqn);
            writer.write("(\"" + ModInit.quote(this.name) + "\"))");
            writer.write(".forGetter(" + this.getter + ")");
        }
    }
    
    public static class CodecRegistry extends CodecElement {

        @Nullable
        public final String registryNamespace;
        @Nullable
        public final String registryPath;
        public final String registryTypeStr;
        public final String registryTypeFqn;
        public final String getter;

        public CodecRegistry(String typeFqn, String typeFqnBoxed, @Nullable String registryNamespace, @Nullable String registryPath, String registryTypeStr, String registryTypeFqn, String getter) {
            super(typeFqn, typeFqnBoxed);
            this.registryNamespace = registryNamespace;
            this.registryPath = registryPath;
            this.registryTypeStr = registryTypeStr;
            this.registryTypeFqn = registryTypeFqn;
            this.getter = getter;
        }

        @Override
        public void writeCode(Writer writer) throws IOException {
            if (this.registryNamespace != null && this.registryPath != null) {
                writer.write(ProcessorInterface.class.getCanonicalName() + ".<" + this.registryTypeStr + ">registryCodec(");
                writer.write(ProcessorInterface.class.getCanonicalName() + ".<" + this.registryTypeStr + ">rootKey(");
                writer.write(ProcessorInterface.class.getCanonicalName() + ".newRL(\"" + ModInit.quote(this.registryNamespace) + "\",\"" + ModInit.quote(this.registryPath) + "\")");
                writer.write(")");
                writer.write(")");
            } else {
                writer.write(ProcessorInterface.class.getCanonicalName() + ".<" + this.registryTypeStr + ">registryCodec(");
                writer.write(ProcessorInterface.class.getCanonicalName() + ".<" + this.registryTypeStr + ">getCodecDefaultRegistryKey(" + this.registryTypeFqn + ".class)");
                writer.write(")");
            }
            writer.write(".forGetter(" + this.getter + ")");
        }
    }

    public static String fieldGetter(String elementFqn, String field) {
        String objName = "codecParam" + (objCounter++);
        return "(" + elementFqn + " " + objName + ")->" + objName + "." + field;
    }

    public static String methodGetter(String elementFqn, String method) {
        return elementFqn + "::" + method;
    }
}
