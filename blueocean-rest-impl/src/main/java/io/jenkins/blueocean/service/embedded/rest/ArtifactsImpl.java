package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueArtifactContainer;
import io.jenkins.blueocean.rest.model.BlueArtifacts;
import org.kohsuke.stapler.Stapler;

public class ArtifactsImpl extends BlueArtifacts {
    private Link self;
    private Run run;
    public ArtifactsImpl(Run run, Reachable parent) {
        this.run = run;
        this.self = parent.getLink().rel("artifacts");
    }

    @Override
    public String getZipFile() {
        if(run.getHasArtifacts()) {
            return Stapler.getCurrentRequest().getContextPath() + "/" + run.getUrl()+"artifact/*zip*/archive.zip";
        }

        return null;
    }

    @Override
    public BlueArtifactContainer getArtifacts() {
        return new ArtifactContainerImpl(run, this);
    }

    @Override
    public Link getLink() {
        return self;
    }
}
