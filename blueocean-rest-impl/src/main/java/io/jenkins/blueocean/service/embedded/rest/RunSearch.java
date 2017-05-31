package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.util.RunList;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.OmniSearch;
import io.jenkins.blueocean.rest.Query;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;
import jenkins.model.Jenkins;
import jenkins.model.lazy.LazyBuildMixIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Search API for Run
 *
 * @author Vivek Pandey
 */
@Extension
public class RunSearch extends OmniSearch<BlueRun> {
    @Override
    public String getType() {
        return "run";
    }

    @Override
    public Pageable<BlueRun> search(Query q) {

        String pipeline = q.param("pipeline", false);

        boolean latestOnly = q.param("latestOnly", Boolean.class);

        if(pipeline != null){
            TopLevelItem p = Jenkins.getActiveInstance().getItem(pipeline);
            if(latestOnly){
                BlueRun r = getLatestRun((Job)p);
                if(r != null) {
                    return Pageables.wrap(Collections.singletonList(r));
                }else{
                    Pageables.empty();
                }
            }
            if (p instanceof Job) {
                return Pageables.wrap(findRuns((Job)p));
            }else{
                throw new ServiceException.BadRequestException(String.format("Pipeline %s not found", pipeline));
            }
        }else if(latestOnly){
            return Pageables.empty();
        }
        return Pageables.wrap(findRuns(null));
    }

    public static Iterable<BlueRun> findRuns(Job job, final Link parent){
        final List<BlueRun> runs = new ArrayList<>();
        Iterable<Job> pipelines;
        if(job != null){
            pipelines = ImmutableList.of(job);
        }else{
            pipelines = Jenkins.getActiveInstance().getItems(Job.class);
        }
        for (Job p : pipelines) {
            RunList<? extends Run> runList = p.getBuilds();

            for (Run r : runList) {
                runs.add(AbstractRunImpl.getBlueRun(r, new Reachable() {
                    @Override
                    public Link getLink() {
                        return parent;
                    }
                }));
            }
        }

        return runs;
    }

    @SuppressWarnings("unchecked")
    public static Iterable<BlueRun> findRuns(Job job, final Link parent, int start, int limit){
        final List<BlueRun> runs = new ArrayList<>();
        Iterable<Job> pipelines;
        if(job != null){
            pipelines = ImmutableList.of(job);
        }else{
            pipelines = Jenkins.getInstance().getItems(Job.class);
        }

        for (Job p : pipelines) {
            Iterator<? extends Run> runIterator;
            if (job instanceof LazyBuildMixIn.LazyLoadingJob) {
                final LazyBuildMixIn lazyLoadMixin = ((LazyBuildMixIn.LazyLoadingJob) job).getLazyBuildMixIn();
                runIterator = lazyLoadMixin.getRunMap().iterator();

            }else{
                runIterator = p.getBuilds().iterator();

            }
            runs.addAll(collectRuns(runIterator, parent, start, limit));
        }

        return runs;
    }

    private static List<BlueRun> collectRuns(Iterator<? extends Run> runIterator, final Link parent, int start, int limit){
        List<BlueRun> runs = new ArrayList<>();
        int skipCount = start; // Skip up to the start
        while (runIterator.hasNext()) {
            if (skipCount > 0) {
                runIterator.next();
                skipCount--;
            } else {
                runs.add(AbstractRunImpl.getBlueRun(runIterator.next(), new Reachable() {
                    @Override
                    public Link getLink() {
                        return parent;
                    }
                }));
            }
            if (runs.size() >= limit) {
                return runs;
            }
        }
        return runs;
    }




    public static Iterable<BlueRun> findRuns(Job pipeline){
        return findRuns(pipeline, null);
    }

    private BlueRun getLatestRun(Job job){
        if(job != null){
            Run r = job.getLastBuild();
            if(r != null) {
                AbstractRunImpl.getBlueRun(r, new PipelineContainerImpl().get(job.getFullName()));
            }
        }
        return null;
    }

}
