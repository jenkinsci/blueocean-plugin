package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepositoryContainer;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;

/**
 * @author Vivek Pandey
 */
public class BitbucketOrg extends ScmOrganization {
    private final Link self;
    private final BbOrg project;
    private final BitbucketApi api;

    public BitbucketOrg(BbOrg project, BitbucketApi api, Link parent) {
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
            if(item instanceof MultiBranchProject){
                MultiBranchProject folder = (MultiBranchProject) item;
                for(Object source: folder.getSCMSources()) {
                    if (source instanceof BitbucketSCMSource) {
                        BitbucketSCMSource scmNavigator = (BitbucketSCMSource) source;
                        if(scmNavigator.getRepoOwner().equals(getKey())){
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