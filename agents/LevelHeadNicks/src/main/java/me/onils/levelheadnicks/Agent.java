package me.onils.levelheadnicks;

import me.onils.agent.commons.Utils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Agent {
    private static int getLevel(String option){
        try{
            return Integer.parseInt(option);
        }catch (NumberFormatException ex){
            return -1;
        }
    }
    public static void premain(String option, Instrumentation inst){
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if(!Utils.isLunar(className)){
                    return classfileBuffer;
                }

                ClassReader cr = new ClassReader(classfileBuffer);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);

                boolean injected = false;
                methodLoop: for(MethodNode mn : cn.methods){
                    for(AbstractInsnNode insn : mn.instructions){
                        if(insn instanceof MethodInsnNode methodInsnNode
                                && methodInsnNode.owner.equals("java/util/concurrent/ThreadLocalRandom")
                                && methodInsnNode.name.equals("nextInt")
                                && methodInsnNode.desc.equals("(I)I")
                                && insn.getPrevious().getOpcode() == Opcodes.BIPUSH
                                && ((IntInsnNode) insn.getPrevious()).operand == 25){
                            while((insn = insn.getNext()) != null){
                                if(insn.getOpcode() != Opcodes.INVOKESTATIC)
                                    continue;
                                methodInsnNode = (MethodInsnNode) insn;

                                if(methodInsnNode.owner.equals("java/lang/Integer")
                                        && methodInsnNode.name.equals("valueOf")
                                        && methodInsnNode.desc.equals("(I)Ljava/lang/Integer;")){
                                    InsnList inject = new InsnList();
                                    inject.add(new InsnNode(Opcodes.POP));
                                    inject.add(new LdcInsnNode(getLevel(option)));
                                    mn.instructions.insertBefore(methodInsnNode, inject);
                                    injected = true;
                                    continue methodLoop;
                                }
                            }
                        }
                    }
                }

                if(injected){
                    ClassWriter cw = new ClassWriter(0);
                    cn.accept(cw);
                    return cw.toByteArray();
                }

                return classfileBuffer;
            }
        });
    }
}
