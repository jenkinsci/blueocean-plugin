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
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Receives the event callback on the model data to be exposed.
 *
 * <p>
 * The call sequence is:
 *
 * <pre>
 * EVENTS := type? startObject PROPERTY* endObject
 * PROPERTY := name VALUE
 * VALUE := valuePrimitive
 *        | value
 *        | valueNull
 *        | startArray VALUE* endArray
 *        | EVENTS
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 */
public interface DataWriter {
    void name(String name) throws IOException;

    void valuePrimitive(Object v) throws IOException;
    void value(String v) throws IOException;
    void valueNull() throws IOException;

    void startArray() throws IOException;
    void endArray() throws IOException;

    /**
     * Augments the next {@link #startObject()} call by specifying the type information of that object.
     *
     * @param expected
     *      The declared type of the variable that references this object.
     *      Null if the object is not referenced by anyone, for example when it's the root.
     * @param actual
     *      The actual type of the object being written.
     *      Null if the object is synthetic and has no valid Java type
     */
    void type(@Nullable Type expected, @Nullable Class actual) throws IOException;
    void startObject() throws IOException;
    void endObject() throws IOException;

    @NonNull
    ExportConfig getExportConfig();

    /**
     * Recommended property name to write out the 'type' parameter of {@link #type(Type,Class)}
     */
    String CLASS_PROPERTY_NAME = "_class";
}
