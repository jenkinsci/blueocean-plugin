package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Job;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * @author Vivek Pandey
 */
public class BranchContainerImpl extends BluePipelineContainer {
    private final MultiBranchPipelineImpl pipeline;
    private final Link self;
    private final Predicate<Job> filter;

    public BranchContainerImpl(MultiBranchPipelineImpl pipeline, Link self) {
        this(pipeline, self, Predicates.<Job>alwaysTrue());
    }
    
    public BranchContainerImpl(MultiBranchPipelineImpl pipeline, Link self, Predicate<Job> filter) {
        this.pipeline = pipeline;
        this.self = self;
        this.filter = filter;
    }
    
    //TODO: implement rest of the methods
    @Override
    public BluePipeline get(String name) {
        Job job = pipeline.mbp.getBranch(name);
        if(job != null && filter.apply(job)){
            return new BranchImpl(job, getLink());
        }
        return null;
    }

    @Override
    public Iterator<BluePipeline> iterator() {
        List<BluePipeline> branches = new ArrayList<>();
        Collection<Job> jobs = pipeline.mbp.getAllJobs();
        for(Job j: jobs) {
            if (j != null && filter.apply(j)) {
                branches.add(new BranchImpl(j, getLink()));
            }
        }
        return branches.iterator();
    }

    @Override
    public Link getLink() {
        return self;
    }
}
