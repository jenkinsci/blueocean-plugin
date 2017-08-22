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

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Provides tool information
 */
@ExportedBean
public class ExportedToolDescriptor {
    private final String toolName;
    private final String symbol;
    private final Class<?> type;
    private final List<ExportedToolInstallation> installations = new ArrayList<ExportedToolInstallation>();

    public ExportedToolDescriptor(String toolName, String symbol, Class<?> type) {
        this.toolName = toolName;
        this.symbol = symbol;
        this.type = type;
    }
    
    @Exported
    public String getToolName() {
        return toolName;
    }
    
    @Exported
    public String getSymbol() {
        return symbol;
    }
    
    @Exported
    public String getType() {
        return type.getName();
    }
    
    @Exported
    public ExportedToolInstallation[] getInstallations() {
        return installations.toArray(new ExportedToolInstallation[installations.size()]);
    }
    
    public void addInstallation(ExportedToolInstallation installation) {
        this.installations.add(installation);
    }
    
    @ExportedBean
    public static class ExportedToolInstallation {
        private final String name;
        private final Class<?> type;
        
        public ExportedToolInstallation(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }
        
        @Exported
        public String getName() {
            return name;
        }
        
        @Exported
        public String getType() {
            return type.getName();
        }
    }
}
