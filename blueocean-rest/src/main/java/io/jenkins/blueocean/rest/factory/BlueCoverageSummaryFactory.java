package io.jenkins.blueocean.rest.factory;

import hudson.ExtensionPoint;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.model.BlueCoverageSummary;
import jenkins.model.Jenkins;

/**
 * @author cliffmeyers
 */
public abstract class BlueCoverageSummaryFactory implements ExtensionPoint {

    public abstract BlueCoverageSummary getCoverageSummary(Run<?, ?> run, final Reachable parent);

    public static BlueCoverageSummary resolve(Run<?, ?> run, Reachable parent) {
        BlueCoverageSummary runningTally = null;
        for (BlueCoverageSummaryFactory factory : Jenkins.getInstance().getExtensionList(BlueCoverageSummaryFactory.class)) {
            BlueCoverageSummary nextSummary = factory.getCoverageSummary(run, parent);
            if (runningTally == null) {
                runningTally = nextSummary;
            } else {
                runningTally = runningTally.tally(nextSummary);
            }

        }
        return runningTally;
    }
}
