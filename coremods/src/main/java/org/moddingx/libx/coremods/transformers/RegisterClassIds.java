package org.moddingx.libx.coremods.transformers;

import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class RegisterClassIds extends SimpleClassProcessor {

    private static final String HELPER = "org/moddingx/libx/annotation/impl/RegistrationPropertiesHelper";
    private static final String ITEM_PROPS = "net/minecraft/world/item/Item$Properties";
    private static final String BLOCK_PROPS = "net/minecraft/world/level/block/state/BlockBehaviour$Properties";

    private final Map<String, List<FieldEntry>> entries;

    public RegisterClassIds() {
        this(RegisterClassIds.loadEntries());
    }

    RegisterClassIds(Map<String, List<FieldEntry>> entries) {
        this.entries = entries;
    }

    @Override
    public ProcessorName name() {
        return new ProcessorName("libx", "register_class_ids");
    }

    @Override
    public Set<Target> targets() {
        return this.entries.keySet().stream()
                .map(k -> new Target(k.replace('/', '.')))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void transform(ClassNode cn, SimpleTransformationContext ctx) {
        List<FieldEntry> fields = this.entries.get(cn.name);
        if (fields == null) {
            throw new IllegalStateException("Failed to patch " + cn.name + ": no registration metadata for a targeted class");
        }
        for (FieldEntry fe : fields) {
            RegisterClassIds.injectField(cn, cn.name, fe);
        }
    }

    private static void injectField(ClassNode cn, String owner, FieldEntry fe) {
        // Find the <clinit> method
        MethodNode clinit = null;
        for (MethodNode mn : cn.methods) {
            if ("<clinit>".equals(mn.name) && "()V".equals(mn.desc)) {
                clinit = mn;
                break;
            }
        }
        if (clinit == null) {
            throw new IllegalStateException("Failed to patch " + owner + ": no static initialiser for field " + fe.fieldName());
        }

        InsnList insns = clinit.instructions;

        // Find the PUTSTATIC for this field.
        FieldInsnNode putStatic = null;
        for (int i = 0; i < insns.size(); i++) {
            AbstractInsnNode node = insns.get(i);
            if (node.getOpcode() == Opcodes.PUTSTATIC && node instanceof FieldInsnNode fin && fin.owner.equals(owner) && fin.name.equals(fe.fieldName())) {
                putStatic = fin;
                break;
            }
        }

        if (putStatic == null) { // field not initialised in <clinit>
            throw new IllegalStateException("Failed to patch " + owner + ": field " + fe.fieldName() + " is not initialised in the static initialiser");
        }

        // Walk backward from PUTSTATIC to find the INVOKESPECIAL <init> that constructs
        // the Item / Block just before it is stored. We look for the closest <init> call
        // whose descriptor ends with either ITEM_PROPS or BLOCK_PROPS (last argument).
        int putIdx = insns.indexOf(putStatic);
        for (int i = putIdx - 1; i >= 0; i--) {
            AbstractInsnNode node = insns.get(i);
            if (node.getOpcode() != Opcodes.INVOKESPECIAL) {
                continue;
            }

            if (!(node instanceof MethodInsnNode min)) {
                continue;
            }

            if (!"<init>".equals(min.name)) {
                continue;
            }

            // Locate the Properties arguments by their position in the descriptor.
            Type[] argTypes = Type.getArgumentTypes(min.desc);
            int itemArg = RegisterClassIds.propertiesIndex(argTypes, ITEM_PROPS, owner, fe);
            int blockArg = RegisterClassIds.propertiesIndex(argTypes, BLOCK_PROPS, owner, fe);

            if (itemArg < 0 && blockArg < 0) {
                continue;
            }

            // ----------------------------------------------------------------
            // Spill every argument from the first Properties upwards into fresh locals, set the
            // ids on the spilled Properties, then push the arguments back in their original
            // order. This leaves the operand stack exactly as the constructor expects it and
            // works for any argument shape, including intervening long/double arguments.
            // ----------------------------------------------------------------

            int firstArg = blockArg < 0 ? itemArg : (itemArg < 0 ? blockArg : Math.min(itemArg, blockArg));

            int[] slots = new int[argTypes.length];
            int nextSlot = clinit.maxLocals;
            for (int arg = firstArg; arg < argTypes.length; arg++) {
                slots[arg] = nextSlot;
                nextSlot += argTypes[arg].getSize();
            }

            InsnList injection = new InsnList();
            for (int arg = argTypes.length - 1; arg >= firstArg; arg--) {
                injection.add(new VarInsnNode(argTypes[arg].getOpcode(Opcodes.ISTORE), slots[arg]));
            }
            if (blockArg >= 0) {
                injection.add(new VarInsnNode(Opcodes.ALOAD, slots[blockArg]));
                injection.add(new LdcInsnNode(fe.id()));
                injection.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        HELPER, "setBlockId",
                        "(L" + BLOCK_PROPS + ";Ljava/lang/String;)V"
                ));
            }
            if (itemArg >= 0) {
                injection.add(new VarInsnNode(Opcodes.ALOAD, slots[itemArg]));
                injection.add(new LdcInsnNode(fe.id()));
                injection.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        HELPER, "setItemId",
                        "(L" + ITEM_PROPS + ";Ljava/lang/String;)V"
                ));
            }
            for (int arg = firstArg; arg < argTypes.length; arg++) {
                injection.add(new VarInsnNode(argTypes[arg].getOpcode(Opcodes.ILOAD), slots[arg]));
            }

            insns.insertBefore(node, injection);
            clinit.maxLocals = Math.max(clinit.maxLocals, nextSlot);

            return;
        }

        throw new IllegalStateException("Failed to patch " + owner + ": field " + fe.fieldName() + " must be initialised by a direct constructor call taking an Item.Properties or BlockBehaviour.Properties argument in the static initialiser, so its registry id can be injected into the properties.");
    }

    /**
     * Finds the argument position of the given {@code Properties} type in a constructor descriptor,
     * or {@code -1} if the constructor takes none. A constructor taking the same {@code Properties}
     * type more than once is rejected rather than guessed at.
     */
    private static int propertiesIndex(Type[] argTypes, String internalName, String owner, FieldEntry fe) {
        int found = -1;
        for (int i = 0; i < argTypes.length; i++) {
            if (argTypes[i].getSort() == Type.OBJECT && internalName.equals(argTypes[i].getInternalName())) {
                if (found >= 0) {
                    throw new IllegalStateException("Failed to patch " + owner + ": field " + fe.fieldName() + " is initialised by a constructor taking more than one " + internalName + " argument, so it is ambiguous which one should receive the registry id.");
                }
                found = i;
            }
        }
        return found;
    }

    private static Map<String, List<FieldEntry>> loadEntries() {
        Collection<JarContents> sources = FMLLoader.getCurrent().getLoadingModList().getAllModFiles().stream()
                .map(IModFile::getContents)
                .toList();
        return RegisterClassIds.loadEntries(sources);
    }

    static Map<String, List<FieldEntry>> loadEntries(Collection<JarContents> sources) {
        Map<String, List<FieldEntry>> map = new HashMap<>();
        for (JarContents contents : sources) {
            try {
                byte[] data = contents.readFile("META-INF/libx_registration.json");
                if (data != null) {
                    Map<String, List<FieldEntry>> sourceEntries = new HashMap<>();
                    RegisterClassIds.parseJson(new String(data, StandardCharsets.UTF_8), sourceEntries);
                    sourceEntries.forEach((className, entries) ->
                            map.computeIfAbsent(className, key -> new ArrayList<>()).addAll(entries));
                }
            } catch (Exception e) {
                System.err.println("[LibX] Failed to read or parse libx_registration.json from mod file "
                        + contents.getPrimaryPath() + ": " + e);
            }
        }
        if (map.isEmpty()) {
            System.err.println("[LibX] WARNING: No libx_registration.json entries found — @RegisterClass ids will not be injected!");
        } else {
            System.out.println("[LibX] Loaded @RegisterClass id entries for " + map.size() + " class(es)");
        }

        return map;
    }

    // Expected format: {@code [{"class":"a/b/C","field":"f","id":"m:n"}, ...]}
    private static void parseJson(String json, Map<String, List<FieldEntry>> map) {
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) {
            throw new IllegalArgumentException("Registration metadata must be a JSON array");
        }
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) {
            return;
        }

        // Split on "},{" to get individual objects
        for (String obj : json.split("\\},\\s*\\{")) {
            obj = obj.replace("{", "").replace("}", "").trim();
            String cls = extractValue(obj, "class");
            String field = extractValue(obj, "field");
            String id = extractValue(obj, "id");
            if (cls == null || field == null || id == null) {
                throw new IllegalArgumentException("Invalid registration metadata entry: {" + obj + "}");
            }

            map.computeIfAbsent(cls, k -> new ArrayList<>()).add(new FieldEntry(field, id));
        }
    }

    private static String extractValue(String obj, String key) {
        String search = "\"" + key + "\":\"";
        int start = obj.indexOf(search);
        if (start < 0) {
            return null;
        }

        start += search.length();
        int end = obj.indexOf('"', start);
        if (end < 0) {
            return null;
        }

        return obj.substring(start, end);
    }

    record FieldEntry(String fieldName, String id) {}
}
