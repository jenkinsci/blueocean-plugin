package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public final class BlueTestSummary {

    public static final String TOTAL = "total";
    public static final String SKIPPED = "skipped";
    public static final String FAILED = "failed";
    public static final String PASSED = "passed";
    public static final String DURATION = "duration";

    private final long passedTotal;
    private final long failedTotal;
    private final long skippedTotal;
    private final long total;
    private final float duration;

    public BlueTestSummary(long passedTotal, long failedTotal, long skippedTotal, long total, float duration) {
        this.passedTotal = passedTotal;
        this.failedTotal = failedTotal;
        this.skippedTotal = skippedTotal;
        this.total = total;
        this.duration = duration;
    }

    @Exported(name = PASSED)
    public long getPassedTotal() {
        return passedTotal;
    }

    @Exported(name = FAILED)
    public long getFailedTotal() {
        return failedTotal;
    }

    @Exported(name = SKIPPED)
    public long getSkippedTotal() {
        return skippedTotal;
    }

    @Exported(name = TOTAL)
    public long getTotal() {
        return total;
    }

    @Exported(name = DURATION)
    public float getDuration() {
        return duration;
    }

    public static BlueTestSummary empty() {
        return new BlueTestSummary(0, 0, 0, 0, 0);
    }

    public BlueTestSummary tally(BlueTestSummary summary) {
        return new BlueTestSummary(
            this.passedTotal + summary.passedTotal,
            this.failedTotal + summary.failedTotal,
            this.skippedTotal + summary.skippedTotal,
            this.total + summary.total,
            0
        );
    }
}
