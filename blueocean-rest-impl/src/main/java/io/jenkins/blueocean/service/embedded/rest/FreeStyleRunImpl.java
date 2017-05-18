package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import hudson.model.FreeStyleBuild;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BlueRunFactory;
import io.jenkins.blueocean.rest.model.BlueChangeSetEntry;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Containers;
import org.kohsuke.stapler.QueryParameter;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_FREE_STYLE_BUILD;

/**
 * FreeStyleRunImpl can add it's own element here
 *
 * @author Vivek Pandey
 */
@Capability(JENKINS_FREE_STYLE_BUILD)
public class FreeStyleRunImpl extends AbstractRunImpl<FreeStyleBuild> {
    public FreeStyleRunImpl(FreeStyleBuild run, Reachable parent) {
        super(run, parent);
    }

    @Override
    public Container<BlueChangeSetEntry> getChangeSet() {

        Map<String, BlueChangeSetEntry> m = new LinkedHashMap<>();
        int cnt=0;
        for (ChangeLogSet.Entry e : run.getChangeSet()) {
            cnt++;
            String id = e.getCommitId();
            if (id==null)   id = String.valueOf(cnt);
            m.put(id,new ChangeSetResource(e, this));
        }
        return Containers.fromResourceMap(this.getLink(),m);
    }

    @Override
    public BlueRun stop(@QueryParameter("blocking") Boolean blocking, @QueryParameter("timeOutInSecs") Integer timeOutInSecs){
        return stop(blocking, timeOutInSecs, new StoppableRun() {
            @Override
            public void stop() throws Exception {
                run.doStop();
            }
        });

    }

    @Extension
    public static class FactoryImpl extends BlueRunFactory {
        @Override
        public BlueRun getRun(Run run, Reachable parent) {
            if (run instanceof FreeStyleBuild) {
                return new FreeStyleRunImpl((FreeStyleBuild)run, parent);
            }
            return null;
        }
    }
}
