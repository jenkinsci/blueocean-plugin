package io.jenkins.blueocean.rest.impl.pipeline.scm;

import hudson.Extension;
import io.jenkins.blueocean.rest.OrganizationRoute;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.Container;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
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
public class ScmContainer extends Container<Scm> implements OrganizationRoute {
    private final Link self;

    private static final String URL_NAME="scm";

    public ScmContainer() {
        Ancestor ancestor = Stapler.getCurrentRequest2().findAncestor(BlueOrganization.class);
        BlueOrganization organization = null;
        if(ancestor != null){
            organization = (BlueOrganization) ancestor.getObject();
        }
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
    public String getUrlName() {
        return URL_NAME;
    }
}
