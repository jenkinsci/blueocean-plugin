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

import com.google.common.base.Predicate;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.jenkins.blueocean.commons.stapler.export.TreePruner.ByDepth;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes all the property of one {@link ExportedBean} to {@link DataWriter}.
 *
 * @author Kohsuke Kawaguchi
 */
public class Model<T> {
    /**
     * The class being modeled.
     */
    public final Class<T> type;

    /**
     * {@link Model} for the super class.
     */
    public final Model<? super T> superModel;

    private final Property[] properties;

    /*package*/ final ModelBuilder parent;
    /*package*/ final int defaultVisibility;

    /**
     * Lazily loaded "*.javadoc" file for this model.
     */
    private volatile Properties javadoc;

    private final Set<String> propertyNames = new HashSet<>();

    /*package*/ Model(ModelBuilder parent, Class<T> type, @CheckForNull Class<?> propertyOwner, @Nullable String property) throws NotExportableException {
        this.parent = parent;
        this.type = type;
        ExportedBean eb = type.getAnnotation(ExportedBean.class);
        if (eb == null) {
            throw propertyOwner != null ? new NotExportableException(type, propertyOwner, property) : new NotExportableException(type);
        }
        this.defaultVisibility = eb.defaultVisibility();

        Class<? super T> sc = type.getSuperclass();
        if(sc!=null && sc.getAnnotation(ExportedBean.class)!=null)
            superModel = parent.get(sc);
        else
            superModel = null;

        List<Property> properties = new ArrayList<>();

        // Use reflection to find out what properties are exposed.
        for( Field f : type.getFields() ) {
            if(f.getDeclaringClass()!=type) continue;
            Exported exported = f.getAnnotation(Exported.class);
            if(exported !=null)
                properties.add(new FieldProperty(this,f, exported));
        }

        for( Method m : type.getMethods() ) {
            if(m.getDeclaringClass()!=type) continue;
            if(m.isSynthetic() && m.isBridge()) continue;
            Exported exported = m.getAnnotation(Exported.class);
            if(exported !=null) {
                if (m.getParameterTypes().length > 0) {
                    LOGGER.log(Level.WARNING, "Method " + m.getName() + " of " + type.getName() + " is annotated @Exported but requires arguments");
                } else {
                    properties.add(new MethodProperty(this, m, exported));
                }
            }
        }

        this.properties = properties.toArray(new Property[properties.size()]);
        Arrays.sort(this.properties);
        for (Property p : properties)
            this.propertyNames.add(p.name);

        parent.models.put(type,this);
    }

    /**
     * Gets all the exported properties.
     */
    public List<Property> getProperties() {
        return Collections.unmodifiableList(Arrays.asList(properties));
    }

    /**
     * Does a property exist strictly in this class?
     */
    /*package*/ final Predicate<String> HAS_PROPERTY_NAME = new Predicate<String>() {
        @Override
        public boolean apply(@Nullable String name) {
            return propertyNames.contains(name);
        }
    };
    /**
     * Does a property exist strictly in this class or its ancestors?
     */
    /*package*/ final Predicate<String> HAS_PROPERTY_NAME_IN_ANCESTORY = new Predicate<String>() {
        @Override
        public boolean apply(@Nullable String name) {
            for (Model m=Model.this; m!=null; m=m.superModel)
                if (m.propertyNames.contains(name))
                    return true;
            return false;
        }
    };

    /**
     * Loads the javadoc list and returns it as {@link Properties}.
     *
     * @return always non-null.
     */
    /*package*/ Properties getJavadoc() {
        if(javadoc!=null)    return javadoc;
        synchronized(this) {
            if(javadoc!=null)    return javadoc;

            // load
            Properties p = new Properties();
            InputStream is = type.getClassLoader().getResourceAsStream(type.getName().replace('$', '/').replace('.', '/') + ".javadoc");
            if(is!=null) {
                try {
                    try {
                        p.load(is);
                    } finally {
                        is.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Unable to load javadoc for "+type,e);
                }
            }
            javadoc = p;
            return javadoc;
        }
    }

    /**
     * Writes the property values of the given object to the writer.
     */
    public void writeTo(T object, DataWriter writer) throws IOException {
        writeTo(object, 0, writer);
    }

    /**
     * Writes the property values of the given object to the writer.
     *
     * @param pruner
     *      Controls which portion of the object graph will be sent to the writer.
     */
    public void writeTo(T object, TreePruner pruner, DataWriter writer) throws IOException {
        try {
            writer.type(null,object.getClass());
        } catch (AbstractMethodError e) {
            // legacy client that doesn't understand this
        }
        writer.startObject();
        writeNestedObjectTo(object, pruner, writer);
        writer.endObject();
    }

    /**
     * Writes the property values of the given object to the writer.
     *
     * @param baseVisibility
     *      This parameters controls how much data we'd be writing,
     *      by adding bias to the sub tree cutting.
     *      A property with {@link Exported#visibility() visibility} X will be written
     *      if the current depth Y and baseVisibility Z satisfies {@code X + Z > Y}.
     *
     *      0 is the normal value. Positive value means writing bigger tree,
     *      and negative value means writing smaller trees.
     *
     * @deprecated as of 1.139
     */
    @Deprecated
    public void writeTo(T object, int baseVisibility, DataWriter writer) throws IOException {
        writeTo(object,new ByDepth(1-baseVisibility),writer);
    }

    void writeNestedObjectTo(T object, TreePruner pruner, DataWriter writer) throws IOException {
        if (superModel != null) {
            superModel.writeNestedObjectTo(object, new FilteringTreePruner(HAS_PROPERTY_NAME,pruner), writer);
        }

        for (Property p : properties) {
            p.writeTo(object, pruner, writer);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(Model.class.getName());
}
