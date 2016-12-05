package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueArtifact;
import org.kohsuke.stapler.Stapler;

public class ArtifactImpl extends BlueArtifact {
    final private Run run;
    final private Run.Artifact artifact;
    final private Link self;
    public ArtifactImpl(Run run, Run.Artifact artifact, Reachable parent) {
        this.run = run;
        this.artifact = artifact;
        this.self = parent.getLink().rel(this.getPath());

    }
    @Override
    public String getName() {
        return artifact.getFileName();
    }

    @Override
    public String getPath() {
        return artifact.relativePath;
    }

    @Override
    public String getUrl() {
        return "/" + run.getUrl()+"artifact/"+ artifact.getHref();
    }

    @Override
    public long getSize() {
        try {
            return artifact.getFileSize();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public Link getLink() {
        return self;
    }
}
