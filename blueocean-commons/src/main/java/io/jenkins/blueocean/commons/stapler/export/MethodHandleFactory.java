package io.jenkins.blueocean.commons.stapler.export;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

public final class MethodHandleFactory {
    private static final Lookup LOOKUP = MethodHandles.lookup();

    public static MethodHandle get(Method method) {
        try {
            return LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            throw (Error)new IllegalAccessError("Protected method: "+method).initCause(e);
        }
    }

    private MethodHandleFactory() {}
}
