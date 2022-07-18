package me.onils.nopinnedservers;

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
public class Transformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!Utils.isLunar(className)) {
            return classfileBuffer;
        }

        ClassReader cr = new ClassReader(classfileBuffer);

        if(!Utils.isLunar(cr.getSuperName())){
            return classfileBuffer;
        }

        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        for(MethodNode mn : cn.methods){
            if(!mn.desc.equals("(Lcom/google/gson/JsonElement;)V")) continue;

            if(Utils.containsStrings(mn, "versions", "name")){
                mn.instructions.clear();
                mn.localVariables.clear();
                mn.exceptions.clear();
                mn.tryCatchBlocks.clear();
                mn.instructions.add(new InsnNode(Opcodes.RETURN));
                ClassWriter cw = new ClassWriter(cr, 0);
                cn.accept(cw);
                return cw.toByteArray();
            }
        }


        return classfileBuffer;
    }
}
