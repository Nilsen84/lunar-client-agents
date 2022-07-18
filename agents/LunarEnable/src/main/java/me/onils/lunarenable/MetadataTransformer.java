package me.onils.lunarenable;

import me.onils.agent.commons.Utils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MetadataTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(!Utils.isLunar(className))
            return classfileBuffer;

        ClassReader cr = new ClassReader(classfileBuffer);

        if(!Utils.isLunar(cr.getSuperName()))
            return classfileBuffer;

        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        for(MethodNode method : cn.methods){
            if(!method.desc.equals("(Lcom/google/gson/JsonElement;)V")) continue;

            if(Utils.containsStrings(method, "ip", "brand", "modSettings")){
                method.instructions.clear();
                method.localVariables.clear();
                method.exceptions.clear();
                method.tryCatchBlocks.clear();
                method.instructions.add(new InsnNode(Opcodes.RETURN));
                ClassWriter cw = new ClassWriter(cr, 0);
                cn.accept(cw);
                return cw.toByteArray();
            }
        }


        return classfileBuffer;
    }
}
