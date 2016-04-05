package io.jenkins.blueocean.service.embedded.rest;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.verb.DELETE;
import org.kohsuke.stapler.verb.POST;
import org.kohsuke.stapler.verb.PUT;

import hudson.model.FreeStyleBuild;
import hudson.scm.ChangeLogSet;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Containers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

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
    public Container<ChangeSetResource> getChangeSet() {

        Map<String,ChangeSetResource> m = new HashMap<>();
        int cnt=0;
        for (ChangeLogSet.Entry e : run.getChangeSet()) {
            cnt++;
            String id = e.getCommitId();
            if (id==null)   id = String.valueOf(cnt);
            m.put(id,new ChangeSetResource(e));
        }
        return Containers.fromResourceMap(m);
    }

    @PUT
    @Override
    public BlueRunState stop() {
        try {
            run.doStop();
            return getStateObj();
        } catch (Exception e) {
           throw new ServiceException.UnexpectedErrorExpcetion("Error while trying to stop run", e);
        }
    }
}
