package io.jenkins.blueocean.rest.factory;

import com.google.common.collect.Sets;
import hudson.ExtensionList;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.model.BlueArtifact;

import java.util.Collection;
import java.util.LinkedHashSet;

public abstract class BlueArtifactFactory {

    /**
     * @param run to resolve from
     * @param parent typically the artifact container
     * @return artifacts
     */
    public abstract Collection<BlueArtifact> getArtifacts(Run<?, ?> run, Reachable parent);

    /**
     * @param run to resolve from
     * @param parent typically the artifact container
     * @return artifacts
     */
    public static Collection<BlueArtifact> resolve(Run<?, ?> run, Reachable parent) {
        LinkedHashSet<BlueArtifact> allArtifacts = Sets.newLinkedHashSet();
        for (BlueArtifactFactory factory : ExtensionList.lookup(BlueArtifactFactory.class)) {
            Collection<BlueArtifact> artifacts = factory.getArtifacts(run, parent);
            if (artifacts == null) {
                continue;
            }
            allArtifacts.addAll(artifacts);
        }
        return allArtifacts;
    }
}
