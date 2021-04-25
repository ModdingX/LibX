package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneratedCodec {
    
    private static int objCounter = 0;
    
    public final String fqn;
    public final List<CodecParam> params;

    public GeneratedCodec(String fqn, List<CodecParam> params) {
        this.fqn = fqn;
        this.params = Collections.unmodifiableList(new ArrayList<>(params));
    }

    public static class CodecParam {
        
        public final String name;
        public final String typeFqn;
        public final String typeFqnBoxed;
        public final String codecFqn;
        public final boolean list;
        public final String getter;

        public CodecParam(String name, String typeFqn, String typeFqnBoxed, String codecFqn, boolean list, String getter) {
            this.name = name;
            this.typeFqn = typeFqn;
            this.typeFqnBoxed = typeFqnBoxed;
            this.codecFqn = codecFqn;
            this.list = list;
            this.getter = getter;
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
