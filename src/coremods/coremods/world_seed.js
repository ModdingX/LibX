"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("../coremods");
function initializeCoreMod() {
    return {
        'world_seed': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.levelgen.WorldGenSettings',
                'methodName': '<init>',
                'methodDesc': '(JZZLnet/minecraft/core/Registry;Ljava/util/Optional;)V'
            },
            'transformer': function (method) {
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.LLOAD, 1));
                target.add(coremods_1.ASMAPI.buildMethodCall('io/github/noeppi_noeppi/libx/impl/libxcore/CoreWorldSeed', 'setWorldSeed', '(J)V', coremods_1.ASMAPI.MethodType.STATIC));
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
                    throw new Error("Failed to patch WorldGenSettings.class");
                }
            }
        }
    };
}
