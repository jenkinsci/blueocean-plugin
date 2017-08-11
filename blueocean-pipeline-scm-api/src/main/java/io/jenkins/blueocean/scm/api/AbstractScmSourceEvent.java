package io.jenkins.blueocean.scm.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMEvent;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSourceEvent;

/**
 * Implementation of {@link SCMSourceEvent} for Multibranch pipeline created from SCM
 *
 * @author Vivek Pandey
 */
public abstract class AbstractScmSourceEvent extends SCMSourceEvent<Object>{
    private final String repoName;

    public AbstractScmSourceEvent(String repoName, String origin) {
        super(SCMEvent.Type.CREATED, new Object(), origin);
        this.repoName = repoName;
    }

    @Override
    public boolean isMatch(@NonNull SCMNavigator navigator) {
        return false;
    }

    @NonNull
    @Override
    public String getSourceName() {
        return repoName;
    }
}
