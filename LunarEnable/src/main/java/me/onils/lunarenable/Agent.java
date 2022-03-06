package me.onils.lunarenable;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Agent {
    public static void premain(String option, Instrumentation inst){
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if(!className.startsWith("lunar/"))
                    return classfileBuffer;

                ClassReader cr = new ClassReader(classfileBuffer);
                if(Arrays.asList(cr.getInterfaces()).contains("java/util/function/Consumer")){
                    ClassNode cn = new ClassNode();
                    cr.accept(cn, 0);

                    for(MethodNode method : cn.methods){
                        Set<String> stringsToMatch = new HashSet<>();
                        stringsToMatch.add("ip");
                        stringsToMatch.add("brand");
                        stringsToMatch.add("modSettings");

                        boolean matchesAllStrings = Arrays.stream(method.instructions.toArray())
                                .filter(LdcInsnNode.class::isInstance)
                                .map(LdcInsnNode.class::cast)
                                .map(ldc -> ldc.cst)
                                .filter(stringsToMatch::remove)
                                .anyMatch(__ -> stringsToMatch.isEmpty());

                        if(matchesAllStrings){
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
                }

                return classfileBuffer;
            }
        });
    }
}
