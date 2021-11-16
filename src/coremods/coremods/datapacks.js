"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("coremods");
function initializeCoreMod() {
    return {
        'datapacks': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.players.PlayerList',
                'methodName': 'm_11315_',
                'methodDesc': '()V'
            },
            'transformer': function (method) {
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 0));
                target.add(coremods_1.ASMAPI.buildMethodCall('io/github/noeppi_noeppi/libx/impl/libxcore/CoreDataPacks', 'fireReload', '(Lnet/minecraft/server/players/PlayerList;)V', coremods_1.ASMAPI.MethodType.STATIC));
                for (var i = 0; i < method.instructions.size(); i++) {
                    var inst = method.instructions.get(i);
                    if (inst != null && inst.getOpcode() == coremods_1.Opcodes.INVOKEVIRTUAL) {
                        var invokeInst = inst;
                        if (invokeInst.owner == 'net/minecraft/server/players/PlayerList'
                            && invokeInst.name == coremods_1.ASMAPI.mapMethod('m_11268_')) {
                            method.instructions.insert(inst, target);
                            return method;
                        }
                    }
                }
                throw new Error("Failed to patch PlayerList.class");
            }
        }
    };
}
