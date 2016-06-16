package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import hudson.model.Item;
import io.jenkins.blueocean.rest.OmniSearch;
import io.jenkins.blueocean.rest.Query;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
@Extension
public class PipelineSearch extends OmniSearch<BluePipeline>{

    @Override
    public String getType() {
        return "pipeline";
    }

    @Override
    public Pageable<BluePipeline> search(Query q) {
        final Iterator<BluePipeline> pipelineIterator = PipelineContainerImpl
            .getPipelines(Jenkins.getActiveInstance().getAllItems(Item.class));
        final List<BluePipeline> pipelines = new ArrayList<>();
        String pipeline = q.param(getType());
        if(pipeline == null) {
            return Pageables.wrap(new Iterable<BluePipeline>() {
                @Override
                public Iterator<BluePipeline> iterator() {
                    return pipelineIterator;
                }
            });
        }else{
            while (pipelineIterator.hasNext()) {
                BluePipeline p = pipelineIterator.next();
                if (!p.getName().equals(pipeline)) {
                    continue;
                }
                pipelines.add(p);
            }
            return Pageables.wrap(pipelines);
        }
    }
}
