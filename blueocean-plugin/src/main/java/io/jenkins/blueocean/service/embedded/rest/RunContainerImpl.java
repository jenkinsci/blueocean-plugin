package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.FreeStyleBuild;
import hudson.model.Job;
import hudson.util.RunList;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.sandbox.BORun;
import io.jenkins.blueocean.rest.sandbox.BOPipeline;
import io.jenkins.blueocean.rest.sandbox.BORunContainer;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class RunContainerImpl extends BORunContainer {

    private final BOPipeline pipeline;

    public RunContainerImpl(BOPipeline pipeline) {
        this.pipeline = pipeline;
    }


    @Override
    public BORun get(String name) {
        List<Job> projects = Jenkins.getActiveInstance().getAllItems(Job.class);
        for (Job p : projects) {
            if (!p.getName().equals(pipeline.getName())) {
                continue;
            }
            RunList<? extends hudson.model.Run> runList = p.getBuilds();

            hudson.model.Run run = null;
            if (name != null) {
                for (hudson.model.Run r : runList) {
                    if (r.getId().equals(name)) {
                        run = r;
                        break;
                    }
                }
                if (run == null) {
                    throw new ServiceException.NotFoundException(
                        String.format("Run %s not found in organization %s and pipeline %s",
                            name, pipeline.getOrganization(), pipeline.getName()));
                }
            } else {
                run = runList.getLastBuild();
            }
            return new FreeStyleRun(run);
        }
        throw new ServiceException.NotFoundException(String.format("Run id %s not found for organization %s, pipeline: %s",
            name, pipeline.getOrganization(), pipeline.getName()));
    }

    @Override
    public Iterator<BORun> iterator() {
        List<BORun> runs = new ArrayList<>();
        List<Job> projects = Jenkins.getActiveInstance().getAllItems(Job.class);
        for (Job p : projects) {
            if (pipeline != null && !p.getName().equals(pipeline.getName())) {
                continue;
            }

            RunList<? extends hudson.model.Run> runList = p.getBuilds();

            Iterator<? extends hudson.model.Run> iterator = runList.iterator();
            while (iterator.hasNext()) {
                hudson.model.Run r = iterator.next();
                if (r.getClass().getSimpleName().equals(FreeStyleBuild.class.getSimpleName())) {
                    runs.add(new FreeStyleRun(r));//TODO: fix this, there are other run types
                }
            }
        }
        return runs.iterator();
    }

    @Override
    public BOPipeline getPipeline(String name) {
        return pipeline;
    }
}
