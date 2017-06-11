package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMNavigator;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbProject;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerProject;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepositoryContainer;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMNavigator;
import org.kohsuke.stapler.export.Exported;

/**
 * @author Vivek Pandey
 */
public class BitbucketProject extends ScmOrganization {
    private final Link self;
    private final BbProject project;
    private final BitbucketApi api;

    public BitbucketProject(BbProject project, BitbucketApi api, Link parent) {
        this.self = parent.rel(project.getKey());
        this.project = project;
        this.api = api;
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public String getName() {
        return project.getName();
    }

    @Exported(name="key")
    public String getKey(){
        return project.getKey();
    }

    @Override
    public String getAvatar() {
        return project.getAvatar();
    }

    @Override
    public boolean isJenkinsOrganizationPipeline() {
        for(TopLevelItem item: Jenkins.getInstance().getItems()){
            if(item instanceof OrganizationFolder){
                OrganizationFolder folder = (OrganizationFolder) item;
                for(SCMNavigator navigator: folder.getNavigators()) {
                    if (navigator instanceof BitbucketSCMNavigator) {
                        BitbucketSCMNavigator scmNavigator = (BitbucketSCMNavigator) navigator;
                        if(scmNavigator.getRepoOwner().equals(getName())){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ScmRepositoryContainer getRepositories() {
        return new BitbucketRepositoryContainer(project, api, this);
    }
}