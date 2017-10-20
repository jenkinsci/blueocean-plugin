package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.Job;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.Resource;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_FREESTYLE_PROJECT;

@Capability(JENKINS_FREESTYLE_PROJECT)
public class FreeStylePipeline extends AbstractPipelineImpl {
    private FreeStylePipeline(BlueOrganization organization, Job job) {
        super(organization, job);
    }

    @Extension(ordinal = 1)
    public static class FactoryImpl extends BluePipelineFactory {
        @Override
        public BluePipeline getPipeline(Item item, Reachable parent, BlueOrganization organization) {
            if (item instanceof FreeStyleProject) {
                FreeStyleProject job = (FreeStyleProject)item;
                return new FreeStylePipeline(organization, job);
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target, BlueOrganization organization) {
            if(context == target && target instanceof FreeStyleProject) {
                return getPipeline(target, parent, organization);
            }
            return null;
        }
    }
}
