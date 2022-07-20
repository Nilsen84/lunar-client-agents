package me.onils.agent.commons;

import lombok.experimental.UtilityClass;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

@UtilityClass
public class Utils {
    public boolean isLunar(String className){
        return className.startsWith("com/moonsworth/lunar/");
    }

    public boolean isMinecraft(String className){
        return className.startsWith("net/minecraft/");
    }

    public boolean write(String fileName, byte[] bytes){
        try(OutputStream os = new FileOutputStream(fileName)){
            os.write(bytes);
            return true;
        }catch (IOException ex){
            return false;
        }
    }
    public boolean containsStrings(MethodNode mn, String... strings){
        var set = new HashSet<String>(strings.length);
        Collections.addAll(set, strings);

        return Arrays.stream(mn.instructions.toArray())
                .filter(LdcInsnNode.class::isInstance)
                .map(LdcInsnNode.class::cast)
                .map(ldc -> ldc.cst)
                .anyMatch(s -> set.remove(s) && set.isEmpty());
    }
}