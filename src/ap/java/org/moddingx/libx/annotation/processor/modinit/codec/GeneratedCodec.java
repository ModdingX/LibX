package org.moddingx.libx.annotation.processor.modinit.codec;

import org.moddingx.libx.annotation.processor.Classes;
import org.moddingx.libx.annotation.processor.modinit.ModInit;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public record GeneratedCodec(String fqn, List<CodecElement> params) {

    private static int objCounter = 0;

    public GeneratedCodec(String fqn, List<CodecElement> params) {
        this.fqn = fqn;
        this.params = List.copyOf(params);
    }

    public static sealed abstract class CodecElement permits CodecParam, CodecDynamic, CodecEnum {

        public final String typeFqn;
        public final String typeFqnBoxed;

        protected CodecElement(String typeFqn, String typeFqnBoxed) {
            this.typeFqn = typeFqn;
            this.typeFqnBoxed = typeFqnBoxed;
        }

        public abstract void writeCode(Writer writer) throws IOException;
    }

    public static final class CodecParam extends CodecElement {

        public final String name;
        public final String codecFqn;
        public final int list;
        public final String getter;

        public CodecParam(String name, String typeFqn, String typeFqnBoxed, String codecFqn, int list, String getter) {
            super(typeFqn, typeFqnBoxed);
            this.name = name;
            this.codecFqn = codecFqn;
            this.list = list;
            this.getter = getter;
        }

        @Override
        public void writeCode(Writer writer) throws IOException {
            writer.write(this.codecFqn);
            for (int i = 0; i < this.list; i++) {
                writer.write(".listOf()");
            }
            writer.write(".fieldOf(" + ModInit.quote(this.name) + ")");
            writer.write(".forGetter(" + this.getter + ")");
        }
    }

    public static final class CodecDynamic extends CodecElement {

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
            writer.write("((" + Classes.sourceName(Classes.MAP_CODEC) + "<" + this.typeFqnBoxed + ">)");
            writer.write(this.factoryFqn);
            writer.write("(" + ModInit.quote(this.name) + "))");
            writer.write(".forGetter(" + this.getter + ")");
        }
    }

    public static final class CodecEnum extends CodecElement {

        public final String name;
        public final String enumClass;
        public final int list;
        public final String getter;

        public CodecEnum(String name, String typeFqn, String typeFqnBoxed, String enumClass, int list, String getter) {
            super(typeFqn, typeFqnBoxed);
            this.name = name;
            this.enumClass = enumClass;
            this.list = list;
            this.getter = getter;
        }

        @Override
        public void writeCode(Writer writer) throws IOException {
            writer.write(Classes.sourceName(Classes.PROCESSOR_INTERFACE) + ".<" + this.enumClass + ">enumCodec(" + this.enumClass + ".class)");
            for (int i = 0; i < this.list; i++) {
                writer.write(".listOf()");
            }
            writer.write(".fieldOf(" + ModInit.quote(this.name) + ")");
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
