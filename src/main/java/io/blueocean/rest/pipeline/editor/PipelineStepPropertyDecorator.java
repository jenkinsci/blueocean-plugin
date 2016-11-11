package io.blueocean.rest.pipeline.editor;

import java.util.List;

import hudson.ExtensionPoint;

/**
 * Allows plugins to modify property metadata, e.g. providing additional form fields and such
 */
public interface PipelineStepPropertyDecorator extends ExtensionPoint {
    /**
     * Adjust the PipelineStepPropertyMetadata for the pipeline step
     */
    public PipelineStepPropertyMetadata decorate(PipelineStepMetadata step, List<PipelineStepPropertyMetadata> property);
}
