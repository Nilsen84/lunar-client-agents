package me.onils.customlevelhead;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String levelPhrase, Instrumentation inst){
        inst.addTransformer(new Transformer(levelPhrase));
    }
}
