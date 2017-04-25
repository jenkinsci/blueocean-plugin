package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.model.ItemGroup;
import io.jenkins.blueocean.rest.factory.OrganizationResolver;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;

/**
 * Default implementation of {@link OrganizationResolver} for a master is to have everything in
 * one organization called "jenkins"
 *
 * @author Kohsuke Kawaguchi
 */
@Extension(ordinal=-100)    // low ordinal to ensure this comes in the very last
public class OrganizationResolverImpl extends OrganizationResolver {
    private static final String ORGANIZATION_NAME = StringUtils.defaultIfBlank(
            System.getProperty("BLUE_ORGANIZATION_NAME"),"jenkins");

    /**
     * In embedded mode, there's only one organization
     */
    private final OrganizationImpl instance;

    public OrganizationResolverImpl() {
        this(ORGANIZATION_NAME);
    }

    public OrganizationResolverImpl(String name) {
        this.instance = new OrganizationImpl(name, Jenkins.getInstance());
    }

    @Override
    public BlueOrganization get(String name) {
        if (instance.getName().equals(name))
            return instance;
        else
            return null;
    }

    @Override
    public Collection<BlueOrganization> list() {
        return ImmutableList.<BlueOrganization>of(instance);
    }

    @Override
    public OrganizationImpl of(ItemGroup group) {
        return group == instance.getGroup() ? instance : null;
    }
}
