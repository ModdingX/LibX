package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import io.github.noeppi_noeppi.libx.annotation.ProcessorInterface;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneratedCodec {

    private static int objCounter = 0;

    public final String fqn;
    public final List<CodecElement> params;

    public GeneratedCodec(String fqn, List<CodecElement> params) {
        this.fqn = fqn;
        this.params = Collections.unmodifiableList(new ArrayList<>(params));
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
    
    public static class CodecRegistry extends CodecElement {
        
        @Nullable
        public final String registryNamespace;
        @Nullable
        public final String registryPath;
        public final String registryTypeFqn;
        public final String getter;

        public CodecRegistry(String typeFqn, String typeFqnBoxed, @Nullable String registryNamespace, @Nullable String registryPath, String registryTypeFqn, String getter) {
            super(typeFqn, typeFqnBoxed);
            this.registryNamespace = registryNamespace;
            this.registryPath = registryPath;
            this.registryTypeFqn = registryTypeFqn;
            this.getter = getter;
        }
        
        @Override
        @SuppressWarnings("deprecation")
        public void writeCode(Writer writer) throws IOException {
            if (this.registryNamespace != null && this.registryPath != null) {
                writer.write(ProcessorInterface.class.getCanonicalName() + ".<" + this.registryTypeFqn + ">registryCodec(");
                writer.write(ProcessorInterface.class.getCanonicalName() + ".<" + this.registryTypeFqn + ">rootKey(");
                writer.write(ProcessorInterface.class.getCanonicalName() + ".newRL(\"" + ModInit.quote(this.registryNamespace) + "\",\"" + ModInit.quote(this.registryPath) + "\")");
                writer.write(")");
                writer.write(")");
            } else {
                writer.write(ProcessorInterface.class.getCanonicalName() + ".<" + this.registryTypeFqn + ">registryCodec(");
                writer.write(ProcessorInterface.class.getCanonicalName() + ".<" + this.registryTypeFqn + ">getCodecDefaultRegistryKey(" + this.registryTypeFqn + ".class)");
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
