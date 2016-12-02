package io.blueocean.rest.pipeline.editor;

import hudson.ExtensionList;
import hudson.model.Descriptor;
import org.jenkinsci.plugins.structs.describable.DescribableParameter;
import org.jenkinsci.plugins.structs.describable.ParameterType;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Exportable form of {@link DescribableParameter}
 */
@ExportedBean
public class ExportedDescribableParameter {
    protected final DescribableParameter param;

    public ExportedDescribableParameter(DescribableParameter param) {
        this.param = param;
    }

    /**
     * Name of the parameter
     * See {@link DescribableParameter#getName()}
     */
    @Exported
    public String getName() {
        return param.getName();
    }

    /**
     * Indicates this parameter is required
     * See {@link DescribableParameter#isRequired()}
     */
    @Exported
    public boolean getIsRequired() {
        return param.isRequired();
    }

    /**
     * Java class name for the parameter
     * See {@link DescribableParameter#getErasedType()}
     */
    @Exported
    public String getType() {
        return param.getErasedType().getName();
    }

    /**
     * Java types allowed if this is a collection
     * See {@link DescribableParameter#getType()} and {@link ParameterType#getActualType()}
     */
    @Exported
    public List<String> getCollectionTypes() {
        List<String> collectionTypes = new ArrayList<>();

        Type typ = param.getType().getActualType();
        if (typ instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) typ).getActualTypeArguments();
            for (Type ptyp : typeArgs) {
                if (ptyp instanceof Class<?>) {
                    collectionTypes.add(((Class<?>) ptyp).getName());
                }
            }
        }
        return collectionTypes;
    }

    /**
     * Capitalized name of the parameter
     * See {@link DescribableParameter#getCapitalizedName()}
     */
    @Exported
    public String getCapitalizedName() {
        return param.getCapitalizedName();
    }

    /**
     * Indicates this parameter is deprecated
     * See {@link DescribableParameter#isDeprecated()}
     */
    @Exported
    public boolean getIsDeprecated() {
        return param.isDeprecated();
    }

    /**
     * Help HTML (in English locale) for this parameter if available, else null
     * See {@link DescribableParameter#getHelp()}
     */
    @Exported
    public String getHelp() throws IOException {
        return param.getHelp();
    }

    @Exported
    public String getDescriptorUrl() {
        Descriptor<?> pd = Descriptor.findByDescribableClassName(ExtensionList.lookup(Descriptor.class),
                param.getErasedType().getName());

        if (pd != null) {
            return pd.getDescriptorUrl();
        }

        return null;
    }
}
