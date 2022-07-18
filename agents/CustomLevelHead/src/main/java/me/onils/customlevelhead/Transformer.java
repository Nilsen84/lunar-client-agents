package me.onils.customlevelhead;

import me.onils.agent.commons.Utils;
import me.onils.agent.commons.asm.StringReplacer;
import org.objectweb.asm.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class Transformer implements ClassFileTransformer {
    public String levelPhrase;

    Transformer(String levelPhrase){
        this.levelPhrase = levelPhrase;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(!Utils.isLunar(className)){
            return classfileBuffer;
        }

        ClassReader cr = new ClassReader(classfileBuffer);
        if(Utils.isLunar(cr.getSuperName()) && cr.getInterfaces().length == 1){
            ClassWriter cw = new ClassWriter(cr, 0);

            cr.accept(
                    new StringReplacer(
                            "Level: ",
                            levelPhrase,
                            cw
                    ),
                    0
            );

            return cw.toByteArray();
        }

        return classfileBuffer;
    }
}