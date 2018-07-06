package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.Extension;
import hudson.matrix.MatrixProject;
import hudson.model.Item;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.factory.BlueRunFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.PipelineFolderImpl;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_MATRIX_PROJECT;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.NO_FAVORITE;

/**
 * @author Vivek Pandey
 */
@Capability({JENKINS_MATRIX_PROJECT, NO_FAVORITE})
public class MatrixProjectImpl extends PipelineFolderImpl {

    private final MatrixProject matrixProject;

    public MatrixProjectImpl(BlueOrganization organization, MatrixProject folder, Link parent) {
        super(organization, folder, parent);
        this.matrixProject = folder;
    }

    @Extension(ordinal = 1)
    public static class PipelineFactoryImpl extends BluePipelineFactory{

        @Override
        public MatrixProjectImpl getPipeline(Item item, Reachable parent, BlueOrganization organization) {
            if (item instanceof MatrixProject) {
                return new MatrixProjectImpl(organization, (MatrixProject) item, parent.getLink());
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target, BlueOrganization organization) {
            MatrixProjectImpl project = getPipeline(context, parent, organization);
            if (project!=null) {
                if(context == target){
                    return project;
                }
                Item nextChild = findNextStep(project.matrixProject,target);
                for (BluePipelineFactory f : all()) {
                    Resource answer = f.resolve(nextChild, project, target, organization);
                    if (answer!=null)
                        return answer;
                }
            }
            return null;
        }
    }

    @Override
    public BluePipeline getDynamic(String name) {
        return null;
    }

    @Override
    public BluePipelineContainer getPipelines() {
        return null;
    }

    @Override
    public Integer getNumberOfFolders() {
        return 0;
    }

    @Override
    public Integer getNumberOfPipelines() {
        return 0;
    }

    @Override
    public Link getLink() {
        return new Link("/"+ matrixProject.getUrl());
    }

    @Override
    public BlueRunContainer getRuns() {
        return null; //TODO: matrix build have run but we are not returning any for now. to be fixed when full matrix build support is defined.
    }

    @Override
    public BlueRun getLatestRun() {
        return matrixProject.getLastBuild() == null ? null : BlueRunFactory.getRun(matrixProject.getLastBuild(), this);
    }
}
