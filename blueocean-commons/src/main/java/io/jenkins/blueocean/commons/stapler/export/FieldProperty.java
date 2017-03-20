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

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * {@link org.kohsuke.stapler.export.Property} based on {@link Field}.
 * @author Kohsuke Kawaguchi
 */
class FieldProperty extends Property {
    private final Field field;

    public FieldProperty(Model owner, Field field, Exported exported) {
        super(owner, field.getName(), field.getGenericType(), exported);
        this.field = field;
    }

    public Type getGenericType() {
        return field.getGenericType();
    }

    public Class getType() {
        return field.getType();
    }

    public String getJavadoc() {
        return parent.getJavadoc().getProperty(field.getName());
    }

    public Object getValue(Object object) throws IllegalAccessException {
        return field.get(object);
    }
}
