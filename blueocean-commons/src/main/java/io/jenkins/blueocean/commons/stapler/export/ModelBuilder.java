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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.kohsuke.stapler.export.ExportedBean;

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
     * Registration happens in {@link Model#Model(ModelBuilder, Class, Class, String)} so that cyclic references
     * are handled correctly.
     */
    /*package*/ final Map<Class, Model> models = new ConcurrentHashMap<>();

    @NonNull
    public <T> Model<T> get(Class<T> type) throws NotExportableException {
        return get(type, null, null);
    }

    /**
     * @throws NotExportableException if type is not exportable
     * @return model
     */
    @NonNull
    public <T> Model<T> get(Class<T> type, @CheckForNull Class<?> propertyOwner, @Nullable String property) throws NotExportableException {
        Model<T> model = getOrNull(type, propertyOwner, property);
        if (model == null) {
            throw new NotExportableException(type);
        }
        return model;
    }

    /**
     * Instead of throwing {@link NotExportableException} this method will return null
     * This should be used on hot paths where throwing the exception and catching it would incur a performance hit
     * @return model
     * @since 1.253
     */
    @CheckForNull
    public <T> Model<T> getOrNull(Class<T> type, @CheckForNull Class<?> propertyOwner, @Nullable String property) {
        Model<T> m = models.get(type);
        if(m==null && type.getAnnotation(ExportedBean.class) != null) {
            m = new Model<>(this, type, propertyOwner, property);
        }
        return m;
    }
}
