package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.model.FreeStyleBuild;
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
            if (p instanceof Job) {
                return Pageables.wrap(findRuns((Job)p,latestOnly));
            }else{
                throw new ServiceException.BadRequestExpception(String.format("Pipeline %s not found", pipeline));
            }
        }
        return Pageables.wrap(findRuns(null,latestOnly));
    }

    public static Iterable<BlueRun> findRuns(Job pipeline, boolean latestOnly){
        final List<BlueRun> runs = new ArrayList<>();
        List<Job> pipelines;
        if(pipeline != null){
            pipelines = ImmutableList.of(pipeline);
        }else{
            pipelines = Jenkins.getActiveInstance().getItems(Job.class);
        }
        for (Job p : pipelines) {
            if (latestOnly) {
                hudson.model.Run r = p.getLastBuild();
                if(r != null) {
                    if (r.getClass().getSimpleName().equals(FreeStyleBuild.class.getSimpleName())) {
                        runs.add(new FreeStyleRun(r));
                    }
                }else{
                    Pageables.empty();
                }
            }else{
                RunList<? extends Run> runList = p.getBuilds();

                Iterator<? extends Run> iterator = runList.iterator();
                while (iterator.hasNext()) {
                    hudson.model.Run r = iterator.next();
                    if (r.getClass().getSimpleName().equals(FreeStyleBuild.class.getSimpleName())) {
                        runs.add(new FreeStyleRun(r));//TODO: fix this, there are other run types
                    }
                }
            }
        }
        return runs;
    }
}
