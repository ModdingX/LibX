import {
  ASMAPI,
  CoreMods,
  InsnList,
  InsnNode,
  JumpInsnNode,
  LabelNode,
  MethodNode,
  Opcodes,
  VarInsnNode
} from "coremods";

function initializeCoreMod(): CoreMods {
  return {
    'holder_serialize': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.core.Holder$Reference',
        'methodName': 'm_203401_',
        'methodDesc': '(Lnet/minecraft/core/HolderOwner;)Z'
      },
      'transformer': (method: MethodNode) => {
        const label = new LabelNode();
        const target = new InsnList();
        target.add(new VarInsnNode(Opcodes.ALOAD, 0));
        target.add(new VarInsnNode(Opcodes.ALOAD, 1));
        target.add(ASMAPI.buildMethodCall(
            'org/moddingx/libx/impl/libxcore/CoreHolderSerialize',
            'forceSerializeIn', '(Lnet/minecraft/core/Holder$Reference;Lnet/minecraft/core/HolderOwner;)Z',
            ASMAPI.MethodType.STATIC
        ));
        target.add(new JumpInsnNode(Opcodes.IFEQ, label));
        target.add(new InsnNode(Opcodes.ICONST_1))
        target.add(new InsnNode(Opcodes.IRETURN));
        target.add(label);
        
        method.instructions.insert(target);
        return method;
      }
    }
  }
}
