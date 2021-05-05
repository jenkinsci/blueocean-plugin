package io.jenkins.blueocean.service.embedded.rest.junit;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import hudson.Extension;
import io.jenkins.blueocean.rest.factory.BlueTrendFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.BlueTableRow;
import io.jenkins.blueocean.rest.model.BlueTestSummary;
import io.jenkins.blueocean.rest.model.BlueTrend;
import io.jenkins.blueocean.rest.model.Container;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.export.Exported;

import java.util.Iterator;
import java.util.Map;

@Restricted(NoExternalUse.class)
public class BlueJUnitTrend extends BlueTrend {

    public static final String TOTAL = "total";
    public static final String PASSED = "passed";
    public static final String FIXED = "fixed";
    public static final String FAILED = "failed";
    public static final String EXISTING_FAILED = "existingFailed";
    public static final String REGRESSIONS = "regressions";
    public static final String SKIPPED = "skipped";

    private final BluePipeline pipeline;
    private final Link parent;

    private static final Map<String, String> COLUMNS = ImmutableMap.<String, String> builder()
        .put(TOTAL, "Total")
        .put(PASSED, "Passed")
        .put(FIXED, "Fixed")
        .put(FAILED, "Failed")
        .put(EXISTING_FAILED, "Existing Failed")
        .put(REGRESSIONS, "Regressions")
        .put(SKIPPED, "Skipped")
        .build();

    public BlueJUnitTrend(BluePipeline pipeline, Link parent) {
        this.pipeline = pipeline;
        this.parent = parent;
    }

    @Override
    public String getId() {
        return "junit";
    }

    @Override
    public String getDisplayName() {
        return "JUnit";
    }

    @Override
    @Exported
    public Map<String, String> getColumns() {
        return COLUMNS;
    }

    @Override
    public Container<BlueTableRow> getRows() {
        BlueRunContainer blueRunContainer = pipeline.getRuns();

        return new Container<BlueTableRow>() {
            @Override
            public Link getLink() {
                return parent.rel("rows");
            }

            @Override
            public BlueTableRow get(String name) {
                return null;
            }

            @Override
            public Iterator<BlueTableRow> iterator() {
                return blueRunContainer == null ? null
                    : Iterators.transform(blueRunContainer.iterator(),run -> new BlueJUnitTrendRow(run.getBlueTestSummary(), run.getId()));
            }
        };
    }

    @Override
    public Link getLink() {
        return parent.rel(getId());
    }


    public static class BlueJUnitTrendRow extends BlueTableRow {
        private final String id;
        private final BlueTestSummary summary;

        BlueJUnitTrendRow(BlueTestSummary summary, String id) {
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
