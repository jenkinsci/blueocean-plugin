package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueTestSummary;
import io.jenkins.blueocean.rest.model.Resource;
import java.util.Collection;
import java.util.Collections;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_FREESTYLE_PROJECT;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_FREE_STYLE_BUILD;

@Capability(JENKINS_FREESTYLE_PROJECT)
public class FreeStylePipeline extends AbstractPipelineImpl {
    private final Job job;
    private FreeStylePipeline(BlueOrganization organization, Job job) {
        super(organization, job);
        this.job = job;
    }

    @Override
    public BlueRun getLatestRun() {
        Run run = job.getLastBuild();
        if(run instanceof FreeStyleBuild){
            BlueRun blueRun = new FreeStyleRunImpl((FreeStyleBuild) run, this, organization);
            return new FreeStyleRunSummary(blueRun, run, this, organization);
        }
        return super.getLatestRun();
    }

    @Capability(JENKINS_FREE_STYLE_BUILD)
    static class FreeStyleRunSummary extends AbstractRunImpl{
        private final BlueRun blueRun;
        public FreeStyleRunSummary(BlueRun blueRun, Run run, Reachable parent, BlueOrganization organization) {
            super(run, parent, organization);
            this.blueRun = blueRun;
        }

        @Override
        public Collection<BlueActionProxy> getActions() {
            return Collections.emptyList();
        }

        @Override
        public BlueTestSummary getTestSummary() {
            return null;
        }

        @Override
        public String getCauseOfBlockage() {
            return blueRun.getCauseOfBlockage();
        }

        @Override
        public BlueRunState getStateObj() {
            return blueRun.getStateObj();
        }
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
