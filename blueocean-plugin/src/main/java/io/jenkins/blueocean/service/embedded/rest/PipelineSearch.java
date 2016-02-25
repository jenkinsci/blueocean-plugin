package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import hudson.model.Project;
import io.jenkins.blueocean.rest.OmniSearch;
import io.jenkins.blueocean.rest.Query;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;
import io.jenkins.blueocean.rest.sandbox.BOPipeline;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
@Extension
public class PipelineSearch extends OmniSearch<BOPipeline>{

    @Override
    public String getType() {
        return "pipeline";
    }

    @Override
    public Pageable<BOPipeline> search(Query q) {
        List<Project> projects = Jenkins.getActiveInstance().getAllItems(Project.class);
        final List<BOPipeline> pipelines = new ArrayList<>();
        String pipeline = q.param(getType());
        for (Project project : projects) {
            if (pipeline != null && !project.getName().equals(pipeline)) {
                continue;
            }
            pipelines.add(new PipelineImpl(project));
        }

        return Pageables.wrap(pipelines);
    }
}
