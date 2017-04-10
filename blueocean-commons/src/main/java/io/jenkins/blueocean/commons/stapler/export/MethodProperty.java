/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.jenkins.blueocean.commons.stapler.export;

import org.kohsuke.stapler.export.Exported;

import java.beans.Introspector;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * {@link Property} based on {@link Method}.
 * @author Kohsuke Kawaguchi
 */
final class MethodProperty extends Property {
    private final MethodHandle handle;
    private final Method method;

    MethodProperty(Model owner, Method m, Exported exported) {
        super(owner,buildName(m.getName()), m.getGenericReturnType(), exported);
        this.method = m;
        this.handle = MethodHandleFactory.get(method);
    }

    private static String buildName(String name) {
        if(name.startsWith("get"))
            name = name.substring(3);
        else
        if(name.startsWith("is"))
            name = name.substring(2);

        return Introspector.decapitalize(name);
    }

    public Type getGenericType() {
        return method.getGenericReturnType();
    }

    public Class getType() {
        return method.getReturnType();
    }

    public String getJavadoc() {
        return parent.getJavadoc().getProperty(method.getName()+"()");
    }

    public Object getValue(Object object) throws IllegalAccessException, InvocationTargetException {
        try {
            return handle.invoke(object);
        } catch (Throwable throwable) {
            throw new InvocationTargetException(throwable);
        }
    }
}
