package me.onils.teamsautogg;

import me.onils.agent.commons.Utils;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Agent {
    public static void premain(String option, Instrumentation inst){
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if (!Utils.isLunar(className))
                    return classfileBuffer;

                ClassReader cr = new ClassReader(classfileBuffer);

                if(Utils.isLunar(cr.getSuperName()) && cr.getInterfaces().length == 1){
                    ClassWriter cw = new ClassWriter(cr, 0);

                    cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
                        boolean changedString = false;

                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature
                                , String[] exceptions) {
                            return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                                @Override
                                public void visitLdcInsn(Object value) {
                                    if("Winner: ".equals(value) && !changedString){
                                        super.visitLdcInsn("Blocks Placed: ");
                                        changedString = true;
                                    }else{
                                        super.visitLdcInsn(value);
                                    }
                                }
                            };
                        }
                    }, 0);

                    return cw.toByteArray();
                }

                return classfileBuffer;
            }
        });
    }
}
