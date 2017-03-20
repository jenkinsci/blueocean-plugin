/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.blueocean.commons.stapler;

import io.jenkins.blueocean.commons.stapler.export.ExportConfig;
import io.jenkins.blueocean.commons.stapler.export.Flavor;
import io.jenkins.blueocean.commons.stapler.export.Model;
import io.jenkins.blueocean.commons.stapler.export.ModelBuilder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Simple Jenkins Model Object serializer.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ModelObjectSerializer {

    private static ExportConfig config = new ExportConfig();

    private ModelObjectSerializer() {
    }

    /**
     * Serialize the supplied object to JSON and return as a {@link String}.
     * @param object The object to serialize.
     * @return The JSON as a {@link String}.
     * @throws IOException Error serializing model object.
     */
    @Nonnull
    public static String toJson(@Nonnull Object object) throws IOException {
        try (StringWriter writer = new StringWriter()) {
            toJson(object, writer);
            return writer.toString();
        }
    }

    /**
     * Serialize the supplied object to JSON and write to the supplied {@link Writer}.
     * @param object The object to serialize.
     * @param writer The writer to output to.
     * @throws IOException Error serializing model object.
     */
    public static void toJson(@Nonnull Object object, @Nonnull Writer writer) throws IOException {
        Model model = new ModelBuilder().get(object.getClass());
        model.writeTo(object, Flavor.JSON.createDataWriter(object, writer, config));
        writer.flush();
    }
}
