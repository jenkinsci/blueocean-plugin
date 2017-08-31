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
import org.kohsuke.stapler.export.Exported;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Restricted(NoExternalUse.class)
public class BlueJUnitTrend extends BlueTrend {
    private static final String TOTAL = "total";
    private static final String PASSED = "passed";
    private static final String FIXED = "fixed";
    private static final String FAILED = "failed";
    private static final String EXISTING_FAILED = "existingFailed";
    private static final String REGRESSIONS = "regressions";
    private static final String SKIPPED = "skipped";

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

        private static final Map<String, String> COLUMNS = ImmutableMap.<String, String> builder()
            .put(TOTAL, "Total")
            .put(PASSED, "Passed")
            .put(FIXED, "Fixed")
            .put(FAILED, "Failed")
            .put(EXISTING_FAILED, "Existing Failed")
            .put(REGRESSIONS, "Regressions")
            .put(SKIPPED, "Skipped")
            .build();

        private final Iterator<BlueRun> runs;

        public JUnitHistoryTable(Iterator<BlueRun> runs) {
            this.runs = runs;
        }

        @Override
        public Map<String, String> getColumns() {
            return COLUMNS;
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

    public static class RowImpl extends BlueTable.Row {
        private final String id;
        private final BlueTestSummary summary;

        RowImpl(BlueTestSummary summary, String id) {
            this.id = id;
            this.summary = summary;
        }

        @Override
        public String getId() {
            return id;
        }

        @Exported(name = TOTAL)
        public long getTotal() {
            return summary.getTotal();
        }

        @Exported(name = PASSED)
        public long getPassed() {
            return summary.getPassedTotal();
        }

        @Exported(name = FIXED)
        public long getFixed() {
            return summary.getFixedTotal();
        }

        @Exported(name = FAILED)
        public long getFailed() {
            return summary.getFailedTotal();
        }

        @Exported(name = EXISTING_FAILED)
        public long getExistingFailed() {
            return summary.getExistingFailedTotal();
        }

        @Exported(name = REGRESSIONS)
        public long getRegressions() {
            return summary.getRegressionsTotal();
        }

        @Exported(name = SKIPPED)
        public long getSkipped() {
            return summary.getSkippedTotal();
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
