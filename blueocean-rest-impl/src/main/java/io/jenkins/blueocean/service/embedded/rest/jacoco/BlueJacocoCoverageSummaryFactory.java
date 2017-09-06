package io.jenkins.blueocean.service.embedded.rest.jacoco;

import hudson.Extension;
import hudson.model.Run;
import hudson.plugins.jacoco.JacocoBuildAction;
import hudson.plugins.jacoco.model.Coverage;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueCoverageSummaryFactory;
import io.jenkins.blueocean.rest.model.BlueCoverageMetrics;
import io.jenkins.blueocean.rest.model.BlueCoverageSummary;

/**
 * @author cliffmeyers
 */
@Extension
public class BlueJacocoCoverageSummaryFactory extends BlueCoverageSummaryFactory {
    @Override
    public BlueCoverageSummary getCoverageSummary(Run<?, ?> run, Reachable parent) {
        JacocoBuildAction action = run.getAction(JacocoBuildAction.class);

        if (action != null) {
            Coverage clazzCover = action.getClassCoverage();
            BlueCoverageMetrics clazz = new BlueCoverageMetrics(clazzCover.getCovered(), clazzCover.getMissed());
            Coverage methodCover = action.getMethodCoverage();
            BlueCoverageMetrics method = new BlueCoverageMetrics(methodCover.getCovered(), methodCover.getMissed());
            Coverage lineCover = action.getLineCoverage();
            BlueCoverageMetrics line = new BlueCoverageMetrics(lineCover.getCovered(), clazzCover.getMissed());
            Coverage branchCover = action.getBranchCoverage();
            BlueCoverageMetrics branch = new BlueCoverageMetrics(branchCover.getCovered(), clazzCover.getMissed());
            Coverage instructionCover = action.getInstructionCoverage();
            BlueCoverageMetrics instruction = new BlueCoverageMetrics(instructionCover.getCovered(), clazzCover.getMissed());

            return new BlueCoverageSummary(
                clazz,
                method,
                line,
                branch,
                instruction
            );
        }

        return null;
    }
}
