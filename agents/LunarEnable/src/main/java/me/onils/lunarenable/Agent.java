package me.onils.lunarenable;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String option, Instrumentation inst){
        inst.addTransformer(new MetadataTransformer());
        inst.addTransformer(new BukkitTransformer());
    }
}
