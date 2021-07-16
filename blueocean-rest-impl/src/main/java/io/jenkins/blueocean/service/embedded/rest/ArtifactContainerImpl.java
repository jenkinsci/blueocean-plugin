package io.jenkins.blueocean.service.embedded.rest;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Functions;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueArtifactFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueArtifact;
import io.jenkins.blueocean.rest.model.BlueArtifactContainer;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.StreamSupport;

public class ArtifactContainerImpl extends BlueArtifactContainer {
    final private Run run;
    final private Link self;

    public ArtifactContainerImpl(Run r, Reachable parent) {
        this.run = r;
        this.self = parent.getLink().rel("artifacts");
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public BlueArtifact get(final String name) {
        // Check security for artifacts
        if(Functions.isArtifactsPermissionEnabled() && !run.hasPermission(Run.ARTIFACTS)) {
            return null;
        }
        return StreamSupport.stream( iterable().spliterator(), false).
            filter(input -> input != null && name.equals(input.getId())).
            findFirst().orElse(null);
    }

    @Override
    @NonNull
    public Iterator<BlueArtifact> iterator() {
        return iterable().iterator();
    }

    Iterable<BlueArtifact> iterable() {
        // Check security for artifacts
        if(Functions.isArtifactsPermissionEnabled() && !run.hasPermission(Run.ARTIFACTS)) {
            return Collections.emptyList();
        }
        return BlueArtifactFactory.resolve(run, this);
    }
}
