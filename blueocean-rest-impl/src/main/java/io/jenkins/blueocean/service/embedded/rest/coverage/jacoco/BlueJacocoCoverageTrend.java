package io.jenkins.blueocean.service.embedded.rest.coverage.jacoco;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.plugins.jacoco.JacocoBuildAction;
import hudson.plugins.jacoco.model.Coverage;
import io.jenkins.blueocean.rest.factory.BlueTrendFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueTable;
import io.jenkins.blueocean.rest.model.BlueTrend;
import io.jenkins.blueocean.service.embedded.rest.coverage.BlueCoverageMetrics;
import io.jenkins.blueocean.service.embedded.rest.coverage.BlueCoverageSummary;
import io.jenkins.blueocean.service.embedded.rest.coverage.BlueCoverageTrend;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author cliffmeyers
 */
public class BlueJacocoCoverageTrend extends BlueCoverageTrend {

    public BlueJacocoCoverageTrend(BluePipeline pipeline, Link parent) {
        super(pipeline, parent);
    }

    @Override
    public String getId() {
        return "jacoco";
    }

    @Override
    public BlueTable getTable() {
        Iterator<BlueRun> iterator = pipeline.getRuns().iterator(0, 101);
        return new TableImpl(iterator);
    }


    public static class TableImpl extends BlueCoverageTrend.CoverageHistoryTable {

        private final Iterator<BlueRun> runs;

        public TableImpl(Iterator<BlueRun> runs) {
            this.runs = runs;
        }

        @Override
        public List<Row> getRows() {
            return Lists.newArrayList(Iterators.transform(runs, new Function<BlueRun, Row>() {
                @Override
                public Row apply(BlueRun run) {
                    BlueCoverageSummary summary = createSummary(run);
                    return new RowImpl(summary, run.getId());
                }
            }));
        }

        private BlueCoverageSummary createSummary(BlueRun run) {
            BlueActionProxy actionProxy = Iterators.find(run.getAllActions().iterator(), new Predicate<BlueActionProxy>() {
                @Override
                public boolean apply(@Nullable BlueActionProxy input) {
                    return input != null && input.getAction() instanceof JacocoBuildAction;
                }
            });

            if (actionProxy != null) {
                JacocoBuildAction jacoco = (JacocoBuildAction) actionProxy.getAction();

                Coverage clazzCover = jacoco.getClassCoverage();
                BlueCoverageMetrics clazz = new BlueCoverageMetrics(clazzCover.getCovered(), clazzCover.getMissed());
                Coverage methodCover = jacoco.getMethodCoverage();
                BlueCoverageMetrics method = new BlueCoverageMetrics(methodCover.getCovered(), methodCover.getMissed());
                Coverage lineCover = jacoco.getLineCoverage();
                BlueCoverageMetrics line = new BlueCoverageMetrics(lineCover.getCovered(), clazzCover.getMissed());
                Coverage branchCover = jacoco.getBranchCoverage();
                BlueCoverageMetrics branch = new BlueCoverageMetrics(branchCover.getCovered(), clazzCover.getMissed());
                Coverage instructionCover = jacoco.getInstructionCoverage();
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

    @Extension
    public static class BlueJacocoCoverageTrendFactory extends BlueTrendFactory {
        @Override
        public BlueTrend getTrend(BluePipeline pipeline, Link parent) {
            return new BlueJacocoCoverageTrend(pipeline, parent);
        }
    }
}
