package io.blueocean.rest.pipeline.editor;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.security.csrf.CrumbIssuer;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.ApiRoutable;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import jenkins.tasks.SimpleBuildWrapper;
import org.jenkinsci.plugins.pipeline.modeldefinition.agent.DeclarativeAgent;
import org.jenkinsci.plugins.pipeline.modeldefinition.agent.DeclarativeAgentDescriptor;
import org.jenkinsci.plugins.pipeline.modeldefinition.model.BuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.validator.BlockedStepsAndMethodCalls;
import org.jenkinsci.plugins.structs.SymbolLookup;
import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.stapler.NoStaplerConstructorException;
import org.kohsuke.stapler.verb.GET;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This provides and Blueocean REST API endpoint to obtain pipeline step metadata.
 *
 * TODO: this should be provided off of the organization endpoint:
 * e.g. /organization/:id/pipeline-metadata
 */
@Extension
public class PipelineMetadataService implements ApiRoutable {

    final static List<String> INCLUDED_ADVANCED_STEPS = Collections.unmodifiableList(Arrays.asList("catchError", "container"));

    @Override
    public String getUrlName() {
        return "pipeline-metadata";
    }

    @GET
    public String doCrumbInfo() {
        CrumbIssuer crumbIssuer = Jenkins.get().getCrumbIssuer();
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

                String symbol = symbolForObject(d);
                if ("label".equals(symbol)) { // Label has 2 symbols, but we need "node"
                    symbol = "node";
                }
                models.add(new ExportedDescribableModel(model, symbol));
            } catch (NoStaplerConstructorException e) {
                // Ignore!
            }
        }
        return models.toArray(new ExportedDescribableModel[0]);
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
        return models.toArray(new ExportedToolDescriptor[0]);
    }

    /**
     * Function to return the names of all build conditions present in the system when accessed through the REST API
     */
    @GET
    @TreeResponse
    public ExportedBuildCondition[] doBuildConditions() {
        List<ExportedBuildCondition> exported =
            BuildCondition.all().stream()
                .map(c -> new ExportedBuildCondition(symbolForObject(c), c.getDescription()))
            .collect( Collectors.toList());

        exported.sort(Comparator.comparing(ExportedBuildCondition::getName));
        return exported.toArray(new ExportedBuildCondition[0]);
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
        populateMetaSteps(metaStepDescriptors, BuildWrapper.class);

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
        if (BlockedStepsAndMethodCalls.blockedInSteps().containsKey(d.getFunctionName())) {
            include = false;
        } else if (d.isAdvanced()
                && !INCLUDED_ADVANCED_STEPS.contains(d.getFunctionName())) {
            include = false;
        }

        return include;
    }

    private <T extends Describable<T>,D extends Descriptor<T>> void populateMetaSteps(List<Descriptor<?>> r, Class<T> c) {
        Jenkins j = Jenkins.get();
        for (Descriptor<?> d : j.getDescriptorList(c)) {
            if (SimpleBuildStep.class.isAssignableFrom(d.clazz) && symbolForObject(d) != null) {
                r.add(d);
            } else if (SimpleBuildWrapper.class.isAssignableFrom(d.clazz) && symbolForObject(d) != null) {
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
