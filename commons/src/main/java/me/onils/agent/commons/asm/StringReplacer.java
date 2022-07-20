package me.onils.agent.commons.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class StringReplacer extends ClassVisitor {
    String from;
    String to;

    public StringReplacer(String from, String to, ClassVisitor cv) {
        super(Opcodes.ASM9, cv);

        this.from = from;
        this.to = to;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
            @Override
            public void visitLdcInsn(Object value) {
                super.visitLdcInsn(from.equals(value) ? to : value);
            }
        };
    }
}
