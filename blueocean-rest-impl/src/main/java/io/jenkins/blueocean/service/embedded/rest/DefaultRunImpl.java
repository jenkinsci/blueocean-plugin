package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueRunFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Catch all for any run implementation
 */
@Restricted(NoExternalUse.class)
public final class DefaultRunImpl extends AbstractRunImpl {
    public DefaultRunImpl(Run run, Reachable parent, BlueOrganization organization) {
        super(run, parent, organization);
    }

    @Extension(ordinal = -1)
    public static class FactoryImpl extends BlueRunFactory {
        @Override
        public BlueRun getRun(Run run, Reachable parent, BlueOrganization organization) {
            return new DefaultRunImpl(run,  parent, organization);
        }
    }
}
