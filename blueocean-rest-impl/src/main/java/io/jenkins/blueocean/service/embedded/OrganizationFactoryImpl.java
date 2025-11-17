package io.jenkins.blueocean.service.embedded;

import hudson.Extension;
import hudson.model.ItemGroup;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of {@link OrganizationFactory} for a controller is to have everything in
 * one organization called "jenkins"
 *
 * @author Kohsuke Kawaguchi
 */
@Extension(ordinal=-100)    // low ordinal to ensure this comes in the very last
public class OrganizationFactoryImpl extends OrganizationFactory {
    private static final String ORGANIZATION_NAME = StringUtils.defaultIfBlank(
            System.getProperty("BLUE_ORGANIZATION_NAME"),"jenkins");

    private static final String ROOT_FOLDER_NAME = System.getProperty("BLUE_ORGANIZATION_ROOT_FOLDER");

    private static final Logger LOG = Logger.getLogger(OrganizationFactoryImpl.class.getName());

    /**
     * In embedded mode, there's only one organization
     */
    private final OrganizationImpl instance;

    public OrganizationFactoryImpl() {
        this(ORGANIZATION_NAME);
    }

    public OrganizationFactoryImpl(String name) {
        if (ROOT_FOLDER_NAME != null) {
            var root = Jenkins.get().getItemByFullName(ROOT_FOLDER_NAME);
            if (root instanceof ItemGroup<?> group) {
                this.instance = new OrganizationImpl(name, group);
            } else {
                LOG.warning(() -> "Specified BLUE_ORGANIZATION_ROOT_FOLDER '" + ROOT_FOLDER_NAME "' not found, or not a Folder. Falling back to Jenkins root folder.");
                this.instance = new OrganizationImpl(name, Jenkins.get());
            }
        } else {
            this.instance = new OrganizationImpl(name, Jenkins.get());
        }
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
        return Collections.singletonList(instance);
    }

    @Override
    public OrganizationImpl of(ItemGroup group) {
        return group == instance.getGroup() ? instance : null;
    }
}
