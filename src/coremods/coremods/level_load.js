"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("coremods");
function initializeCoreMod() {
    return {
        'level_load': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.level.ServerLevel',
                'methodName': '<init>',
                'methodDesc': '(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/world/level/storage/ServerLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;Lnet/minecraft/server/level/progress/ChunkProgressListener;ZJLjava/util/List;Z)V'
            },
            'transformer': function (method) {
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.DUP));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 1));
                target.add(new coremods_1.MethodInsnNode(coremods_1.Opcodes.INVOKESTATIC, 'org/moddingx/libx/impl/libxcore/CoreLevelLoad', 'startLevelLoad', '(Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/server/MinecraftServer;)V'));
                for (var i = 0; i < method.instructions.size(); i++) {
                    var node = method.instructions.get(i);
                    if (node.getOpcode() == coremods_1.Opcodes.INVOKEVIRTUAL) {
                        var methodNode = node;
                        if (methodNode.owner == 'net/minecraft/world/level/dimension/LevelStem' && methodNode.name == coremods_1.ASMAPI.mapMethod('f_63976_') && methodNode.desc == '()Lnet/minecraft/world/level/chunk/ChunkGenerator;') {
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
