package me.onils.lunarenable;

import me.onils.agent.commons.Utils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class BukkitTransformer implements ClassFileTransformer {
    private void hookHandleModSettings(ClassNode cn){
        for(MethodNode methodNode : cn.methods){
            if(methodNode.desc.startsWith("(Lcom/lunarclient/bukkitapi/nethandler/client/LCPacketModSettings;")){
                methodNode.instructions.clear();
                methodNode.localVariables.clear();
                methodNode.tryCatchBlocks.clear();
                methodNode.exceptions.clear();

                methodNode.instructions.add(new InsnNode(Opcodes.RETURN));
                methodNode.maxStack = 0;
            }
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(!Utils.isLunar(className)) return classfileBuffer;

        ClassReader cr = new ClassReader(classfileBuffer);

        if(Arrays.asList(cr.getInterfaces()).contains("com/lunarclient/bukkitapi/nethandler/client/LCNetHandlerClient")){
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);

            hookHandleModSettings(cn);

            ClassWriter cw = new ClassWriter(cr, 0);
            cn.accept(cw);
            return cw.toByteArray();
        }

        return classfileBuffer;
    }
}
