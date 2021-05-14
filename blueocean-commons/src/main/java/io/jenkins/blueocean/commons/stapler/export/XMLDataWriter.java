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

import edu.umd.cs.findbugs.annotations.NonNull;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerResponse;

import java.lang.reflect.Type;
import java.util.Stack;
import java.io.Writer;
import java.io.IOException;
import java.beans.Introspector;

/**
 * Writes XML.
 *
 * @author Kohsuke Kawaguchi
 */
final class XMLDataWriter implements DataWriter {

    private String name;
    private final Stack<String> objectNames = new Stack<>();
    /**
     * Stack that keeps track of whether we are inside an array.
     * The top element represents the current state.
     */
    private final Stack<Boolean> isArray = new Stack<>();
    private final Writer out;
    private ExportConfig config;
    private String classAttr;
    private final ExportConfig exportConfig;

    XMLDataWriter(Object bean, Writer out, ExportConfig config) throws IOException {
        Class c=bean.getClass();
        while (c.isAnonymousClass())
            c = c.getSuperclass();
        name = Introspector.decapitalize(c.getSimpleName());
        this.out = out;
        this.config = config;
        this.isArray.push(false);
        this.exportConfig = config;
        // TODO: support pretty printing
    }

    XMLDataWriter(Object bean, StaplerResponse rsp, ExportConfig config) throws IOException {
        this(bean,rsp.getWriter(),config);
    }

    @Override @NonNull
    public ExportConfig getExportConfig() {
        return exportConfig;
    }

    public void name(String name) {
        this.name = name;
    }

    public void valuePrimitive(Object v) throws IOException {
        value(v.toString());
    }

    public void value(String v) throws IOException {
        String n = adjustName();
        out.write('<'+n+'>');
        out.write(Stapler.escape(v));
        out.write("</"+n+'>');
    }

    public void valueNull() {
        // use absence to indicate null.
    }

    public void startArray() {
        // use repeated element to display array
        // this means nested arrays are not supported
        isArray.push(true);
    }

    public void endArray() {
        isArray.pop();
    }

    @Override
    public void type(Type expected, Class actual) throws IOException {
        classAttr = config.getClassAttribute().print(expected, actual);
    }

    public void startObject() throws IOException {
        objectNames.push(name);
        out.write('<' + adjustName());
        isArray.push(false);

        if (classAttr!=null) {
            out.write(CLASS_ATTRIBUTE_PREFIX +classAttr+"'");
            classAttr = null;
        }
        out.write('>');
    }

    public void endObject() throws IOException {
        isArray.pop();
        name = objectNames.pop();
        out.write("</"+adjustName()+'>');
    }

    /**
     * Returns the name to be used as an element name
     * by considering {@link #isArray}
     */
    private String adjustName() {
        String escaped = makeXmlName(name);
        if(isArray.peek()) return toSingular(escaped);
        return escaped;
    }

    /*package*/ static String toSingular(String name) {
        return name.replaceFirst("ies$", "y").replaceFirst("s$", "");
    }

    /*package*/ static String makeXmlName(String name) {
        if (name.length()==0)   name="_";

        if (!XmlChars.isNameStart(name.charAt(0))) {
            if (name.length()>1 && XmlChars.isNameStart(name.charAt(1)))
                name = name.substring(1);
            else
                name = '_'+name;
        }

        int i=1;
        while (i<name.length()) {
            if (XmlChars.isNameChar(name.charAt(i)))
                i++;
            else
                name = name.substring(0,i)+name.substring(i+1);
        }

        return name;
    }

    private static final String CLASS_ATTRIBUTE_PREFIX = " "+ CLASS_PROPERTY_NAME +"='";
}
