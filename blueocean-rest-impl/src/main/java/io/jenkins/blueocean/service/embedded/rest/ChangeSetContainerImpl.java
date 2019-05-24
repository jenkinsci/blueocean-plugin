package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueChangeSetEntry;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import jenkins.scm.RunWithSCM;
import org.jenkinsci.plugins.workflow.cps.replay.ReplayCause;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ChangeSetContainerImpl extends Container<BlueChangeSetEntry> {

    private final BlueOrganization organization;
    private final BlueRun blueRun;
    private final Run run;

    public ChangeSetContainerImpl(BlueOrganization organization, BlueRun blueRun, Run run) {
        super();
        this.organization = organization;
        this.blueRun = blueRun;
        this.run = run;
    }

    @Override
    public Link getLink() {
        return this.blueRun.getLink().rel("changeSet");
    }

    @Override
    public BlueChangeSetEntry get(String name) {
        for (Iterator i = this.iterator(); i.hasNext(); ) {
            BlueChangeSetEntry set = (BlueChangeSetEntry) i.next();
            if (name.equals(set.getCommitId())) {
                return set;
            }
        }
        return null;
    }

    @Override
    public Iterator<BlueChangeSetEntry> iterator() {
        Run run = this.run;
        List<BlueChangeSetEntry> changesets = new LinkedList<>();
        int checkoutCount = 0;

        // If this run is a replay then return the changesets from the original run
        ReplayCause replayCause = (ReplayCause) run.getCause(ReplayCause.class);
        while (replayCause != null) {
            Run originalRun = this.run.getParent().getBuildByNumber(replayCause.getOriginalNumber());
            if (originalRun != null) {
                run = originalRun;
                replayCause = (ReplayCause) run.getCause(ReplayCause.class);
            } else {
                // the replay we are dependant on no longer exists
                break;
            }
        }

        if (run instanceof AbstractBuild) {
            for (ChangeLogSet<? extends ChangeLogSet.Entry> cs : ((AbstractBuild<?, ?>) run).getChangeSets()) {
                for (ChangeLogSet.Entry e : cs) {
                    changesets.add(new ChangeSetResource(this.organization, e, this).setCheckoutCount(checkoutCount));
                }
                checkoutCount++;
            }

        } else if (run instanceof RunWithSCM) {
            for (ChangeLogSet<? extends ChangeLogSet.Entry> cs : ((RunWithSCM<?, ?>) run).getChangeSets()) {
                for (ChangeLogSet.Entry e : cs) {
                    changesets.add(new ChangeSetResource(this.organization, e, this).setCheckoutCount(checkoutCount));
                }
                checkoutCount++;
            }
        }
        return changesets.iterator();
    }
}
