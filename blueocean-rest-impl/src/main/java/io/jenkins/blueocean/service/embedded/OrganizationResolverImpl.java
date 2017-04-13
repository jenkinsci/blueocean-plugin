package io.jenkins.blueocean.service.embedded;

import hudson.Extension;
import hudson.model.ItemGroup;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.Collections;

/**
 * Default implementation of {@link OrganizationResolver} for a master is to have everything in
 * one organization called "jenkins"
 *
 * @author Kohsuke Kawaguchi
 */
@Extension(ordinal=-100)    // low ordinal to ensure this comes in the very last
public class OrganizationResolverImpl extends OrganizationResolver {
    /**
     * In embedded mode, there's only one organization
     */
    private final OrganizationImpl instance;

    public OrganizationResolverImpl() {
        this("jenkins");
    }

    public OrganizationResolverImpl(String name) {
        this.instance = new OrganizationImpl(name, Jenkins.getInstance());
    }

    @Override
    public OrganizationImpl get(String name) {
        if (instance.getName().equals(name))
            return instance;
        else
            return null;
    }

    @Override
    public Collection<OrganizationImpl> list() {
        return Collections.singleton(instance);
    }

    @Override
    public OrganizationImpl of(ItemGroup group) {
        return group == instance.getGroup() ? instance : null;
    }
}
