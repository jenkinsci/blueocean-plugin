package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.FreeStyleBuild;
import hudson.scm.ChangeLogSet;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Containers;

import java.util.HashMap;
import java.util.Map;

/**
 * FreeStyleRunImpl can add it's own element here
 *
 * @author Vivek Pandey
 */
public class FreeStyleRunImpl extends AbstractRunImpl<FreeStyleBuild> {
    public FreeStyleRunImpl(FreeStyleBuild run) {
        super(run);
    }

    @Override
    public String getBranch() {
        return null;
    }

    @Override
    public String getCommitId() {
        return null;
    }

    @Override
    public Container<?> getChangeSet() {
        Map<String,Object> m = new HashMap<>();
        int cnt=0;
        for (ChangeLogSet.Entry e : run.getChangeSet()) {
            cnt++;
            String id = e.getCommitId();
            if (id==null)   id = String.valueOf(cnt);
            m.put(id,e);
        }
        return Containers.from(m);
    }
}
