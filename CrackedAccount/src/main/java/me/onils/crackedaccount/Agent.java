package me.onils.crackedaccount;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Agent {
    static MethodNode getConstructor(ClassNode cn){
        for(MethodNode methodNode : cn.methods){
            if(methodNode.name.equals("<init>")){
                return methodNode;
            }
        }
        return null;
    }

    static boolean containsString(MethodNode methodNode, String str){
        for(AbstractInsnNode insnNode : methodNode.instructions){
            if(insnNode instanceof LdcInsnNode ldcInsnNode){
                if(str.equals(ldcInsnNode.cst)){
                    return true;
                }
            }
        }
        return false;
    }

    static boolean hasMethodCall(MethodNode methodNode, String name){
        AbstractInsnNode insnNode = methodNode.instructions.getLast();

        while (insnNode != null){
            if(insnNode instanceof MethodInsnNode methodInsnNode && methodInsnNode.name.equals(name)) return true;

            insnNode = insnNode.getPrevious();
        }

        return false;
    }

    public static void premain(String username, Instrumentation inst){
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if(!className.startsWith("lunar/")) return classfileBuffer;

                ClassReader cr = new ClassReader(classfileBuffer);
                ClassNode cn = new ClassNode();
                cr.accept(cn, ClassReader.SKIP_FRAMES);

                MethodNode constructor = getConstructor(cn);

                if(constructor == null) return classfileBuffer;

                if(!containsString(constructor, "multiplayer")) return classfileBuffer;

                System.err.println("[CrackedAccount] found main menu class!");

                boolean injected = false;

                for(MethodNode methodNode : cn.methods){
                    if((methodNode.access & Opcodes.ACC_SYNTHETIC) == 0) continue;

                    if(hasMethodCall(methodNode, "initGuiMultiplayer") || hasMethodCall(methodNode, "initGuiSelectWorld")){
                        for(AbstractInsnNode insnNode : methodNode.instructions){
                            if(insnNode.getOpcode() == Opcodes.IFNE){
                                methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.POP));
                                ((JumpInsnNode)insnNode).setOpcode(Opcodes.GOTO);
                                injected = true;
                            }
                        }
                    }
                }

                if(injected){
                    System.err.println("[CrackedAccount] injected");

                    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
                    cn.accept(cw);
                    return cw.toByteArray();
                }

                return classfileBuffer;
            }
        });

        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if(!className.equals("net/minecraft/client/main/Main")) return classfileBuffer;

                ClassReader cr = new ClassReader(classfileBuffer);
                ClassWriter cw = new ClassWriter(cr, 0);
                cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                                     String[] exceptions) {
                        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);

                        if(name.equals("main")){
                            return new MethodVisitor(Opcodes.ASM9, methodVisitor) {
                                boolean injected = false;

                                @Override
                                public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
                                                            boolean isInterface) {
                                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

                                    if(owner.equals("java/lang/StringBuilder") && name.equals("toString") && !injected){
                                        super.visitInsn(Opcodes.POP);
                                        super.visitLdcInsn(username);
                                        injected = true;
                                    }
                                }
                            };
                        }

                        return methodVisitor;
                    }
                }, 0);

                return cw.toByteArray();
            }
        });
    }
}
