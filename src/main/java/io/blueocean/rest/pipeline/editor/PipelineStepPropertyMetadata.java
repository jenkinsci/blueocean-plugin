package io.blueocean.rest.pipeline.editor;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;


/**
 * Basic pipeline step property descriptor
 */
@ExportedBean
public interface PipelineStepPropertyMetadata {
    /**
     * Name of the property
     */
    @Exported
    public String getName();
    
    /**
     * Indicates this property is required
     */
    @Exported
    public boolean getIsRequired();

    /**
     * Java class name for the property
     */
    @Exported
    public String getType();
}
