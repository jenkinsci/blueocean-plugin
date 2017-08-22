package io.blueocean.rest.pipeline.editor;

import java.util.List;

import hudson.ExtensionPoint;

/**
 * Allows plugins to modify property metadata, e.g. providing additional form fields and such
 */
public interface ExportedDescribableParameterDecorator extends ExtensionPoint {
    /**
     * Adjust the PipelineStepPropertyMetadata for the pipeline step
     */
    public ExportedDescribableParameter decorate(ExportedDescribableModel model, List<ExportedDescribableParameter> parameters);
}
