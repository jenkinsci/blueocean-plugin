package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import hudson.model.Job;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.pageable.PagedResponse;
import io.jenkins.blueocean.service.embedded.rest.ContainerFilter;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class BranchContainerImpl extends BluePipelineContainer {


    /**
     * Orders by:
     * - Default branch
     * - Favourites ordered by last run time
     * - All other branches ordered by last run time
     */
    private static final Comparator<BluePipeline> BRANCH_COMPARATOR = new Comparator<BluePipeline>() {
        @Override
        public int compare(BluePipeline _pipeline1, BluePipeline _pipeline2) {
            BranchImpl pipeline1 = (BranchImpl)_pipeline1;
            BranchImpl pipeline2 = (BranchImpl)_pipeline2;

            BranchImpl.Branch branch1 = pipeline1.getBranch();
            if (branch1 == null) {
                return -1;
            }

            BranchImpl.Branch branch2 = pipeline2.getBranch();
            if (branch2 == null) {
                return 1;
            }

            // If one pipeline isnt the primary there is no need to go further
            if(branch1.isPrimary() && !branch2.isPrimary()) {
                return -1;
            }

            if(!branch1.isPrimary() && branch2.isPrimary()) {
                return 1;
            }

            // If One pipeline isnt a favorite there is no need to go further.
            if(pipeline1.isFavorite() && !pipeline2.isFavorite()) {
                return -1;
            }

            if(!pipeline1.isFavorite() && pipeline2.isFavorite()) {
                return 1;
            }

            BlueRun latestRun1 = pipeline1.getLatestRun();
            BlueRun latestRun2 = pipeline2.getLatestRun();

            // If a pipeline doesn't have a run yet, no need to go further.
            if(latestRun1 != null && latestRun2 == null) {
                return -1;
            }

            if(latestRun1 == null && latestRun2 != null) {
                return 1;
            }

            //If neither have runs, lets just order by name.
            if(latestRun1 == null) {
                return pipeline1.getName().compareTo(pipeline2.getName());
            }

            // If one run hasnt finished yet, then lets order by that.
            Date endTime1 = latestRun1.getEndTime() ;
            Date endTime2 = latestRun2.getEndTime();
            if(endTime1 != null && endTime2 == null) {
                return 1;
            }

            if(endTime1 == null && endTime2 != null) {
                return -1;
            }

            // If both jobs have ended, lets order by the one that ended last.
            if(endTime1 != null) {
                if(endTime1.getTime() > endTime2.getTime()) {
                    return -1;
                }

                if(endTime1.getTime() < endTime2.getTime()) {
                    return 1;
                }

                return pipeline1.getName().compareTo(pipeline2.getName());
            }

            //If both jobs have not ended yet, we need to order by start time.
            Date startTime1 = latestRun1.getStartTime();
            Date startTime2 = latestRun2.getStartTime();
            if(startTime1 != null && startTime2 == null) {
                return 1;
            }

            if(startTime1 == null && startTime2 != null) {
                return -1;
            }

            if(startTime1 != null && startTime2 != null) {
                if(startTime1.getTime() > startTime2.getTime()) {
                    return -1;
                }

                if(startTime1.getTime() < startTime2.getTime()) {
                    return 1;
                }

                return pipeline1.getName().compareTo(pipeline2.getName());
            }

            return pipeline1.getName().compareTo(pipeline2.getName());
        }
    };

    private final MultiBranchPipelineImpl pipeline;
    private final Link self;

    public BranchContainerImpl(MultiBranchPipelineImpl pipeline, Link self) {
        super(pipeline.getOrganization());
        this.pipeline = pipeline;
        this.self = self;
    }

    //TODO: implement rest of the methods
    @Override
    public BluePipeline get(String name) {
        Job job = pipeline.mbp.getItem(name);
        if (job == null) {
            return null;
        }
        BlueOrganization organization = OrganizationFactory.getInstance().getContainingOrg(job);
        if (organization == null) {
            return null;
        }
        return new BranchImpl(organization, job, getLink());
    }

    @Override
    public Iterator<BluePipeline> iterator() {
        return iterator(0, PagedResponse.DEFAULT_LIMIT);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<BluePipeline> iterator(int start, int limit) {
        final BlueOrganization organization = OrganizationFactory.getInstance().getContainingOrg(pipeline.mbp.getItemGroup());
        if (organization == null) {
            throw new ServiceException.UnexpectedErrorException("Could not find organization for " + pipeline.mbp.getFullName());
        }
        final Link link = getLink();
        // Filter will decide if the requester wants branches or pull requests
        Collection allJobsMatchinFilter = ContainerFilter.filter(pipeline.mbp.getAllJobs());
        // Transform all of these to branches (these represent branches or pull requests)
        Iterable<BluePipeline> branches = Iterables.transform(allJobsMatchinFilter, new Function<Job, BluePipeline>() {
            @Override
            public BluePipeline apply(Job input) {
                return new BranchImpl(organization, input, link);
            }
        });
        // Order them using the comparator
        branches = Ordering.from(BRANCH_COMPARATOR).sortedCopy(branches);
        // Return the page requested by the client
        return Iterables.limit(Iterables.skip(branches, start), limit).iterator();
    }

    @Override
    public Link getLink() {
        return self;
    }
}
