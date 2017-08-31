package io.jenkins.blueocean.service.embedded.rest.junit;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import hudson.Extension;
import io.jenkins.blueocean.rest.factory.BlueTrendFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueTable;
import io.jenkins.blueocean.rest.model.BlueTestSummary;
import io.jenkins.blueocean.rest.model.BlueTrend;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.jenkins.blueocean.service.embedded.rest.junit.BlueJUnitTrend.TestCategory.EXISITING_FAILED;
import static io.jenkins.blueocean.service.embedded.rest.junit.BlueJUnitTrend.TestCategory.FAILED;
import static io.jenkins.blueocean.service.embedded.rest.junit.BlueJUnitTrend.TestCategory.FIXED;
import static io.jenkins.blueocean.service.embedded.rest.junit.BlueJUnitTrend.TestCategory.PASSED;
import static io.jenkins.blueocean.service.embedded.rest.junit.BlueJUnitTrend.TestCategory.REGRESSIONS;
import static io.jenkins.blueocean.service.embedded.rest.junit.BlueJUnitTrend.TestCategory.SKIPPED;
import static io.jenkins.blueocean.service.embedded.rest.junit.BlueJUnitTrend.TestCategory.TOTAL;

@Restricted(NoExternalUse.class)
public class BlueJUnitTrend extends BlueTrend {
    private final BluePipeline pipeline;
    private final Link parent;

    public BlueJUnitTrend(BluePipeline pipeline, Link parent) {
        this.pipeline = pipeline;
        this.parent = parent;
    }

    @Override
    public String getId() {
        return "junit";
    }

    @Override
    public BlueTable getTable() {
        // TODO: make this understand query params for paging, date ranges, etc
        Iterator<BlueRun> iterator = pipeline.getRuns().iterator(0, 101);
        return new JUnitHistoryTable(iterator);
    }

    @Override
    public Link getLink() {
        return parent.rel(getId());
    }

    public static class JUnitHistoryTable extends BlueTable {

        private static final Map<String, String> LABELS = ImmutableMap.<String, String> builder()
            .put(TOTAL.toString(), "Total")
            .put(PASSED.toString(), "Passed")
            .put(FIXED.toString(), "Fixed")
            .put(FAILED.toString(), "Failed")
            .put(EXISITING_FAILED.toString(), "Existing Failed")
            .put(REGRESSIONS.toString(), "Regressions")
            .put(SKIPPED.toString(), "Skipped")
            .build();
        private final Iterator<BlueRun> runs;

        public JUnitHistoryTable(Iterator<BlueRun> runs) {
            this.runs = runs;
        }

        @Override
        public Map<String, String> getLabels() {
            return LABELS;
        }

        @Override
        public List<Row> getRows() {
            return Lists.newArrayList(Iterators.transform(runs, new Function<BlueRun, Row>() {
                @Override
                public Row apply(BlueRun input) {
                    return new RowImpl(input.getTestSummary(), input.getId());
                }
            }));
        }
    }

    enum TestCategory {
        TOTAL, PASSED, FIXED, FAILED, EXISITING_FAILED, REGRESSIONS, SKIPPED
    }

    public static class RowImpl extends BlueTable.Row {
        private final String id;
        private final Map<String, Long> totals;

        RowImpl(BlueTestSummary summary, String id) {
            this.id = id;
            totals = new HashMap<>();
            totals.put(TOTAL.toString(), summary.getTotal());
            totals.put(PASSED.toString(), summary.getPassedTotal());
            totals.put(FIXED.toString(), summary.getFixedTotal());
            totals.put(FAILED.toString(), summary.getFailedTotal());
            totals.put(EXISITING_FAILED.toString(), summary.getExistingFailedTotal());
            totals.put(REGRESSIONS.toString(), summary.getRegressionsTotal());
            totals.put(SKIPPED.toString(), summary.getSkippedTotal());
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Map<String, Long> getColumns() {
            return totals;
        }
    }

    @Extension
    public static class FactoryImpl extends BlueTrendFactory {
        @Override
        public BlueTrend getTrend(BluePipeline pipeline, Link parent) {
            return new BlueJUnitTrend(pipeline, parent);
        }
    }
}
