package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Queue;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Utils;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.service.embedded.rest.QueueItemImpl;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static io.jenkins.blueocean.rest.impl.pipeline.PipelineRunImpl.LATEST_RUN_START_TIME_COMPARATOR;

/**
 * @author Vivek Pandey
 */
public class MultibranchPipelineRunContainer extends BlueRunContainer{
    private static final int MAX_MBP_RUNS_ROWS = Integer.getInteger("MAX_MBP_RUNS_ROWS", 250);

    private final MultiBranchPipelineImpl blueMbPipeline;
    private final Link self;

    public MultibranchPipelineRunContainer(MultiBranchPipelineImpl pipeline) {
        this.blueMbPipeline = pipeline;
        this.self = pipeline.getLink().rel("runs");
    }

    @Override
    public Link getLink() {
        return self;
    }


    @Override
    public BlueRun get(String name) {
        return null;
    }

    @Override
    public Iterator<BlueRun> iterator() {
        throw new ServiceException.NotImplementedException("Not implemented");
    }

    /**
     * Fetches maximum up to  MAX_MBP_RUNS_ROWS rows from each branch and does pagination on that.
     *
     * JVM property MAX_MBP_RUNS_ROWS can be used to tune this value to optimize performance for given setup
     */
    @Override
    public Iterator<BlueRun> iterator(int start, int limit) {
        List<BlueRun> c = new ArrayList<>();

        List<BluePipeline> branches;

        // Check for branch filter
        StaplerRequest req = Stapler.getCurrentRequest();
        String branchFilter = null;
        if (req != null) {
            branchFilter = req.getParameter("branch");
        }

        if (!StringUtils.isEmpty(branchFilter)) {
            BluePipeline pipeline = blueMbPipeline.getBranches().get(branchFilter);
            if (pipeline != null) {
                branches = Collections.singletonList(pipeline);
            } else {
                branches = Collections.emptyList();
            }
        } else {
            branches = Lists.newArrayList(blueMbPipeline.getBranches().list());
            sortBranchesByLatestRun(branches);
        }

        for (final BluePipeline b : branches) {
            BlueRunContainer blueRunContainer = b.getRuns();
            if(blueRunContainer==null){
                continue;
            }
            Iterator<BlueRun> it = blueRunContainer.iterator(0, MAX_MBP_RUNS_ROWS);
            int count = 0;
            Utils.skip(it, start);
            while (it.hasNext() && count++ < limit) {
                c.add(it.next());
            }
        }

        Collections.sort(c, LATEST_RUN_START_TIME_COMPARATOR);

        return Iterators.limit(c.iterator(), limit);
    }

    static void sortBranchesByLatestRun(List<BluePipeline> branches) {
        Collections.sort(branches, ( o1, o2 ) ->
            LATEST_RUN_START_TIME_COMPARATOR.compare(o1.getLatestRun(), o2.getLatestRun()));
    }

    private boolean retry(boolean[] retries) {
        //if at least one of the branch needs retry we will retry it
        for (boolean r : retries) {
            if (r) {
                return true;
            }
        }
        return false;
    }

    private int computeLimit(boolean[] retries, int limit) {
        //if at least one of the branch needs retry we will retry it
        int count = 0;
        for (boolean r : retries) {
            if (r) {
                count++;
            }
        }
        if (count == 0) {
            return 0;
        }
        return limit / count > 0 ? limit / count : 1;
    }

    private int collectRuns(List<BluePipeline> branches, List<BlueRun> runs,
                            boolean[] retries, int remainingCount, int[] startIndexes, int[] limits) {
        int count = 0;
        for (int i = 0; i < branches.size(); i++) {
            BluePipeline b = branches.get(i);
            if (!retries[i]) {
                continue;
            }
            BlueRunContainer blueRunContainer = b.getRuns();
            if(blueRunContainer==null){
                continue;
            }
            Iterator<BlueRun> it = blueRunContainer.iterator(startIndexes[i], limits[i]);
            int lcount = 0;
            while (it.hasNext() && count < remainingCount) {
                lcount++;
                count++;
                runs.add(it.next());
            }
            if (lcount < limits[i]) { //if its less than l
                retries[i] = false; //iterator already exhausted so lets not retry next time
            } else {
                startIndexes[i] = startIndexes[i] + lcount; //set the new start index for next time
            }
        }
        return count;
    }

    @Override
    public BlueRun create(StaplerRequest request) {
        blueMbPipeline.mbp.checkPermission(Item.BUILD);
        Queue.Item queueItem = blueMbPipeline.mbp.scheduleBuild2(0, new CauseAction(new Cause.UserIdCause()));
        if(queueItem == null){ // possible mbp.isBuildable() was false due to no sources fetched yet
            return null;
        }
        return new QueueItemImpl(
                blueMbPipeline.getOrganization(),
                queueItem,
                blueMbPipeline,
                1
        ).toRun();
    }

}
