package me.onils.lunarpacksfix;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Agent {
    public static void premain(String option, Instrumentation inst){
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                ClassReader cr = new ClassReader(classfileBuffer);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);


                for(MethodNode methodNode : cn.methods){
                    for(AbstractInsnNode insnNode : methodNode.instructions){
                        if(insnNode instanceof LdcInsnNode ldcNode && "assets/lunar/".equals(ldcNode.cst)){
                            methodNode.instructions.clear();
                            methodNode.localVariables.clear();
                            methodNode.exceptions.clear();
                            methodNode.tryCatchBlocks.clear();

                            methodNode.instructions.add(new InsnNode(Opcodes.ICONST_1));
                            methodNode.instructions.add(new InsnNode(Opcodes.IRETURN));

                            ClassWriter cw = new ClassWriter(cr, 0);
                            cn.accept(cw);
                            return cw.toByteArray();
                        }
                    }
                }

                return classfileBuffer;
            }
        });
    }
}
