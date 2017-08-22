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

import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.jenkinsci.plugins.structs.describable.DescribableParameter;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ExportedBean
public class ExportedDescribableModel {
    protected final DescribableModel<?> model;
    protected final String symbol;

    public ExportedDescribableModel(DescribableModel<?> model) {
        this(model, null);
    }

    public ExportedDescribableModel(DescribableModel<?> model, String symbol) {
        this.model = model;
        this.symbol = symbol;
    }

    /**
     * The Java class name for this describable (since we can't seem to export a Class&lt;?&gt; ...)
     * See {@link DescribableModel#getType()}
     */
    @Exported
    public String getType() {
        return model.getType().getName();
    }
    
    /**
     * Provides the symbol for this describable
     * @return
     */
    @Exported
    public String getSymbol() {
        return symbol;
    }

    /**
     * Display Name of the describable class
     * See {@link DescribableModel#getDisplayName()}
     */
    @Exported
    public String getDisplayName() {
        return model.getDisplayName();
    }

    /**
     * Whether this describable has one and only one parameter and it is required.
     * See {@link DescribableModel#hasSingleRequiredParameter()}
     */
    @Exported
    public boolean getHasSingleRequiredParameter() {
        return model.hasSingleRequiredParameter();
    }

    /**
     * Loads help defined for this object as a whole if available, else null.
     * See {@link DescribableModel#getHelp()}
     */
    @Exported
    public String getHelp() throws IOException {
        return model.getHelp();
    }

    /**
     * Properties the describable supports
     * See {@link DescribableModel#getParameters()}
     */
    @Exported
    public List<ExportedDescribableParameter> getParameters() {
        List<ExportedDescribableParameter> params = new ArrayList<>();

        for (DescribableParameter p : model.getParameters()) {
            params.add(new ExportedDescribableParameter(p));
        }
        return params;
    }
}
