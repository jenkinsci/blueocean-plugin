package io.blueocean.rest.pipeline.editor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.jenkinsci.plugins.structs.describable.DescribableParameter;
import org.jenkinsci.plugins.workflow.cps.Snippetizer;
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

/**
 * This provides and Blueocean REST API endpoint to obtain pipeline step metadata.
 * 
 * TODO: this should be provided off of the organization endpoint:
 * e.g. /organization/:id/pipeline-step-metadata
 */
@Extension
public class PipelineStepMetadataService implements ApiRoutable {
    ParameterNameDiscoverer nameFinder = new LocalVariableTableParameterNameDiscoverer();

    @Override
    public String getUrlName() {
        return "pipeline-step-metadata";
    }

    /**
     * Basic exported model for {@link PipelineStepMetadata}
     */
    @ExportedBean
    public static class BasicPipelineStepMetadata implements PipelineStepMetadata {
        private String displayName;
        private String functionName;
        private Class<?> type;
        private String descriptorUrl;
        private List<Class<?>> requiredContext = new ArrayList<Class<?>>();
        private List<Class<?>> providedContext = new ArrayList<Class<?>>();
        private boolean isWrapper = false;
        private boolean hasSingleRequiredParameter = false;
        private String snippetizerUrl;
        private List<PipelineStepPropertyMetadata> props = new ArrayList<PipelineStepPropertyMetadata>();

        public BasicPipelineStepMetadata(String functionName, Class<?> type, String displayName) {
            super();
            this.displayName = displayName;
            this.type = type;
            this.functionName = functionName;
        }

        @Exported
        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Exported
        @Override
        public String getFunctionName() {
            return functionName;
        }

        @Exported
        @Override
        public String[] getRequiredContext() {
            List<String> out = new ArrayList<String>();
            for (Class<?> c : requiredContext) {
                out.add(c.getName());
            }
            return out.toArray(new String[out.size()]);
        }

        @Exported
        @Override
        public String[] getProvidedContext() {
            List<String> out = new ArrayList<String>();
            for (Class<?> c : providedContext) {
                out.add(c.getName());
            }
            return out.toArray(new String[out.size()]);
        }

        @Exported
        @Override
        public String getSnippetizerUrl() {
            return snippetizerUrl;
        }

        @Exported
        public String descriptorUrl() {
            return descriptorUrl;
        }

        @Exported
        @Override
        public boolean getIsBlockContainer() {
            return isWrapper;
        }

        @Exported
        @Override
        public String getType() {
            return type.getName();
        }

        @Exported
        @Override
        public boolean getHasSingleRequiredParameter() {
            return hasSingleRequiredParameter;
        }

        @Exported
        @Override
        public PipelineStepPropertyMetadata[] getProperties() {
            return props.toArray(new PipelineStepPropertyMetadata[props.size()]);
        }
    }

    /**
     * Basic exported model for {@link PipelineStepPropertyMetadata)
     */
    @ExportedBean
    public static class BasicPipelineStepPropertyMetadata implements PipelineStepPropertyMetadata {
        private String name;
        private Class<?> type;
        private List<Class<?>> collectionTypes = new ArrayList<>();
        private boolean isRequired = false;
        private String descriptorUrl;

        @Exported
        @Override
        public String getName() {
            return name;
        }

        @Exported
        @Override
        public String getType() {
            return type.getName();
        }

        @Exported
        @Override
        public boolean getIsRequired() {
            return isRequired;
        }

        @Exported
        public String[] getCollectionTypes() {
            String[] types = new String[collectionTypes.size()];
            for (int i = 0; i < types.length; i++) {
                types[i] = collectionTypes.get(i).getName();
            }
            return types;
        }

        @Exported
        public String descriptorUrl() {
            return descriptorUrl;
        }
    }

    /**
     * Function to return all step descriptors present in the system when accessed through the REST API
     */
    @GET
    @WebMethod(name = "")
    @TreeResponse
    public PipelineStepMetadata[] getPipelineStepMetadata() {
        Jenkins j = Jenkins.getInstance();
        Snippetizer snippetizer = ExtensionList.create(j, Snippetizer.class).get(0);

        List<PipelineStepMetadata> pd = new ArrayList<PipelineStepMetadata>();
        // POST to this with parameter names
        // e.g. json:{"time": "1", "unit": "NANOSECONDS", "stapler-class": "org.jenkinsci.plugins.workflow.steps.TimeoutStep", "$class": "org.jenkinsci.plugins.workflow.steps.TimeoutStep"}
        String snippetizerUrl = Stapler.getCurrentRequest().getContextPath() + "/" + snippetizer.getUrlName() + "/generateSnippet";

        for (StepDescriptor d : StepDescriptor.all()) {
            PipelineStepMetadata step = getStepMetadata(d, snippetizerUrl);
            if (step != null) {
                pd.add(step);
            }
        }

        return pd.toArray(new PipelineStepMetadata[pd.size()]);
    }

    private PipelineStepMetadata getStepMetadata(StepDescriptor d, String snippetizerUrl) {
        BasicPipelineStepMetadata step = new BasicPipelineStepMetadata(d.getFunctionName(), d.clazz, d.getDisplayName());

        try {
            DescribableModel<?> model = new DescribableModel<>(step.type);

            step.snippetizerUrl = snippetizerUrl + "?$class=" + d.clazz.getName(); // this isn't really accurate

            step.isWrapper = d.takesImplicitBlockArgument();
            step.requiredContext.addAll(d.getRequiredContext());
            step.providedContext.addAll(d.getProvidedContext());
            step.descriptorUrl = d.getDescriptorFullUrl();
            step.hasSingleRequiredParameter = model.hasSingleRequiredParameter();

            for (DescribableParameter descParam : model.getParameters()) {
                
                BasicPipelineStepPropertyMetadata param = new BasicPipelineStepPropertyMetadata();

                param.type = descParam.getErasedType();
                Type typ = descParam.getType().getActualType();
                if (typ instanceof ParameterizedType) {
                    Type[] typeArgs = ((ParameterizedType)typ).getActualTypeArguments();
                    for (Type ptyp : typeArgs) {
                        if (ptyp instanceof Class<?>) {
                            param.collectionTypes.add((Class<?>)ptyp);
                        }
                    }
                }
                param.name = descParam.getName();
                param.isRequired = descParam.isRequired();

                Descriptor<?> pd = Descriptor.findByDescribableClassName(ExtensionList.lookup(Descriptor.class),
                        param.type.getName());

                if (pd != null) {
                    param.descriptorUrl = pd.getDescriptorFullUrl();
                }
                
                step.props.add(param);
            }

            // Let any decorators adjust the step properties
            for (PipelineStepPropertyDecorator decorator : ExtensionList.lookup(PipelineStepPropertyDecorator.class)) {
                decorator.decorate(step, step.props);
            }
        } catch (NoStaplerConstructorException e) {
            // not a normal step?
            return null;
        }

        return step;
    }
}
