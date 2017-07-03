package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import io.jenkins.blueocean.rest.model.Container;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author cliffmeyers
 */
public class GithubEnterpriseOrganizationContainer extends Container<ScmOrganization> {

    private final Link link;
    private final Map<String, ScmOrganization> orgs;

    public GithubEnterpriseOrganizationContainer(Container<ScmOrganization> orgContainer) {
        this.link = orgContainer.getLink();
        this.orgs = new HashMap<>();

        Iterator<ScmOrganization> iterator = orgContainer.iterator();
        while (iterator.hasNext()) {
            ScmOrganization scmOrg = iterator.next();
            this.orgs.put(scmOrg.getName(), new GithubEnterpriseOrganization(scmOrg));
        }
    }

    @Override
    public ScmOrganization get(String name) {
        ScmOrganization org = orgs.get(name);
        if(org == null){
            throw new ServiceException.NotFoundException(String.format("GitHub Enterprise organization %s not found", name));
        }
        return org;
    }

    @Override
    public Link getLink() {
        return link;
    }

    @Override
    public Iterator<ScmOrganization> iterator() {
        return orgs.values().iterator();
    }
}
