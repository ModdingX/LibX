function initializeCoreMod() {
  return {
    'pack_repository': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.server.packs.repository.PackRepository',
        'methodName': '<init>',
        'methodDesc': '(Lnet/minecraft/server/packs/repository/Pack$PackConstructor;[Lnet/minecraft/server/packs/repository/RepositorySource;)V'
      },
      'transformer': function(method) {
        var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
        var Opcodes = Java.type('org.objectweb.asm.Opcodes');
        var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
        var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

        var target = new InsnList();
        target.add(new VarInsnNode(Opcodes.ALOAD, 0));
        target.add(ASMAPI.buildMethodCall(
            'io/github/noeppi_noeppi/libx/impl/libxcore/CorePackRepository',
            'initRepository', '(Lnet/minecraft/server/packs/repository/PackRepository;)V',
            ASMAPI.MethodType.STATIC
        ));
        
        var ret = [];
        for (var i = 0; i < method.instructions.size(); i++) {
          var inst = method.instructions.get(i);
          if (inst.getOpcode() == Opcodes.RETURN) {
            ret.push(inst);
          }
        }
        
        if (ret.length > 0) {
          for (var i = 0; i < ret.length; i++) {
            // Need to insert before as inserting after return means we would never get called
            method.instructions.insertBefore(ret[i], target);
          }
          return method;
        } else {
          throw new Error("Failed to patch PackRepository.class");
        }
      }
    }
  }
}
