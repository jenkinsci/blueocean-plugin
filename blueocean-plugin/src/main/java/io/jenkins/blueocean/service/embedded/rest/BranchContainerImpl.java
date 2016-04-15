package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Job;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class BranchContainerImpl extends BluePipelineContainer {
    private final MultiBranchPipelineImpl pipeline;

    public BranchContainerImpl(MultiBranchPipelineImpl pipeline) {
        this.pipeline = pipeline;
    }

    //TODO: implement rest of the methods
    @Override
    public BluePipeline get(String name) {
        Job job = pipeline.mbp.getBranch(name);
        if(job != null){
            return new BranchImpl(job);
        }
        return null;
    }

    @Override
    public Iterator<BluePipeline> iterator() {
        List<BluePipeline> branches = new ArrayList<>();
        Collection<Job> jobs = pipeline.mbp.getAllJobs();
        for(Job j: jobs){
            branches.add(new BranchImpl(j));
        }
        return branches.iterator();
    }
}
