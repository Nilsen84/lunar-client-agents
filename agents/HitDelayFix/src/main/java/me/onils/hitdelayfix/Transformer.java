package me.onils.hitdelayfix;

import me.onils.agent.commons.Utils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class Transformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(!Utils.isMinecraft(className)){
            return classfileBuffer;
        }

        ClassReader cr = new ClassReader(classfileBuffer);
        if(cr.getInterfaces().length == 3 && "java/lang/Object".equals(cr.getSuperName())){
            ClassNode cn = new ClassNode();

            cr.accept(cn, 0);

            for (MethodNode method : cn.methods) {
                if (Utils.containsStrings(method, "Null returned as 'hitResult', this shouldn't happen!")) {
                    for(AbstractInsnNode insn : method.instructions){
                        if(insn.getOpcode() == Opcodes.BIPUSH){
                            IntInsnNode intNode = (IntInsnNode)insn;
                            if(intNode.operand == 10)
                                intNode.operand = 0;
                        }
                    }
                    ClassWriter cw = new ClassWriter(cr, 0);
                    cn.accept(cw);
                    return cw.toByteArray();
                }
            }
        }

        return classfileBuffer;
    }
}
