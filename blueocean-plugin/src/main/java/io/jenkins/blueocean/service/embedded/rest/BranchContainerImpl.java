package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Job;
import io.jenkins.blueocean.rest.model.BlueBranch;
import io.jenkins.blueocean.rest.model.BlueBranchContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class BranchContainerImpl extends BlueBranchContainer {
    private final MultiBranchBluePipeline pipeline;

    public BranchContainerImpl(MultiBranchBluePipeline pipeline) {
        this.pipeline = pipeline;
    }

    //TODO: implement rest of the methods
    @Override
    public BlueBranch get(String name) {
        Job job = pipeline.mbp.getBranch(name);
        if(job != null){
            return new BranchImpl(pipeline, job);
        }
        return null;
    }

    @Override
    public Iterator<BlueBranch> iterator() {
        List<BlueBranch> branches = new ArrayList<>();
        Collection<Job> jobs = pipeline.mbp.getAllJobs();
        for(Job j: jobs){
            branches.add(new BranchImpl(pipeline, j));
        }
        return branches.iterator();
    }
}
