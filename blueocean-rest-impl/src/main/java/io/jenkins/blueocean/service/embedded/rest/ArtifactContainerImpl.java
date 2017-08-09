package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueArtifactFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueArtifact;
import io.jenkins.blueocean.rest.model.BlueArtifactContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

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
        return Iterators.find(iterator(), new Predicate<BlueArtifact>() {
            @Override
            public boolean apply(@Nullable BlueArtifact input) {
                return input != null && input.getName().equals(name);
            }
        }, null);
    }

    @Override
    @Nonnull
    public Iterator<BlueArtifact> iterator() {
        return BlueArtifactFactory.resolve(run, this).iterator();
    }
}
