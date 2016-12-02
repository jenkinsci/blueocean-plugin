package io.blueocean.rest.pipeline.editor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import hudson.model.Describable;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.structs.SymbolLookup;
import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.jenkinsci.plugins.structs.describable.DescribableParameter;
import org.jenkinsci.plugins.workflow.cps.Snippetizer;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.stapler.NoStaplerConstructorException;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.verb.GET;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Descriptor;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.ApiRoutable;
import jenkins.model.Jenkins;

import javax.annotation.CheckForNull;

/**
 * This provides and Blueocean REST API endpoint to obtain pipeline step metadata.
 * 
 * TODO: this should be provided off of the organization endpoint:
 * e.g. /organization/:id/pipeline-step-metadata
 */
@Extension
public class PipelineMetadataService implements ApiRoutable {

    final static List<String> INCLUDED_ADVANCED_STEPS = Collections.unmodifiableList(Arrays.asList("catchError"));

    ParameterNameDiscoverer nameFinder = new LocalVariableTableParameterNameDiscoverer();

    @Override
    public String getUrlName() {
        return "pipeline-step-metadata";
    }

    /**
     * Function to return all step descriptors present in the system when accessed through the REST API
     */
    @GET
    @WebMethod(name = "")
    @TreeResponse
    public ExportedPipelineFunction[] getPipelineStepMetadata() {
        Jenkins j = Jenkins.getInstance();

        List<ExportedPipelineFunction> pd = new ArrayList<>();
        // POST to this with parameter names
        // e.g. json:{"time": "1", "unit": "NANOSECONDS", "stapler-class": "org.jenkinsci.plugins.workflow.steps.TimeoutStep", "$class": "org.jenkinsci.plugins.workflow.steps.TimeoutStep"}

        for (StepDescriptor d : StepDescriptor.all()) {
            if (includeStep(d)) {
                ExportedPipelineStep step = getStepMetadata(d);
                if (step != null) {
                    pd.add(step);
                }
            }
        }

        List<Descriptor<?>> metaStepDescriptors = new ArrayList<Descriptor<?>>();
        populateMetaSteps(metaStepDescriptors, Builder.class);
        populateMetaSteps(metaStepDescriptors, Publisher.class);

        for (Descriptor<?> d : metaStepDescriptors) {
            ExportedPipelineFunction metaStep = getStepMetadata(d);
            if (metaStep != null) {
                pd.add(metaStep);
            }
        }

        return pd.toArray(new ExportedPipelineFunction[pd.size()]);
    }

    private boolean includeStep(StepDescriptor d) {
        boolean include = true;
        if (ModelASTStep.getBlockedSteps().containsKey(d.getFunctionName())) {
            include = false;
        } else if (d.isAdvanced()
                && !INCLUDED_ADVANCED_STEPS.contains(d.getFunctionName())) {
            include = false;
        }

        return include;
    }

    private <T extends Describable<T>,D extends Descriptor<T>> void populateMetaSteps(List<Descriptor<?>> r, Class<T> c) {
        Jenkins j = Jenkins.getInstance();
        for (Descriptor<?> d : j.getDescriptorList(c)) {
            if (SimpleBuildStep.class.isAssignableFrom(d.clazz) && symbolForDescriptor(d) != null) {
                r.add(d);
            }
        }
    }

    private @CheckForNull String symbolForDescriptor(Descriptor<?> d) {
        Set<String> symbols = SymbolLookup.getSymbolValue(d);
        if (!symbols.isEmpty()) {
            return symbols.iterator().next();
        } else {
            return null;
        }
    }

    private @CheckForNull ExportedPipelineFunction getStepMetadata(Descriptor<?> d) {
        String symbol = symbolForDescriptor(d);

        if (symbol != null) {
            ExportedPipelineFunction f = new ExportedPipelineFunction(new DescribableModel<>(d.clazz), symbol);
            // Let any decorators adjust the step properties
            for (ExportedDescribableParameterDecorator decorator : ExtensionList.lookup(ExportedDescribableParameterDecorator.class)) {
                decorator.decorate(f, Arrays.asList(f.getParameters()));
            }

            return f;
        } else {
            return null;
        }
    }

    private @CheckForNull ExportedPipelineStep getStepMetadata(StepDescriptor d) {
        try {
            DescribableModel<? extends Step> model = new DescribableModel<>(d.clazz);

            ExportedPipelineStep step = new ExportedPipelineStep(model, d.getFunctionName(), d);

            // Let any decorators adjust the step properties
            for (ExportedDescribableParameterDecorator decorator : ExtensionList.lookup(ExportedDescribableParameterDecorator.class)) {
                decorator.decorate(step, Arrays.asList(step.getParameters()));
            }

            return step;
        } catch (NoStaplerConstructorException e) {
            // not a normal step?
            return null;
        }
    }
}
