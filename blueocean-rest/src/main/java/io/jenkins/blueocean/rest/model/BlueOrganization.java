package io.jenkins.blueocean.rest.model;

import hudson.ExtensionList;
import io.jenkins.blueocean.Routable;
import io.jenkins.blueocean.rest.ApiRoutable;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.export.Exported;

import java.util.HashMap;
import java.util.Map;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_ORGANIZATION;

/**
 * API endpoint for an organization that houses all the pipelines.
 *
 * @author Kohsuke Kawaguchi
 */
@Capability(BLUE_ORGANIZATION)
public abstract class BlueOrganization extends Resource implements Routable{
    public static final String NAME="name";
    public static final String PIPELINES="pipelines";

    private final Map<String, ApiRoutable> apis = new HashMap<>();

    public BlueOrganization() {
        for(ApiRoutable api: ExtensionList.lookup(ApiRoutable.class)){
            if(api.isParent(this)) {
                apis.put(api.getUrlName(), api);
            }
        }
    }

    @Override
    public String getUrlName() {
        return null;
    }

    @Exported(name = NAME)
    public abstract String getName();

    @Navigable
    //   /organizations/jenkins/piplelines/f1
    public abstract BluePipelineContainer getPipelines();

    /**
     * A set of users who belong to this organization.
     *
     * @return {@link BlueUserContainer}
     */
    @Navigable
    public abstract BlueUserContainer getUsers();

    /**
     *  Gives currently authenticated user
     *
     * @return {@link BlueUser}
     */
    @Navigable
    public abstract BlueUser getUser();

    public ApiRoutable getDynamic(String route){
        return apis.get(route);
    }
}

