package io.jenkins.blueocean.rest.factory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.Resource;
import jenkins.model.Jenkins;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Factory that gives instance of {@link BluePipeline}
 *
 * It's useful for example in cases where a plugin that has custom project and they want to serve
 * extra meta-data thru BluePipeline, would provide implementation of their BluePipeline and and implementation
 * of BluePipelineFactory.
 *
 * @author Vivek Pandey
 */
public abstract class BluePipelineFactory implements ExtensionPoint {
    /**
     * @param item to convert into a pipeline
     * @param parent of the item
     * @param organization the item is a child of
     * @return resolved pipeline or null
     */
    public abstract BluePipeline getPipeline(Item item, Reachable parent, BlueOrganization organization);

    /**
     * Finds a blue ocean API model object that pairs up with the given {@code target},
     * by looking at the intermediate core model object {@code context} that is an ancestor
     * of {@code target}.
     *
     * If this {@link BluePipelineFactory} understands how to map {@code context} to
     * {@link BluePipeline} (as in {@code getPipeline(item,parent)!=null}), then the resolve
     * method should also apply the same logic to map {@code context} and then recursively
     * resolve {@code target}.
     *
     * @param context
     *      This is always an ancestor of target (including target==context)
     * @param parent
     *      The parent object of the blue ocean API model object that pairs up with the 'context' parameter
     * @param target
     *      The core model object that we are trying to map to {@link Resource}
     * @param organization
     *      The organization that the context and target are a child of
     *
     * @return
     *      null if this implementation doesn't know how to map {@code context} to a blue ocean API model object.
     *      Otherwise return the BO API model object that pairs up with {@code target}
     */
    public abstract Resource resolve(Item context, Reachable parent, Item target, BlueOrganization organization);

    public static ExtensionList<BluePipelineFactory> all(){
        return ExtensionList.lookup(BluePipelineFactory.class);
    }

    /**
     * Given a Job in Jenkins, map that to corresponding blue ocean API object,
     * for example so that you can get its URL.
     */
    public static Resource resolve(Item item) {
        BlueOrganization org = OrganizationFactory.getInstance().getContainingOrg(item);
        if (org == null) {
            return null;
        }
        Item nextStep = findNextStep(Jenkins.getInstance(), item);

        for (BluePipelineFactory f : all()) {
            Resource r = f.resolve(nextStep, org.getPipelines(), item, org);
            if (r!=null)    return r;
        }
        return null;
    }

    /**
     * Resolves a BluePipeline by fullName
     * @param organization to search
     * @param fullName to find
     * @return optional
     */
    public static Optional<BluePipeline> resolve(BlueOrganization organization, String fullName) {
        return StreamSupport.stream(organization.getPipelines().spliterator(), false)
            .filter(input -> input.getFullName().equals(fullName)).findFirst();
    }

    /**
     * Returns the immediate child of 'context' that is also the ancestor of 'target'
     */
    @SuppressFBWarnings(value = "EC_UNRELATED_TYPES_USING_POINTER_EQUALITY", justification = "Folder/MBP etc. are Item and ItemGroup")
    protected static Item findNextStep(ItemGroup context, Item target) {
        Item i = null;
        while (context!=target) {
            i = target;
            if (target.getParent() instanceof Item) {
                target = (Item) target.getParent();
            } else {
                break;
            }
        }
        return i == null ? target : i;
    }

    /**
     * Gives {@link BluePipeline} instance from the first pipeline found.
     *
     *
     * @param item {@link Item} for which corresponding BlueOcean API object needs to be found. Must implement
     *                         {@link TopLevelItem} for return to be not null
     * @param parent Parent {@link Reachable} object
     * @return {@link BluePipeline} if a map of item to BlueOcean API found, null otherwise.
     *
     */
    public static BluePipeline getPipelineInstance(Item item, final Reachable parent){
        if(!(item instanceof TopLevelItem)) {
            return null;
        }
        BlueOrganization organization = OrganizationFactory.getInstance().getContainingOrg(item);
        if (organization == null) {
            return null;
        }
        for(BluePipelineFactory factory:BluePipelineFactory.all()){
            BluePipeline pipeline = factory.getPipeline(item, parent, organization);

            if(pipeline != null){
                return pipeline;
            }
        }
        return null;
    }
}
