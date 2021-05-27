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

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * JSON writer.
 *
 * @author Kohsuke Kawaguchi
 */
class JSONDataWriter implements DataWriter {

    private static final JsonStringEncoder jsonEncoder = new JsonStringEncoder ();

    protected boolean needComma;
    protected final Writer out;
    protected final ExportConfig config;

    private int indent;
    private String classAttr;

    JSONDataWriter(Writer out, ExportConfig config) throws IOException {
        this.out = out;
        this.config = config;
        indent = config.isPrettyPrint() ? 0 : -1;
    }

    @Override @NonNull
    public ExportConfig getExportConfig() {
        return config;
    }

    public void name(String name) throws IOException {
        comma();
        if (indent<0)   out.write('"'+name+"\":");
        else            out.write('"'+name+"\" : ");
        needComma = false;
    }

    protected void data(String v) throws IOException {
        comma();
        out.write(v);
    }

    protected void comma() throws IOException {
        if(needComma) {
            out.write(',');
            indent();
        }
        needComma = true;
    }

    /**
     * Prints indentation.
     */
    private void indent() throws IOException {
        if (indent>=0) {
            out.write('\n');
            for (int i=indent*2; i>0; ) {
                int len = Math.min(i,INDENT.length);
                out.write(INDENT,0,len);
                i-=len;
            }
        }
    }

    private void inc() {
        if (indent<0)   return; // no indentation
        indent++;
    }

    private void dec() {
        if (indent<0)   return;
        indent--;
    }

    public void valuePrimitive(Object v) throws IOException {
        data(v.toString());
    }

    public void value(String v) throws IOException {
        StringBuilder buf = new StringBuilder(v.length());
        buf.append('\"');
        // TODO: remove when JENKINS-45099 has been fixed correctly in upstream stapler
        if (config.isHtmlEncode()) {
            jsonEncoder.quoteAsString(StringEscapeUtils.escapeHtml(v), buf);
        } else {
            jsonEncoder.quoteAsString(v, buf);
        }
        buf.append('\"');
        data(buf.toString());
    }

    public void valueNull() throws IOException {
        data("null");
    }

    private void open(char symbol) throws IOException {
        comma();
        out.write(symbol);
        needComma = false;
        inc();
        indent();
    }

    private void close(char symbol) throws IOException {
        dec();
        indent();
        needComma = true;
        out.write(symbol);
    }

    public void startArray() throws IOException {
        open('[');
    }

    public void endArray() throws IOException {
        close(']');
    }

    @Override
    public void type(Type expected, Class actual) throws IOException {
        classAttr = config.getClassAttribute().print(expected, actual);
    }

    public void startObject() throws IOException {
        _startObject();

        if (classAttr!=null) {
            name(CLASS_PROPERTY_NAME);
            value(classAttr);
            classAttr = null;
        }
    }

    protected void _startObject() throws IOException {
        open('{');
    }

    public void endObject() throws IOException {
        close('}');
    }

    private static final char[] INDENT = new char[32];
    static {
        Arrays.fill(INDENT, ' ');
    }
}
