package io.blueocean.rest.pipeline.editor;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public interface PipelineStepMetadata {
    /**
     * Identifier used for the 'function' name in the pipeline step, used in the pipeline file
     */
    @Exported
    public String getFunctionName();
    
    /**
     * The Java class name for this step (since we can't seem to export a Class<?>...)
     */
    @Exported
    public String getType();
    
    /**
     * Display Name of the pipeline step, used in the pipeline file
     */
    @Exported
    public String getDisplayName();
    
    /**
     * The Java class names that this pipeline step exports into context
     */
    @Exported
    public String[] getProvidedContext();
    
    /**
     * The Java class names that this pipeline requires to be in context
     */
    @Exported
    public String[] getRequiredContext();
    
    /**
     * Indicates this step wraps a block of other steps
     */
    @Exported
    public boolean getIsBlockContainer();
    
    /**
     * Snippetizer URL for this step (these are the same with different POST parameters...)
     */
    @Exported
    public String getSnippetizerUrl();

    /**
     * Whether this step has one and only one parameter and it is required.
     */
    @Exported
    public boolean getHasSingleRequiredParameter();

    /**
     * Properties the steps supports
     */
    @Exported
    public PipelineStepPropertyMetadata[] getProperties();
}
