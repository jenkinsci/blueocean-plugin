package io.jenkins.blueocean.rest.impl.pipeline.scm;

import hudson.Extension;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueOrganizationContainer;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.service.embedded.rest.OrganizationAction;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Iterator;

/**
 *
 * SCM container.
 *
 * @author Vivek Pandey
 */
@Extension
@ExportedBean
public class ScmContainer extends Container<Scm> implements OrganizationAction {
    private final Link self;

    private static final String URL_NAME="scm";

    public ScmContainer() {
        BlueOrganization organization= BlueOrganizationContainer.getBlueOrganization();
        this.self = (organization != null) ? organization.getLink().rel("scm")
                : new Link("/organizations/jenkins/scm/");
    }

    public Scm get(String name){
        return ScmFactory.resolve(name, this);
    }

    @Override
    public Iterator<Scm> iterator() {
        return ScmFactory.resolve(this).iterator();
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return URL_NAME;
    }
}
