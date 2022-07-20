package me.onils.customautogg;

import lombok.AllArgsConstructor;
import me.onils.agent.commons.Utils;
import me.onils.agent.commons.asm.StringReplacer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

@AllArgsConstructor
public class Transformer implements ClassFileTransformer {
    public String ggMessage;

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!Utils.isLunar(className)) {
            return classfileBuffer;
        }

        ClassReader cr = new ClassReader(classfileBuffer);
        if (Utils.isLunar(cr.getSuperName()) && cr.getInterfaces().length == 1) {
            ClassWriter cw = new ClassWriter(cr, 0);
            cr.accept(new StringReplacer(
                    "/achat gg",
                    "/achat " + ggMessage,
                    cw
            ), 0);

            return cw.toByteArray();
        }
        return classfileBuffer;
    }
}