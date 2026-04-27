package org.moddingx.libx.coremods.transformers;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class RegisterClassIds extends SimpleClassProcessor {

    private static final String HELPER = "org/moddingx/libx/annotation/impl/RegistrationPropertiesHelper";
    private static final String ITEM_PROPS = "net/minecraft/world/item/Item$Properties";
    private static final String BLOCK_PROPS = "net/minecraft/world/level/block/state/BlockBehaviour$Properties";

    private final Map<String, List<FieldEntry>> entries;

    public RegisterClassIds() {
        this.entries = RegisterClassIds.loadEntries();
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
        if (fields == null) return;
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
        if (clinit == null) return;

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
            return;
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

            // Determine which Properties types appear as constructor arguments.
            boolean lastIsItem = min.desc.contains("L" + ITEM_PROPS + ";)V");
            boolean lastIsBlock = min.desc.contains("L" + BLOCK_PROPS + ";)V");

            if (!lastIsItem && !lastIsBlock) {
                continue;
            }

            // ----------------------------------------------------------------
            // Determine injection(s) needed.
            // The Properties object of interest is always the TOP of the
            // operand stack just before INVOKESPECIAL executes.
            // ----------------------------------------------------------------

            boolean hasItemArg = min.desc.contains("L" + ITEM_PROPS + ";");
            boolean hasBlockArg = min.desc.contains("L" + BLOCK_PROPS + ";");
            boolean bothPresent = hasItemArg && hasBlockArg;

            InsnList injection = new InsnList();

            if (!bothPresent) {
                injection.add(new InsnNode(Opcodes.DUP));
                injection.add(new LdcInsnNode(fe.id()));
                if (lastIsItem) {
                    injection.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            HELPER, "setItemId",
                            "(Lnet/minecraft/world/item/Item$Properties;Ljava/lang/String;)V"
                    ));
                } else {
                    injection.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            HELPER, "setBlockId",
                            "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;Ljava/lang/String;)V"
                    ));
                }
            } else if (lastIsItem) {
                // Stack: [..., blockProps, itemProps]  (item on TOP)
                injection.add(new InsnNode(Opcodes.DUP));
                injection.add(new LdcInsnNode(fe.id()));
                injection.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        HELPER, "setItemId",
                        "(Lnet/minecraft/world/item/Item$Properties;Ljava/lang/String;)V"
                ));

                injection.add(new InsnNode(Opcodes.SWAP));
                injection.add(new InsnNode(Opcodes.DUP));
                injection.add(new LdcInsnNode(fe.id()));
                injection.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        HELPER, "setBlockId",
                        "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;Ljava/lang/String;)V"
                ));
                injection.add(new InsnNode(Opcodes.SWAP));
            } else {
                // Stack: [..., itemProps, blockProps]  (block on TOP)
                injection.add(new InsnNode(Opcodes.DUP));
                injection.add(new LdcInsnNode(fe.id()));
                injection.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        HELPER, "setBlockId",
                        "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;Ljava/lang/String;)V"
                ));
                // Set item id (second): SWAP, DUP, LDC, setItemId, SWAP  → stack unchanged
                injection.add(new InsnNode(Opcodes.SWAP));
                injection.add(new InsnNode(Opcodes.DUP));
                injection.add(new LdcInsnNode(fe.id()));
                injection.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        HELPER, "setItemId",
                        "(Lnet/minecraft/world/item/Item$Properties;Ljava/lang/String;)V"
                ));
                injection.add(new InsnNode(Opcodes.SWAP));
            }

            insns.insertBefore(node, injection);

            return;
        }
    }

    private static Map<String, List<FieldEntry>> loadEntries() {
        Map<String, List<FieldEntry>> map = new HashMap<>();
        Set<String> seenUrls = new HashSet<>();

        // Production: mod JARs are accessible via the service classloader
        try {
            Enumeration<URL> resources = RegisterClassIds.class.getClassLoader().getResources("META-INF/libx_registration.json");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (seenUrls.add(url.toString())) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                        RegisterClassIds.readAndParse(reader, map);
                    } catch (Exception e) {
                        System.err.println("[LibX] Failed to parse libx_registration.json from " + url + ": " + e);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[LibX] Failed to enumerate libx_registration.json: " + e);
        }

        // Dev: NeoForge InDevFolderLocator exposes mod output dirs via MOD_CLASSES env var
        // in the format "modid%%/path/to/dir:modid%%/path/to/other/dir:..."
        String modClassesEnv = System.getenv("MOD_CLASSES");
        if (modClassesEnv != null) {
            for (String entry : modClassesEnv.split(File.pathSeparator)) {
                int sep = entry.indexOf("%%");
                String dirPath = (sep >= 0) ? entry.substring(sep + 2) : entry;
                File jsonFile = new File(dirPath, "META-INF/libx_registration.json");
                if (jsonFile.isFile() && seenUrls.add(jsonFile.toURI().toString())) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8))) {
                        RegisterClassIds.readAndParse(reader, map);
                    } catch (Exception e) {
                        System.err.println("[LibX] Failed to parse libx_registration.json from " + jsonFile + ": " + e);
                    }
                }
            }
        }

        if (map.isEmpty()) {
            System.err.println("[LibX] WARNING: No libx_registration.json entries found — @RegisterClass ids will not be injected!");
        } else {
            System.out.println("[LibX] Loaded @RegisterClass id entries for " + map.size() + " class(es)");
        }

        return map;
    }

    private static void readAndParse(BufferedReader reader, Map<String, List<FieldEntry>> map) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        RegisterClassIds.parseJson(sb.toString(), map);
    }

    // Expected format: {@code [{"class":"a/b/C","field":"f","id":"m:n"}, ...]}
    private static void parseJson(String json, Map<String, List<FieldEntry>> map) {
        // Strip outer array brackets
        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length() - 1);

        // Split on "},{" to get individual objects
        for (String obj : json.split("\\},\\s*\\{")) {
            obj = obj.replace("{", "").replace("}", "").trim();
            String cls = extractValue(obj, "class");
            String field = extractValue(obj, "field");
            String id = extractValue(obj, "id");
            if (cls == null || field == null || id == null) {
                continue;
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

    private record FieldEntry(String fieldName, String id) {}
}
