package me.onils.crackedaccount;

import me.onils.agent.commons.Utils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class Agent {
    public static String[] modifyArgs(String[] args, String username){
        args = Arrays.copyOf(args, args.length+2);
        args[args.length-2] = "--username";
        args[args.length-1] = username;

        return args;
    }

    static MethodNode getConstructor(ClassNode cn){
        for(MethodNode methodNode : cn.methods){
            if(methodNode.name.equals("<init>")){
                return methodNode;
            }
        }
        return null;
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
                if(!Utils.isLunar(className)) return classfileBuffer;

                ClassReader cr = new ClassReader(classfileBuffer);
                ClassNode cn = new ClassNode();
                cr.accept(cn, ClassReader.SKIP_FRAMES);

                MethodNode constructor = getConstructor(cn);

                if(constructor == null) return classfileBuffer;

                if(!Utils.containsStrings(constructor, "multiplayer")) return classfileBuffer;

                boolean injected = false;

                methodLoop:
                for(MethodNode methodNode : cn.methods){
                    if((methodNode.access & Opcodes.ACC_SYNTHETIC) == 0) continue;

                    if(hasMethodCall(methodNode, "initGuiMultiplayer") || hasMethodCall(methodNode, "initGuiSelectWorld")){
                        for(AbstractInsnNode insnNode : methodNode.instructions){
                            if(insnNode.getOpcode() == Opcodes.IFNE){
                                methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.POP));
                                ((JumpInsnNode)insnNode).setOpcode(Opcodes.GOTO);
                                injected = true;

                                continue methodLoop;
                            }
                        }
                    }
                }

                if(injected){
                    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
                    cn.accept(cw);
                    return cw.toByteArray();
                }

                return classfileBuffer;
            }
        });

        if(username.isEmpty()) return;

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
                                @Override
                                public void visitCode() {
                                    super.visitVarInsn(Opcodes.ALOAD, 0);
                                    super.visitLdcInsn(username);
                                    super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Agent.class), "modifyArgs", "([Ljava/lang/String;Ljava/lang/String;)[Ljava/lang/String;", false);
                                    super.visitVarInsn(Opcodes.ASTORE, 0);

                                    super.visitCode();
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
