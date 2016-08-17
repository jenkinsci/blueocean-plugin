package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineImpl;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

/**
 * @author Kohsuke Kawaguchi
 */
@Capability({"org.jenkinsci.plugins.workflow.job.WorkflowJob"})
public class PipelineImpl extends AbstractPipelineImpl {
    protected PipelineImpl(Job job) {
        super(job);
    }

    @Extension(ordinal = 1)
    public static class PipelineFactoryImpl extends BluePipelineFactory {

        @Override
        public BluePipeline getPipeline(Item item, Reachable parent) {
            if (item instanceof WorkflowJob) {
                return new PipelineImpl((Job) item);
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target) {
            if(context == target && target instanceof WorkflowJob) {
                return getPipeline(target,parent);
            }
            return null;
        }
    }
}
