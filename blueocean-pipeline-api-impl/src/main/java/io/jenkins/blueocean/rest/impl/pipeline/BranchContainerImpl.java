package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.collect.Ordering;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.CaseInsensitiveComparator;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.ContainerFilter;
import org.joda.time.DateTimeComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class BranchContainerImpl extends BluePipelineContainer {

    private static final Comparator<Job> BRANCH_COMPARITOR = new Comparator<Job>() {
        @Override
        public int compare(Job pipeline1, Job pipeline2) {
            long endTime1 = getDate(pipeline1);
            long endTime2 = getDate(pipeline2);
            if (endTime1 > endTime2) {
                return -1;
            }
            if (endTime1 < endTime2) {
                return 1;
            }
            return 0;
        }

        long getDate(Job pipeline) {
            Run latestRun = pipeline.getLastBuild();
            Date date = latestRun != null ? new Date(latestRun.getStartTimeInMillis() + latestRun.getDuration()) : null;
            return latestRun != null ? date.getTime() : 0;
        }
    };

    private final MultiBranchPipelineImpl pipeline;
    private final Link self;

    public BranchContainerImpl(MultiBranchPipelineImpl pipeline, Link self) {
        this.pipeline = pipeline;
        this.self = self;
    }
    //TODO: implement rest of the methods
    @Override
    public BluePipeline get(String name) {
        Job job = pipeline.mbp.getBranch(name);
        if(job != null){
            return new BranchImpl(job, getLink());
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<BluePipeline> iterator() {
        List<BluePipeline> branches = new ArrayList<>();
        Collection<Job> jobs = Ordering.from(BRANCH_COMPARITOR).sortedCopy(pipeline.mbp.getAllJobs());
        jobs = ContainerFilter.filter(jobs);
        for(Job j: jobs){
            branches.add(new BranchImpl(j, getLink()));
        }
        return branches.iterator();
    }

    @Override
    public Link getLink() {
        return self;
    }
}
