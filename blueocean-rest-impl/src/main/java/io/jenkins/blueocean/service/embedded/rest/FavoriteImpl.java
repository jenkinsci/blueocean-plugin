package io.jenkins.blueocean.service.embedded.rest;

import hudson.Util;
import hudson.model.Item;
import hudson.model.Job;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import jenkins.branch.MultiBranchProject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

/**
 * @author Vivek Pandey
 */
public class FavoriteImpl extends BlueFavorite {

    private final Object item;
    private final Link self;

    public FavoriteImpl(Item item, Reachable parent) {
        this.self = parent.getLink().rel(Util.rawEncode(item.getFullName()));


        // TODO: MBP nested inside folder won't work nor would a favorited folder is going to work
        //TODO: Needs https://issues.jenkins-ci.org/browse/JENKINS-36286 to be fixed,
        //      it should use LinkResolver for it to work in all cases
        Object obj = null;
        if(item instanceof WorkflowJob){
            if(item.getParent() instanceof MultiBranchProject){
                Link s = OrganizationImpl.INSTANCE.getLink().rel(String.format("pipelines/%s/branches/",
                    ((MultiBranchProject) item.getParent()).getName()));
                obj = new BranchImpl((Job) item, s);
            }
        }
        if(obj == null){
            this.item = new PipelineImpl((Job) item);
        }else{
            this.item = obj;
        }
    }

    @Override
    public Object getItem() {
        return item;
    }

    @Override
    public Link getLink() {
        return self;
    }

}
