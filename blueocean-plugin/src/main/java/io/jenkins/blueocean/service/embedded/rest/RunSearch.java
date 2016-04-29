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
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
                throw new ServiceException.BadRequestExpception(String.format("Pipeline %s not found", pipeline));
            }
        }else if(latestOnly){
            return Pageables.empty();
        }
        return Pageables.wrap(findRuns(null));
    }

    public static Iterable<BlueRun> findRuns(Job pipeline){
        final List<BlueRun> runs = new ArrayList<>();
        Iterable<Job> pipelines;
        if(pipeline != null){
            pipelines = ImmutableList.of(pipeline);
        }else{
            pipelines = Jenkins.getActiveInstance().getItems(Job.class);
        }
        for (Job p : pipelines) {
            RunList<? extends Run> runList = p.getBuilds();

            for (Run r : runList) {
                runs.add(AbstractRunImpl.getBlueRun(r));
            }
        }

        return runs;
    }

    private BlueRun getLatestRun(Job job){
        if(job != null){
            Run r = job.getLastBuild();
            if(r != null) {
                AbstractRunImpl.getBlueRun(r);
            }
        }
        return null;
    }

}
