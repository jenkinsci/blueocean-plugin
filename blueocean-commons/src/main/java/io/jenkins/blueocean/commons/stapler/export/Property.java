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

import org.jvnet.tiger_types.Types;
import org.kohsuke.stapler.export.CustomExportedBean;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exposes one {@link Exported exposed property} of {@link ExportedBean} to
 * {@link DataWriter}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Property implements Comparable<Property> {

    private static final Logger LOGGER = Logger.getLogger(Property.class.getName());

    /**
     * Name of the property.
     */
    public final String name;
    final ModelBuilder owner;
    /**
     * Visibility depth level of this property.
     *
     * @see Exported#visibility()
     */
    public final int visibility;

    /**
     * Model to which this property belongs to.
     * Never null.
     */
    public final Model parent;

    /**
     * @see Exported#inline()
     */
    public final boolean inline;

    /**
     * @see Exported#merge()
     */
    public final boolean merge;

    /**
     * @see Exported#skipNull()
     */
    private final boolean skipNull;

    private String[] verboseMap;

    private final Type type;

    Property(Model parent, String name, Type type, Exported exported) {
        this.parent = parent;
        this.owner = parent.parent;
        this.name = exported.name().length()>1 ? exported.name() : name;
        this.type = type;
        int v = exported.visibility();
        if(v==0)
            v = parent.defaultVisibility;
        this.visibility = v;
        this.inline = exported.inline();
        this.merge = exported.merge();
        this.skipNull = exported.skipNull();
        String[] s = exported.verboseMap().split("/");
        if (s.length<2)
            this.verboseMap = null;
        else
            this.verboseMap = s;
    }

    public int compareTo(Property that) {
        return this.name.compareTo(that.name);
    }

    public abstract Type getGenericType();
    public abstract Class getType();

    /**
     * Gets the associated javadoc, if any, or null.
     */
    public abstract String getJavadoc();

    /**
     * Writes one property of the given object to {@link DataWriter}.
     *
     * @param pruner
     *      Determines how to prune the object graph tree.
     */
    public void writeTo(Object object, TreePruner pruner, DataWriter writer) throws IOException {
        TreePruner child = pruner.accept(object, this);
        if (child==null)        return;

        Object d = writer.getExportConfig().getExportInterceptor().getValue(this,object, writer.getExportConfig());

        if ((d==null && skipNull) || d == ExportInterceptor.SKIP) { // don't write anything
            return;
        }
        if (merge) {
            // merged property will get all its properties written here
            if (d != null) {
                Class<?> objectType = d.getClass();
                Model model = owner.getOrNull(objectType, parent.type, name);
                if (model == null && !writer.getExportConfig().isSkipIfFail()) {
                    throw new NotExportableException(objectType);
                } else if (model != null) {
                    model.writeNestedObjectTo(d, new FilteringTreePruner(parent.HAS_PROPERTY_NAME_IN_ANCESTORY,child), writer);
                }
            }
        } else {
            writer.name(name);
            writeValue(type, d, child, writer);
        }
    }

    /**
     * @deprecated as of 1.139
     */
    public void writeTo(Object object, int depth, DataWriter writer) throws IOException {
        writeTo(object,new TreePruner.ByDepth(depth),writer);
    }

    /**
     * Writes one value of the property to {@link DataWriter}.
     */
    private void writeValue(Type expected, Object value, TreePruner pruner, DataWriter writer) throws IOException {
        writeValue(expected,value,pruner,writer,writer.getExportConfig().isSkipIfFail());
    }

    private void writeBuffered(Type expected, Object value, TreePruner pruner, DataWriter writer) throws IOException {
        BufferedDataWriter buffer = new BufferedDataWriter(writer.getExportConfig());
        try {
            writeValue(expected, value, pruner, buffer, true);
            buffer.finished();
        } catch (IOException x) {
            if (x.getCause() instanceof InvocationTargetException) {
                LOGGER.log(Level.WARNING, "skipping export of " + value, x);
            }
        }
        buffer.commit(writer);
    }

    /**
     * Writes one value of the property to {@link DataWriter}.
     */
    @SuppressWarnings("unchecked")
    private void writeValue(Type expected, Object value, TreePruner pruner, DataWriter writer, boolean skipIfFail) throws IOException {
        if(value==null) {
            writer.valueNull();
            return;
        }

        if(value instanceof CustomExportedBean) {
            writeValue(expected,((CustomExportedBean)value).toExportedObject(),pruner,writer);
            return;
        }

        Class c = value.getClass();

        Model model = owner.getOrNull(c, parent.type, name);
        if (model == null) {
            if(STRING_TYPES.contains(c)) {
                writer.value(value.toString());
                return;
            }
            if(PRIMITIVE_TYPES.contains(c)) {
                writer.valuePrimitive(value);
                return;
            }
            Class act = c.getComponentType();
            if (act !=null) { // array
                Range r = pruner.getRange();
                writer.startArray();
                if (value instanceof Object[]) {
                    // typical case
                    for (Object item : r.apply((Object[]) value)) {
                        writeBuffered(act, item, pruner, writer);
                    }
                } else {
                    // more generic case
                    int len = Math.min(r.max, Array.getLength(value));
                    for (int i=r.min; i<len; i++) {
                        writeBuffered(act, Array.get(value, i), pruner, writer);
                    }
                }
                writer.endArray();
                return;
            }
            if(value instanceof Iterable) {
                writer.startArray();
                Type expectedItemType = Types.getTypeArgument(expected, 0, null);
                for (Object item : pruner.getRange().apply((Iterable) value)) {
                    writeBuffered(expectedItemType, item, pruner, writer);
                }
                writer.endArray();
                return;
            }
            if(value instanceof Map) {
                if (verboseMap!=null) {// verbose form
                    writer.startArray();
                    for (Map.Entry e : ((Map<?,?>) value).entrySet()) {
                        BufferedDataWriter buffer = new BufferedDataWriter(writer.getExportConfig());
                        try {
                            writeStartObjectNullType(buffer);
                            buffer.name(verboseMap[0]);
                            writeValue(null, e.getKey(), pruner, buffer);
                            buffer.name(verboseMap[1]);
                            writeValue(null, e.getValue(), pruner, buffer);
                            buffer.endObject();
                            buffer.finished();
                        } catch (IOException x) {
                            if (x.getCause() instanceof InvocationTargetException) {
                                LOGGER.log(Level.WARNING, "skipping export of " + e, x);
                            }
                        }
                        buffer.commit(writer);
                    }
                    writer.endArray();
                } else {// compact form
                    writeStartObjectNullType(writer);
                    for (Map.Entry e : ((Map<?,?>) value).entrySet()) {
                        BufferedDataWriter buffer = new BufferedDataWriter(writer.getExportConfig());
                        try {
                            buffer.name(e.getKey().toString());
                            writeValue(null, e.getValue(), pruner, buffer);
                            buffer.finished();
                        } catch (IOException x) {
                            if (x.getCause() instanceof InvocationTargetException) {
                                LOGGER.log(Level.WARNING, "skipping export of " + e, x);
                            }
                        }
                        buffer.commit(writer);
                    }
                    writer.endObject();
                }
                return;
            }
            if(value instanceof Date) {
                writer.valuePrimitive(((Date) value).getTime());
                return;
            }
            if(value instanceof Calendar) {
                writer.valuePrimitive(((Calendar) value).getTimeInMillis());
                return;
            }
            if(value instanceof Enum) {
                writer.value(value.toString());
                return;
            }

            if (skipIfFail) {
                writer.startObject();
                writer.endObject();
                return;
            }

            throw new NotExportableException(c);
        } else {
            writer.type(expected, value.getClass());
            writer.startObject();
            model.writeNestedObjectTo(value, pruner, writer);
            writer.endObject();
        }
    }

    private static class BufferedDataWriter implements DataWriter {
        private final ExportConfig exportConfig;

        private BufferedDataWriter(ExportConfig exportConfig) {
            this.exportConfig = exportConfig;
        }

        @Override
        public @Nonnull
        ExportConfig getExportConfig() {
            return exportConfig;
        }

        enum Op {name, valuePrimitive, value, valueNull, startArray, endArray, type, startObject, endObject}
        static class Step {
            final Op op;
            final Object[] args;
            Step(Op op, Object... args) {
                this.op = op;
                this.args = args;
            }
        }
        final List<Step> steps = new ArrayList<>();
        boolean finished = false;
        @Override
        public void name(String name) throws IOException {
            steps.add(new Step(Op.name, name));
        }
        @Override
        public void valuePrimitive(Object v) throws IOException {
            steps.add(new Step(Op.valuePrimitive, v));
        }
        @Override
        public void value(String v) throws IOException {
            steps.add(new Step(Op.value, v));
        }
        @Override
        public void valueNull() throws IOException {
            steps.add(new Step(Op.valueNull));
        }
        @Override
        public void startArray() throws IOException {
            steps.add(new Step(Op.startArray));
        }
        @Override
        public void endArray() throws IOException {
            steps.add(new Step(Op.endArray));
        }
        @Override
        public void type(Type expected, Class actual) throws IOException {
            steps.add(new Step(Op.type, expected, actual));
        }
        @Override
        public void startObject() throws IOException {
            steps.add(new Step(Op.startObject));
        }
        @Override
        public void endObject() throws IOException {
            steps.add(new Step(Op.endObject));
        }
        private void finished() {
            finished = true;
        }
        void commit(DataWriter w) throws IOException {
            if (!finished) {
                return;
            }
            for (Step step : steps) {
                switch (step.op) {
                case name:
                    w.name((String) step.args[0]);
                    break;
                case valuePrimitive:
                    w.valuePrimitive(step.args[0]);
                    break;
                case value:
                    w.value((String) step.args[0]);
                    break;
                case valueNull:
                    w.valueNull();
                    break;
                case startArray:
                    w.startArray();
                    break;
                case endArray:
                    w.endArray();
                    break;
                case type:
                    try {
                        w.type((Type) step.args[0], (Class) step.args[1]);
                    } catch (AbstractMethodError ignored) {}
                    break;
                case startObject:
                    w.startObject();
                    break;
                case endObject:
                    w.endObject();
                    break;
                default:
                    throw new AssertionError();
                }
            }
        }
    }

    private void writeStartObjectNullType(DataWriter writer) throws IOException {
        try {
            writer.type(null,null);
        } catch (AbstractMethodError e) {
            // legacy client that doesn't understand this
        }
        writer.startObject();
    }

    /**
     * Gets the value of this property from the bean.
     */
    public abstract Object getValue(Object bean) throws IllegalAccessException, InvocationTargetException;

    /*package*/ static final Set<Class> STRING_TYPES = new HashSet<>(Arrays.asList(
        String.class,
        URL.class
    ));

    /*package*/ static final Set<Class> PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
        Integer.class,
        Long.class,
        Boolean.class,
        Short.class,
        Character.class,
        Float.class,
        Double.class
    ));
}
