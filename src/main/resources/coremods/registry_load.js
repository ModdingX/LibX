function initializeCoreMod() {
    var ASMAPI = Java.type('net.neoforged.coremod.api.ASMAPI');
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
    var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
    var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
    var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
    var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    
    return {
        'registry_load': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.WorldLoader',
                'methodName': 'load',
                'methodDesc': '(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;Lnet/minecraft/server/WorldLoader$ResultFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;'
            },
            'transformer': function (method) {
                var target = new InsnList();
                target.add(new InsnNode(Opcodes.DUP));
                target.add(ASMAPI.buildMethodCall(
                    'org/moddingx/libx/impl/libxcore/CoreRegistryLoad', 'afterWorldGenLayerLoad',
                    '(Lnet/minecraft/core/LayeredRegistryAccess;)V',
                    ASMAPI.MethodType.STATIC
                ));
                var foundWorldGenField = false;
                for (var i = 0; i < method.instructions.size(); i++) {
                    var node = method.instructions.get(i);
                    if (node.getOpcode() == Opcodes.GETSTATIC && !foundWorldGenField) {
                        var fieldNode = node;
                        if (fieldNode.owner == 'net/minecraft/server/RegistryLayer' && fieldNode.name == 'WORLDGEN') {
                            foundWorldGenField = true;
                        }
                    }
                    else if (node.getOpcode() == Opcodes.INVOKESTATIC && foundWorldGenField) {
                        var methodNode = node;
                        if (methodNode.owner == 'net/minecraft/server/WorldLoader' && methodNode.name == 'loadAndReplaceLayer' && methodNode.desc == '(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/LayeredRegistryAccess;Lnet/minecraft/server/RegistryLayer;Ljava/util/List;)Lnet/minecraft/core/LayeredRegistryAccess;') {
                            method.instructions.insert(node, target);
                            return method;
                        }
                    }
                }
                throw new Error('Failed to patch WorldLoader.class');
            }
        }
    };
}
