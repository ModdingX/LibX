"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("coremods");
function initializeCoreMod() {
    return {
        'pack_repository': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.packs.repository.PackRepository',
                'methodName': '<init>',
                'methodDesc': '(Lnet/minecraft/server/packs/repository/Pack$PackConstructor;[Lnet/minecraft/server/packs/repository/RepositorySource;)V'
            },
            'transformer': function (method) {
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 0));
                target.add(coremods_1.ASMAPI.buildMethodCall('io/github/noeppi_noeppi/libx/impl/libxcore/CorePackRepository', 'initRepository', '(Lnet/minecraft/server/packs/repository/PackRepository;)V', coremods_1.ASMAPI.MethodType.STATIC));
                var ret = [];
                for (var i = 0; i < method.instructions.size(); i++) {
                    var inst = method.instructions.get(i);
                    if (inst != null && inst.getOpcode() == coremods_1.Opcodes.RETURN) {
                        ret.push(inst);
                    }
                }
                if (ret.length > 0) {
                    for (var i = 0; i < ret.length; i++) {
                        // Need to insert before as inserting after return means we would never get called
                        method.instructions.insertBefore(ret[i], target);
                    }
                    return method;
                }
                else {
                    throw new Error("Failed to patch PackRepository.class");
                }
            }
        }
    };
}
