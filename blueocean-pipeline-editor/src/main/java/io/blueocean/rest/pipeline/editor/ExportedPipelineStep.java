package io.blueocean.rest.pipeline.editor;

import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        return descriptor.getProvidedContext()
            .stream().map(Class::getName)
            .collect(Collectors.toList());
    }

    /**
     * The Java class names that this pipeline requires to be in context
     */
    @Exported
    public List<String> getRequiredContext() {
        return descriptor.getRequiredContext()
            .stream().map(Class::getName)
            .collect(Collectors.toList());
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
