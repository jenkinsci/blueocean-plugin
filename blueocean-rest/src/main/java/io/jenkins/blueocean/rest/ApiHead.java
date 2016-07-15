package io.jenkins.blueocean.rest;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.ExtensionList;
import io.jenkins.blueocean.BlueOceanUI;
import io.jenkins.blueocean.RootRoutable;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;
import io.jenkins.blueocean.rest.pageable.PagedResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;

import java.util.HashMap;
import java.util.Map;

/**
 * Entrypoint for blueocean REST apis. $CONTEXT_PATH/rest being root. e.g. /jenkins/rest
 *
 * @author Vivek Pandey
 */
@Extension
public final class ApiHead implements RootRoutable, Reachable  {

    @Inject
    private BlueOceanUI blueOceanUI;

    private final Map<String,ApiRoutable> apis = new HashMap<>();

    public ApiHead() {
        for ( ApiRoutable api : ExtensionList.lookup(ApiRoutable.class)) {
            apis.put(api.getUrlName(),api);
        }
    }

    /**
     * Search API
     *
     * Gives collection starting from start parameter value with max numbers &lt;= limit
     *
     *
     * @param query query object parsed from value of q URL query parameter
     *
     * @return Gives pageable collection always non-null
     */
    @WebMethod(name="search") @GET @PagedResponse
    public Pageable<?> search(@QueryParameter("q") Query query) {
        for (OmniSearch os : OmniSearch.all()) {
            if (os.getType().equals(query.type))
                return os.search(query);
        }
        return Pageables.empty();
    }

    /**
     * This {@link ApiHead} gets bound to "/rest"
     */
    @Override
    public String getUrlName() {
        return "rest";
    }

    /**
     * Exposes all {@link ApiRoutable}s to URL space.
     *
     * @param route current URL route handled by ApiHead
     * @return {@link ApiRoutable} object
     */
    public ApiRoutable getDynamic(String route) {
        StaplerRequest request = Stapler.getCurrentRequest();
        String m = request.getMethod();
        if(m.equalsIgnoreCase("POST") || m.equalsIgnoreCase("PUT") || m.equalsIgnoreCase("PATCH")) {
            String header = request.getHeader("Content-Type");
            if(header == null || !header.contains("application/json")) {
                throw new ServiceException(415, "Content-Type: application/json required");
            }
        }
        return apis.get(route);
    }

    @Override
    public Link getLink() {
        return new Link("/"+blueOceanUI.getUrlBase()).rel(getUrlName());
    }

    /**
     * Gives instance of ApiHead by looking in to Extensions. In some cases it might be null, such as when jenkins is
     * booting up.
     */
    public static ApiHead INSTANCE(){
        ExtensionList<ApiHead> extensionList = ExtensionList.lookup(ApiHead.class);
        if(!extensionList.isEmpty()){
            return extensionList.get(0); //ApiHead is singleton
        }

        return null;
    }
}
