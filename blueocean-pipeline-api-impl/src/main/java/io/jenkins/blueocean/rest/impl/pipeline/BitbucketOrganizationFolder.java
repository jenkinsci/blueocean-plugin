package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMNavigator;
import com.google.common.collect.Iterables;
import hudson.Extension;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import jenkins.scm.api.SCMNavigator;

public class BitbucketOrganizationFolder extends OrganizationFolder {

    public BitbucketOrganizationFolder(jenkins.branch.OrganizationFolder folder, Link parent) {
        super(folder, parent);
    }

    @Extension
    public static class OrganizationFolderFactoryImpl extends OrganizationFolderFactory {
        @Override
        protected OrganizationFolder getFolder(jenkins.branch.OrganizationFolder folder, Reachable parent) {
            SCMNavigator navigator = Iterables.getFirst(folder.getNavigators(), null);
            return BitbucketSCMNavigator.class.isInstance(navigator) ? new BitbucketOrganizationFolder(folder, parent.getLink()) : null;
        }
    }
}
