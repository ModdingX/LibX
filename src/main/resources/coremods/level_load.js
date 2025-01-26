function initializeCoreMod() {
    var ASMAPI = Java.type('net.neoforged.coremod.api.ASMAPI');
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
    var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
    var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    
    return {
        'level_load': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.level.ServerLevel',
                'methodName': '<init>',
                'methodDesc': '(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/world/level/storage/ServerLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;Lnet/minecraft/server/level/progress/ChunkProgressListener;ZJLjava/util/List;ZLnet/minecraft/world/RandomSequences;)V'
            },
            'transformer': function (method) {
                var target = new InsnList();
                target.add(new InsnNode(Opcodes.DUP));
                target.add(new VarInsnNode(Opcodes.ALOAD, 1));
                target.add(ASMAPI.buildMethodCall(
                    'org/moddingx/libx/impl/libxcore/CoreLevelLoad', 'startLevelLoad',
                    '(Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/server/MinecraftServer;)V',
                    ASMAPI.MethodType.STATIC
                ));
                for (var i = 0; i < method.instructions.size(); i++) {
                    var node = method.instructions.get(i);
                    if (node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        var methodNode = node;
                        if (methodNode.owner == 'net/minecraft/world/level/dimension/LevelStem' && methodNode.name == 'generator' && methodNode.desc == '()Lnet/minecraft/world/level/chunk/ChunkGenerator;') {
                            method.instructions.insert(node, target);
                            return method;
                        }
                    }
                }
                throw new Error('Failed to patch ServerLevel.class');
            }
        }
    };
}
