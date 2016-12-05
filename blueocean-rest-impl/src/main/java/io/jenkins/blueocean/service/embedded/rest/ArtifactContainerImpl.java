package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.Service;
import hudson.model.Run;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueArtifact;
import io.jenkins.blueocean.rest.model.BlueArtifactContainer;
import io.jenkins.blueocean.rest.model.Resource;
import jenkins.util.VirtualFile;
import org.kohsuke.stapler.Stapler;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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
        final VirtualFile file = run.getArtifactManager().root().child(name);
        try {
            if(file == null || !file.exists() || !file.isFile()) {
                return null;
            }
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Something is wrong with an artifact",e);
        }
        return new BlueArtifact() {
            @Override
            public String getName() {
                return file.getName();
            }

            @Override
            public String getPath() {
                return name;
            }

            @Override
            public String getUrl() {
                return Stapler.getCurrentRequest().getContextPath() +
                    "/" + run.getUrl()+"artifact/"+ name;
            }

            @Override
            public long getSize() {
                try {
                    return file.length();
                } catch (IOException e) {
                    throw new ServiceException.UnexpectedErrorException("qError getting file length", e);
                }
            }

            @Override
            public Link getLink() {
                return new Link(getUrl());
            }
        };
    }

    @Override
    public Iterator<BlueArtifact> iterator(int start, int limit) {
        List<Run.Artifact> artifactsUpTo = run.getArtifactsUpTo(limit);

        // If start exceeds number of artifacts return an emtpy one.
        if(start >= artifactsUpTo.size()) {
            return Iterators.emptyIterator();
        }

        int calculatedLimit = limit;

        if(calculatedLimit > artifactsUpTo.size()) {
            calculatedLimit = artifactsUpTo.size();
        }

        List<Run.Artifact> artifacts = artifactsUpTo.subList(start, calculatedLimit);

        return Iterators.transform(artifacts.iterator(), new Function<Run.Artifact, BlueArtifact>() {
            @Override
            public BlueArtifact apply(@Nullable Run.Artifact artifact) {
                if(artifact == null) {
                    return null;
                }

                return new ArtifactImpl(run, artifact, ArtifactContainerImpl.this);
            }
        });
    }

    @Override
    public Iterator<BlueArtifact> iterator() {
        throw new ServiceException.NotImplementedException("Not implemented");
    }
}
