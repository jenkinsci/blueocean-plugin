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

import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates and maintains {@link Model}s, that are used to write out
 * the value representation of {@link ExportedBean exposed beans}.
 * @author Kohsuke Kawaguchi
 */
public class ModelBuilder {
    /**
     * Instantiated {@link Model}s.
     * Registration happens in {@link Model#Model(org.kohsuke.stapler.export.ModelBuilder,Class)} so that cyclic references
     * are handled correctly.
     */
    /*package*/ final Map<Class, Model> models = new ConcurrentHashMap<Class, Model>();

    public <T> Model<T> get(Class<T> type) throws NotExportableException {
        if (type.getAnnotation(ExportedBean.class) == null) {
            throw new NotExportableException(type);
        }
        return get(type, null, null);
    }

    public <T> Model<T> getOrNull(Class<T> type) throws NotExportableException {
        return get(type, null, null);
    }

    public <T> Model<T> get(Class<T> type, @CheckForNull Class<?> propertyOwner, @Nullable String property) {
        if (type.getAnnotation(ExportedBean.class) == null) {
            throw new NotExportableException(type);
        }
        return getOrNull(type, propertyOwner, property);
    }

    public <T> Model<T> getOrNull(Class<T> type, @CheckForNull Class<?> propertyOwner, @Nullable String property) throws NotExportableException {
        Model m = models.get(type);
        if(m==null && type.getAnnotation(ExportedBean.class) != null) {
            m = new Model<T>(this, type, propertyOwner, property);
        }
        return m;
    }
}
