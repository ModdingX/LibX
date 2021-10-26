import {AbstractInsnNode, ASMAPI, CoreMods, InsnList, MethodNode, Opcodes, VarInsnNode} from "../coremods";

function initializeCoreMod(): CoreMods {
  return {
    'world_seed': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.world.level.levelgen.WorldGenSettings',
        'methodName': '<init>',
        'methodDesc': '(JZZLnet/minecraft/core/MappedRegistry;Ljava/util/Optional;)V'
      },
      'transformer': function(method: MethodNode) {
        const target = new InsnList();
        target.add(new VarInsnNode(Opcodes.LLOAD, 1));
        target.add(ASMAPI.buildMethodCall(
            'io/github/noeppi_noeppi/libx/impl/libxcore/CoreWorldSeed',
            'setWorldSeed', '(J)V',
            ASMAPI.MethodType.STATIC
        ));
        
        const ret: Array<AbstractInsnNode> = [];
        for (let i = 0; i < method.instructions.size(); i++) {
          const inst = method.instructions.get(i);
          if (inst != null && inst.getOpcode() == Opcodes.RETURN) {
            ret.push(inst);
          }
        }
        
        if (ret.length > 0) {
          for (let i = 0; i < ret.length; i++) {
            // Need to insert before as inserting after return means we would never get called
            method.instructions.insertBefore(ret[i] as AbstractInsnNode, target);
          }
          return method;
        } else {
          throw new Error("Failed to patch WorldGenSettings.class");
        }
      }
    }
  }
}
