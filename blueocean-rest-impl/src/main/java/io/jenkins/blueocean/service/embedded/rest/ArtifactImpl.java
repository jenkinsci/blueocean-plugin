package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueArtifact;
import org.kohsuke.stapler.Stapler;

public class ArtifactImpl extends BlueArtifact {
    final private Run run;
    final private Run.Artifact artifact;
    public ArtifactImpl(Run run, Run.Artifact artifact) {
        this.run = run;
        this.artifact = artifact;
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
        return Stapler.getCurrentRequest().getContextPath() +
            "/" + run.getUrl()+"artifact/"+ artifact.getHref();
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
        return new Link(getUrl());
    }
}
