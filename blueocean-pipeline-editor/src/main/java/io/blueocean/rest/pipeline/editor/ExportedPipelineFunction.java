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

package io.blueocean.rest.pipeline.editor;

import jenkins.tasks.SimpleBuildWrapper;
import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class ExportedPipelineFunction extends ExportedDescribableModel {
    protected final String functionName;

    public ExportedPipelineFunction(DescribableModel<?> model, String functionName) {
        super(model);
        this.functionName = functionName;
    }

    /**
     * Identifier used for the 'function' name in the pipeline step, used in the pipeline file
     */
    @Exported
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Indicates this step wraps a block of other steps
     */
    @Exported
    public boolean getIsBlockContainer() {
        return SimpleBuildWrapper.class.isAssignableFrom(model.getType());
    }
}
