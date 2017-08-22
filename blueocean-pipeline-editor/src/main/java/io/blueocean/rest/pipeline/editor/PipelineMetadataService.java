package io.blueocean.rest.pipeline.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Describable;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.agent.DeclarativeAgent;
import org.jenkinsci.plugins.pipeline.modeldefinition.agent.DeclarativeAgentDescriptor;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.model.BuildCondition;
import org.jenkinsci.plugins.structs.SymbolLookup;
import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.stapler.NoStaplerConstructorException;
import org.kohsuke.stapler.verb.GET;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Descriptor;
import hudson.security.csrf.CrumbIssuer;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.ApiRoutable;
import jenkins.model.Jenkins;

import javax.annotation.CheckForNull;

/**
 * This provides and Blueocean REST API endpoint to obtain pipeline step metadata.
 * 
 * TODO: this should be provided off of the organization endpoint:
 * e.g. /organization/:id/pipeline-metadata
 */
@Extension
public class PipelineMetadataService implements ApiRoutable {

    final static List<String> INCLUDED_ADVANCED_STEPS = Collections.unmodifiableList(Arrays.asList("catchError"));

    @Override
    public String getUrlName() {
        return "pipeline-metadata";
    }

    @GET
    public String doCrumbInfo() {
        CrumbIssuer crumbIssuer = Jenkins.getInstance().getCrumbIssuer();
        if (crumbIssuer != null) {
            return crumbIssuer.getCrumbRequestField()  + "=" + crumbIssuer.getCrumb();
        }
        return "";
    }

    /**
     * Function to return all {@link DeclarativeAgent}s present in the system when accessed through the REST API
     */
    @GET
    @TreeResponse
    public ExportedDescribableModel[] doAgentMetadata() {
        List<ExportedDescribableModel> models = new ArrayList<>();

        for (DeclarativeAgentDescriptor d : DeclarativeAgentDescriptor.all()) {
            try {
                DescribableModel<? extends DeclarativeAgent> model = new DescribableModel<>(d.clazz);

                models.add(new ExportedDescribableModel(model, symbolForObject(d)));
            } catch (NoStaplerConstructorException e) {
                // Ignore!
            }
        }
        return models.toArray(new ExportedDescribableModel[models.size()]);
    }

    /**
     * Function to return all {@link ExportedToolDescriptor}s present in the system when accessed through the REST API,
     * pipeline scripts need: symbol and name to specify tools
     */
    @GET
    @TreeResponse
    public ExportedToolDescriptor[] doToolMetadata() {
        List<ExportedToolDescriptor> models = new ArrayList<>();
        for (ToolDescriptor<? extends ToolInstallation> d : ToolInstallation.all()) {
            ExportedToolDescriptor descriptor = new ExportedToolDescriptor(d.getDisplayName(), symbolForObject(d), d.getClass());
            models.add(descriptor);
            for (ToolInstallation installation : d.getInstallations()) {
                descriptor.addInstallation(new ExportedToolDescriptor.ExportedToolInstallation(installation.getName(), installation.getClass()));
            }
        }
        return models.toArray(new ExportedToolDescriptor[models.size()]);
    }

    /**
     * Function to return the names of all build conditions present in the system when accessed through the REST API
     */
    @GET
    @TreeResponse
    public ExportedBuildCondition[] doBuildConditions() {
        List<ExportedBuildCondition> exported = new ArrayList<>();
        for (BuildCondition c : BuildCondition.all()) {
            exported.add(new ExportedBuildCondition(symbolForObject(c), c.getDescription()));
        }

        Collections.sort(exported, new Comparator<ExportedBuildCondition>() {
            @Override
            public int compare(ExportedBuildCondition o1, ExportedBuildCondition o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return exported.toArray(new ExportedBuildCondition[exported.size()]);
    }


    /**
     * Function to return all applicable step descriptors for the "wrappers" section.
     */
    @GET
    @TreeResponse
    public ExportedPipelineStep[] doWrapperMetadata() {
        List<ExportedPipelineStep> wrappers = new ArrayList<>();

        for (StepDescriptor d : StepDescriptor.all()) {
            if (isWrapper(d)) {
                ExportedPipelineStep step = getStepMetadata(d);
                if (step != null) {
                    wrappers.add(step);
                }
            }
        }

        return wrappers.toArray(new ExportedPipelineStep[wrappers.size()]);
    }

    /**
     * Function to return all step descriptors present in the system when accessed through the REST API
     */
    @GET
    @TreeResponse
    public ExportedPipelineFunction[] doPipelineStepMetadata() {
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

    private boolean isWrapper(StepDescriptor d) {
        return includeStep(d)
                && d.takesImplicitBlockArgument()
                && !d.getRequiredContext().contains(FilePath.class)
                && !d.getRequiredContext().contains(Launcher.class);
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
            if (SimpleBuildStep.class.isAssignableFrom(d.clazz) && symbolForObject(d) != null) {
                r.add(d);
            }
        }
    }

    private @CheckForNull String symbolForObject(Object d) {
        Set<String> symbols = SymbolLookup.getSymbolValue(d);
        if (!symbols.isEmpty()) {
            return symbols.iterator().next();
        } else {
            return null;
        }
    }

    private @CheckForNull ExportedPipelineFunction getStepMetadata(Descriptor<?> d) {
        String symbol = symbolForObject(d);

        if (symbol != null) {
            ExportedPipelineFunction f = new ExportedPipelineFunction(new DescribableModel<>(d.clazz), symbol);
            // Let any decorators adjust the step properties
            for (ExportedDescribableParameterDecorator decorator : ExtensionList.lookup(ExportedDescribableParameterDecorator.class)) {
                decorator.decorate(f, f.getParameters());
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
                decorator.decorate(step, step.getParameters());
            }

            return step;
        } catch (NoStaplerConstructorException e) {
            // not a normal step?
            return null;
        }
    }
}
