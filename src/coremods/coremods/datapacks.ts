import {CoreMods, MethodNode, InsnList, VarInsnNode, Opcodes, ASMAPI, MethodInsnNode} from "coremods";

function initializeCoreMod(): CoreMods {
  return {
    'datapacks': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.server.players.PlayerList',
        'methodName': 'm_11315_',
        'methodDesc': '()V'
      },
      'transformer': function(method: MethodNode) {
        const target = new InsnList();
        target.add(new VarInsnNode(Opcodes.ALOAD, 0));
        target.add(ASMAPI.buildMethodCall(
            'io/github/noeppi_noeppi/libx/impl/libxcore/CoreDataPacks',
            'fireReload', '(Lnet/minecraft/server/players/PlayerList;)V',
            ASMAPI.MethodType.STATIC
        ));

        for (let i = 0; i < method.instructions.size(); i++) {
          const inst = method.instructions.get(i);
          if (inst != null && inst.getOpcode() == Opcodes.INVOKEVIRTUAL) {
            const invokeInst = inst as MethodInsnNode
            if (invokeInst.owner == 'net/minecraft/server/players/PlayerList' 
                && invokeInst.name == ASMAPI.mapMethod('m_11268_')) {
              method.instructions.insert(inst, target);
              return method;
            }
          }
        }
        throw new Error("Failed to patch PlayerList.class");
      }
    }
  }
}
