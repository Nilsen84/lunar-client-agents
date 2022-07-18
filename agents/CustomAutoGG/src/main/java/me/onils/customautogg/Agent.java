package me.onils.customautogg;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String msg, Instrumentation inst){
        inst.addTransformer(new Transformer(msg));
    }
}
