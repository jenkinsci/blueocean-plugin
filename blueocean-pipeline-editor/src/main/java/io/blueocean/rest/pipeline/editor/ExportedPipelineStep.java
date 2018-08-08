package io.blueocean.rest.pipeline.editor;

import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.List;

@ExportedBean
public class ExportedPipelineStep extends ExportedPipelineFunction {
    protected final StepDescriptor descriptor;

    public ExportedPipelineStep(DescribableModel<? extends Step> model, String functionName,
                                StepDescriptor descriptor) {
        super(model, functionName);
        this.descriptor = descriptor;
    }

    /**
     * The Java class names that this pipeline step exports into context
     */
    @Exported
    public List<String> getProvidedContext() {
        List<String> out = new ArrayList<String>();
        for (Class<?> c : descriptor.getProvidedContext()) {
            out.add(c.getName());
        }
        return out;
    }
    
    /**
     * The Java class names that this pipeline requires to be in context
     */
    @Exported
    public List<String> getRequiredContext() {
        List<String> out = new ArrayList<String>();
        for (Class<?> c : descriptor.getRequiredContext()) {
            out.add(c.getName());
        }
        return out;
    }
    
    /**
     * Indicates this step wraps a block of other steps
     */
    @Override
    @Exported
    public boolean getIsBlockContainer() {
        return descriptor.takesImplicitBlockArgument();
    }
    
    /**
     * Relative descriptor URL for this step
     */
    @Exported
    public String getDescriptorUrl() {
        return descriptor.getDescriptorUrl();
    }
}
