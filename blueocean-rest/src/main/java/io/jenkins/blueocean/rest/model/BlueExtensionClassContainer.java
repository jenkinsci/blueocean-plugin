package io.jenkins.blueocean.rest.model;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.ApiRoutable;
import io.jenkins.blueocean.rest.Reachable;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.POST;

import java.util.List;
import java.util.Map;

/**
 * Map representation of {@link BlueExtensionClass}es
 *
 * @author Vivek Pandey
 */
public abstract class BlueExtensionClassContainer implements ApiRoutable, ExtensionPoint, Reachable {

    /**
     * Gives {@link BlueExtensionClass} for the given class name
     *
     * @param name name of the class
     * @return {@link BlueExtensionClass} for the given class name, null if not found
     */
    public abstract BlueExtensionClass get(String name);

    public final Object getDynamic(String name) {
        return get(name);
    }

    /**
     * Gives Map of given class in the query to {@link BlueExtensionClass}
     *
     * @param request POST body with query element with value as list of classes e.g.
     *                {'q':['class1', 'class2', 'class3']}
     *
     *
     * @return Map of given class in the query to {@link BlueExtensionClass}. If given class in the parameter is not
     *         known then 400, BadRequest should be returned
     */
    @POST
    @WebMethod(name= "")
    @TreeResponse
    public abstract BlueExtensionClassMap getMap(@JsonBody Map<String,List<String>> request);


    @Override
    public String getUrlName() {
        return "classes";
    }
}



